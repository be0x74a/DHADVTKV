package DHADVTKV.ProposedLB;

import DHADVTKV.common.Channel;
import DHADVTKV.messages.*;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.List;

public class ValidatorProtocol implements EDProtocol {

    private final int noPartitions;
    private Validator validator;
    private String prefix;

    public ValidatorProtocol(String prefix) {
        this.prefix = prefix;
        this.noPartitions = Configuration.getInt(prefix + "." + "nopartitions");
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
    }

    public void processEventCustom(Node node, int pid, Object event) {

        if (validator == null) {
            validator = new Validator(noPartitions);
        }

        if (event instanceof Message)  {
            if (!((Message) event).isForCPU()) {
                ((Message) event).setForCPU(true);
                EDSimulator.add(((Message) event).getCpuTime(), event, node, pid);
                return;
            }
        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

        if (event instanceof NonLeafValidateAndCommitRequest) {
            NonLeafValidateAndCommitRequest message = (NonLeafValidateAndCommitRequest) event;
            List<Message> responses = this.validator.nonLeafValidateAndCommit(message);

            for (Message response : responses) {
                if (response instanceof ClientValidationResponse) {
                    sendMessage(((ClientValidationResponse) response).getClient(), response, pid);
                } else if (response instanceof PartitionValidationResponse) {
                    sendMessage(((PartitionValidationResponse) response).getPartition(), response, pid);
                }
            }

        } else {
            throw new RuntimeException("Unknown message type: " + event.getClass().getSimpleName());
        }

    }

    @Override
    public Object clone() {
        return new ValidatorProtocol(prefix);
    }


    private void sendMessage(int dstId, Message message, int pid) {

        Node dst = Network.get(dstId);

        EDSimulator.add(Channel.putMessageInChannel(message.getLength()), message, dst, pid);

    }
}
