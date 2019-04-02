package DHADVTKV;

import peersim.core.CommonState;
import peersim.util.ExtendedRandom;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private ExtendedRandom randomGenerator = CommonState.r;

    private long id = randomGenerator.nextLong();
    private long snapshot = -1;
    private List<DataObject> puts = new ArrayList<>();
    private List<DataObject> gets = new ArrayList<>();
    private long commitTimestamp = -1;
    private boolean conflicts = false;
    private List<Long> getQueue = new ArrayList<>();
    private int prepareRequestsSent = 0;
    private int prepareResponsesReceived = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(long snapshot) {
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

    public long getCommitTimestamp() {
        return commitTimestamp;
    }

    public void setCommitTimestamp(long commitTimestamp) {
        this.commitTimestamp = commitTimestamp;
    }

    public boolean hasConflicts() {
        return conflicts;
    }

    public void setConflicts(boolean conflicts) {
        this.conflicts = conflicts;
    }

    public void addToGetQueue(long key) {
        getQueue.add(key);
    }

    public void removeFromGetQueue(long key) {
        getQueue.remove(key);
    }

    public int getSizeOfGetQueue() {
        return getQueue.size();
    }

    public int getPrepareRequestsSent() {
        return prepareRequestsSent;
    }

    public void setPrepareRequestsSent(int prepareRequestsSent) {
        this.prepareRequestsSent = prepareRequestsSent;
    }

    public int getPrepareResponsesReceived() {
        return prepareResponsesReceived;
    }

    public void addToPrepareResponsesReceived() {
        prepareResponsesReceived++;
    }
}
