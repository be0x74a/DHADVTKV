package DHADVTKV.ProposedLB;

import DHADVTKV.common.DataObject;
import DHADVTKV.common.KeyValueStorage;
import DHADVTKV.messages.TransactionalGetMessageRequest;
import DHADVTKV.messages.TransactionalGetMessageResponse;
import DHADVTKV.messages.ValidateAndCommitTransactionRequest;

import java.util.List;

public class Partition {

    private final Validator validator;
    private KeyValueStorage kv;
    private long clock = 0;
    private int transactionsDone = 0;

    public Partition(long nodeId, int noPartitions, int keyValueStoreSize) {
        this.kv = new KeyValueStorage(nodeId, noPartitions, keyValueStoreSize);
        this.validator = new Validator();
    }

    public TransactionalGetMessageResponse transactionalGet(TransactionalGetMessageRequest message) {
        List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(message.getKey());
        List<DataObject> committedObjectVersions = kv.getCommittedVersions(message.getKey());

        DataObject obj = selectSnapshotConsistentVersion(message.getSnapshot(), tentativeObjectVersions, committedObjectVersions);

        return new TransactionalGetMessageResponse(obj, message.getPartition(), message.getClient());
    }

    public void validateAndCommit(ValidateAndCommitTransactionRequest message) {
        kv.storeAsTentative(message.getTransactionId(), message.getPuts());
        validator.leaf_validate(message.getTransactionId(), message.getSnapshot(), message.getGets(), message.getPuts(), message.getClient());
    }

    public void validationResponse(ValidationResponse message) {
        if (message.hasConflicts()) {
            kv.deleteTentativeVersions(message.getTransactionId(), message.getPuts());
        } else {
            kv.commitTentativeVersions(message.getTransactionId(), message.getPuts(), message.getCommitTimestamp());
        }

        validator.leafCommit(message.getGets(), message.getPuts(), message.getCommitTimestamp());
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

    //TODO: This is obviously ONLY a placeholder!!!
    private void waitUntilVersionIsCommitted() {

    }

}
