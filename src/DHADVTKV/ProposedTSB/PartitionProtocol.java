package DHADVTKV.ProposedTSB;

import DHADVTKV.common.Channel;
import DHADVTKV.messages.*;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PartitionProtocol implements EDProtocol {

    private final int size;
    private final int noPartitions;
    private final boolean countCPU;
    private Partition partition;
    private String prefix;

    public PartitionProtocol(String prefix) {
        this.prefix = prefix;
        this.noPartitions = Configuration.getInt(prefix + "." + "nopartitions");
        this.size = Configuration.getInt(prefix + "." + "keyvaluestoresize");
        this.countCPU = Configuration.getBoolean(prefix + "." + "countcpu");
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
    }

    public void processEventCustom(Node node, int pid, Object event) {

        if (partition == null) {
            int nodeId = Math.toIntExact(node.getID());
            partition = new Partition(nodeId, this.noPartitions, size);
        }

        Message response;

        if (event instanceof Message)  {
            if ((!((Message) event).isForCPU()) && countCPU) {
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
        } else if (event instanceof ValidateAndCommitTransactionRequest) {
            ValidateAndCommitTransactionRequest message = (ValidateAndCommitTransactionRequest) event;
            response = this.partition.validateAndCommit(message);
            if (response instanceof ClientValidationResponse) {
                sendMessage(((ClientValidationResponse) response).getClient(), response, pid);
            } else if (response instanceof NonLeafValidateAndCommitRequest) {
                sendMessage(noPartitions, response, pid);
            }
        } else if (event instanceof PartitionValidationResponse) {
            PartitionValidationResponse message = (PartitionValidationResponse) event;
            this.partition.onTransactionValidationResult(message);
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }
    }

    @Override
    public Object clone() {
        return new PartitionProtocol(prefix);
    }


    private void sendMessage(int client, Message message, int pid) {

        Node dst = Network.get(client);

        EDSimulator.add(Channel.putMessageInChannel(message.getLength()), message, dst, pid);

    }
}
