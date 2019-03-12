package DHADVTKV;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Partition {

    public DataObject transactionalGet(int key, int version) {
        return new DataObject(0,0,0);
    }

    public void prepare(int id, int snapshot, Set<Partition> putPartitions, Set<Partition> getPartitions) {

    }

    public void commit(int id, List<DataObject> gets, ArrayList<DataObject> puts, boolean hasConflicts, int commitTimestamp) {
    }

}
