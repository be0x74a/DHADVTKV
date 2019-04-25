package DHADVTKV.TPC;

import DHADVTKV.common.DataObject;
import DHADVTKV.common.Transaction;
import DHADVTKV.messages.*;

import java.util.*;

public class Client {

    public void setGetsSend(int noPartitions) {
        this.transaction.setGetsSent(noPartitions);
        this.transaction.setGetsReceived(0);
    }

    public long getTransactionId() {
        if (this.transaction == null) {
            return -1L;
        }
        return this.transaction.getId();
    }

    public enum State {
        initialState,
        transactionCreated,
        getSent,
        canCommit,
        waitingToFinish
    }

    private Transaction transaction = null;
    private long clock = 0;
    private State state;
    private int noPartitions;
    private int nodeId;

    public Client(int noPartitions, int nodeId) {
        this.noPartitions = noPartitions;
        this.nodeId = nodeId;
        this.state = State.initialState;
    }

    public void begin() {
        this.transaction = new Transaction();
        this.state = State.transactionCreated;
    }

    public void abort() {
        this.transaction = null;
        this.state = State.initialState;
    }

    public void put(long key, long value) {
        long tentativeVersion = this.transaction.getSnapshot();
        if (tentativeVersion == -1) {
            tentativeVersion = this.clock;
        }
        DataObject object = new DataObject(key, value, tentativeVersion + 1);
        this.transaction.getPuts().add(object);
    }

    public TransactionalGetMessageRequest get(long key) {
        int partition = partitionForKey(key);
        long version = this.transaction.getSnapshot();
        if (version == -1) {
            version = this.clock;
        }

        return new TransactionalGetMessageRequest(key, version, nodeId, partition);
    }

    public DataObject onTransactionalGetResponse(TransactionalGetMessageResponse message) {

        if (message.getObject() == null) {
            return null;
        }

        if (this.transaction.getSnapshot() == -1) {
            this.transaction.setSnapshot(message.getObject().getVersion());
        }

        this.transaction.getGets().add(message.getObject());
        this.put(message.getObject().getKey(), message.getObject().getValue());

        this.transaction.addGetsReceived();
        if (this.transaction.getGetsSent() == this.transaction.getGetsReceived()) {
            this.state = State.canCommit;
        }

        return message.getObject();
    }

    public List<PrepareMessageRequest> commit() {
        Map<Integer, ArrayList<DataObject>> putPartitions = new HashMap<>();
        Map<Integer, ArrayList<DataObject>> getPartitions = new HashMap<>();
        Set<Integer> partitions = new HashSet<>();
        List<PrepareMessageRequest> prepareMessageRequests = new ArrayList<>();

        for (DataObject object : this.transaction.getPuts()) {
            int partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (DataObject object : this.transaction.getGets()) {
            int partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = getPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (Integer partition : partitions) {
            prepareMessageRequests.add(new PrepareMessageRequest(this.transaction.getId(), this.transaction.getSnapshot(),
                    putPartitions.get(partition), getPartitions.get(partition), nodeId, partition));
        }

        this.transaction.setPrepareRequestsSent(prepareMessageRequests.size());
        return prepareMessageRequests;
    }

    public List<CommitMessageRequest> onPrepareResponse(PrepareMessageResponse message) {
        List<CommitMessageRequest> commitMessageRequests = new ArrayList<>();

        this.transaction.setCommitTimestamp(Math.max(this.transaction.getCommitTimestamp(), message.getCommitTimestamp()));
        this.transaction.setConflicts(this.transaction.hasConflicts() || message.hasConflicts());
        this.transaction.addToPrepareResponsesReceived();

        if (enoughInformationToCommit()) {
            Map<Integer, List<DataObject>> putPartitions = new HashMap<>();

            for (DataObject object : this.transaction.getPuts()) {
                int partition = partitionForKey(object.getKey());
                List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
                if (res != null) {
                    res.add(object);
                }
            }

            for (Map.Entry<Integer, List<DataObject>> entry : putPartitions.entrySet()) {

                commitMessageRequests.add(new CommitMessageRequest(this.transaction.getId(), entry.getValue(),
                        this.transaction.hasConflicts(), this.transaction.getCommitTimestamp(), nodeId, entry.getKey()));
            }
        }

        return commitMessageRequests;
    }

    public void onCommitResult(CommitMessageResponse message) {
        this.clock = this.transaction.getCommitTimestamp();
        this.transaction = null;
        this.state = State.initialState;
    }

    private boolean enoughInformationToCommit() {
        return this.transaction.getPrepareRequestsSent() == this.transaction.getPrepareResponsesReceived();
    }

    private int partitionForKey(long key) {
        return (int) key % this.noPartitions;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
