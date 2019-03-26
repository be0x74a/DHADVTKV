package DHADVTKV.datatypes;

import DHADVTKV.DataObject;

public class TransactionalGetMessageResponse {

    private final DataObject object;

    public TransactionalGetMessageResponse(DataObject object) {
        this.object = object;
    }

    public DataObject getObject() {
        return object;
    }
}
