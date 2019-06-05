package dhadvtkv.messages;

import dhadvtkv.common.DataObject;
import java.util.List;

public class PartitionValidationResponse extends Message {

  private final long transactionId;
  private final List<DataObject> gets;
  private final List<DataObject> puts;
  private final boolean conflicts;
  private final long commitTimestamp;
  private final int partition;
  private static final long CPU_TIME = 0;

  public PartitionValidationResponse(
      long transactionId,
      List<DataObject> gets,
      List<DataObject> puts,
      boolean conflicts,
      long commitTimestamp,
      int partition) {
    // super(2 * LENGTH_LONG + (puts.size() + gets.size()) * LENGTH_OBJ + LENGTH_BOOL, CPU_TIME);
    super(0, CPU_TIME);
    this.transactionId = transactionId;
    this.gets = gets;
    this.puts = puts;
    this.conflicts = conflicts;
    this.commitTimestamp = commitTimestamp;
    this.partition = partition;
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

  public long getTransactionId() {
    return transactionId;
  }

  public List<DataObject> getGets() {
    return gets;
  }

  public List<DataObject> getPuts() {
    return puts;
  }
}
