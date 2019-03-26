package DHADVTKV;

import DHADVTKV.datatypes.CommitMessageRequest;
import DHADVTKV.datatypes.CommitMessageResponse;
import DHADVTKV.datatypes.PrepareMessageResponse;
import DHADVTKV.datatypes.TransactionalGetMessageResponse;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.List;

public class ClientProtocol implements CDProtocol, EDProtocol {

    private Client client;


    @Override
    public void nextCycle(Node node, int protocolID) {
        //TODO: begin(), get(), put(), commit()
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (event instanceof TransactionalGetMessageResponse) {
            TransactionalGetMessageResponse message = (TransactionalGetMessageResponse) event;
            this.client.onTransactionalGetResponse(message);
        } else if (event instanceof PrepareMessageResponse) {
            PrepareMessageResponse message = (PrepareMessageResponse) event;
            List<CommitMessageRequest> requests = this.client.onPrepareResponse(message);
            for (CommitMessageRequest request : requests) {
                sendMessage(node, request); //TODO: It's not that node
            }
        } else if (event instanceof CommitMessageResponse) {
            CommitMessageResponse message = (CommitMessageResponse) event;
            this.client.onCommitResult(message);
        }
    }

    @Override
    public Object clone() {
        ClientProtocol cp = null;
        try {
            cp = (ClientProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return cp;
    }

    public void sendMessage(Node node, Object message) {
        return; //TODO: Figure out how to send messages
    }

}
