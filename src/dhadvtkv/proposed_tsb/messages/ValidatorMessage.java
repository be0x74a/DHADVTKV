package dhadvtkv.proposed_tsb.messages;

import dhadvtkv.messages.Message;

public abstract class ValidatorMessage extends Message {

  ValidatorMessage(int from, int to, long size) {
    super(from, to, size);
  }
}
