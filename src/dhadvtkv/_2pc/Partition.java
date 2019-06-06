package dhadvtkv._2pc;

import dhadvtkv.common.DataObject;
import dhadvtkv.common.KeyValueStorage;
import dhadvtkv.messages.CommitMessageRequest;
import dhadvtkv.messages.CommitMessageResponse;
import dhadvtkv.messages.PrepareMessageRequest;
import dhadvtkv.messages.PrepareMessageResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Partition {

  private KeyValueStorage kv;
  private long clock = 0;
  private int transactionsDone = 0;
  private Map<Long, Long> latestObjectVersions = new HashMap<>();

  public Partition(long nodeId, int noPartitions, int keyValueStoreSize) {
    this.kv = new KeyValueStorage(nodeId, noPartitions, keyValueStoreSize);
  }

  public TransactionalGetMessageResponse transactionalGet(TransactionalGetMessageRequest message) {
    List<DataObject> tentativeObjectVersions = kv.getTentativeVersions(message.getKey());
    List<DataObject> committedObjectVersions = kv.getCommittedVersions(message.getKey());

    DataObject obj =
        selectSnapshotConsistentVersion(
            message.getSnapshot(), tentativeObjectVersions, committedObjectVersions);

    return new TransactionalGetMessageResponse(obj, message.getPartition(), message.getClient());
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

    return new PrepareMessageResponse(
        conflicts, commitTimestamp, message.getPartition(), message.getClient());
  }

  public CommitMessageResponse commit(CommitMessageRequest message) {
    if (clock < message.getCommitTimestamp()) {
      clock = message.getCommitTimestamp() + 1;
    }

    if (message.hasConflicts()) {
      kv.deleteTentativeVersions(message.getTransactionId(), message.getPuts());
    } else {
      kv.commitTentativeVersions(
          message.getTransactionId(), message.getPuts(), message.getCommitTimestamp());
      updateLatestObjectVersions(message.getPuts(), message.getCommitTimestamp());
    }

    releaseLocks(message.getPuts());
    this.transactionsDone++;
    System.out.println(this.transactionsDone);

    return new CommitMessageResponse(
        message.getPartition(), message.getClient(), message.getTransactionId());
  }

  private DataObject selectSnapshotConsistentVersion(
      long snapshot,
      List<DataObject> tentativeObjectVersions,
      List<DataObject> committedObjectVersions) {

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
    if (gets != null) {
      for (DataObject object : gets) {
        if (this.latestObjectVersions.getOrDefault(object.getKey(), -1L) > snapshot) {
          return true;
        }
      }
    }

    if (puts != null) {
      for (DataObject object : puts) {
        if (this.latestObjectVersions.getOrDefault(object.getKey(), -1L) > snapshot) {
          return true;
        }
      }
    }

    return false;
  }

  private void updateLatestObjectVersions(List<DataObject> objects, long version) {
    for (DataObject object : objects) {
      this.latestObjectVersions.put(object.getKey(), version);
    }
  }

  // TODO: This is obviously ONLY a placeholder!!!
  private boolean acquireLocks(List<DataObject> puts) {
    return true;
  }

  // TODO: This is obviously ONLY a placeholder!!!
  private void releaseLocks(List<DataObject> puts) {}

  // TODO: This is obviously ONLY a placeholder!!!
  private void waitUntilVersionIsCommitted() {}
}
