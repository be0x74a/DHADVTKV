package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

public class TransactionalGetMessageResponse extends Message {

    private final DataObject object;
    private final int partition;
    private final int client;
    private final static long LENGTH = 4;

    public TransactionalGetMessageResponse(DataObject object, int partition, int client) {
        super(LENGTH);
        this.object = object;
        this.partition = partition;
        this.client = client;
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
