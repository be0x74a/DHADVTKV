package DHADVTKV.common;

import java.util.*;
import java.util.stream.Collectors;

public class KeyValueStorage {

    private Map<Long, List<TransactionalDataObject>> tentativeVersions = new HashMap<> ();
    private Map<Long, List<TransactionalDataObject>> committedVersions = new HashMap<> ();

    public KeyValueStorage(long id, int noKeyValueStores, int size) {
        for (int i = 0; i < size; i++) {
            List<TransactionalDataObject> list = new ArrayList<>();
            list.add(new TransactionalDataObject(new DataObject(id+i*noKeyValueStores, id+i*noKeyValueStores, 0L), 0L));
            committedVersions.put(id+i*noKeyValueStores, list);
            tentativeVersions.put(id+i*noKeyValueStores, new ArrayList<>());
        }
    }

    public void storeAsTentative(long transactionId, List<DataObject> objects) {

        for (DataObject object : objects) {
            TransactionalDataObject transactionalDataObject = new TransactionalDataObject(object, transactionId);
            tentativeVersions.computeIfAbsent(object.getKey(), k -> new ArrayList<>()).add(0, transactionalDataObject);
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

    public void deleteTentativeVersions(long transactionId, List<Long> objectKeys) {
        for (Long key : objectKeys) {
            tentativeVersions.get(key)
                    .removeIf(objectVersion -> objectVersion.getTransactionId() == transactionId);
        }
    }

    public void commitTentativeVersions(long transactionId, List<Long> objectKeys, long lsn) {
        for (Long key: objectKeys) {
            for (TransactionalDataObject transactionalDataObject : tentativeVersions.get(key)) {
                if (transactionalDataObject.getTransactionId() == transactionId) {
                    transactionalDataObject.getObject().setVersion(lsn);
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
        private long transactionLockedId = -1;

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

        public boolean lock(long transactionId) {
            if (this.transactionLockedId == -1 || this.transactionLockedId == transactionId) {
                this.transactionLockedId = transactionId;
            }

            return false;
        }

        public void releaseLock(long transactionId) {
            if (this.transactionLockedId == transactionId) {
                this.transactionLockedId = -1;
            }
        }
    }

}
