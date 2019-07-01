package dhadvtkv._2pc;

import static dhadvtkv._2pc.ProtocolMapperInit.Type.CLIENT;
import static dhadvtkv._2pc.ProtocolMapperInit.nodeType;

import dhadvtkv._2pc.messages.CommitResult;
import dhadvtkv._2pc.messages.PrepareCommitResult;
import dhadvtkv._2pc.messages.PrepareResult;
import dhadvtkv.common.CPU;
import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import dhadvtkv.messages.TransactionalGetResponse;
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

    if (event instanceof Message) {
      if ((!((Message) event).isCpuReady()) && Configurations.ADD_CPU_DELAY) {
        cpu.processMessage((Message) event);
        return;
      }
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
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
    } else if (event instanceof PrepareResult) {
      PrepareResult message = (PrepareResult) event;
      client.onPrepareResult(message);
    } else if (event instanceof PrepareCommitResult) {
      PrepareCommitResult message = (PrepareCommitResult) event;
      client.onPrepareCommitResult(message);
    } else if (event instanceof CommitResult) {
      CommitResult message = (CommitResult) event;
      if (message.getTransactionID() == client.getTransactionID()) {
        if (client.onCommitResult(message)) {
          state = State.initialState;
          if (Configurations.DEBUG) {
            if (Configurations.DEBUG) {
              System.err.println(String.format("Transaction done @ %d", CommonState.getTime()));
            }
          }
          updateAvg();
          nextCycleCustom(node, pid);
        }
      }
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
