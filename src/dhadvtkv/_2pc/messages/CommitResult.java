package dhadvtkv._2pc.messages;

import dhadvtkv.messages.Message;

public class CommitResult extends Message {

  private final boolean success;
  private final long timestamp;

  public CommitResult(int from, int to, boolean success, long timestamp) {
    super(from, to, 0);

    this.success = success;
    this.timestamp = timestamp;
  }

  @SuppressWarnings("unused")
  public boolean isSuccess() {
    return success;
  }

  @SuppressWarnings("unused")
  public long getTimestamp() {
    return timestamp;
  }
}
