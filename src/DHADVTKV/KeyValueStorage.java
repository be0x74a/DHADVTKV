package DHADVTKV;

import java.util.*;
import java.util.stream.Collectors;

public class KeyValueStorage {

    private Map<Long, List<TransactionalDataObject>> tentativeVersions = new HashMap<> ();
    private Map<Long, List<TransactionalDataObject>> committedVersions = new HashMap<> ();


    public void storeAsTentative(long transactionId, List<DataObject> objects) {

        for (DataObject object : objects) {
            TransactionalDataObject transactionalDataObject = new TransactionalDataObject(object, transactionId);
            List<TransactionalDataObject> res = tentativeVersions.putIfAbsent(object.getKey(), new ArrayList<>(Arrays.asList(transactionalDataObject)));
            if (res != null) {
                res.add(0, transactionalDataObject);
            }
        }
    }

    public List<DataObject> getTentativeVersions(long key) {

        return tentativeVersions
                .get(key)
                .stream()
                .map(TransactionalDataObject::getObject)
                .collect(Collectors.toList());
    }

    public List<DataObject> getCommittedVersions(long key) {

        return committedVersions
                .get(key)
                .stream()
                .map(TransactionalDataObject::getObject)
                .collect(Collectors.toList());
    }

    public void deleteTentativeVersions(long transactionId, List<DataObject> objects) {
        for (DataObject object : objects) {
            tentativeVersions.get(object.getKey())
                    .removeIf(objectVersion -> objectVersion.getTransactionId() == transactionId);
        }
    }

    public void commitTentativeVersions(long transactionId, List<DataObject> objects, long commitTimestamp) {
        for (DataObject object : objects) {
            for (TransactionalDataObject transactionalDataObject : tentativeVersions.get(object.getKey())) {
                if (transactionalDataObject.getTransactionId() == transactionId) {
                    transactionalDataObject.getObject().setVersion(commitTimestamp);
                    List<TransactionalDataObject> res = committedVersions.putIfAbsent(transactionalDataObject.getObject().getKey(), new ArrayList<>(Arrays.asList(transactionalDataObject)));
                    if (res != null) {
                        res.add(0, transactionalDataObject);
                    }
                }
            }
        }

        deleteTentativeVersions(transactionId, objects);
    }

    private class TransactionalDataObject {

        private DataObject object;
        private long transactionId;

        public TransactionalDataObject(DataObject object, long transactionId) {
            this.object = object;
            this.transactionId = transactionId;

        }

        public DataObject getObject() {
            return object;
        }

        public long getTransactionId() {
            return transactionId;
        }
    }

}
