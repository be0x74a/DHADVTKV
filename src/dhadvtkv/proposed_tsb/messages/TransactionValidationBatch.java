package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import java.util.List;

public class TransactionValidationBatch extends Message {

  private final List<TransactionValidation> transactionValidationBatch;

  public TransactionValidationBatch(
      int from, int to, List<TransactionValidation> transactionValidationBatch) {
    super(
        from,
        to,
        transactionValidationBatch.stream()
            .mapToLong(t -> t.getSize() - Configurations.HEADER_SIZE)
            .sum());

    this.transactionValidationBatch = transactionValidationBatch;
  }

  public List<TransactionValidation> getTransactionValidationBatch() {
    return transactionValidationBatch;
  }
}
