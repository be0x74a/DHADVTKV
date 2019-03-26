package DHADVTKV;

import DHADVTKV.datatypes.*;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class PartitionProtocol implements EDProtocol {

    private Partition partition;

    @Override
    public void processEvent(Node node, int pid, Object event) {
        Object response = null;

        if (event instanceof TransactionalGetMessageRequest) {
            TransactionalGetMessageRequest message = (TransactionalGetMessageRequest) event;
            response = this.partition.transactionalGet(message);
        } else if (event instanceof PrepareMessageRequest) {
            PrepareMessageRequest message = (PrepareMessageRequest) event;
            response = this.partition.prepare(message);
        } else if (event instanceof CommitMessageRequest) {
            CommitMessageRequest message = (CommitMessageRequest) event;
            response = this.partition.commit(message);
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

        sendMessage(node, response);
    }

    @Override
    public Object clone() {
        PartitionProtocol pp = null;
        try {
            pp = (PartitionProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return pp;
    }


    public void sendMessage(Node node, Object message) {
        return; //TODO: Figure out how to send messages
    }

}
