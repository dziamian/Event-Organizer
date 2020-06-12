package network_structures;

import java.util.Arrays;

public class BaseMessage {
    private final String command;
    private final String[] args; // Optional
    private final Object data;
    private final long communicationIdentifier;

    public BaseMessage(String command, String[] args, Object data, long communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
        this.command = (command != null ? command : "");
        this.args = (args != null ? args : new String[0]);
        this.data = data;
    }

    public BaseMessage(String command, String[] args, Object data) {
        this(command, args, data, 0L);
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public Object getData() {
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
     * Converts given NetworkMessage to BaseMessage; Since {@link NetworkMessage#getData()}
     * makes only a shallow copy of <b>data</b> field, it's advised to abandon the NetworkMessage
     * reference thereafter.
     * @param networkMessage NetworkMessage to convert
     * @return BaseMessage conversion NetworkMessage
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
