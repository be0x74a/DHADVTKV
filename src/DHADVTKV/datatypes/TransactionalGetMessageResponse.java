package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

public class TransactionalGetMessageResponse {

    private final DataObject object;
    private final int partition;
    private final int client;

    public TransactionalGetMessageResponse(DataObject object, int partition, int client) {
        this.object = object;
        this.partition = partition;
        this.client = client;

        System.out.println(String.format("%d:%s:%d", partition, getClass().getSimpleName(), client));

    }

    public DataObject getObject() {
        return object;
    }

    public int getClient() {
        return client;
    }

    public int getPartition() {
        return partition;
    }
}
