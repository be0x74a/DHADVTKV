package DHADVTKV.datatypes;

public class TransactionalGetMessageRequest {

    private final long key;
    private final long snapshot;
    private final int partition;
    private final int client;

    public TransactionalGetMessageRequest(long key, long snapshot, int client, int partition) {
        this.key = key;
        this.snapshot = snapshot;
        this.client = client;
        this.partition = partition;
    }


    public long getKey() {
        return key;
    }

    public long getSnapshot() {
        return snapshot;
    }

    public int getPartition() {
        return partition;
    }

    public int getClient() {
        return client;
    }
}
