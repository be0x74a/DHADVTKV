package dhadvtkv._2pc;

import static dhadvtkv._2pc.ProtocolMapperInit.Type.CLIENT;
import static dhadvtkv._2pc.ProtocolMapperInit.nodeType;

import dhadvtkv._2pc.messages.CommitResult;
import dhadvtkv._2pc.messages.PrepareCommitResult;
import dhadvtkv._2pc.messages.PrepareResult;
import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import dhadvtkv.messages.TransactionalGetResponse;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class ClientProtocol implements CDProtocol, EDProtocol {

  private Client client;
  private String prefix;
  private State state;
  private int getsSent;
  private int getsReceived;

  public ClientProtocol(String prefix) {
    this.prefix = prefix;
    state = State.initialState;
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
        client.beginTransaction();
        getsSent = Configurations.NO_PARTITIONS;
        getsReceived = 0;
        for (int i = 0; i < Configurations.NO_PARTITIONS; i++) {
          client.get(i, client.getNodeID() * Configurations.NO_PARTITIONS + i);
        }
        state = State.getSent;
        break;
      case getSent:
        if (getsSent == getsReceived) {
          for (int i = 0; i < Configurations.NO_PARTITIONS; i++) {
            client.put(i, client.getNodeID() * Configurations.NO_PARTITIONS + i, i);
          }
          state = State.canCommit;
        }
        break;
      case canCommit:
        client.commit();
        state = State.waitingToFinish;
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
        ((Message) event).setCpuReady(true);
        EDSimulator.add(Configurations.CPU_DELAY, event, node, pid);
        return;
      }
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }

    if (event instanceof TransactionalGetResponse) {
      System.out.println("Received message: TransactionalGetResponse");
      TransactionalGetResponse message = (TransactionalGetResponse) event;
      client.onTransactionalGetResponse(message);
      getsReceived++;
    } else if (event instanceof PrepareResult) {
      System.out.println("Received message: PrepareResult");
      PrepareResult message = (PrepareResult) event;
      client.onPrepareResult(message);
    } else if (event instanceof PrepareCommitResult) {
      System.out.println("Received message: PrepareCommitResult");
      PrepareCommitResult message = (PrepareCommitResult) event;
      client.onPrepareCommitResult(message);
    } else if (event instanceof CommitResult) {
      System.out.println("Received message: CommitResult");
      CommitResult message = (CommitResult) event;
      if (message.getTransactionID() == client.getTransactionID()) {
        if (client.onCommitResult(message)) {
          state = State.initialState;
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
}
