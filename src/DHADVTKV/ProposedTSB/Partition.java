package DHADVTKV.ProposedTSB;

import static DHADVTKV.common.Configurations.UNDEFINED;

import DHADVTKV.ProposedTSB.messages.CommitTransaction;
import DHADVTKV.ProposedTSB.messages.TransactionValidation;
import DHADVTKV.ProposedTSB.messages.TransactionValidationBatch;
import DHADVTKV.ProposedTSB.messages.TransactionalGet;
import DHADVTKV.ProposedTSB.messages.TransactionalGetResponse;
import DHADVTKV.ProposedTSB.messages.ValidateAndCommit;
import DHADVTKV.common.Channel;
import DHADVTKV.common.DataObject;
import DHADVTKV.common.KeyValueStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Partition {

  private final int nodeID;
  private final KeyValueStorage kv;
  private final Map<Long, List<TransactionalGet>> pendingTransactionalGets;
  private int transactionsDone;

  Partition(int nodeID) {
    this.nodeID = nodeID;
    this.kv = new KeyValueStorage();
    this.pendingTransactionalGets = new HashMap<>();
  }

  void transactionalGet(TransactionalGet request) {
    List<DataObject> objectVersions = kv.getCommittedVersions(request.getKey());
    objectVersions.addAll(kv.getTentativeVersions(request.getKey()));

    DataObject snapshotConsistentContent =
        selectSnapshotConsistentVersion(objectVersions, request.getSnapshot());

    if (snapshotConsistentContent == null) {
      return;
    }

    long version = snapshotConsistentContent.getMetadata().getOrDefault("version", UNDEFINED);

    if (version == UNDEFINED) {
      long tentativeVersion =
          snapshotConsistentContent.getMetadata().getOrDefault("tentativeVersion", UNDEFINED);
      if (tentativeVersion <= request.getSnapshot()) {
        long transactionID =
            snapshotConsistentContent.getMetadata().getOrDefault("transactionID", UNDEFINED);
        this.pendingTransactionalGets
            .computeIfAbsent(transactionID, k -> new ArrayList<>())
            .add(request);
      }
    } else {
      Channel.sendMessage(
          new TransactionalGetResponse(
              request.getTo(), request.getFrom(), snapshotConsistentContent));
    }
  }

  void commitTransaction(CommitTransaction request) {
    Channel.sendMessage(
        new ValidateAndCommit(
            nodeID,
            nodeID,
            request.getTransactionID(),
            request.getSnapshot(),
            request.getGetKeys(),
            request.getPuts(),
            request.getnValidations(),
            request.getFrom()));
    kv.storeAsTentative(request.getPuts());
  }

  void transactionValidationBatch(TransactionValidationBatch batch) {
    for (TransactionValidation request : batch.getTransactionValidationBatch()) {
      transactionValidation(request);
    }
  }

  void transactionValidation(TransactionValidation request) {
    if (request.isConflicts()) {
      kv.deleteTentativeVersions(request.getTransactionID(), request.getPutKeys());
    } else {
      kv.commitTentativeVersions(
          request.getTransactionID(), request.getPutKeys(), request.getLsn());
    }

    for (TransactionalGet getRequest :
        pendingTransactionalGets.getOrDefault(request.getTransactionID(), new ArrayList<>())) {
      transactionalGet(getRequest);
    }

    logTransaction();
    pendingTransactionalGets.remove(request.getTransactionID());
  }

  private DataObject selectSnapshotConsistentVersion(List<DataObject> objects, long snapshot) {

    DataObject consistent = null;

    for (DataObject object : objects) {
      long objectVersion = object.getMetadata().getOrDefault("version", UNDEFINED);
      if (objectVersion == UNDEFINED) {
        objectVersion = object.getMetadata().getOrDefault("tentativeVersion", UNDEFINED);
      }

      if (consistent == null && objectVersion <= snapshot) {
        consistent = object;
      } else {
        assert consistent != null;
        long consistentVersion = consistent.getMetadata().getOrDefault("version", UNDEFINED);
        if (consistentVersion == UNDEFINED) {
          consistentVersion = consistent.getMetadata().getOrDefault("tentativeVersion", UNDEFINED);
        }

        if (objectVersion > consistentVersion && objectVersion <= snapshot) {
          consistent = object;
        }
      }
    }

    return consistent;
  }

  private void logTransaction() {
    this.transactionsDone++;
    System.out.println(this.transactionsDone);
  }
}
