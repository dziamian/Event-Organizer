package network_structures;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Class representing single message sent through the network between client and server.
 * Unlike {@link BaseMessage}, this class is guaranteed to contain serializable objects,
 * and thus is suitable for use in network communication.
 * The message should be constructed with following assumptions:
 * <b>command</b> should be string recognized by receiving side;
 * <b>args</b> should be array of strings containing modifiers specific to this particular command;
 * <b>data</b> should contain any form of data needed to execute the command, such as collections;
 * <b>communicationIdentifier</b> should be zero if we are not expecting an answer, any non-zero long integer otherwise;
 * It should be noted that all fields except data are immutable if accessed through proper accessor methods.
 */
public class NetworkMessage implements Serializable {
    /** Command requested for execution by receiver */
    private final String command;
    /** Arguments detailing the command */
    private final String[] args; // Optional
    /** Any form of data required for execution by receiver */
    private final Serializable data;
    /** Unique identifier used for matching messages sent within one conversation */
    private final long communicationIdentifier;

    /**
     * Creates NetworkMessage with provided arguments; since fields are final, they cannot be change later.
     * @param command Command to execute by receiver
     * @param args Arguments detailing the command
     * @param data Any form of data required for execution of this command
     * @param communicationIdentifier Unique communication identifier for this message used by receiver for replying
     */
    public NetworkMessage(String command, String[] args, Serializable data, long communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        this.command = (command != null ? command : "");
        this.args = (args != null ? args : new String[0]);
        this.data = data;
    }

    /**
     * Getter for command
     * @return Command of this message
     */
    public String getCommand() {
        return command;
    }

    /**
     * Getter for args, providing immutability bo copying internal array.
     * @return Copy of message's internal arguments array
     */
    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    /**
     * Getter for data, returning data field by shallow copy.
     * Caution is advised since data object itself is not guaranteed to be immutable.
     * @return Serializable containing data assigned to this message
     */
    public Serializable getData() {
        return data;
    }

    /**
     * Getter for communication identifier.
     * @return Communication identifier for this message
     */
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
     * @return NetworkMessage conversion of given BaseMessage
     * @throws NotSerializableException if <b>data</b> does not implement Serializable
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
