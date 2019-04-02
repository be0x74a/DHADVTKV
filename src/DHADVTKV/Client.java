package DHADVTKV;

import DHADVTKV.datatypes.*;

import java.util.*;

public class Client {

    public enum State {
        initialState,
        transactionCreated,
        getSent,
        getGotten,
        canCommit
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
        this.state = State.canCommit;
    }

    public TransactionalGetMessageRequest get(long key) {
        int partition = partitionForKey(key);
        long version = this.transaction.getSnapshot();
        if (version == -1) {
            version = this.clock;
        }

        this.transaction.addToGetQueue(key);
        return new TransactionalGetMessageRequest(key, version, nodeId, partition);
    }

    public DataObject onTransactionalGetResponse(TransactionalGetMessageResponse message) {

        if (message.getObject() == null) {
            System.out.println("DataObject not found");
            return null;
        }

        if (this.transaction.getSnapshot() == -1) {
            this.transaction.setSnapshot(message.getObject().getVersion());
        }

        this.transaction.removeFromGetQueue(message.getObject().getKey());
        if (this.transaction.getSizeOfGetQueue() == 0) {
            this.state = State.getGotten;
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
                    getPartitions.get(partition), putPartitions.get(partition), nodeId, partition));
        }

        this.transaction.setPrepareRequestsSent(prepareMessageRequests.size());
        return prepareMessageRequests;
    }

    public List<CommitMessageRequest> onPrepareResponse(PrepareMessageResponse message) {
        List<CommitMessageRequest> commitMessageRequests = new ArrayList<>();

        if (this.transaction == null) {
            System.out.println("HEY");
        }

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
