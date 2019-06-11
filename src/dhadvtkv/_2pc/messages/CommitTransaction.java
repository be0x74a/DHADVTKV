package dhadvtkv._2pc.messages;

import dhadvtkv.messages.Message;
import java.util.List;

public class CommitTransaction extends Message {

  private final long transactionID;
  private final List<Long> getKeys;
  private final List<Long> putKeys;
  private final boolean aborted;
  private final long timestamp;

  public CommitTransaction(
      int from,
      int to,
      long transactionID,
      List<Long> getKeys,
      List<Long> putKeys,
      boolean aborted,
      long timestamp) {
    super(from, to, (2 + getKeys.size() + putKeys.size()) * LENGTH_LONG + LENGTH_BOOL);

    this.transactionID = transactionID;
    this.getKeys = getKeys;
    this.putKeys = putKeys;
    this.aborted = aborted;
    this.timestamp = timestamp;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public List<Long> getGetKeys() {
    return getKeys;
  }

  public List<Long> getPutKeys() {
    return putKeys;
  }

  public boolean isAborted() {
    return aborted;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
