package dhadvtkv.proposed_tsb;

import dhadvtkv.proposed_tsb.messages.CommitTransaction;
import dhadvtkv.messages.Message;
import dhadvtkv.proposed_tsb.messages.TransactionValidation;
import dhadvtkv.proposed_tsb.messages.TransactionValidationBatch;
import dhadvtkv.messages.TransactionalGet;
import dhadvtkv.common.Configurations;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PartitionProtocol implements EDProtocol {

  private Partition partition;
  private String prefix;

  public PartitionProtocol(String prefix) {
    this.prefix = prefix;
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
        ((Message) event).setCpuReady(true);
        EDSimulator.add(Configurations.CPU_DELAY, event, node, pid);
        return;
      }
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }

    logEvent(event);

    if (event instanceof TransactionalGet) {
      TransactionalGet message = (TransactionalGet) event;
      partition.transactionalGet(message);
    } else if (event instanceof CommitTransaction) {
      CommitTransaction message = (CommitTransaction) event;
      partition.commitTransaction(message);
    } else if (event instanceof TransactionValidation) {
      TransactionValidation message = (TransactionValidation) event;
      partition.transactionValidation(message);
    } else if (event instanceof TransactionValidationBatch) {
      TransactionValidationBatch message = (TransactionValidationBatch) event;
      partition.transactionValidationBatch(message);
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
              "Received %s @ %s", obj.getClass().getSimpleName(), this.getClass().getSimpleName()));
    }
  }
}
