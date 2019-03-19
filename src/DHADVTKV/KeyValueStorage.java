package DHADVTKV;

import java.util.*;
import java.util.stream.Collectors;

public class KeyValueStorage {

    private Map<Integer, List<TransactionalDataObject>> tentativeVersions = new HashMap<> ();
    private Map<Integer, List<TransactionalDataObject>> committedVersions = new HashMap<> ();


    public void storeAsTentative(int transactionId, List<DataObject> objects) {

        for (DataObject object : objects) {
            TransactionalDataObject transactionalDataObject = new TransactionalDataObject(object, transactionId);
            List<TransactionalDataObject> res = tentativeVersions.putIfAbsent(object.getKey(), new ArrayList<>(Arrays.asList(transactionalDataObject)));
            if (res != null) {
                res.add(0, transactionalDataObject);
            }
        }
    }

    public List<DataObject> getTentativeVersions(int key) {

        return tentativeVersions
                .get(key)
                .stream()
                .map(TransactionalDataObject::getObject)
                .collect(Collectors.toList());
    }

    public List<DataObject> getCommittedVersions(int key) {

        return committedVersions
                .get(key)
                .stream()
                .map(TransactionalDataObject::getObject)
                .collect(Collectors.toList());
    }

    public void deleteTentativeVersions(int transactionId, List<DataObject> objects) {
        for (DataObject object : objects) {
            tentativeVersions.get(object.getKey())
                    .removeIf(objectVersion -> objectVersion.getTransactionId() == transactionId);
        }
    }

    public void commitTentativeVersions(int transactionId, List<DataObject> objects, int commitTimestamp) {
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
        private int transactionId;

        public TransactionalDataObject(DataObject object, int transactionId) {
            this.object = object;
            this.transactionId = transactionId;

        }

        public DataObject getObject() {
            return object;
        }

        public int getTransactionId() {
            return transactionId;
        }
    }

}
