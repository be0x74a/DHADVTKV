package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.messages.Message;

public class TransactionCommitResult extends Message {

  private final long transactionID;
  private final boolean conflicts;
  private final long lsn;

  public TransactionCommitResult(
      int from, int to, long transactionID, boolean conflicts, long lsn) {
    super(from, to, 0);

    this.transactionID = transactionID;
    this.conflicts = conflicts;
    this.lsn = lsn;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public boolean isConflicts() {
    return conflicts;
  }

  public long getLsn() {
    return lsn;
  }
}
