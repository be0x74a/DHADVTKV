package DHADVTKV.ProposedTSB.dataStructures;

import DHADVTKV.common.DataObject;
import java.util.List;

public class WaitingStabilityTransaction {

  private final long transactionID;
  private final long snapshot;
  private final List<Long> getKeys;
  private final List<DataObject> puts;
  private final int nValidations;
  private final int client;
  private boolean conflicts;

  public WaitingStabilityTransaction(
      long transactionID,
      long snapshot,
      List<Long> getKeys,
      List<DataObject> puts,
      int nValidations,
      int client,
      boolean conflicts) {
    this.transactionID = transactionID;
    this.snapshot = snapshot;
    this.getKeys = getKeys;
    this.puts = puts;
    this.nValidations = nValidations;
    this.client = client;
    this.conflicts = conflicts;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public long getSnapshot() {
    return snapshot;
  }

  public List<Long> getGetKeys() {
    return getKeys;
  }

  public List<DataObject> getPuts() {
    return puts;
  }

  public int getnValidations() {
    return nValidations;
  }

  public int getClient() {
    return client;
  }

  public boolean isConflicts() {
    return conflicts;
  }
}
