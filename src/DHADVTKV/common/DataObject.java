package DHADVTKV.common;

public class DataObject {

    private long key;
    private long value;
    private long version;

    public DataObject(long key, long value, long version) {
        this.key = key;
        this.value = value;
        this.version = version;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
