package DHADVTKV.messages;

public class PrepareMessageResponse extends Message {

    private final boolean conflicts;
    private final long commitTimestamp;
    private final int partition;
    private final int client;
    private final static long LENGTH = 9;
    private final static long CPU_TIME = 0;

    public PrepareMessageResponse(boolean conflicts, long commitTimestamp, int partition, int client) {
        super(LENGTH, CPU_TIME);
        this.conflicts = conflicts;
        this.commitTimestamp = commitTimestamp;
        this.partition = partition;
        this.client = client;
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
