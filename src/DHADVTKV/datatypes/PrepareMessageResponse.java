package DHADVTKV.datatypes;

public class PrepareMessageResponse {

    private final boolean conflicts;
    private final long commitTimestamp;


    public PrepareMessageResponse(boolean conflicts, long commitTimestamp) {
        this.conflicts = conflicts;
        this.commitTimestamp = commitTimestamp;
    }

    public boolean hasConflicts() {
        return conflicts;
    }

    public long getCommitTimestamp() {
        return commitTimestamp;
    }
}
