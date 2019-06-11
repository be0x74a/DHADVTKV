package dhadvtkv.messages;

import dhadvtkv.common.DataObject;

public class TransactionalGetResponse extends Message {

  private final DataObject object;

  public TransactionalGetResponse(int from, int to, DataObject object) {
    super(from, to, LENGTH_OBJ);

    this.object = object;
  }

  public DataObject getObject() {
    return object;
  }
}
