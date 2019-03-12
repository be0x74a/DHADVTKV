package DHADVTKV;

public class DataObject {

    private int key;
    private int value;
    private int version;

    public DataObject(int cKey, int cValue, int cVersion) {
        key = cKey;
        value = cValue;
        version = cVersion;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
