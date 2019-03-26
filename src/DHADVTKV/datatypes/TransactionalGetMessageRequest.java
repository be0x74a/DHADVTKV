package DHADVTKV.datatypes;

public class TransactionalGetMessageRequest {

    private final long key;
    private final long snapshot;

    public TransactionalGetMessageRequest(long key, long snapshot) {
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
