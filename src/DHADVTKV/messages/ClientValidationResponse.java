package DHADVTKV.messages;

public class ClientValidationResponse extends Message {

  private final boolean conflicts;
  private final long commitTimestamp;
  private final int client;
  private final long transactionId;
  private static final long CPU_TIME = 0;

  public ClientValidationResponse(
      boolean conflicts, long commitTimestamp, int client, long transactionId) {
    super(LENGTH_BOOL + LENGTH_LONG, CPU_TIME);
    this.conflicts = conflicts;
    this.commitTimestamp = commitTimestamp;
    this.client = client;
    this.transactionId = transactionId;
  }

  public boolean hasConflicts() {
    return conflicts;
  }

  public long getCommitTimestamp() {
    return commitTimestamp;
  }

  public int getClient() {
    return client;
  }

  public long getTransactionId() {
    return transactionId;
  }
}
