package DHADVTKV.ProposedTSB.messages;

import DHADVTKV.common.DataObject;

public class TransactionalGetResponse extends Message {

    private final DataObject object;

    public TransactionalGetResponse(int from, int to, DataObject object) {
        super(from, to, 0);

        this.object = object;
    }

    public DataObject getObject() {
        return object;
    }
}
