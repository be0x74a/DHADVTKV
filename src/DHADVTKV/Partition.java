package DHADVTKV;

import DHADVTKV.datatypes.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Partition {

    private KeyValueStorage kv;
    private long clock = 0;
    private Map<Long, Long> latestObjectVersions = new HashMap<>();

    public Partition(long nodeId, int noPartitions, int keyValueStoreSize) {
        this.kv = new KeyValueStorage(nodeId, noPartitions, keyValueStoreSize);
    }


    public TransactionalGetMessageResponse transactionalGet(TransactionalGetMessageRequest message) {
        List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(message.getKey());
        List<DataObject> committedObjectVersions = kv.getCommittedVersions(message.getKey());

        return new TransactionalGetMessageResponse(selectSnapshotConsistentVersion(message.getSnapshot(), tentativeObjectVersions, committedObjectVersions));
    }

    public PrepareMessageResponse prepare(PrepareMessageRequest message) {
        long commitTimestamp = generateCommitTimestamp(message.getSnapshot());
        boolean locksAcquired = acquireLocks(message.getPuts());
        boolean conflicts = true;

        if (locksAcquired) {
            conflicts = checkConflicts(message.getGets(), message.getPuts(), message.getSnapshot());
        }

        if (!conflicts) {
            kv.storeAsTentative(message.getTransactionId(), message.getPuts());
        }

        clock++;

        return new PrepareMessageResponse(conflicts, commitTimestamp);
    }

    public CommitMessageResponse commit(CommitMessageRequest message) {
        if (clock < message.getCommitTimestamp()) {
            clock = message.getCommitTimestamp() + 1;
        }

        if (message.hasConflicts()) {
            kv.deleteTentativeVersions(message.getTransactionId(), message.getPuts());
        } else {
            kv.commitTentativeVersions(message.getTransactionId(), message.getPuts(), message.getCommitTimestamp());
            updateLatestObjectVersions(message.getPuts(), message.getCommitTimestamp());
        }

        releaseLocks(message.getPuts());

        return new CommitMessageResponse();
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
    private boolean acquireLocks(List<DataObject> puts) {
        return false;
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void releaseLocks(List<DataObject> puts) {
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void waitUntilVersionIsCommitted() {

    }
}
