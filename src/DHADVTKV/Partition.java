package DHADVTKV;

import java.util.List;
import java.util.Map;

public class Partition {

    private KeyValueStorage kv = new KeyValueStorage();
    private int clock = 0;
    private Map<Integer, Integer> latestObjectVersions;


    public DataObject transactionalGet(int key, int snapshot) {
        return onClientTransactionGetRequest(key, snapshot);
    }

    private DataObject onClientTransactionGetRequest(int key, int snapshot) {

        List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(key);
        List<DataObject> committedObjectVersions = kv.getCommittedVersions(key);

        return selectSnapshotConsistentVersion(snapshot, tentativeObjectVersions, committedObjectVersions);
    }

    public void prepare(int transactionId, int snapshot, List<DataObject> puts, List<DataObject> gets, Client client) {
        onPrepareRequest(transactionId, snapshot, gets, puts, client);
    }

    private void onPrepareRequest(int transactionId, int snapshot, List<DataObject> gets, List<DataObject> puts, Client client) {
        int commitTimestamp = generateCommitTimestamp(snapshot);
        boolean locksAcquired = acquireLocks(gets, puts);
        boolean conflicts = true;

        if (locksAcquired) {
            conflicts = checkConflicts(gets, puts, snapshot);
        }

        if (!conflicts) {
            kv.storeAsTentative(transactionId, puts);
        }

        client.send_prepare_results(conflicts, commitTimestamp);

        clock++;
    }

    public void commit(int transactionId, List<DataObject> gets, List<DataObject> puts, boolean conflicts, int commitTimestamp, Client client) {
        onCommitRequest(transactionId, gets, puts, conflicts, commitTimestamp, client);
    }

    private void onCommitRequest(int transactionId, List<DataObject> gets, List<DataObject> puts, boolean conflicts, int commitTimestamp, Client client) {
        if (clock < commitTimestamp) {
            clock = commitTimestamp + 1;
        }

        if (conflicts) {
            kv.deleteTentativeVersions(transactionId, puts);
        } else {
            kv.commitTentativeVersions(transactionId, puts, commitTimestamp);
            updateLatestObjectVersions(puts, commitTimestamp);
        }

        releaseLocks(puts);
        client.send_commit_result();
    }


    private DataObject selectSnapshotConsistentVersion(int snapshot, List<DataObject> tentativeObjectVersions, List<DataObject> committedObjectVersions) {

        for (DataObject object : tentativeObjectVersions) {
            if (object.getVersion() <= snapshot) {
                waitUntilVersionIsCommitted();
                return object;
            }
        }

        for (DataObject object : committedObjectVersions) {
            if (object.getVersion() <= snapshot) {
                return object;
            }
        }

        return null;
    }

    private int generateCommitTimestamp(int snapshot) {
        if (clock <= snapshot) {
            clock = snapshot + 1;
        }

        return clock;
    }

    private boolean checkConflicts(List<DataObject> gets, List<DataObject> puts, int snapshot) {
        for (DataObject object : gets) {
            if (latestObjectVersions.get(object.getKey()) > snapshot) {
                return true;
            }
        }

        for (DataObject object : puts) {
            if (latestObjectVersions.get(object.getKey()) > snapshot) {
                return true;
            }
        }

        return false;
    }



    private void updateLatestObjectVersions(List<DataObject> objects, int version) {
        for (DataObject object : objects) {
            latestObjectVersions.put(object.getKey(), version);
        }
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private boolean acquireLocks(List<DataObject> gets, List<DataObject> puts) {
        return false;
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void releaseLocks(List<DataObject> puts) {
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void waitUntilVersionIsCommitted() {

    }

}
