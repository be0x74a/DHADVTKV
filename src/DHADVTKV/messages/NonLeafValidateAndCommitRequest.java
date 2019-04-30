package DHADVTKV.messages;

import DHADVTKV.common.DataObject;

import java.util.List;

public class NonLeafValidateAndCommitRequest extends Message {

    private final long transactionId;
    private final List<DataObject> gets;
    private final List<DataObject> puts;
    private final int client;
    private final long commitTimestamp;
    private final boolean conflicts;
    private final int noPartitionsTouched;

    private static final long CPU_TIME = 0;

    public NonLeafValidateAndCommitRequest(long transactionId, List<DataObject> gets, List<DataObject> puts, int client, long commitTimestamp, boolean conflicts, int noPartitionsTouched) {
        super(2 * LENGTH_LONG + (puts.size()) * LENGTH_OBJ + LENGTH_INT + LENGTH_BOOL, CPU_TIME);

        this.transactionId = transactionId;
        this.gets = gets;
        this.puts = puts;
        this.client = client;
        this.commitTimestamp = commitTimestamp;
        this.conflicts = conflicts;
        this.noPartitionsTouched = noPartitionsTouched;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public List<DataObject> getGets() {
        return gets;
    }

    public List<DataObject> getPuts() {
        return puts;
    }

    public int getClient() {
        return client;
    }

    public long getCommitTimestamp() {
        return commitTimestamp;
    }

    public boolean hasConflicts() {
        return conflicts;
    }

    public int getNoPartitionsTouched() {
        return noPartitionsTouched;
    }
}
