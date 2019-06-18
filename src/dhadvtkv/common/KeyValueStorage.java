package dhadvtkv.common;

import static dhadvtkv.common.Configurations.UNDEFINED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueStorage {

  private Map<Long, List<DataObject>> tentativeVersions;
  private Map<Long, List<DataObject>> committedVersions;

  public KeyValueStorage(int nodeID) {
    this.tentativeVersions = new HashMap<>();
    this.committedVersions = new HashMap<>();

    for (int i = 0; i < 1000; i++) {
      List<DataObject> list = new ArrayList<>();
      HashMap<String, Long> metadata = new HashMap<>();
      metadata.put("version", 0L);
      list.add(
          new DataObject(
              nodeID,
              nodeID + i * Configurations.NO_PARTITIONS,
              nodeID + i * Configurations.NO_PARTITIONS,
              metadata));
      committedVersions.put((long) nodeID + i * Configurations.NO_PARTITIONS, list);
      tentativeVersions.put((long) nodeID + i * Configurations.NO_PARTITIONS, new ArrayList<>());
    }
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
      tentativeVersions
          .getOrDefault(key, new ArrayList<>())
          .removeIf(
              object ->
                  object.getMetadata().getOrDefault("transactionID", UNDEFINED) == transactionId);
    }
  }

  public void commitTentativeVersions(long transactionId, List<Long> objectKeys, long lsn) {

    for (Long key : objectKeys) {
      for (DataObject object : tentativeVersions.getOrDefault(key, new ArrayList<>())) {
        if (object.getMetadata().getOrDefault("transactionID", UNDEFINED) == transactionId) {
          object.getMetadata().put("version", lsn);
          object.getMetadata().remove("tentativeVersion");
          committedVersions.computeIfAbsent(key, k -> new ArrayList<>()).add(0, object);
          int size = committedVersions.get(key).size();
          if (size > Configurations.MAX_VERSIONS) {
            committedVersions.get(key).remove(size - 1);
          }
        }
      }
    }
    deleteTentativeVersions(transactionId, objectKeys);
  }
}
