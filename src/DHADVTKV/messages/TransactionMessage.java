package DHADVTKV.messages;

import DHADVTKV.common.DataObject;
import java.util.List;

public class TransactionMessage extends Message {

  private final long transactionID;
  private final long snapshot;
  private final List<Long> gets;
  private final List<DataObject> puts;
  private final int nValidations;
  private final int client;
  private final boolean conflicts;
  private final long lsn;

  public TransactionMessage(
      long transactionID,
      long snapshot,
      List<Long> gets,
      List<DataObject> puts,
      int nValidations,
      int client,
      boolean conflicts,
      long lsn) {
    super(0, 0);

    this.transactionID = transactionID;
    this.snapshot = snapshot;
    this.gets = gets;
    this.puts = puts;
    this.nValidations = nValidations;
    this.client = client;
    this.conflicts = conflicts;
    this.lsn = lsn;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public long getSnapshot() {
    return snapshot;
  }

  public List<Long> getGets() {
    return gets;
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

  public long getLsn() {
    return lsn;
  }
}
