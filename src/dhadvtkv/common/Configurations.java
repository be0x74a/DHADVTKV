package dhadvtkv.common;

public class Configurations {

  public static final long UNDEFINED = -1;
  public static final boolean DEBUG = false;
  static final int MAX_VERSIONS = 188;
  public static int NO_PARTITIONS;
  public static int ROOT_ID;
  public static int BATCH_SIZE;
  public static long CPU_DELAY;
  public static boolean ADD_CPU_DELAY;
  public static long BATCH_TIMEOUT;
  public static long HEADER_SIZE;
  static int PID;
  static double BANDWIDTH;
  static long DELAY_PER_DISTANCE;

  private static boolean printed;

  public Configurations(
      int rootID,
      int batchSize,
      int pid,
      double bandwidth,
      long cpuDelay,
      boolean addCPUDelay,
      long batchTimeout,
      long delayPerDistance,
      long headerSize) {
    ROOT_ID = NO_PARTITIONS = rootID;
    BATCH_SIZE = batchSize;
    PID = pid;
    BANDWIDTH = bandwidth;
    CPU_DELAY = cpuDelay;
    ADD_CPU_DELAY = addCPUDelay;
    BATCH_TIMEOUT = batchTimeout;
    DELAY_PER_DISTANCE = delayPerDistance;
    HEADER_SIZE = headerSize;
    printed = false;
  }

  public static boolean getPrinted() {
    return printed;
  }

  public static void setPrinted(boolean printed) {
    Configurations.printed = printed;
  }
}
