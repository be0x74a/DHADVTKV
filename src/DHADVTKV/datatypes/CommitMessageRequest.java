package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

import java.util.List;

public class CommitMessageRequest {

    private final long transactionId;
    private final List<DataObject> puts;
    private final boolean conflicts;
    private final long commitTimestamp;

    public CommitMessageRequest(long transactionId, List<DataObject> puts, boolean conflicts, long commitTimestamp) {
        this.transactionId = transactionId;
        this.puts = puts;
        this.conflicts = conflicts;
        this.commitTimestamp = commitTimestamp;
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
}
