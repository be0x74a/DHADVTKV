package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.common.Configurations;
import java.util.List;

public class BatchValidate extends ValidatorMessage {

  private final List<Transaction> transactionsBatch;

  public BatchValidate(int from, int to, List<Transaction> transactionsBatch) {
    super(from, to, transactionsBatch.stream().mapToLong(t -> t.getSize() - Configurations.HEADER_SIZE).sum());

    this.transactionsBatch = transactionsBatch;
  }

  public List<Transaction> getTransactionsBatch() {
    return transactionsBatch;
  }
}
