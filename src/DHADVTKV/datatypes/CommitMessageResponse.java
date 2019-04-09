package DHADVTKV.datatypes;

public class CommitMessageResponse {

    private final int client;
    private final int partition;
    private final long transactionId;

    public CommitMessageResponse(int partition, int client, long transactionId){
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
