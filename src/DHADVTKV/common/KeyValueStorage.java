package DHADVTKV.common;

import java.util.*;

import static DHADVTKV.common.Configurations.UNDEFINED;

public class KeyValueStorage {

    private Map<Long, List<DataObject>> tentativeVersions;
    private Map<Long, List<DataObject>> committedVersions;

    public KeyValueStorage() {
        this.tentativeVersions = new HashMap<>();
        this.committedVersions = new HashMap<>();
    }

    public void storeAsTentative(List<DataObject> objects) {
        for (DataObject object : objects) {
            tentativeVersions.computeIfAbsent(object.getKey(), k -> new ArrayList<>()).add(0, object);
        }
    }

    public List<DataObject> getTentativeVersions(long key) {
        return tentativeVersions.getOrDefault(key, new ArrayList<>());
    }

    public List<DataObject> getCommittedVersions(long key) {
        return committedVersions.getOrDefault(key, new ArrayList<>());
    }

    public void deleteTentativeVersions(long transactionId, List<Long> objectKeys) {
        for (Long key : objectKeys) {
            tentativeVersions.getOrDefault(key, new ArrayList<>())
                    .removeIf(object -> object.getMetadata().getOrDefault("transactionID", UNDEFINED) == transactionId);
        }
    }

    public void commitTentativeVersions(long transactionId, List<Long> objectKeys, long lsn) {
        for (Long key: objectKeys) {
            for (DataObject object : tentativeVersions.getOrDefault(key, new ArrayList<>())) {
                if (object.getMetadata().getOrDefault("transactionID", UNDEFINED) == transactionId) {
                    object.getMetadata().put("version", lsn);
                    object.getMetadata().remove("tentativeVersion");
                    committedVersions.computeIfAbsent(key, k -> new ArrayList<>()).add(0, object);
                }
            }
        }
        deleteTentativeVersions(transactionId, objectKeys);
    }
}
