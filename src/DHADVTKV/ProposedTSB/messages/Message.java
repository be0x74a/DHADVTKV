package DHADVTKV.ProposedLB.messages;

public abstract class Message {
    static int LENGTH_LONG = 8;
    static int LENGTH_INT = 4;
    static int LENGTH_BOOL = 1;
    static int LENGTH_OBJ = 1024;

    private final int from;
    private final int to;
    private final long size;
    private boolean cpuReady;

    public Message(int from, int to, long size) {
        this.from = from;
        this.to = to;
        this.size = size;
        this.cpuReady = false;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public long getSize() {
        return size;
    }

    public boolean isCpuReady() {
        return cpuReady;
    }

    public void setCpuReady(boolean cpuReady) {
        this.cpuReady = cpuReady;
    }
}
