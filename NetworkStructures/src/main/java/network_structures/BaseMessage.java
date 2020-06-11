package network_structures;

import java.io.Serializable;
import java.util.Arrays;

public class BaseMessage {
    private String command;
    private String[] args; // Optional
    private Object data;
    private long communicationIdentifier;

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

    protected BaseMessage() {
        this.command = null;
        this.args = null;
        this.data = null;
        this.communicationIdentifier = 0;
    }

    protected void setCommand(String command) {
        this.command = command;
    }

    protected void setArgs(String[] args) {
        this.args = args;
    }

    protected void setData(Object data) {
        this.data = data;
    }

    protected void setCommunicationIdentifier(long communicationIdentifier) {
        this.communicationIdentifier = communicationIdentifier;
    }
}
