package DHADVTKV.ProposedTSB;

import DHADVTKV.ProposedTSB.common.Channel;
import DHADVTKV.ProposedTSB.dataStructures.ReceiveBufferTransaction;
import DHADVTKV.ProposedTSB.dataStructures.WaitingStabilityTransaction;
import DHADVTKV.common.DataObject;
import DHADVTKV.ProposedTSB.messages.*;
import DHADVTKV.common.Configurations;

import java.util.*;
import java.util.stream.Collectors;

class Validator {

    private int validatorID;
    private long next_lsn;
    private long step;
    private int currentBatchSize;
    private Map<Long, Long> latestObjectVersions;
    private List<Transaction> leafBatch;
    private Map<Integer, List<TransactionValidation>> rootBatch;
    private Map<Integer, Long> childLSN;
    private Map<Long, ReceiveBufferTransaction> receiveBuffer;
    private Map<Long, List<WaitingStabilityTransaction>> waitingStability;

    Validator(int validatorID) {
        this.validatorID = validatorID;
        this.next_lsn = 0;
        this.step = 1;
        this.currentBatchSize = 0;
        this.latestObjectVersions = new HashMap<>();
        this.leafBatch = new ArrayList<>();
        this.rootBatch = new HashMap<>();
        this.childLSN = new HashMap<>();
        this.receiveBuffer = new HashMap<>();
        this.waitingStability = new HashMap<>();
    }

    /* Validator */
    void validateAndCommit(ValidateAndCommit request) {
        long lsn = generateLsn(request.getSnapshot(), next_lsn, step);
        List<Long> putKeys = request.getPuts().stream().map(DataObject::getKey).collect(Collectors.toList());
        boolean conflicts = checkConflicts(request.getGetKeys(), putKeys, request.getSnapshot());
        if (!conflicts) {
            updateLatestObjectVersions(putKeys, lsn);
        }

        commit(request.getTransactionID(), request.getSnapshot(), request.getGetKeys(), request.getPuts(), request.getnValidations(), request.getClient(), conflicts, lsn, true);

        this.next_lsn = lsn + step;
    }

    void batchValidate(BatchValidate batch) {
        for (Transaction transaction : batch.getTransactionsBatch()) {
            childLSN.put(transaction.getFrom(), transaction.getLsn());
            receiveTransaction(transaction);
        }

        rootValidate(Collections.min(childLSN.values()));
    }

    private void receiveTransaction(Transaction transaction) {
        if (transaction.getnValidations() == 1) {
            updateLatestObjectVersions(transaction.getPuts().
                    stream().
                    map(DataObject::getKey).
                    collect(Collectors.toList()), transaction.getLsn());
            return;
        }

        receiveBuffer.computeIfAbsent(transaction.getTransactionID(), k -> new ReceiveBufferTransaction(transaction.getTransactionID())).add(transaction);

        ReceiveBufferTransaction receiveBufferTransaction = receiveBuffer.get(transaction.getTransactionID());
        if (receiveBufferTransaction.getReceivedValidations() == transaction.getnValidations()) {
            WaitingStabilityTransaction waitingStabilityTransaction = new WaitingStabilityTransaction(
                    receiveBufferTransaction.getTransactionID(),
                    transaction.getSnapshot(),
                    receiveBufferTransaction.getGetKeys(),
                    receiveBufferTransaction.getPuts(),
                    transaction.getnValidations(),
                    transaction.getClient(),
                    receiveBufferTransaction.isConflicts()
            );

            waitingStability.computeIfAbsent(receiveBufferTransaction.getLsn(), k -> new ArrayList<>()).add(waitingStabilityTransaction);
            receiveBuffer.remove(receiveBufferTransaction.getTransactionID());
        }
    }

    private void rootValidate(long stableLsn) {
        if (waitingStability.isEmpty()) return;

        Long lsn = Collections.min(waitingStability.keySet());
        if (lsn <= stableLsn) {
            for (WaitingStabilityTransaction transaction : waitingStability.get(lsn)) {
                doRootValidate(transaction, lsn);
            }
            waitingStability.remove(lsn);
            rootValidate(stableLsn);
        }
    }

    private void doRootValidate(WaitingStabilityTransaction transaction, long lsn) {
        if (transaction.isConflicts()) {
            commit(transaction.getTransactionID(), transaction.getSnapshot(), transaction.getGetKeys(), transaction.getPuts(), transaction.getnValidations(), transaction.getClient(), transaction.isConflicts(), lsn, false);
            return;
        }

        List<Long> putKeys = transaction.getPuts().stream().map(DataObject::getKey).collect(Collectors.toList());
        boolean conflicts = checkConflicts(transaction.getGetKeys(), putKeys, transaction.getSnapshot());
        commit(transaction.getTransactionID(), transaction.getSnapshot(), transaction.getGetKeys(), transaction.getPuts(), transaction.getnValidations(), transaction.getClient(), conflicts, lsn, true);
        if (!conflicts) {
            updateLatestObjectVersions(putKeys, lsn);
        }
    }

