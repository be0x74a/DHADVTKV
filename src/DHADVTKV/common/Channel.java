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
        //System.out.println("Common time: " + CommonState.getTime());

        float messageTime = messageLength/bandwidth;
        //System.out.println("Message time: " + messageTime);

        float nextAvailable = Math.max(Channel.nextAvailableStep, CommonState.getTime());
        //System.out.println("Next available: " + Math.max(Channel.nextAvailableStep, CommonState.getTime()));

        Channel.nextAvailableStep = nextAvailable + Math.max(messageTime , 1);

        //System.out.println("Time sent: " + Math.max((long) Channel.nextAvailableStep - CommonState.getTime(), 1));

        return Math.max((long) Channel.nextAvailableStep - CommonState.getTime(), 1);
    }

}
