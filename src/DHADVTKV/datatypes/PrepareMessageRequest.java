package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

import java.util.List;

public class PrepareMessageRequest {

    private final long transactionId;
    private final long snapshot;
    private final List<DataObject> puts;
    private final List<DataObject> gets;

    public PrepareMessageRequest(long transactionId, long snapshot, List<DataObject> puts, List<DataObject> gets) {
        this.transactionId = transactionId;
        this.snapshot = snapshot;
        this.puts = puts;
        this.gets = gets;
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
}
