package dhadvtkv.common;

public class Configurations {

  public static final long UNDEFINED = -1;
  public static int ROOT_ID;
  public static int BATCH_SIZE;
  public static long CPU_DELAY;
  public static boolean ADD_CPU_DELAY;
  public static long BATCH_TIMEOUT;
  static int PID;
  static double BANDWIDTH;
  static long MIN;
  static long RANGE;
  static long DELAY_PER_DISTANCE;

  public Configurations(
      int rootID,
      int batchSize,
      int pid,
      double bandwidth,
      long min,
      long range,
      long cpuDelay,
      boolean addCPUDelay,
      long batchTimeout,
      long delayPerDistance) {
    ROOT_ID = rootID;
    BATCH_SIZE = batchSize;
    PID = pid;
    BANDWIDTH = bandwidth;
    MIN = min;
    RANGE = range;
    CPU_DELAY = cpuDelay;
    ADD_CPU_DELAY = addCPUDelay;
    BATCH_TIMEOUT = batchTimeout;
    DELAY_PER_DISTANCE = delayPerDistance;
  }
}
