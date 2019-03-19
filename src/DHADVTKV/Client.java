package DHADVTKV;

import java.util.*;

public class Client {

    private Transaction transaction = null;
    private int clock = 0;

    public void begin() {
        transaction = new Transaction();
    }

    public DataObject get(int key) {
        Partition partition = partitionForKey(key);
        int version = transaction.getSnapshot();
        if (version == -1) {
            version = clock;
        }

        DataObject object = partition.transactionalGet(key, version);

        if (object == null) {
            System.out.println("DataObject associated with key: " + key + " not found");
            return null;
        }

        if (transaction.getSnapshot() == -1) {
            transaction.setSnapshot(object.getVersion());
        }

        return object;
    }

    public void abort() {
        System.out.println("Transaction " + transaction.getId() + " was aborted");
        transaction = null;
    }

    public void put(int key, int value) {
        int tentativeVersion = transaction.getSnapshot();
        if (tentativeVersion == -1) {
            tentativeVersion = clock;
        }
        DataObject object = new DataObject(key, value, tentativeVersion + 1);
        transaction.getPuts().add(object);
    }

    public void commit() {
        Map<Partition, ArrayList<DataObject>> putPartitions = new HashMap<>();
        Map<Partition, ArrayList<DataObject>> getPartitions = new HashMap<>();
        Set<Partition> partitions = new HashSet<>();

        for (DataObject object : transaction.getPuts()) {
            Partition partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (DataObject object : transaction.getGets()) {
            Partition partition = partitionForKey(object.getKey());

            partitions.add(partition);
            List<DataObject> res = getPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
            if (res != null) {
                res.add(object);
            }
        }

        for (Partition partition : partitions) {
            partition.prepare(transaction.getId(), transaction.getSnapshot(), getPartitions.get(partition), putPartitions.get(partition), this);
        }
    }


    public void send_prepare_results(boolean conflicts, int commitTimestamp) {
        onPrepareResult(conflicts, commitTimestamp);
    }

    public void onPrepareResult(boolean conflicts, int commitTimestamp) {
        transaction.setCommitTimestamp(Math.max(transaction.getCommitTimestamp(), commitTimestamp));
        transaction.setConflicts(transaction.hasConflicts() || conflicts);

        if (enoughInformationToCommit()) {
            Map<Partition, List<DataObject>> putPartitions = new HashMap<>();
            Map<Partition, List<DataObject>> getPartitions = new HashMap<>();

            for (DataObject object : transaction.getPuts()) {
                Partition partition = partitionForKey(object.getKey());
                List<DataObject> res = putPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
                if (res != null) {
                    res.add(object);
                }
            }

            for (DataObject object : transaction.getGets()) {
                Partition partition = partitionForKey(object.getKey());
                List<DataObject> res = getPartitions.putIfAbsent(partition, new ArrayList<>(Arrays.asList(object)));
                if (res != null) {
                    res.add(object);
                }
            }

            for (Map.Entry<Partition, List<DataObject>> entry : putPartitions.entrySet()) {
                entry.getKey().commit(transaction.getId(), getPartitions.get(entry.getKey()), entry.getValue(), transaction.hasConflicts(), transaction.getCommitTimestamp(), this);
            }
        }
    }

    public void send_commit_result() {
        onCommitResult();
    }

    public boolean onCommitResult() {
        boolean result = !transaction.hasConflicts();
        clock = transaction.getCommitTimestamp();
        transaction = null;
        return result;
    }

    private boolean enoughInformationToCommit() {
        return false;  //TODO: This is obviously ONLY a placeholder!!!
    }

    private Partition partitionForKey(int key) {
        return new Partition(); //TODO: This is obviously ONLY a placeholder too!!!
    }

}
