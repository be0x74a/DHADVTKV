package DHADVTKV.ProposedTSB.messages;

import DHADVTKV.common.DataObject;

import java.util.List;

public class Transaction extends Message {

    private final long transactionID;
    private final long snapshot;
    private final List<Long> getKeys;
    private final List<DataObject> puts;
    private final int nValidations;
    private final int client;
    private final boolean conflicts;
    private final long lsn;

    public Transaction(int from, int to, long transactionID, long snapshot, List<Long> getKeys, List<DataObject> puts, int nValidations, int client, boolean conflicts, long lsn) {
        super(from, to, 0);

        this.transactionID = transactionID;
        this.snapshot = snapshot;
        this.getKeys = getKeys;
        this.puts = puts;
        this.nValidations = nValidations;
        this.client = client;
        this.conflicts = conflicts;
        this.lsn = lsn;
    }

    public long getTransactionID() {
        return transactionID;
    }

    public long getSnapshot() {
        return snapshot;
    }

    public List<Long> getGetKeys() {
        return getKeys;
    }

    public List<DataObject> getPuts() {
        return puts;
    }

    public int getnValidations() {
        return nValidations;
    }

    public int getClient() {
        return client;
    }

    public boolean isConflicts() {
        return conflicts;
    }

    public long getLsn() {
        return lsn;
    }
}
