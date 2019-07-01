package dhadvtkv.common;

import dhadvtkv.messages.Message;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class CPU {

  private double nextAvailableStep;

  public CPU() {
    nextAvailableStep = 0;
  }

  public void processMessage(Message message) {
    Node dst = Network.get(message.getTo());
    message.setCpuReady(true);
    double nextAvailable = Math.max(nextAvailableStep, message.getReceivedTime());
    nextAvailableStep = nextAvailable + Configurations.CPU_DELAY;
    EDSimulator.add(
        (long) nextAvailableStep - CommonState.getTime(), message, dst, Configurations.PID);
  }
}
