package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.messages.Message;
import java.util.List;

public class TransactionValidationBatch extends Message {

  private final List<TransactionValidation> transactionValidationBatch;

  public TransactionValidationBatch(
      int from, int to, List<TransactionValidation> transactionValidationBatch) {
    super(from, to, transactionValidationBatch.stream().mapToLong(TransactionValidation::getSize).sum());

    this.transactionValidationBatch = transactionValidationBatch;
  }

  public List<TransactionValidation> getTransactionValidationBatch() {
    return transactionValidationBatch;
  }
}
