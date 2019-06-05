package DHADVTKV.messages;

public class Message {

  static int LENGTH_LONG = 8;
  static int LENGTH_INT = 4;
  static int LENGTH_BOOL = 1;
  static int LENGTH_OBJ = 1024;

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