    /* Committer */
    private void commit(long transactionID, long snapshot, List<Long> gets, List<DataObject> puts, int nValidations, int client, boolean conflicts, long lsn, boolean informClient) {
        if (validatorID != Configurations.ROOT_ID) {
            leafCommit(transactionID, snapshot, gets, puts, nValidations, client, conflicts, lsn);
        } else {
            rootCommit(transactionID, puts, client, conflicts, lsn, informClient);
        }

        sendBatch();
    }

    private void leafCommit(long transactionID, long snapshot, List<Long> gets, List<DataObject> puts, int nValidations, int client, boolean conflicts, long lsn) {
        if (nValidations == 1) {
            Channel.sendMessage(new TransactionCommitResult(validatorID, client, transactionID, conflicts, lsn));
            sendValidationResultToVNodes(transactionID, lsn, puts, conflicts);
            if (!conflicts) {
                Transaction transaction = new Transaction(validatorID, Configurations.ROOT_ID, transactionID, snapshot, gets, puts, nValidations, client, conflicts, lsn);
                leafAddToBatch(transaction);
            }
        } else {
            if (conflicts) {
                Channel.sendMessage(new TransactionCommitResult(validatorID, client, transactionID, conflicts, lsn));
            }

            Transaction transaction = new Transaction(validatorID, Configurations.ROOT_ID, transactionID, snapshot, gets, puts, nValidations, client, conflicts, lsn);
            leafAddToBatch(transaction);
        }
    }

    private void leafAddToBatch(Transaction message) {
        leafBatch.add(message);
        currentBatchSize++;
    }

    private void rootCommit(long transactionID, List<DataObject> puts, int client, boolean conflicts, long lsn, boolean informClient) {

        if (informClient) {
            Channel.sendMessage(new TransactionCommitResult(validatorID, client, transactionID, conflicts, lsn));
        }

        rootAddToBatch(transactionID, puts, conflicts, lsn);
    }

    private void rootAddToBatch(long transactionID, List<DataObject> puts, boolean conflicts, long lsn) {
        Set<Integer> nodes = puts.stream().map(DataObject::getNode).collect(Collectors.toSet());

        for (Integer node : nodes) {
             rootBatch.computeIfAbsent(node, k->new ArrayList<>()).add(new TransactionValidation(validatorID, node, transactionID, puts.
                             stream().
                             filter(dataObject -> dataObject.getNode() == node).
                             map(DataObject::getKey).collect(Collectors.toList()), conflicts, lsn));
             currentBatchSize++;
        }
    }

    private void sendValidationResultToVNodes(long transactionID, long lsn, List<DataObject> puts, boolean conflicts) {
        Set<Integer> nodes = puts.stream().map(DataObject::getNode).collect(Collectors.toSet());

        for (Integer node : nodes) {
            Channel.sendMessage(new TransactionValidation(validatorID, node, transactionID, puts.
                    stream().
                    filter(dataObject -> dataObject.getNode() == node).
                    map(DataObject::getKey).collect(Collectors.toList()), conflicts, lsn));
        }
    }

    private void sendBatch() {
        if (currentBatchSize >= Configurations.BATCH_SIZE) {
            doSendBatch();
        }
    }

    void doSendBatch() {
        if (validatorID != Configurations.ROOT_ID) {
            leafBatchSend();
        } else {
            rootBatchSend();
        }

        currentBatchSize = 0;
    }

    private void leafBatchSend() {
        if (leafBatch.size() > 0) {
            Channel.sendMessage(new BatchValidate(validatorID, Configurations.ROOT_ID, leafBatch));
            leafBatch = new ArrayList<>();
        }
    }

    private void rootBatchSend() {
        for (Integer node : rootBatch.keySet()) {
            if (rootBatch.get(node).size() > 0) {
                Channel.sendMessage(new TransactionValidationBatch(validatorID, node, rootBatch.get(node)));
            }
        }
    }

    private long generateLsn(long snapshot, long lsn, long step) {
        if (lsn > snapshot) return lsn;
        else return generateLsn(snapshot, lsn + step, step);
    }

    private boolean checkConflicts(List<Long> gets, List<Long> puts, long snapshot) {

        if (gets != null) {
            for (Long key: gets) {
                if (this.latestObjectVersions.getOrDefault(key, -1L) > snapshot) {
                    return true;
                }
            }
        }

        if (puts != null) {
            for (Long key: puts) {
                if (this.latestObjectVersions.getOrDefault(key, -1L) > snapshot) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateLatestObjectVersions(List<Long> objectKeys, long version) {
        for (Long key : objectKeys) {
            this.latestObjectVersions.put(key, version);
        }
    }
}
