package DHADVTKV.messages;

public class ValidateAndCommitTransactionResponse extends Message {

    private final int client;
    private final int partition;
    private final long transactionId;
    private static final long LENGTH = 8;
    private final static long CPU_TIME = 0;
    private final long commitTimestamp;

    public ValidateAndCommitTransactionResponse(int partition, int client, long transactionId, long commitTimestamp){
        super(LENGTH, CPU_TIME);
        this.partition = partition;
        this.client = client;
        this.transactionId = transactionId;
        this.commitTimestamp = commitTimestamp;
    }

    public int getClient() {
        return client;
    }

    public int getPartition() {
        return partition;
    }

    public long getTransactionId() {
        return this.transactionId;
    }

    public long getCommitTimestamp() {
        return commitTimestamp;
    }
}
