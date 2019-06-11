package dhadvtkv._2pc.messages;

import dhadvtkv.messages.Message;

public class CommitResult extends Message {

  // Not accountable for size
  private final long transactionID;

  public CommitResult(int from, int to, long transactionID) {
    super(from, to, 0);
    this.transactionID = transactionID;
  }

  public long getTransactionID() {
    return transactionID;
  }
}
