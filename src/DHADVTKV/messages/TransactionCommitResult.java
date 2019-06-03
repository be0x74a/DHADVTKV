package DHADVTKV.messages;

import java.util.List;

public class TransactionCommitResult extends Message {

    private final long transactionID;
    private final boolean conflicts;
    private final long lsn;

    public TransactionCommitResult(long transactionID, boolean conflicts, long lsn) {
        super(0,0);

        this.transactionID = transactionID;
        this.conflicts = conflicts;
        this.lsn = lsn;
    }

    public long getTransactionID() {
        return transactionID;
    }

    public boolean isConflicts() {
        return conflicts;
    }

    public long getLsn() {
        return lsn;
    }
}
