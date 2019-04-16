package DHADVTKV;

import DHADVTKV.common.Channel;
import DHADVTKV.datatypes.*;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PartitionProtocol implements EDProtocol {

    private final int size;
    private final int noPartitions;
    private Partition partition;
    private int nodeId;
    private String prefix;

    public PartitionProtocol(String prefix) {
        this.prefix = prefix;
        this.noPartitions = Configuration.getInt(prefix + "." + "nopartitions");
        this.size = Configuration.getInt(prefix + "." + "keyvaluestoresize");
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
    }

    public void processEventCustom(Node node, int pid, Object event) {

        if (partition == null) {
            int nodeId = Math.toIntExact(node.getID());
            this.nodeId = nodeId;
            partition = new Partition(nodeId, this.noPartitions, size);
        }

        Message response;

        if (event instanceof Message)  {
            if (!((Message) event).isForCPU()) {
                ((Message) event).setForCPU(true);
                EDSimulator.add(((Message) event).getCpuTime(), event, node, pid);
                return;
            }
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }


        if (event instanceof TransactionalGetMessageRequest) {
            TransactionalGetMessageRequest message = (TransactionalGetMessageRequest) event;
            response = this.partition.transactionalGet(message);
            sendMessage(message.getClient(), response, pid);
        } else if (event instanceof PrepareMessageRequest) {
            PrepareMessageRequest message = (PrepareMessageRequest) event;
            response = this.partition.prepare(message);
            sendMessage(message.getClient(), response, pid);
        } else if (event instanceof CommitMessageRequest) {
            CommitMessageRequest message = (CommitMessageRequest) event;
            response = this.partition.commit(message);
            sendMessage(message.getClient(), response, pid);
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

    }

    @Override
    public Object clone() {
        return new PartitionProtocol(prefix);
    }


    public void sendMessage(int client, Message message, int pid) {

        Node dst = Network.get(client);

        EDSimulator.add(Channel.putMessageInChannel(message.getLength()), message, dst, pid);

    }
}
