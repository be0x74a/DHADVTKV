package dhadvtkv.messages;

import dhadvtkv.common.DataObject;

public class TransactionalGetMessageResponse extends Message {

  private final DataObject object;
  private final int partition;
  private final int client;
  private static final long CPU_TIME = 0;

  public TransactionalGetMessageResponse(DataObject object, int partition, int client) {
    super(LENGTH_OBJ, CPU_TIME);
    this.object = object;
    this.partition = partition;
    this.client = client;
  }

  public DataObject getObject() {
    return object;
  }

  public int getClient() {
    return client;
  }

  public int getPartition() {
    return partition;
  }
}
