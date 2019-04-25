package DHADVTKV.ProposedLB;

import DHADVTKV.common.DataObject;
import DHADVTKV.common.Transaction;
import DHADVTKV.messages.ClientValidationResponse;
import DHADVTKV.messages.Message;
import DHADVTKV.messages.NonLeafValidateAndCommitRequest;
import DHADVTKV.messages.PartitionValidationResponse;

import java.util.*;

public class Validator {

    private long clock = 0;
    private Map<Long, Long> latestObjectVersions = new HashMap<>();
    private Map<Long, Transaction> runningTransactions = new HashMap<>();
    private int noPartitions;

    public Validator(int noPartitions) {
        this.noPartitions = noPartitions;
    }

    public Message leafValidate(long transactionId, long snapshot, List<DataObject> gets, List<DataObject> puts, int client, int noPartitionsTouched) {
        long commitTimestamp = generateCommitTimestamp(snapshot);
        boolean locksAcquired = acquireLocks(puts);
        boolean conflicts = true;
        clock++;

        if (locksAcquired) {
            conflicts = checkConflicts(gets, puts, snapshot);
        }

        if (noPartitionsTouched == 1) {
            return new ClientValidationResponse(conflicts, commitTimestamp, client, transactionId);
        } else {
            return new NonLeafValidateAndCommitRequest(transactionId, gets, puts, client, commitTimestamp, conflicts, noPartitionsTouched);
        }
    }

    public void leafCommit(List<DataObject> puts, long commitTimestamp, boolean conflicts) {
        if (!conflicts) {
            updateLatestObjectVersions(puts, commitTimestamp);
        }

        releaseLocks(puts);
    }


    public List<Message> nonLeafValidateAndCommit(NonLeafValidateAndCommitRequest message) {
        List<Message> responses = Collections.emptyList();

        Transaction transaction = runningTransactions.getOrDefault(message.getTransactionId(), new Transaction());
        transaction.setId(message.getTransactionId());
        transaction.setPartitionsChecked(transaction.getPartitionsChecked() + 1);
        transaction.getGets().addAll(message.getGets());
        transaction.getPuts().addAll(message.getPuts());
        transaction.setClient(message.getClient());
        transaction.setCommitTimestamp(Math.max(transaction.getCommitTimestamp(), message.getCommitTimestamp()));
        transaction.setConflicts(transaction.hasConflicts() || message.hasConflicts());

        if (transaction.getPartitionsChecked() == message.getNoPartitionsTouched()) {
            responses = commitTransaction(transaction);
            runningTransactions.remove(message.getTransactionId());
        } else {
            runningTransactions.put(message.getTransactionId(), transaction);
        }

        return responses;
    }


    private long generateCommitTimestamp(long snapshot) {
        if (clock <= snapshot) {
            clock = snapshot + 1;
        }

        return clock;
    }

    private boolean checkConflicts(List<DataObject> gets, List<DataObject> puts, long snapshot) {
        if (gets != null) {
            for (DataObject object : gets) {
                if (this.latestObjectVersions.getOrDefault(object.getKey(), -1L) > snapshot) {
                    return true;
                }
            }
        }

        if (puts != null) {
            for (DataObject object : puts) {
                if (this.latestObjectVersions.getOrDefault(object.getKey(), -1L) > snapshot) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateLatestObjectVersions(List<DataObject> objects, long version) {
        for (DataObject object : objects) {
            this.latestObjectVersions.put(object.getKey(), version);
        }
    }

    private List<Message> commitTransaction(Transaction transaction) {
        List<Message> responses = new ArrayList<>();
        Map<Integer, List<DataObject>> gets = new HashMap<>();
        Map<Integer, List<DataObject>> puts = new HashMap<>();
        Set<Integer> partitions = new HashSet<>();

        responses.add(new ClientValidationResponse(transaction.hasConflicts(), transaction.getCommitTimestamp(), transaction.getClient(), transaction.getId()));

        for (DataObject obj : transaction.getGets()) {
            int partition = partitionForKey(obj.getKey());
            partitions.add(partition);
            gets.computeIfAbsent(partition, k -> new ArrayList<>()).add(obj);

        }

        for (DataObject obj : transaction.getGets()) {
            int partition = partitionForKey(obj.getKey());
            partitions.add(partition);
            puts.computeIfAbsent(partition, k -> new ArrayList<>()).add(obj);
        }

        for (Integer partition : partitions) {
            List<DataObject> partitionGets = gets.get(partition);
            List<DataObject> partitionPuts = puts.get(partition);
            responses.add(new PartitionValidationResponse(transaction.getId(), partitionGets, partitionPuts, transaction.hasConflicts(), transaction.getCommitTimestamp(), partition));
        }

        return responses;
    }


    private int partitionForKey(long key) {
        return (int) key % this.noPartitions;
    }


    //TODO: This is obviously ONLY a placeholder!!!
    private boolean acquireLocks(List<DataObject> puts) {
        return true;
    }

    //TODO: This is obviously ONLY a placeholder!!!
    private void releaseLocks(List<DataObject> puts) {
    }

}
