package DHADVTKV.ProposedTSB.common;

import DHADVTKV.ProposedTSB.messages.Message;
import DHADVTKV.common.Settings;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

import java.util.HashMap;
import java.util.Map;

public class Channel {

    private static float nextAvailableStep = 0;
    private static Map<String, Float> deliveryTimes = new HashMap<>();

    public static void sendMessage(Message message) {
        Node dst = Network.get(message.getTo());

        if (message.getFrom() == message.getTo()) {
            EDSimulator.add(0, message, dst, Settings.PID);
            return;
        }

        float nextAvailable = Math.max(nextAvailableStep, CommonState.getTime());
        float lastMessageDelivery = deliveryTimes.getOrDefault(message.getFrom() + "->" + message.getTo(), 0f);
        nextAvailableStep = nextAvailable + message.getSize()/Settings.BANDWIDTH;

        float min = Math.max((lastMessageDelivery - nextAvailableStep) * 1000, Settings.MIN);
        float deliveryTime =  message.getSize()/Settings.BANDWIDTH + ((Settings.RANGE==1?min:min + CommonState.r.nextLong(Settings.RANGE))/1000);

        deliveryTimes.put(message.getFrom() + "->" + message.getTo(), deliveryTime);

        EDSimulator.add((long) deliveryTime - CommonState.getTime(), message, dst, Settings.PID);
    }

}
