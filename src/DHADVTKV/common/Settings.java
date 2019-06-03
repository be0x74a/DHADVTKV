package DHADVTKV.common;

public class Settings {

    public static long UNDEFINED = -1;
    public static int ROOT_ID;
    public static int BATCH_SIZE;
    public static int PID;
    public static float BANDWIDTH;
    public static long MIN;
    public static long RANGE;
    public static long CPU_DELAY;
    public static boolean ADD_CPU_DELAY;
    public static long BATCH_TIMEOUT = ;

    public Settings(int rootID, int batchSize, int pid, float bandwidth, long min, long range, long cpuDelay, boolean addCPUDelay, long batchTimeout) {
        ROOT_ID = rootID;
        BATCH_SIZE = batchSize;
        PID = pid;
        BANDWIDTH = bandwidth;
        MIN = min;
        RANGE = range;
        CPU_DELAY = cpuDelay;
        ADD_CPU_DELAY = addCPUDelay;
        BATCH_TIMEOUT = batchTimeout;
    }
}
