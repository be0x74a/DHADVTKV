package dhadvtkv.proposed_tsb.messages;

import java.util.List;

public class TransactionValidation extends Message {

  private final long transactionID;
  private final List<Long> putKeys;
  private final boolean conflicts;
  private final long lsn;

  public TransactionValidation(
      int from, int to, long transactionID, List<Long> putKeys, boolean conflicts, long lsn) {
    super(from, to, 0);

    this.transactionID = transactionID;
    this.putKeys = putKeys;
    this.conflicts = conflicts;
    this.lsn = lsn;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public List<Long> getPutKeys() {
    return putKeys;
  }

  public boolean isConflicts() {
    return conflicts;
  }

  public long getLsn() {
    return lsn;
  }
}
