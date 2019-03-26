package DHADVTKV;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Partition {

    private KeyValueStorage kv = new KeyValueStorage();
    private long clock = 0;
    private Map<Long, Long> latestObjectVersions = new HashMap<>();


    public DataObject transactionalGet(long key, long snapshot) {
        return onClientTransactionGetRequest(key, snapshot);
    }

    private DataObject onClientTransactionGetRequest(long key, long snapshot) {

        List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(key);
        List<DataObject> committedObjectVersions = kv.getCommittedVersions(key);

        return selectSnapshotConsistentVersion(snapshot, tentativeObjectVersions, committedObjectVersions);
    }

    public void prepare(long transactionId, long snapshot, List<DataObject> puts, List<DataObject> gets, Client client) {
        onPrepareRequest(transactionId, snapshot, gets, puts, client);
    }

    private void onPrepareRequest(long transactionId, long snapshot, List<DataObject> gets, List<DataObject> puts, Client client) {
        long commitTimestamp = generateCommitTimestamp(snapshot);
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

    public void commit(long transactionId, List<DataObject> puts, boolean conflicts, long commitTimestamp, Client client) {
        onCommitRequest(transactionId, puts, conflicts, commitTimestamp, client);
    }

    private void onCommitRequest(long transactionId, List<DataObject> puts, boolean conflicts, long commitTimestamp, Client client) {
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


    private DataObject selectSnapshotConsistentVersion(long snapshot, List<DataObject> tentativeObjectVersions, List<DataObject> committedObjectVersions) {

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

    private long generateCommitTimestamp(long snapshot) {
        if (clock <= snapshot) {
            clock = snapshot + 1;
        }

        return clock;
    }

    private boolean checkConflicts(List<DataObject> gets, List<DataObject> puts, long snapshot) {
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



    private void updateLatestObjectVersions(List<DataObject> objects, long version) {
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
