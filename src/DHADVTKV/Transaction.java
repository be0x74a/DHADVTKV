package DHADVTKV;

import peersim.core.CommonState;
import peersim.util.ExtendedRandom;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private ExtendedRandom randomGenerator = CommonState.r;

    private int id = randomGenerator.nextInt();
    private int snapshot = -1;
    private List<DataObject> puts = new ArrayList<>();
    private List<DataObject> gets = new ArrayList<>();
    private int commitTimestamp = -1;
    private boolean conflicts = false;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(int snapshot) {
        this.snapshot = snapshot;
    }

    public List<DataObject> getPuts() {
        return puts;
    }

    public void setPuts(List<DataObject> puts) {
        this.puts = puts;
    }

    public List<DataObject> getGets() {
        return gets;
    }

    public void setGets(List<DataObject> gets) {
        this.gets = gets;
    }

    public int getCommitTimestamp() {
        return commitTimestamp;
    }

    public void setCommitTimestamp(int commitTimestamp) {
        this.commitTimestamp = commitTimestamp;
    }

    public boolean hasConflicts() {
        return conflicts;
    }

    public void setConflicts(boolean conflicts) {
        this.conflicts = conflicts;
    }
}
