package dhadvtkv._2pc.messages;

import dhadvtkv.common.DataObject;
import dhadvtkv.messages.Message;
import java.util.List;

public class PrepareTransaction extends Message {

  private final long snapshot;
  private final List<Long> getKeys;
  private final List<DataObject> puts;

  public PrepareTransaction(
      int from, int to, long snapshot, List<Long> getKeys, List<DataObject> puts) {
    super(from, to, 0);

    this.snapshot = snapshot;
    this.getKeys = getKeys;
    this.puts = puts;
  }

  public long getSnapshot() {
    return snapshot;
  }

  public List<Long> getGetKeys() {
    return getKeys;
  }

  public List<DataObject> getPuts() {
    return puts;
  }
}
