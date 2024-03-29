package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.common.DataObject;
import dhadvtkv.messages.Message;
import java.util.List;

public class CommitTransaction extends Message {

  private final long transactionID;
  private final long snapshot;
  private final List<Long> getKeys;
  private final List<DataObject> puts;
  private final int nValidations;

  public CommitTransaction(
      int from,
      int to,
      long transactionID,
      long snapshot,
      List<Long> getKeys,
      List<DataObject> puts,
      int nValidations) {
    super(from, to, (2 + getKeys.size()) * LENGTH_LONG + puts.size() * LENGTH_OBJ + LENGTH_INT);

    this.transactionID = transactionID;
    this.snapshot = snapshot;
    this.getKeys = getKeys;
    this.puts = puts;
    this.nValidations = nValidations;
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
}
