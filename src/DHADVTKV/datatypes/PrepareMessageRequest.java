package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

import java.util.List;

public class PrepareMessageRequest extends Message {

    private final long transactionId;
    private final long snapshot;
    private final List<DataObject> puts;
    private final List<DataObject> gets;
    private final int client;
    private final int partition;
    private static final long LENGTH = 9;


    public PrepareMessageRequest(long transactionId, long snapshot, List<DataObject> puts, List<DataObject> gets, int client, int partition) {
        super(LENGTH);
        this.transactionId = transactionId;
        this.snapshot = snapshot;
        this.puts = puts;
        this.gets = gets;
        this.client = client;
        this.partition = partition;
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
