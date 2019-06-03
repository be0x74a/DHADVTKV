package DHADVTKV.common;

import java.util.Map;

public class DataObject {

    private int node;
    private long key;
    private long value;
    private Map<String, Long> metadata;

    public DataObject(int node, long key, long value, Map<String, Long> metadata) {
        this.node = node;
        this.key = key;
        this.value = value;
        this.metadata = metadata;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Map<String, Long> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Long> metadata) {
        this.metadata = metadata;
    }
}
