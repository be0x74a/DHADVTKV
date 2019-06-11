package dhadvtkv.proposed_tsb;

import static dhadvtkv.proposed_tsb.ProtocolMapperInit.Type.CLIENT;
import static dhadvtkv.proposed_tsb.ProtocolMapperInit.nodeType;

import dhadvtkv.messages.Message;
import dhadvtkv.proposed_tsb.messages.TransactionCommitResult;
import dhadvtkv.messages.TransactionalGetResponse;
import dhadvtkv.common.Configurations;
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

    if (client == null) {
      this.client = new Client(Math.toIntExact(node.getID()));
    }

    if (event instanceof Message) {
      if ((!((Message) event).isCpuReady())) {
        ((Message) event).setCpuReady(true);
        EDSimulator.add(Configurations.CPU_DELAY, event, node, pid);
        return;
      }
    }

    logEvent(event);

    if (event instanceof TransactionalGetResponse) {
      TransactionalGetResponse message = (TransactionalGetResponse) event;
      client.onTransactionalGetResponse(message);
      getsReceived++;
    } else if (event instanceof TransactionCommitResult) {
      TransactionCommitResult message = (TransactionCommitResult) event;
      client.onTransactionCommitResult(message);
      state = State.initialState;
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
              "Received %s @ %s", obj.getClass().getSimpleName(), this.getClass().getSimpleName()));
    }
  }
}
