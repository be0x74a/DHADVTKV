package DHADVTKV.common;

import DHADVTKV.ProposedTSB.messages.Message;
import java.util.HashMap;
import java.util.Map;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Channel {

  private static double nextAvailableStep;
  private static Map<String, Double> deliveryTimes;
  private static Map<Integer, Coordinate> nodesPositions;

  public Channel() {
    nextAvailableStep = 0;
    deliveryTimes = new HashMap<>();
    nodesPositions = new HashMap<>();
  }

  public static void sendMessage(Message message) {
    Node dst = Network.get(message.getTo());

    if (message.getFrom() == message.getTo()) {
      EDSimulator.add(0, message, dst, Configurations.PID);
      return;
    }

    double nextAvailable = Math.max(nextAvailableStep, CommonState.getTime());
    double lastMessageDelivery =
        deliveryTimes.getOrDefault(message.getFrom() + "->" + message.getTo(), 0d);
    nextAvailableStep = nextAvailable + message.getSize() / Configurations.BANDWIDTH;

    double min = Math.max((lastMessageDelivery - nextAvailableStep) * 1000, Configurations.MIN);
    double deliveryTime =
        message.getSize() / Configurations.BANDWIDTH
            + ((Configurations.RANGE == 1
                    ? min
                    : min + CommonState.r.nextLong(Configurations.RANGE))
                / 1000)
            + (getDistance(message.getFrom(), message.getTo())
                * Configurations.DELAY_PER_DISTANCE
                / 1000);

    deliveryTimes.put(message.getFrom() + "->" + message.getTo(), deliveryTime);

    EDSimulator.add((long) deliveryTime - CommonState.getTime(), message, dst, Configurations.PID);
  }

  public static void setPositions(Map<Integer, Coordinate> nodesPositions) {
    Channel.nodesPositions = nodesPositions;
  }

  private static Double getDistance(int node0, int node1) {
    Coordinate pos0 = nodesPositions.get(node0);
    Coordinate pos1 = nodesPositions.get(node1);
    return pos0.distance(pos1);
  }
}
