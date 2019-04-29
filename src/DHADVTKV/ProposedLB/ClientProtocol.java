package DHADVTKV.ProposedLB;

import DHADVTKV.common.Channel;
import DHADVTKV.messages.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.List;

import static DHADVTKV.ProposedLB.Client.State.*;
import static DHADVTKV.ProposedLB.ProtocolMapperInit.Type.CLIENT;
import static DHADVTKV.ProposedLB.ProtocolMapperInit.nodeType;

public class ClientProtocol implements CDProtocol, EDProtocol {

    private Client client;
    private int noPartitions;
    private int nodeId;
    private String prefix;

    public ClientProtocol(String prefix) {
        this.prefix = prefix;
        this.noPartitions = Configuration.getInt(prefix + "." + "nopartitions");
    }

    @Override
    public void nextCycle(Node node, int pid) {
    }

    public void nextCycleCustom(Node node, int pid) {

        if (client == null) {
            int nodeId = Math.toIntExact(node.getID());
            this.nodeId = nodeId;
            this.client = new Client(noPartitions, nodeId);
        }

        if (nodeType.get(node.getID()) != CLIENT) {
            return;
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
                List<ValidateAndCommitTransactionRequest> validateAndCommitTransactionRequests = this.client.commit();
                for (ValidateAndCommitTransactionRequest request : validateAndCommitTransactionRequests) {
                    sendMessage(request.getPartition(), request, pid);
                }
                this.client.setState(waitingToFinish);
                break;
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
    }

    public void processEventCustom(Node node, int pid, Object event) {
        if (event instanceof TransactionalGetMessageResponse) {
            TransactionalGetMessageResponse message = (TransactionalGetMessageResponse) event;
            this.client.onTransactionalGetResponse(message);
        } else if (event instanceof ClientValidationResponse) {
            ClientValidationResponse message = (ClientValidationResponse) event;
            if (message.getTransactionId() == this.client.getTransactionId()) {
                this.client.onTransactionValidationResult(message);
            }
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }
    }

    @Override
    public Object clone() {
        return new ClientProtocol(prefix);
    }

    public void sendMessage(int partition, Message message, int pid) {
        Node dst = Network.get(partition);

        EDSimulator.add(Channel.putMessageInChannel(message.getLength()), message, dst, pid);
    }

}
