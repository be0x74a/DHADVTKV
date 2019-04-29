package DHADVTKV.TPC;

import DHADVTKV.common.Channel;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.HashMap;
import java.util.Map;

public class ProtocolMapperInit implements Control {

    public static Map<Long, Type> nodeType = new HashMap<>();

    private int numberPartitions;

    public enum Type {
        CLIENT,
        PARTITION
    }

    public ProtocolMapperInit(String prefix) {
        numberPartitions =  Configuration.getInt(prefix + "." + "nopartitions");
        new Channel();
    }

    @Override
    public boolean execute() {
        for (int i = 0; i < numberPartitions; i++) {
            Node node = Network.get(i);
            nodeType.put(node.getID(), Type.PARTITION);
        }
        for (int i = numberPartitions; i < Network.size(); i++) {
            Node node = Network.get(i);
            nodeType.put(node.getID(), Type.CLIENT);
        }

        return false;
    }
}
