package DHADVTKV.TPC;

import DHADVTKV.common.Channel;
import DHADVTKV.messages.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.List;

import static DHADVTKV.TPC.Client.State.getSent;
import static DHADVTKV.TPC.Client.State.waitingToFinish;
import static DHADVTKV.TPC.ProtocolMapperInit.Type.CLIENT;
import static DHADVTKV.TPC.ProtocolMapperInit.nodeType;

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
                List<PrepareMessageRequest> prepareRequests = this.client.commit();
                for (PrepareMessageRequest request : prepareRequests) {
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

        Message response;

        if (event instanceof Message)  {
            if ((!((Message) event).isForCPU())) {
                ((Message) event).setForCPU(true);
                EDSimulator.add(100, event, node, pid);
                return;
            }
        }

        if (event instanceof TransactionalGetMessageResponse) {
            TransactionalGetMessageResponse message = (TransactionalGetMessageResponse) event;
            this.client.onTransactionalGetResponse(message);
            } else if (event instanceof PrepareMessageResponse) {
            PrepareMessageResponse message = (PrepareMessageResponse) event;
            List<CommitMessageRequest> requests = this.client.onPrepareResponse(message);
            for (CommitMessageRequest request : requests) {
                sendMessage(request.getPartition(), request, pid);
            }
        } else if (event instanceof CommitMessageResponse) {
            CommitMessageResponse message = (CommitMessageResponse) event;
            if (message.getTransactionId() == this.client.getTransactionId()) {
                this.client.onCommitResult(message);
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
