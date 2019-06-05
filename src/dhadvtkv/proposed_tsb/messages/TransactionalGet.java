package dhadvtkv.proposed_tsb.messages;

public class TransactionalGet extends Message {

  private final long key;
  private final long snapshot;

  public TransactionalGet(int from, int to, long key, long snapshot) {
    super(from, to, 0);

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
