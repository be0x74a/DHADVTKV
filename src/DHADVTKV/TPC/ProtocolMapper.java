package DHADVTKV.TPC;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import static DHADVTKV.TPC.ProtocolMapperInit.*;


public class ProtocolMapper implements CDProtocol, EDProtocol {

    private final int partitionPid;
    private final int clientPid;

    public ProtocolMapper(String prefix) {
        this.clientPid = Configuration.getPid("client");
        this.partitionPid = Configuration.getPid("partition");
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        Type type = nodeType.get(node.getID());
        // Only clients should start actions
        if (type == Type.CLIENT) {
            ClientProtocol client = (ClientProtocol) node.getProtocol(clientPid);
            client.nextCycleCustom(node, protocolID);
        } else if (type == Type.PARTITION) {
            return;
        } else {
            throw new RuntimeException("Unknown node type.");
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        Type type = nodeType.get(node.getID());
        if (type == Type.CLIENT) {
            ClientProtocol client = (ClientProtocol) node.getProtocol(clientPid);
            client.processEventCustom(node, pid, event);
        } else if (type == Type.PARTITION) {
            PartitionProtocol partition = (PartitionProtocol) node.getProtocol(partitionPid);
            partition.processEventCustom(node, pid, event);
        } else {
            throw new RuntimeException("Unknown node type.");
        }
    }

    @Override
    public Object clone() {
        return new ProtocolMapper(null);
    }
}
