package DHADVTKV.datatypes;

public class CommitMessageResponse {

    private final int client;
    private final int partition;

    public CommitMessageResponse(int partition, int client){
        this.partition = partition;
        this.client = client;

        System.out.println(String.format("%d:%s:%d", partition, getClass().getSimpleName(), client));
    }

    public int getClient() {
        return client;
    }

    public int getPartition() {
        return partition;
    }
}
