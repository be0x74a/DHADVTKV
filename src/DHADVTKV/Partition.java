package DHADVTKV;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Partition {

    private KeyValueStorage kv = new KeyValueStorage();


    public DataObject transactionalGet(int key, int snapshot) {
        return onClientTransactionGetRequest(key, snapshot);
    }

    private DataObject onClientTransactionGetRequest(int key, int snapshot) {

        ArrayList<DataObject> tentativeObjectVersions = kv.getTentativeVersions(key);
        ArrayList<DataObject> committedObjectVersions = kv.getCommittedVersions(key);
        DataObject snapshotConsistentVersion = selectSnapshotConsistentVersion(snapshot, tentativeObjectVersions, committedObjectVersions);

        return snapshotConsistentVersion;
    }

    //TODO: Check if instead of Set<Partition> its ArrayList<DataObjects>!!!
    public void prepare(int transactionId, int snapshot, Set<Partition> puts, Set<Partition> gets) {
    }

    public void commit(int id, List<DataObject> gets, ArrayList<DataObject> puts, boolean hasConflicts, int commitTimestamp) {
    }




    private DataObject selectSnapshotConsistentVersion(int snapshot, ArrayList<DataObject> tentativeObjectVersions, ArrayList<DataObject> commitedObjectVersions) {

        for (DataObject object : tentativeObjectVersions) {
            if (object.getVersion() <= snapshot) {
                waitUntilVersionIsCommitted();
                return object;
            }
        }

        for (DataObject object : commitedObjectVersions) {
            if (object.getVersion() <= snapshot) {
                return object;
            }
        }

        return null;
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void waitUntilVersionIsCommitted() {

    }

}
