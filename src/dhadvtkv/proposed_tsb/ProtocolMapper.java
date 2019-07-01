package dhadvtkv.proposed_tsb;

import static dhadvtkv.proposed_tsb.ProtocolMapperInit.Type;
import static dhadvtkv.proposed_tsb.ProtocolMapperInit.nodeType;

import dhadvtkv.common.Channel;
import dhadvtkv.common.Configurations;
import dhadvtkv.proposed_tsb.messages.ValidatorMessage;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class ProtocolMapper implements CDProtocol, EDProtocol {

  private final String prefix;
  private final int partitionPid;
  private final int clientPid;
  private final int validatorPid;

  public ProtocolMapper(String prefix) {
    this.prefix = prefix;
    this.clientPid = Configuration.getPid(prefix + "." + "client");
    this.partitionPid = Configuration.getPid(prefix + "." + "partition");
    this.validatorPid = Configuration.getPid(prefix + "." + "validator");
  }

  @Override
  public void nextCycle(Node node, int protocolID) {

    Type type = nodeType.get(node.getID());
    if (type == Type.CLIENT) {
      ClientProtocol client = (ClientProtocol) node.getProtocol(clientPid);
      client.nextCycleCustom(node, protocolID);
    } else if (type == Type.PARTITION) {
      ValidatorProtocol validator = (ValidatorProtocol) node.getProtocol(validatorPid);
      validator.nextCycleCustom(node);
    } else if (type == Type.VALIDATOR) {
      ValidatorProtocol validator = (ValidatorProtocol) node.getProtocol(validatorPid);
      validator.nextCycleCustom(node);
    } else {
      throw new RuntimeException("Unknown node type.");
    }

    if (CommonState.getTime() == CommonState.getEndTime() - 1 && !Configurations.getPrinted()) {
      System.out.print(
          ((PartitionProtocol) Network.get(1).getProtocol(partitionPid)).getTransactionsDone()
              + ", "
              + Channel.getTotalBandwidthUsed()
                  / (Configurations.BANDWIDTH * CommonState.getEndTime())
              + ", "
              + getAvgTransactionTime()
              + ", "
              + getAvgTransactionsDone());
      Configurations.setPrinted(true);
    }
  }

  @Override
  public void processEvent(Node node, int pid, Object event) {
    Type type = nodeType.get(node.getID());
    if (type == Type.CLIENT) {
      ClientProtocol client = (ClientProtocol) node.getProtocol(clientPid);
      client.processEventCustom(node, pid, event);
    } else if (type == Type.PARTITION) {
      if (event instanceof ValidatorMessage) {
        ValidatorProtocol validator = (ValidatorProtocol) node.getProtocol(validatorPid);
        validator.processEventCustom(node, pid, event);
      } else {
        PartitionProtocol partition = (PartitionProtocol) node.getProtocol(partitionPid);
        partition.processEventCustom(node, pid, event);
      }
    } else if (type == Type.VALIDATOR) {
      ValidatorProtocol validator = (ValidatorProtocol) node.getProtocol(validatorPid);
      validator.processEventCustom(node, pid, event);
    } else {
      throw new RuntimeException("Unknown node type.");
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new ProtocolMapper(prefix);
  }

  private double getAvgTransactionTime() {
    double avg = 0;
    for (int i = 0; i < Network.size() - 9; i++) {
      long tt = ((ClientProtocol) Network.get(i+9).getProtocol(clientPid)).getAvgTransactionTime();
      avg = (avg * i + tt)/(i + 1);
    }

    return avg;
  }

  private double getAvgTransactionsDone() {
    double avg = 0;
    for (int i = 0; i < Network.size() - 9; i++) {
      long tt = ((ClientProtocol) Network.get(i+9).getProtocol(clientPid)).getTransactionsDone();
      avg = (avg * i + tt)/(i + 1);
    }

    return avg;
  }
}
