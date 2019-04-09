package DHADVTKV.datatypes;

public class PrepareMessageResponse {

    private final boolean conflicts;
    private final long commitTimestamp;
    private final int partition;
    private final int client;


    public PrepareMessageResponse(boolean conflicts, long commitTimestamp, int partition, int client) {
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
