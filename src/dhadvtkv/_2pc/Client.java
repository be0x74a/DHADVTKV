package dhadvtkv._2pc;

import static dhadvtkv.common.Configurations.UNDEFINED;

import dhadvtkv._2pc.messages.CommitResult;
import dhadvtkv._2pc.messages.CommitTransaction;
import dhadvtkv._2pc.messages.PrepareCommitResult;
import dhadvtkv._2pc.messages.PrepareCommitTransaction;
import dhadvtkv._2pc.messages.PrepareResult;
import dhadvtkv._2pc.messages.PrepareTransaction;
import dhadvtkv.common.Channel;
import dhadvtkv.common.DataObject;
import dhadvtkv.messages.TransactionalGet;
import dhadvtkv.messages.TransactionalGetResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import peersim.core.CommonState;

class Client {

  private long clock;
  private long transactionID;
  private long snapshot;
  private int nodeID;
  private List<DataObject> gets;
  private List<DataObject> puts;
  private boolean inTransaction;
  // Committer fsm
  private Set<Integer> nodes;
  private Map<Integer, List<DataObject>> nodeGets;
  private Map<Integer, List<DataObject>> nodePuts;
  private PrepareResult prepareResult;
  private int receivedPrepareResults;
  private int receivedCommitResults;

  Client(int nodeId) {
    this.clock = 0;
    this.transactionID = UNDEFINED;
    this.snapshot = UNDEFINED;
    this.nodeID = nodeId;
    this.gets = new ArrayList<>();
    this.puts = new ArrayList<>();
    this.inTransaction = false;
    this.nodes = new HashSet<>();
    this.nodeGets = new HashMap<>();
    this.nodePuts = new HashMap<>();
    this.prepareResult = null;
    this.receivedPrepareResults = 0;
    this.receivedCommitResults = 0;
  }

  void beginTransaction() {
    if (this.inTransaction)
      throw new RuntimeException("Trying to start transaction when it's already in course");
    cleanState();
    this.inTransaction = true;
    this.transactionID = CommonState.r.nextLong();
  }

  void get(int node, long key) {
    long version = this.snapshot;
    if (version == UNDEFINED) {
      version = this.clock;
    }
    Channel.sendMessage(new TransactionalGet(nodeID, node, key, version));
  }

  DataObject onTransactionalGetResponse(TransactionalGetResponse response) {
    maybeSetSnapshot(response);
    gets.add(response.getObject());
    if (checkGetConflict(response)) {
      cleanState();
      return null;
    }
    return response.getObject();
  }

  void put(int partition, long key, long value) {
    DataObject object;
    if (snapshot == UNDEFINED) {
      object = createObject(partition, key, value, transactionID, clock + 1);
    } else {
      object = createObject(partition, key, value, transactionID, snapshot + 1);
    }
    puts.add(object);
  }

  void commit() {
    nodeGets = new HashMap<>();
    nodePuts = new HashMap<>();

    for (DataObject dataObject : gets) {
      nodeGets.computeIfAbsent(dataObject.getNode(), k -> new ArrayList<>()).add(dataObject);
    }
    for (DataObject dataObject : puts) {
      nodePuts.computeIfAbsent(dataObject.getNode(), k -> new ArrayList<>()).add(dataObject);
    }

    nodes = new HashSet<>();
    nodes.addAll(nodeGets.keySet());
    nodes.addAll(nodePuts.keySet());

    if (nodes.size() == 1) {
      prepareCommit();
    } else {
      prepare();
    }
  }

  void onPrepareResult(PrepareResult result) {

    if (prepareResult == null) {
      prepareResult = result;
    } else {
      prepareResult.setAborted(prepareResult.isAborted() || result.isAborted());
      prepareResult.setTimestamp(Math.max(prepareResult.getTimestamp(), result.getTimestamp()));
    }

    if (++receivedPrepareResults == nodes.size()) {
      for (Integer node : nodes) {
        Channel.sendMessage(
            new CommitTransaction(
                nodeID,
                node,
                transactionID,
                nodeGets.getOrDefault(node, new ArrayList<>()).stream()
                    .map(DataObject::getKey)
                    .collect(Collectors.toList()),
                nodePuts.getOrDefault(node, new ArrayList<>()).stream()
                    .map(DataObject::getKey)
                    .collect(Collectors.toList()),
                prepareResult.isAborted(),
                prepareResult.getTimestamp()));
      }
    }
  }

  boolean onPrepareCommitResult(PrepareCommitResult result) {
    cleanState();
    clock = result.getTimestamp();
    return result.isSuccess();
  }

  boolean onCommitResult(CommitResult result) {
    if (++receivedCommitResults == nodes.size()) {
      clock = prepareResult.getTimestamp();
      cleanState();
      return true;
    }
    return false;
  }

  private void prepare() {
    for (Integer node : nodes) {
      Channel.sendMessage(
          new PrepareTransaction(
              nodeID,
              node,
              snapshot == UNDEFINED ? clock : snapshot,
              nodeGets.getOrDefault(node, new ArrayList<>()).stream()
                  .map(DataObject::getKey)
                  .collect(Collectors.toList()),
              nodePuts.getOrDefault(node, new ArrayList<>())));
    }
  }

  private void prepareCommit() {
    int node = nodes.iterator().next();
    Channel.sendMessage(
        new PrepareCommitTransaction(
            nodeID,
            node,
            snapshot == UNDEFINED ? clock : snapshot,
            nodeGets.getOrDefault(node, new ArrayList<>()).stream()
                .map(DataObject::getKey)
                .collect(Collectors.toList()),
            nodePuts.getOrDefault(node, new ArrayList<>())));
  }

  private void maybeSetSnapshot(TransactionalGetResponse response) {
    long objectVersion = response.getObject().getMetadata().getOrDefault("version", UNDEFINED);
    if (snapshot == UNDEFINED && objectVersion != UNDEFINED) {
      snapshot = Math.max(objectVersion, clock);
    }
  }

  private boolean checkGetConflict(TransactionalGetResponse response) {
    return response.getObject().getMetadata().getOrDefault("version", UNDEFINED) > snapshot;
  }

  private DataObject createObject(
      int node, long key, long value, long transactionID, long tentativeVersion) {
    Map<String, Long> metadata = new HashMap<>();
    metadata.put("transactionID", transactionID);
    metadata.put("tentativeVersion", tentativeVersion);
    return new DataObject(node, key, value, metadata);
  }

  private void cleanState() {
    this.transactionID = UNDEFINED;
    this.snapshot = UNDEFINED;
    this.gets = new ArrayList<>();
    this.puts = new ArrayList<>();
    this.inTransaction = false;
    this.nodes = new HashSet<>();
    this.nodeGets = new HashMap<>();
    this.nodePuts = new HashMap<>();
    this.prepareResult = null;
    this.receivedPrepareResults = 0;
    this.receivedCommitResults = 0;
  }

  long getTransactionID() {
    return transactionID;
  }

  int getNodeID() {
    return nodeID;
  }
}
