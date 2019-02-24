package DHADVTKV;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Assigns types to nodes
 * @author bravogestoso
 * @author diogofsvilela
 *
 */
public class InitTypeProtocol implements Control {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_TYPE_PROT = "type_protocol";
    private static final String PAR_NDATANODES = "ndatanodes";
    private static final String PAR_NCLIENTS = "nclients";


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier, obtained from config property {#PAR_PROT}. */
    private final int pid;
    private final int n_datanodes;
    private final int n_clients;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     *
     * @param prefix
     *            the configuration prefix for this class.
     */
    public InitTypeProtocol(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_TYPE_PROT);
        n_datanodes = Configuration.getInt(prefix + "." + PAR_NDATANODES);
        n_clients = Configuration.getInt(prefix + "." + PAR_NCLIENTS);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------


    public boolean execute() {
        for (int i = 0; i < n_datanodes; i++) {
            Node node = Network.get(i);
            TypeProtocol prot = (TypeProtocol) node.getProtocol(pid);
            prot.setType(TypeProtocol.Type.DATACENTER);
        }
        for (int i = n_datanodes; i < n_datanodes + n_clients; i++) {
            Node node = Network.get(i);
            TypeProtocol prot = (TypeProtocol) node.getProtocol(pid);
            prot.setType(TypeProtocol.Type.CLIENT);
        }
        for (int i = n_datanodes + n_clients; i < Network.size(); i++) {
            Node node = Network.get(i);
            TypeProtocol prot = (TypeProtocol) node.getProtocol(pid);
            prot.setType(TypeProtocol.Type.COORDINATOR);
        }
        return false;
    }
}
