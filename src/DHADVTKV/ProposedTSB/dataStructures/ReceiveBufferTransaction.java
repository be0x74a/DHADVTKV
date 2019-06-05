package DHADVTKV.ProposedTSB.dataStructures;

import DHADVTKV.ProposedTSB.messages.Transaction;
import DHADVTKV.common.DataObject;
import java.util.ArrayList;
import java.util.List;

public class ReceiveBufferTransaction {

  private final long transactionID;
  private List<Long> getKeys;
  private List<DataObject> puts;
  private boolean conflicts;
  private long lsn;
  private int receivedValidations;

  public ReceiveBufferTransaction(long transactionID) {
    this.transactionID = transactionID;
    this.getKeys = new ArrayList<>();
    this.puts = new ArrayList<>();
    this.conflicts = false;
    this.lsn = -1;
    this.receivedValidations = 0;
  }

  public void add(Transaction transaction) {
    this.getKeys.addAll(transaction.getGetKeys());
    this.puts.addAll(transaction.getPuts());
    this.conflicts = this.conflicts || transaction.isConflicts();
    this.lsn = Math.max(this.lsn, transaction.getLsn());
    this.receivedValidations++;
  }

  public long getTransactionID() {
    return transactionID;
  }

  public List<Long> getGetKeys() {
    return getKeys;
  }

  public List<DataObject> getPuts() {
    return puts;
  }

  public boolean isConflicts() {
    return conflicts;
  }

  public long getLsn() {
    return lsn;
  }

  public int getReceivedValidations() {
    return receivedValidations;
  }
}
