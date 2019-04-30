package DHADVTKV.messages;

import DHADVTKV.common.DataObject;

import java.util.List;

public class ValidateAndCommitTransactionRequest extends Message{

    private final long transactionId;
    private final long snapshot;
    private final List<DataObject> puts;
    private final List<DataObject> gets;
    private final int client;
    private final int partition;
    private final int noPartitionsTouched;
    private final static long CPU_TIME = 560;

    public ValidateAndCommitTransactionRequest(long transactionId, long snapshot, List<DataObject> puts, List<DataObject> gets, int client, int partition, int noPartitionsTouched) {
        super(2 * LENGTH_LONG + (puts.size() + gets.size()) * LENGTH_OBJ + LENGTH_INT, CPU_TIME);
        this.transactionId = transactionId;
        this.snapshot = snapshot;
        this.puts = puts;
        this.gets = gets;
        this.client = client;
        this.partition = partition;
        this.noPartitionsTouched = noPartitionsTouched;
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

    public int getNoPartitionsTouched() {
        return noPartitionsTouched;
    }
}
