package DHADVTKV.ProposedTSB;

import DHADVTKV.ProposedTSB.messages.Message;
import DHADVTKV.ProposedTSB.messages.TransactionCommitResult;
import DHADVTKV.ProposedTSB.messages.TransactionalGetResponse;
import DHADVTKV.common.Configurations;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class ClientProtocol implements CDProtocol, EDProtocol {

  private Client client;
  private String prefix;

  public ClientProtocol(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public void nextCycle(Node node, int pid) {}

  void nextCycleCustom(Node node, int pid) {

    if (client == null) {
      this.client = new Client(Math.toIntExact(node.getID()));
    }

    switch (this.client.getState()) {
      case initialState:
        this.client.begin();
        break;
      case transactionCreated:
        this.client.setGetsSend(noPartitions);
        for (int i = 0; i < noPartitions; i++) {
          TransactionalGetMessageRequest getRequest = this.client.get(nodeId * noPartitions + i);
          sendMessage(getRequest.getPartition(), getRequest, pid);
        }
        this.client.setState(getSent);
        break;
      case getSent:
        break;
      case canCommit:
        List<ValidateAndCommitTransactionRequest> validateAndCommitTransactionRequests =
            this.client.commit();
        for (ValidateAndCommitTransactionRequest request : validateAndCommitTransactionRequests) {
          sendMessage(request.getPartition(), request, pid);
        }
        this.client.setState(waitingToFinish);
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

    if (event instanceof TransactionalGetResponse) {
      TransactionalGetResponse message = (TransactionalGetResponse) event;
      client.onTransactionalGetResponse(message);
    } else if (event instanceof TransactionCommitResult) {
      TransactionCommitResult message = (TransactionCommitResult) event;
      client.onTransactionCommitResult(message);
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new ClientProtocol(prefix);
  }
}
