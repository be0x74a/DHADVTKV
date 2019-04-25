package DHADVTKV.ProposedLB;

import DHADVTKV.common.DataObject;
import DHADVTKV.common.KeyValueStorage;
import DHADVTKV.messages.*;

import java.util.List;

public class Partition {

    private final Validator validator;
    private KeyValueStorage kv;
    private int transactionsDone = 0;

    public Partition(long nodeId, int noPartitions, int keyValueStoreSize) {
        this.kv = new KeyValueStorage(nodeId, noPartitions, keyValueStoreSize);
        this.validator = new Validator(noPartitions);
    }

    public TransactionalGetMessageResponse transactionalGet(TransactionalGetMessageRequest message) {
        List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(message.getKey());
        List<DataObject> committedObjectVersions = kv.getCommittedVersions(message.getKey());

        DataObject obj = selectSnapshotConsistentVersion(message.getSnapshot(), tentativeObjectVersions, committedObjectVersions);

        return new TransactionalGetMessageResponse(obj, message.getPartition(), message.getClient());
    }

    public Message validateAndCommit(ValidateAndCommitTransactionRequest message) {
        kv.storeAsTentative(message.getTransactionId(), message.getPuts());
        Message response = validator.leafValidate(message.getTransactionId(), message.getSnapshot(), message.getGets(), message.getPuts(), message.getClient(), message.getNoPartitionsTouched());

        if (response instanceof ClientValidationResponse) {
            if (((ClientValidationResponse) response).hasConflicts()) {
                kv.deleteTentativeVersions(message.getTransactionId(), message.getPuts());
            } else {
                kv.commitTentativeVersions(message.getTransactionId(), message.getPuts(), ((ClientValidationResponse) response).getCommitTimestamp());
            }

            validator.leafCommit(message.getPuts(), ((ClientValidationResponse) response).getCommitTimestamp(), ((ClientValidationResponse) response).hasConflicts());
        }

        return response;
    }

    public void onTransactionValidationResult(PartitionValidationResponse message) {
        if (message.hasConflicts()) {
            kv.deleteTentativeVersions(message.getTransactionId(), message.getPuts());
        } else {
            kv.commitTentativeVersions(message.getTransactionId(), message.getPuts(), message.getCommitTimestamp());
        }

        validator.leafCommit(message.getPuts(), message.getCommitTimestamp(), message.hasConflicts());
        this.transactionsDone++;
        System.out.println(this.transactionsDone);
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
