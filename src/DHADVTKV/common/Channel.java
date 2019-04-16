package DHADVTKV.common;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class Channel {

    static float bandwidth = (float) Configuration.getLong("bandwidth");
    static long min = Configuration.getLong("mindelay");
    static long range = Configuration.getLong("maxdelay") - min + 1;
    static float nextAvailableStep = 0;

    public Channel() {
        bandwidth = (float) Configuration.getLong("bandwidth");
        min = Configuration.getLong("mindelay");
        range = Configuration.getLong("maxdelay") - min + 1;
        nextAvailableStep = 0;
    }

    public static float getNextAvailableStep() {
        return nextAvailableStep;
    }

    public static long putMessageInChannel(long messageLength) {
        //messageLength += range==1?min:min + CommonState.r.nextLong(range);
        float nextAvailable = Math.max(Channel.getNextAvailableStep(), CommonState.getTime());
        Channel.nextAvailableStep = nextAvailable + messageLength/bandwidth;

        return Math.max((long) Channel.nextAvailableStep - CommonState.getTime(), 1);
    }

}
