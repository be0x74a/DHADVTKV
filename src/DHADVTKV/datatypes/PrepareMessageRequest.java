package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

import java.util.List;

public class PrepareMessageRequest {

    private final long transactionId;
    private final long snapshot;
    private final List<DataObject> puts;
    private final List<DataObject> gets;
    private final int client;
    private final int partition;

    public PrepareMessageRequest(long transactionId, long snapshot, List<DataObject> puts, List<DataObject> gets, int client, int partition) {
        this.transactionId = transactionId;
        this.snapshot = snapshot;
        this.puts = puts;
        this.gets = gets;
        this.client = client;
        this.partition = partition;
        System.out.println(String.format("%d:%s:%d", client, getClass().getSimpleName(), partition));
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

    public List<DataObject> getGets() {
        return gets;
    }

    public int getPartition() {
        return partition;
    }

    public int getClient() {
        return client;
    }
}
