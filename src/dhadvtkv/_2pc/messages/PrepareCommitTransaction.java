package dhadvtkv._2pc.messages;

import dhadvtkv.common.DataObject;
import dhadvtkv.messages.Message;
import java.util.List;

public class PrepareCommitTransaction extends Message {

  private final long snapshot;
  private final List<Long> getKeys;
  private final List<DataObject> puts;

  public PrepareCommitTransaction(
      int from, int to, long snapshot, List<Long> getKeys, List<DataObject> puts) {
    super(from, to, (1 + getKeys.size()) * LENGTH_LONG + puts.size() * LENGTH_OBJ);

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
