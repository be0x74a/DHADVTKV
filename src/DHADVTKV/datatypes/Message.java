package DHADVTKV.datatypes;

public class Message {
    private final long length;

    public Message(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }
}
