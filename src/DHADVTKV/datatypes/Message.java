package DHADVTKV.datatypes;

public class Message {
    private final long length;
    private final long cpuTime;
    private boolean forCPU = false;

    public Message(long length, long cpuTime) {
        this.length = length;
        this.cpuTime = cpuTime;
    }

    public long getLength() {
        return length;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public boolean isForCPU() {
        return forCPU;
    }

    public void setForCPU(boolean forCPU) {
        this.forCPU = forCPU;
    }
}
