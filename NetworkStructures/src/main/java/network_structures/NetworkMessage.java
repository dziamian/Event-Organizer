package network_structures;

import java.io.Serializable;

public class NetworkMessage extends BaseMessage implements Serializable {
    public NetworkMessage(String command, String[] args, Serializable data, long communicationIdentifier) {
        super(command, args, data, communicationIdentifier);
    }

    public NetworkMessage(String command, String[] args, long communicationIdentifier) {
        super(command, args, null, communicationIdentifier);
    }

    public NetworkMessage(String command, String[] args) {
        super(command, args, null);
    }

    @Override
    public Serializable getData() {
        return (Serializable)super.getData();
    }
}
