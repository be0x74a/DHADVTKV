package DHADVTKV.ProposedTSB;

import DHADVTKV.common.Settings;
import DHADVTKV.ProposedTSB.messages.*;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class ValidatorProtocol implements EDProtocol {

    private final String prefix;
    private Validator validator;

    public ValidatorProtocol(String prefix) {
        this.prefix = prefix;
    }

    void nextCycleCustom(Node node, int pid) {
        if (validator == null) {
            validator = new Validator(Math.toIntExact(node.getID()));
        }

        if (CommonState.getTime() % Settings.BATCH_TIMEOUT == 0) {
            validator.doSendBatch();
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {}

    void processEventCustom(Node node, int pid, Object event) {

        if (validator == null) {
            validator = new Validator(Math.toIntExact(node.getID()));
        }

        if (event instanceof Message)  {
            if ((!((Message) event).isCpuReady()) && Settings.ADD_CPU_DELAY) {
                ((Message) event).setCpuReady(true);
                EDSimulator.add(Settings.CPU_DELAY, event, node, pid);
                return;
            }
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

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

    @Override
    public Object clone() {
        return new ValidatorProtocol(prefix);
    }
}