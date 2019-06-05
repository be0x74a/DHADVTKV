package DHADVTKV.ProposedTSB;

import DHADVTKV.common.Configurations;
import DHADVTKV.ProposedTSB.messages.*;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class ValidatorProtocol implements EDProtocol {

    private Validator validator;
    private final String prefix;

    public ValidatorProtocol(String prefix) {
        this.prefix = prefix;
    }

    void nextCycleCustom(Node node) {
        if (validator == null) {
            validator = new Validator(Math.toIntExact(node.getID()));
        }

        if (CommonState.getTime() % Configurations.BATCH_TIMEOUT == 0) {
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
            if ((!((Message) event).isCpuReady()) && Configurations.ADD_CPU_DELAY) {
                ((Message) event).setCpuReady(true);
                EDSimulator.add(Configurations.CPU_DELAY, event, node, pid);
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return new ValidatorProtocol(prefix);
    }
}
