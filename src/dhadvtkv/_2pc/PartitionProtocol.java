package dhadvtkv._2pc;

import dhadvtkv._2pc.messages.CommitTransaction;
import dhadvtkv._2pc.messages.PrepareTransaction;
import dhadvtkv.common.CPU;
import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import dhadvtkv.messages.TransactionalGet;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PartitionProtocol implements EDProtocol {

  private Partition partition;
  private CPU cpu;
  private String prefix;

  public PartitionProtocol(String prefix) {
    this.prefix = prefix;
    this.cpu = new CPU();
  }

  @Override
  public void processEvent(Node node, int pid, Object event) {}

  void processEventCustom(Node node, int pid, Object event) {

    if (partition == null) {
      int nodeID = Math.toIntExact(node.getID());
      partition = new Partition(nodeID);
    }

    if (event instanceof Message) {
      if ((!((Message) event).isCpuReady()) && Configurations.ADD_CPU_DELAY) {
        cpu.processMessage((Message) event);
        return;
      }
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }

    logEvent(event);

    if (event instanceof TransactionalGet) {
      TransactionalGet message = (TransactionalGet) event;
      partition.transactionalGet(message);
    } else if (event instanceof PrepareTransaction) {
      PrepareTransaction message = (PrepareTransaction) event;
      partition.prepareTransaction(message);
    } else if (event instanceof CommitTransaction) {
      CommitTransaction message = (CommitTransaction) event;
      partition.commit(message);
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new PartitionProtocol(prefix);
  }

  private void logEvent(Object obj) {
    if (Configurations.DEBUG) {
      System.err.println(
          String.format(
              "Received %s @ %s @ %d with size %d", obj.getClass().getSimpleName(), this.getClass().getSimpleName(),
              CommonState.getTime(), ((Message)obj).getSize()));
    }
  }

  void printTransactionsDone() {
    if (partition == null) {
      System.out.println(0);
    } else {
      partition.printTransactionsDone();
    }
  }

  long getTransactionsDone() {
    if (partition == null) {
      return 0;
    } else {
      return partition.getTransactionsDone();
    }
  }
}
