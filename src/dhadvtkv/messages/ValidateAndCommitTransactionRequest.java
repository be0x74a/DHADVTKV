package dhadvtkv.messages;

import dhadvtkv.common.DataObject;
import java.util.List;

public class ValidateAndCommitTransactionRequest extends Message {

  private final long transactionId;
  private final long snapshot;
  private final List<DataObject> puts;
  private final List<Long> gets;
  private final int client;
  private final int partition;
  private final int noPartitionsTouched;
  private static final long CPU_TIME = 560;

  public ValidateAndCommitTransactionRequest(
      long transactionId,
      long snapshot,
      List<Long> gets,
      List<DataObject> puts,
      int noPartitionsTouched,
      int client,
      int partition) {
    super((2 + gets.size()) * LENGTH_LONG + puts.size() * LENGTH_OBJ + LENGTH_INT, CPU_TIME);
    this.transactionId = transactionId;
    this.snapshot = snapshot;
    this.puts = puts;
    this.gets = gets;
    this.client = client;
    this.partition = partition;
    this.noPartitionsTouched = noPartitionsTouched;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public long getSnapshot() {
    return snapshot;
  }

  public List<DataObject> getPuts() {
    return puts;
  }

  public List<Long> getGets() {
    return gets;
  }

  public int getPartition() {
    return partition;
  }

  public int getClient() {
    return client;
  }

  public int getNoPartitionsTouched() {
    return noPartitionsTouched;
  }
}
