package DHADVTKV;

import javax.xml.crypto.Data;
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
        Set<Partition> putPartitions = new HashSet<>();
        Set<Partition> getPartitions = new HashSet<>();
        Set<Partition> partitions = new HashSet<>();

        for (DataObject object : transaction.getPuts()) {
            putPartitions.add(partitionForKey(object.getKey()));
        }

        for (DataObject object : transaction.getGets()) {
            getPartitions.add(partitionForKey(object.getKey()));
        }

        partitions.addAll(putPartitions);
        partitions.addAll(getPartitions);

        for (Partition partition : partitions) {
            partition.prepare(transaction.getId(), transaction.getSnapshot(), putPartitions, getPartitions);
        }
    }

    public void onPrepareResult(boolean conflicts, int commitTimestamp) {
        transaction.setCommitTimestamp(Math.max(transaction.getCommitTimestamp(), commitTimestamp));
        transaction.setConflicts(transaction.hasConflicts() || conflicts);

        if (enoughInformationToCommit()) {
            Map<Partition, ArrayList<DataObject>> putPartitions = new HashMap<>();
            Map<Partition, ArrayList<DataObject>> getPartitions = new HashMap<>();

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

            for (Map.Entry<Partition, ArrayList<DataObject>> entry : putPartitions.entrySet()) {
                List<DataObject> gets = getPartitions.get(entry.getKey());
                entry.getKey().commit(transaction.getId(), gets, entry.getValue(), transaction.hasConflicts(), transaction.getCommitTimestamp());
            }
        }
    }

    public boolean onCommitResult() {
        boolean result = transaction.hasConflicts() == false;
        clock = transaction.getCommitTimestamp();
        transaction = null;
        return result;
    }

    private boolean enoughInformationToCommit() {
        return false;
    }

    private Partition partitionForKey(int key) {
        return new Partition();
    }

}
