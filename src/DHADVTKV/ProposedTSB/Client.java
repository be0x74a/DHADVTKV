package DHADVTKV.ProposedTSB;

import DHADVTKV.common.DataObject;
import DHADVTKV.messages.*;
import peersim.core.CommonState;

import java.util.*;
import java.util.stream.Collectors;

public class Client {

    private static final long UNDEFINED = -1;

    private long clock;
    private long transactionID;
    private long snapshot;
    private int nodeId;
    private List<DataObject> gets;
    private List<DataObject> puts;
    private boolean inTransaction;

    public Client (int nodeId) {
        this.clock = 0;
        this.transactionID = UNDEFINED;
        this.snapshot = UNDEFINED;
        this.nodeId = nodeId;
        this.gets = new ArrayList<>();
        this.puts = new ArrayList<>();
        this.inTransaction = false;
    }

    public boolean beginTransaction() {
        if (this.inTransaction) return false;

        this.inTransaction = true;
        this.transactionID = CommonState.r.nextLong();
        return true;
    }

    public TransactionalGetMessageRequest get(int node, long key) {
        long version = this.snapshot;
        if (version == UNDEFINED) {
            version = this.clock;
        }

        return new TransactionalGetMessageRequest(key, version, nodeId, node);
    }

    public DataObject onTransactionalGetResponse(TransactionalGetMessageResponse response) {
        long version = response.getObject().getMetadata().getOrDefault("version", UNDEFINED);
        if (version != UNDEFINED) {
            this.snapshot = Math.max(version, clock);
        }

        gets.add(response.getObject());

        if (version > snapshot) {
            cleanState();
            return null;
        }

        return response.getObject();
    }

    public void put(int partition, long key, long value) {

        Map <String, Long> metadata = new HashMap<>();
        metadata.put("transactionID", this.transactionID);
        if (this.snapshot == UNDEFINED) {
            metadata.put("tentativeVersion", this.clock + 1);
        } else {
            metadata.put("tentativeVersion", this.snapshot + 1);
        }
        this.puts.add(new DataObject(partition, key, value, metadata));
    }

    public List<ValidateAndCommitTransactionRequest> commit() {

        Map<Integer, List<DataObject>> nodeGets = new HashMap<>();
        Map<Integer, List<DataObject>> nodePuts = new HashMap<>();

        for (DataObject dataObject : gets) {
            nodeGets.computeIfAbsent(dataObject.getNode(), k -> new ArrayList<>()).add(dataObject);
        }

        for (DataObject dataObject : puts) {
            nodePuts.computeIfAbsent(dataObject.getNode(), k -> new ArrayList<>()).add(dataObject);
        }

        Set<Integer> nodes = new HashSet<>();
        nodes.addAll(nodeGets.keySet());
        nodes.addAll(nodePuts.keySet());

        return nodes.stream()
                .map(node ->
                        new ValidateAndCommitTransactionRequest(
                                this.transactionID,
                                this.snapshot,
                                nodeGets.getOrDefault(node, new ArrayList<>()).stream().map(DataObject::getKey).collect(Collectors.toList()),
                                nodePuts.getOrDefault(node, new ArrayList<>()),
                                nodes.size(),
                                this.nodeId,
                                node)
                ).collect(Collectors.toList());
    }

    public boolean onClientValidationResponse(ClientValidationResponse response) {
        this.clock = response.getCommitTimestamp();
        cleanState();
        return !response.hasConflicts();
    }

    private void cleanState() {
        this.inTransaction = false;
        this.transactionID = UNDEFINED;
        this.snapshot = UNDEFINED;
        this.gets = new ArrayList<>();
        this.puts = new ArrayList<>();
    }
}
