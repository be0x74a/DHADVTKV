package DHADVTKV;

import DHADVTKV.datatypes.*;

import java.util.*;

public class Client {

    private Transaction transaction = null;
    private long clock = 0;

    public void begin() {
        this.transaction = new Transaction();
    }

    public void abort() {
        System.out.println("Transaction " + this.transaction.getId() + " was aborted");
        this.transaction = null;
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
        Partition partition = partitionForKey(key);
        long version = this.transaction.getSnapshot();
        if (version == -1) {
            version = this.clock;
        }

        this.transaction.addToGetQueue(key);
        return new TransactionalGetMessageRequest(key, version);
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
        return message.getObject();
    }

    public List<PrepareMessageRequest> commit() {
        Map<Partition, ArrayList<DataObject>> putPartitions = new HashMap<>();
        Map<Partition, ArrayList<DataObject>> getPartitions = new HashMap<>();
        Set<Partition> partitions = new HashSet<>();
        List<PrepareMessageRequest> prepareMessageRequests = new ArrayList<>();

        for (DataObject object : this.transaction.getPuts()) {
            Partition partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (DataObject object : this.transaction.getGets()) {
            Partition partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = getPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (Partition partition : partitions) {
            prepareMessageRequests.add(new PrepareMessageRequest(this.transaction.getId(), this.transaction.getSnapshot(),
                    getPartitions.get(partition), putPartitions.get(partition)));
        }

        return prepareMessageRequests;
    }

    public List<CommitMessageRequest> onPrepareResponse(PrepareMessageResponse message) {
        List<CommitMessageRequest> commitMessageRequests = new ArrayList<>();

        this.transaction.setCommitTimestamp(Math.max(this.transaction.getCommitTimestamp(), message.getCommitTimestamp()));
        this.transaction.setConflicts(this.transaction.hasConflicts() || message.hasConflicts());

        if (enoughInformationToCommit()) {
            Map<Partition, List<DataObject>> putPartitions = new HashMap<>();

            for (DataObject object : this.transaction.getPuts()) {
                Partition partition = partitionForKey(object.getKey());
                List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
                if (res != null) {
                    res.add(object);
                }
            }

            for (Map.Entry<Partition, List<DataObject>> entry : putPartitions.entrySet()) {

                commitMessageRequests.add(new CommitMessageRequest(this.transaction.getId(), entry.getValue(),
                        this.transaction.hasConflicts(), this.transaction.getCommitTimestamp()));
            }
        }

        return commitMessageRequests;
    }

    public void onCommitResult(CommitMessageResponse message) {
        this.clock = this.transaction.getCommitTimestamp();
        this.transaction = null;
    }

    private boolean enoughInformationToCommit() {
        return false;  //TODO: Its possible data has to be retrieved from transaction!!!
    }

    private Partition partitionForKey(long key) {
        return new Partition(); //TODO: This is obviously ONLY a placeholder too!!!
    }
}
