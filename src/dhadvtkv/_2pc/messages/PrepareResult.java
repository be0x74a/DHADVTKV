package dhadvtkv._2pc.messages;

import dhadvtkv.messages.Message;

public class PrepareResult extends Message {

  private boolean aborted;
  private long timestamp;

  public PrepareResult(int from, int to, boolean aborted, long timestamp) {
    super(from, to, LENGTH_BOOL + LENGTH_LONG);

    this.aborted = aborted;
    this.timestamp = timestamp;
  }

  public boolean isAborted() {
    return aborted;
  }

  public void setAborted(boolean aborted) {
    this.aborted = aborted;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
