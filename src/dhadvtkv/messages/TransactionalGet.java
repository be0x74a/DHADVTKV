package dhadvtkv.messages;

public class TransactionalGet extends Message {

  private final long key;
  private final long snapshot;

  public TransactionalGet(int from, int to, long key, long snapshot) {
    super(from, to, 2*LENGTH_LONG);

    this.key = key;
    this.snapshot = snapshot;
  }

  public long getKey() {
    return key;
  }

  public long getSnapshot() {
    return snapshot;
  }
}
