package DHADVTKV.datatypes;

public class CommitMessageResponse extends Message {

    private final int client;
    private final int partition;
    private final long transactionId;
    private static final long LENGTH = 8;
    private final static long CPU_TIME = 0;

    public CommitMessageResponse(int partition, int client, long transactionId){
        super(LENGTH, CPU_TIME);
        this.partition = partition;
        this.client = client;
        this.transactionId = transactionId;
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
}
