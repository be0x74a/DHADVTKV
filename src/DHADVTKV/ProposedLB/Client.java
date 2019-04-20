package DHADVTKV.ProposedLB;

import DHADVTKV.common.DataObject;
import DHADVTKV.common.Transaction;
import DHADVTKV.messages.*;

import java.util.*;

public class Client {

    private Transaction transaction = null;
    private long clock = 0;
    private State state;
    private int noPartitions;
    private int nodeId;

    public enum State {
        initialState,
        transactionCreated,
        getSent,
        canCommit,
        waitingToFinish
    }

    public void begin() {
        this.transaction = new Transaction();
        this.state = State.transactionCreated;
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

    public List<ValidateAndCommitTransactionRequest> commit() {
        Map<Integer, ArrayList<DataObject>> putPartitions = new HashMap<>();
        Map<Integer, ArrayList<DataObject>> getPartitions = new HashMap<>();
        Set<Integer> partitions = new HashSet<>();
        List<ValidateAndCommitTransactionRequest> validateAndCommitTransactionRequests = new ArrayList<>();

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
            validateAndCommitTransactionRequests.add(new ValidateAndCommitTransactionRequest(this.transaction.getId(), this.transaction.getSnapshot(),
                    putPartitions.get(partition), getPartitions.get(partition), nodeId, partition));
        }

        this.transaction.setPrepareRequestsSent(validateAndCommitTransactionRequests.size());
        return validateAndCommitTransactionRequests;
    }

    public void onTransactionValidationResult(ValidateAndCommitTransactionResponse message) {
        this.clock = message.getCommitTimestamp();
        this.transaction = null;
        this.state = State.initialState;
    }


    private int partitionForKey(long key) {
        return (int) key % this.noPartitions;
    }
}
