package DHADVTKV.common;

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
    private int prepareRequestsSent = 0;
    private int prepareResponsesReceived = 0;
    private int getsSent = 0;
    private int getsReceived = 0;
    private int client;
    private int partitionsChecked = 0;

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

    public List<DataObject> getGets() {
        return gets;
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

    public int getGetsSent() {
        return getsSent;
    }

    public void setGetsSent(int getsSent) {
        this.getsSent = getsSent;
    }

    public int getGetsReceived() {
        return getsReceived;
    }

    public void setGetsReceived(int getsReceived) {
        this.getsReceived = getsReceived;
    }

    public void addGetsReceived() {
        this.getsReceived++;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public int getPartitionsChecked() {
        return partitionsChecked;
    }

    public void setPartitionsChecked(int partitionsChecked) {
        this.partitionsChecked = partitionsChecked;
    }
}
