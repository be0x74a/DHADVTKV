package dhadvtkv.messages;

import dhadvtkv.common.DataObject;
import java.util.List;

public class CommitMessageRequest extends Message {

  private final long transactionId;
  private final List<DataObject> puts;
  private final boolean conflicts;
  private final long commitTimestamp;
  private final int client;
  private final int partition;
  private static final long CPU_TIME = 60;

  public CommitMessageRequest(
      long transactionId,
      List<DataObject> puts,
      boolean conflicts,
      long commitTimestamp,
      int client,
      int partition) {
    super(2 * LENGTH_LONG + puts.size() * LENGTH_OBJ + LENGTH_BOOL, CPU_TIME);
    this.transactionId = transactionId;
    this.puts = puts;
    this.conflicts = conflicts;
    this.commitTimestamp = commitTimestamp;
    this.client = client;
    this.partition = partition;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public List<DataObject> getPuts() {
    return puts;
  }

  public boolean hasConflicts() {
    return conflicts;
  }

  public long getCommitTimestamp() {
    return commitTimestamp;
  }

  public int getPartition() {
    return partition;
  }

  public int getClient() {
    return client;
  }
}
