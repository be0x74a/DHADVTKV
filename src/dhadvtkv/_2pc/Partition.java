package dhadvtkv._2pc;

import static dhadvtkv.common.Configurations.UNDEFINED;

import dhadvtkv._2pc.messages.CommitResult;
import dhadvtkv._2pc.messages.CommitTransaction;
import dhadvtkv._2pc.messages.PrepareResult;
import dhadvtkv._2pc.messages.PrepareTransaction;
import dhadvtkv.common.Channel;
import dhadvtkv.common.DataObject;
import dhadvtkv.common.KeyValueStorage;
import dhadvtkv.messages.TransactionalGet;
import dhadvtkv.messages.TransactionalGetResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class Partition {

  private final int nodeID;
  private final KeyValueStorage kv;
  private final Map<Long, List<TransactionalGet>> pendingTransactionalGets;
  private final Map<Long, Long> latestObjectVersions;
  private final Set<Long> lockSet;
  private int transactionsDone;
  private long clock;

  Partition(int nodeID) {
    this.nodeID = nodeID;
    this.kv = new KeyValueStorage(nodeID);
    this.pendingTransactionalGets = new HashMap<>();
    this.latestObjectVersions = new HashMap<>();
    this.lockSet = new HashSet<>();
    this.transactionsDone = 0;
    this.clock = 0;
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

    moveClockForward(request.getSnapshot());
  }

  void prepareTransaction(PrepareTransaction request) {

    long timestamp = clock > request.getSnapshot() ? clock : request.getSnapshot() + 1;
    moveClockForward(timestamp);
    boolean conflicts;
    boolean lockConflicts =
        checkLockConflicts(
            request.getGetKeys(),
            request.getPuts().stream().map(DataObject::getKey).collect(Collectors.toList()));

    if (!lockConflicts) {
      boolean versionConflicts =
          checkVersionConflicts(
              request.getSnapshot(),
              request.getGetKeys(),
              request.getPuts().stream().map(DataObject::getKey).collect(Collectors.toList()));
      if (versionConflicts) {
        conflicts = true;
      } else {
        lockSet.addAll(request.getGetKeys());
        lockSet.addAll(
            request.getPuts().stream().map(DataObject::getKey).collect(Collectors.toList()));
        conflicts = false;
      }
    } else {
      conflicts = true;
    }

    if (!conflicts) {
      kv.storeAsTentative(request.getPuts());
    }

    Channel.sendMessage(new PrepareResult(nodeID, request.getFrom(), conflicts, timestamp));
  }

  void commit(CommitTransaction request) {
    if (!request.isAborted()) {
      for (Long key : request.getPutKeys()) {
        latestObjectVersions.put(key, request.getTimestamp());
      }
      moveClockForward(request.getTimestamp());
    }

    lockSet.removeAll(request.getGetKeys());
    lockSet.removeAll(request.getPutKeys());

    if (request.isAborted()) {
      kv.deleteTentativeVersions(request.getTransactionID(), request.getPutKeys());
    } else {
      kv.commitTentativeVersions(request.getTransactionID(), request.getPutKeys(), clock);
    }

    for (TransactionalGet getRequest :
        pendingTransactionalGets.getOrDefault(request.getTransactionID(), new ArrayList<>())) {
      transactionalGet(getRequest);
    }

    Channel.sendMessage(new CommitResult(nodeID, request.getFrom(), request.getTransactionID()));
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
      } else if (consistent != null) {
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

  private boolean checkLockConflicts(List<Long> getKeys, List<Long> putKeys) {
    for (Long key : getKeys) {
      if (lockSet.contains(key)) {
        return true;
      }
    }

    for (Long key : putKeys) {
      if (lockSet.contains(key)) {
        return true;
      }
    }

    return false;
  }

  private boolean checkVersionConflicts(long snapshot, List<Long> getKeys, List<Long> putKeys) {
    for (Long key : getKeys) {
      if (latestObjectVersions.getOrDefault(key, UNDEFINED) > snapshot) {
        return true;
      }
    }

    for (Long key : putKeys) {
      if (latestObjectVersions.getOrDefault(key, UNDEFINED) > snapshot) {
        return true;
      }
    }

    return false;
  }

  private void moveClockForward(long timestamp) {
    if (clock <= timestamp) {
      clock = timestamp + 1;
    }
  }

  private void logTransaction() {
    this.transactionsDone++;
  }

  void printTransactionsDone() {
    System.out.println(transactionsDone);
  }

  long getTransactionsDone() {
    return transactionsDone;
  }
}
