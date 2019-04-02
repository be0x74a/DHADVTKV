package DHADVTKV;

import DHADVTKV.datatypes.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.List;

import static DHADVTKV.Client.State.getSent;
import static DHADVTKV.ProtocolMapperInit.Type.CLIENT;
import static DHADVTKV.ProtocolMapperInit.nodeType;

public class ClientProtocol implements CDProtocol, EDProtocol {

    private Client client;
    private int noPartitions;
    private long getKey;
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
            this.getKey = nodeId;
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
                TransactionalGetMessageRequest getRequest = this.client.get(getKey += noPartitions);
                sendMessage(node, getRequest.getPartition(), getRequest, pid);
                this.client.setState(getSent);
                break;
            case getSent:
                break;
            case getGotten:
                this.client.put(getKey, getKey);
                break;
            case canCommit:
                List<PrepareMessageRequest> prepareRequests = this.client.commit();
                for (PrepareMessageRequest request : prepareRequests) {
                    sendMessage(node, request.getPartition(), request, pid);
                }
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
        } else if (event instanceof PrepareMessageResponse) {
            PrepareMessageResponse message = (PrepareMessageResponse) event;
            List<CommitMessageRequest> requests = this.client.onPrepareResponse(message);
            for (CommitMessageRequest request : requests) {
                sendMessage(node, request.getPartition(), request, pid);
            }
        } else if (event instanceof CommitMessageResponse) {
            CommitMessageResponse message = (CommitMessageResponse) event;
            this.client.onCommitResult(message);
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }
    }

    @Override
    public Object clone() {
        return new ClientProtocol(prefix);
    }

    public void sendMessage(Node src, int partition, Object message, int pid) {
        System.out.println("Tento enviar msg: "+message.getClass().getSimpleName());

        Node dst = Network.get(partition);

        ((Transport) src.getProtocol(FastConfig.getTransport(pid)))
                .send(src, dst, message, pid);
    }

}
