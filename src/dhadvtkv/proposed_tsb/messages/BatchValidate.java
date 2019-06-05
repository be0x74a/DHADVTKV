package dhadvtkv.proposed_tsb.messages;

import java.util.List;

public class BatchValidate extends Message {

  private final List<Transaction> transactionsBatch;

  public BatchValidate(int from, int to, List<Transaction> transactionsBatch) {
    super(from, to, 0);

    this.transactionsBatch = transactionsBatch;
  }

  public List<Transaction> getTransactionsBatch() {
    return transactionsBatch;
  }
}
