package dhadvtkv.proposed_tsb;

import static dhadvtkv.proposed_tsb.ProtocolMapperInit.Type.CLIENT;
import static dhadvtkv.proposed_tsb.ProtocolMapperInit.nodeType;

import dhadvtkv.common.CPU;
import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import dhadvtkv.messages.TransactionalGetResponse;
import dhadvtkv.proposed_tsb.messages.TransactionCommitResult;
import peersim.cdsim.CDProtocol;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class ClientProtocol implements CDProtocol, EDProtocol {

  private Client client;
  private CPU cpu;
  private String prefix;
  private State state;
  private int getsSent;
  private int getsReceived;
  private long transactionStartTime;
  private long transactionsDone;
  private long avgTransactionTime;

  public ClientProtocol(String prefix) {
    this.prefix = prefix;
    cpu = new CPU();
    state = State.initialState;
    transactionsDone = 0;
    avgTransactionTime = 0;
  }

  @Override
  public void nextCycle(Node node, int pid) {}

  void nextCycleCustom(Node node, int pid) {

    if (client == null) {
      int nodeId = Math.toIntExact(node.getID());
      this.client = new Client(nodeId);
    }

    if (nodeType.get(node.getID()) != CLIENT) {
      return;
    }

    switch (state) {
      case initialState:
        transactionStartTime = CommonState.getTime();
        client.beginTransaction();
        getsSent = Configurations.NO_PARTITIONS;
        getsReceived = 0;
        for (int i = 0; i < Configurations.NO_PARTITIONS; i++) {
          client.get(i, client.getNodeID() * Configurations.NO_PARTITIONS + i);
        }
        state = State.getSent;
        break;
      case getSent:
        break;
      case waitingToFinish:
        break;
    }
  }

  @Override
  public void processEvent(Node node, int pid, Object event) {}

  void processEventCustom(Node node, int pid, Object event) {

    if (client == null) {
      this.client = new Client(Math.toIntExact(node.getID()));
    }

    if (event instanceof Message) {
      if ((!((Message) event).isCpuReady())) {
        cpu.processMessage((Message) event);
        return;
      }
    }

    logEvent(event);

    if (event instanceof TransactionalGetResponse) {
      TransactionalGetResponse message = (TransactionalGetResponse) event;
      client.onTransactionalGetResponse(message);
      getsReceived++;
      if (getsSent == getsReceived) {
        for (int i = 0; i < Configurations.NO_PARTITIONS; i++) {
          client.put(i, client.getNodeID() * Configurations.NO_PARTITIONS + i, i);
        }

        client.commit();
        state = State.waitingToFinish;
      }
    } else if (event instanceof TransactionCommitResult) {
      TransactionCommitResult message = (TransactionCommitResult) event;
      client.onTransactionCommitResult(message);
      state = State.initialState;
      if (Configurations.DEBUG) {
        if (Configurations.DEBUG) {
          System.err.println(String.format("Transaction done @ %d", CommonState.getTime()));
        }
      }
      updateAvg();
      nextCycleCustom(node, pid);
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new ClientProtocol(prefix);
  }

  // About protocol state
  private enum State {
    initialState,
    getSent,
    canCommit,
    waitingToFinish
  }

  private void logEvent(Object obj) {
    if (Configurations.DEBUG) {
      System.err.println(
          String.format(
              "Received %s @ %s @ %d with size %d",
              obj.getClass().getSimpleName(),
              this.getClass().getSimpleName(),
              CommonState.getTime(),
              ((Message) obj).getSize()));
    }
  }

  private void updateAvg() {
    long timeDiff = CommonState.getTime() - transactionStartTime;
    avgTransactionTime =
        (avgTransactionTime * transactionsDone + timeDiff) / (transactionsDone + 1);
  }

  long getAvgTransactionTime() {
    return avgTransactionTime;
  }
}
