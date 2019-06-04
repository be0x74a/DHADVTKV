package DHADVTKV.ProposedTSB;

import DHADVTKV.common.Settings;
import DHADVTKV.ProposedTSB.messages.*;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class PartitionProtocol implements EDProtocol {

    private Partition partition;
    private String prefix;

    public PartitionProtocol(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
    }

    void processEventCustom(Node node, int pid, Object event) {

        if (partition == null) {
            int nodeID = Math.toIntExact(node.getID());
            partition = new Partition(nodeID);
        }

        if (event instanceof Message) {
            if ((!((Message) event).isCpuReady()) && Settings.ADD_CPU_DELAY) {
                ((Message) event).setCpuReady(true);
                EDSimulator.add(Settings.CPU_DELAY, event, node, pid);
                return;
            }
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

        if (event instanceof TransactionalGet) {
            TransactionalGet message = (TransactionalGet) event;
            partition.transactionalGet(message);
        } else if (event instanceof CommitTransaction) {
            CommitTransaction message = (CommitTransaction) event;
            partition.commitTransaction(message);
        } else if (event instanceof TransactionValidation) {
            TransactionValidation message = (TransactionValidation) event;
            partition.transactionValidation(message);
        } else if (event instanceof TransactionValidationBatch) {
            TransactionValidationBatch message = (TransactionValidationBatch) event;
            partition.transactionValidationBatch(message);
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }
    }

    @Override
    public Object clone() {
        return new PartitionProtocol(prefix);
    }
}
