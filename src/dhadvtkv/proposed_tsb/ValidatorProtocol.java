package dhadvtkv.proposed_tsb;

import dhadvtkv.common.CPU;
import dhadvtkv.common.Configurations;
import dhadvtkv.messages.Message;
import dhadvtkv.proposed_tsb.messages.BatchValidate;
import dhadvtkv.proposed_tsb.messages.ValidateAndCommit;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class ValidatorProtocol implements EDProtocol {

  private Validator validator;
  private CPU cpu;
  private final String prefix;

  public ValidatorProtocol(String prefix) {
    this.prefix = prefix;
    this.cpu = new CPU();
  }

  void nextCycleCustom(Node node) {
    if (validator == null) {
      validator = new Validator(Math.toIntExact(node.getID()));
    }

    if ((CommonState.getTime() - validator.getBatchSentTS()) >= Configurations.BATCH_TIMEOUT) {
      validator.doSendBatch(true);
    }
  }

  @Override
  public void processEvent(Node node, int pid, Object event) {}

  void processEventCustom(Node node, int pid, Object event) {

    if (validator == null) {
      validator = new Validator(Math.toIntExact(node.getID()));
    }

    if (event instanceof Message) {
      if ((!((Message) event).isCpuReady()) && Configurations.ADD_CPU_DELAY) {
        cpu.processMessage((Message) event);
        return;
      }
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }

    logEvent(event);

    if (event instanceof ValidateAndCommit) {
      ValidateAndCommit message = (ValidateAndCommit) event;
      validator.validateAndCommit(message);
    } else if (event instanceof BatchValidate) {
      BatchValidate message = (BatchValidate) event;
      validator.batchValidate(message);
    } else {
      throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
    }
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Object clone() {
    return new ValidatorProtocol(prefix);
  }

  private void logEvent(Object obj) {
    if (Configurations.DEBUG) {
      System.err.println(
          String.format(
              "Received %s @ %s @ %d with size %d",
              obj.getClass().getSimpleName(),
              this.getClass().getSimpleName(),
              CommonState.getTime(),
              ((Message) obj).getSize()));
    }
  }
}
