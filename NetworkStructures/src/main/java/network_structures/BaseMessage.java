package network_structures;

import java.util.Arrays;

/**
 * Class representing single message used for local communication (eg. between threads) and task scheduling.
 * Since the class does not give any guarantee as for the possibility of serialization
 * it shouldn't be used for network communication. For such purpose use {@link NetworkMessage} instead.
 * The message should be constructed with following assumptions:
 * <b>command</b> should be string recognized by receiving side;
 * <b>args</b> should be array of strings containing modifiers specific to this particular command;
 * <b>data</b> should contain any form of data needed to execute the command, such as collections.
 * Since this data is not guaranteed to be serializable, notable use case is to transmit callback objects;
 * <b>communicationIdentifier</b> should be zero if we are not expecting an answer, any non-zero long integer otherwise;
 * It must be noted that all fields except data are immutable if accessed through proper accessor methods.
 * Within server's code this structure is often used for inter-thread communication.
 */
public class BaseMessage {
    /** Command requested for execution by receiver */
    private final String command;
    /** Arguments detailing the command */
    private final String[] args; // Optional
    /** Any form of data required for execution by receiver */
    private final Object data;
    /** Unique identifier used for matching messages sent within one conversation */
    private final long communicationIdentifier;

    /**
     * Creates NetworkMessage with provided arguments; since fields are final, they cannot be change later.
     * @param command Command to execute by receiver
     * @param args Arguments detailing the command
     * @param data Any form of data required for execution of this command, including callbacks
     * @param communicationIdentifier Unique communication identifier for this message used by receiver for replying
     */
    public BaseMessage(String command, String[] args, Object data, long communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        this.command = (command != null ? command : "");
        this.args = (args != null ? args : new String[0]);
        this.data = data;
    }

    /**
     * Siplified constructor for creating conversation-independent messages.
     * @param command Command to execute by receiver
     * @param args Arguments detailing the command
     * @param data Any form of data required for execution of this command, including callbacks
     */
    public BaseMessage(String command, String[] args, Object data) {
        this(command, args, data, 0L);
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
    public Object getData() {
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
     * Converts given NetworkMessage to BaseMessage; Since {@link NetworkMessage#getData()}
     * makes only a shallow copy of <b>data</b> field, it's advised to abandon the NetworkMessage
     * reference thereafter.
     * @param networkMessage NetworkMessage to convert
     * @return BaseMessage conversion of NetworkMessage
     */
    public static BaseMessage convertToBaseMessage(NetworkMessage networkMessage) {
        return new BaseMessage(
                networkMessage.getCommand(),
                networkMessage.getArgs(),
                networkMessage.getData(),
                networkMessage.getCommunicationIdentifier()
        );
    }
}
