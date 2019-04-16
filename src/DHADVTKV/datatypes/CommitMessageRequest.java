package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

import java.util.List;

public class CommitMessageRequest extends Message{

    private final long transactionId;
    private final List<DataObject> puts;
    private final boolean conflicts;
    private final long commitTimestamp;
    private final int client;
    private final int partition;
    private final static long LENGTH = 1041;
    private final static long CPU_TIME = 60;

    public CommitMessageRequest(long transactionId, List<DataObject> puts, boolean conflicts, long commitTimestamp, int client, int partition) {
        super(LENGTH, CPU_TIME);
        this.transactionId = transactionId;
        this.puts = puts;
        this.conflicts = conflicts;
        this.commitTimestamp = commitTimestamp;
        this.client = client;
        this.partition = partition;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public List<DataObject> getPuts() {
        return puts;
    }

    public boolean hasConflicts() {
        return conflicts;
    }

    public long getCommitTimestamp() {
        return commitTimestamp;
    }

    public int getPartition() {
        return partition;
    }

    public int getClient() {
        return client;
    }
}
