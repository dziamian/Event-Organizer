package network_structures;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;

public class NetworkMessage implements Serializable {
    private final String command;
    private final String[] args; // Optional
    private final Serializable data;
    private final long communicationIdentifier;

    public NetworkMessage(String command, String[] args, Serializable data, long communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        this.command = (command != null ? command : "");
        this.args = (args != null ? args : new String[0]);
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public Serializable getData() {
        return data;
    }

    public long getCommunicationIdentifier() {
        return communicationIdentifier;
    }

    @Override
    public String toString() {
        String retVal = "[message]: {\n\tcommand: " + command + "\n\targs: ";
        if (args != null) {
            for (String arg : args) {
                retVal += arg + ", ";
            }
        }
        if (data != null) {
            retVal += "\n\t" + data.toString() + "\n}";
        }
        return retVal;
    }

    /**
     * Converts given BaseMessage to NetworkMessage; Since {@link BaseMessage#getData()}
     * makes only a shallow copy of <b>data</b> field, it's advised to abandon the BaseMessage
     * reference thereafter.
     * The conversion cannot be performed if <b>data</b> is non-serializable.
     * @param baseMessage BaseMessage to convert
     * @return NetworkMessage conversion of BaseMessage
     * @throws NotSerializableException If <b>data</b> does not implement Serializable
     */
    public NetworkMessage convertToNetworkMessage(BaseMessage baseMessage) throws NotSerializableException {
        if (!(baseMessage.getData() instanceof Serializable))
            throw new NotSerializableException(baseMessage.getData().getClass().getName());
        return new NetworkMessage(
            baseMessage.getCommand(),
            baseMessage.getArgs(),
            (Serializable) baseMessage.getData(),
            baseMessage.getCommunicationIdentifier()
        );
    }
}
