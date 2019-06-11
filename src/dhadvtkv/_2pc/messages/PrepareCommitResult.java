package dhadvtkv._2pc.messages;

import dhadvtkv.messages.Message;

public class PrepareCommitResult extends Message {

  private final boolean success;
  private final long timestamp;

  public PrepareCommitResult(int from, int to, boolean success, long timestamp) {
    super(from, to, LENGTH_BOOL + LENGTH_LONG);

    this.success = success;
    this.timestamp = timestamp;
  }

  public boolean isSuccess() {
    return success;
  }

  @SuppressWarnings("unused")
  public long getTimestamp() {
    return timestamp;
  }
}
