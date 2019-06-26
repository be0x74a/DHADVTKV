package dhadvtkv.common;

import dhadvtkv.messages.Message;
import java.util.HashMap;
import java.util.Map;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Channel {

  private static double nextAvailableStep;
  private static long totalBandwidthUsed;
  private static Map<String, Double> deliveryTimes;
  private static Map<Integer, Coordinate> nodesPositions;

  public Channel() {
    nextAvailableStep = 0;
    totalBandwidthUsed = 0;
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
    double min = Math.max(lastMessageDelivery - nextAvailableStep, 0);

    nextAvailableStep = nextAvailable + message.getSize() / Configurations.BANDWIDTH;

    double deliveryTime =
        nextAvailable
            + message.getSize() / Configurations.BANDWIDTH
            + min
            + (getDistance(message.getFrom(), message.getTo())
                * Configurations.DELAY_PER_DISTANCE);

    deliveryTimes.put(message.getFrom() + "->" + message.getTo(), deliveryTime);
    totalBandwidthUsed += message.getSize();
    message.setReceivedTime(deliveryTime);

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

  public static long getTotalBandwidthUsed() {
    return totalBandwidthUsed;
  }
}
