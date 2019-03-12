package DHADVTKV;

import java.util.ArrayList;

public class KeyValueStorage {

    public void storeAsTentative(int transactionId, ArrayList<DataObject> puts) {

    }

    public ArrayList<DataObject> getTentativeVersions(int key) {

        return null;
    }

    public ArrayList<DataObject> getCommittedVersions(int key) {

        return null;
    }

    public void deleteTentativeVersions(int transactionId, ArrayList<DataObject> puts) {

    }

    public void commitTentativeVersions(int transactionId, ArrayList<DataObject> puts, int commitTimestamp) {

    }



}
