package network_structures;

import java.io.Serializable;
import java.util.Arrays;

public class BaseMessage implements Serializable {

    //private final int communicationStream;
    private final String command;
    private final String[] args; //Optional
    private final Object data;

    public BaseMessage(String command, String[] args, Object data) {
        //super(0, command, args, data);
        this.command = command;
        this.args = args;
        this.data = data;
    }

    /*public BaseNetworkMessage(int communicationStream, String command, String[] args, Serializable data) {
        this.communicationStream = communicationStream;
        this.command = command;
        this.args = args;
        this.data = data;
    }*/

    public BaseMessage(String command, String[] args) {
        this(command, args, null);
    }

    public BaseMessage(String command, Object data) {
        this(command, null, data);
    }

    public BaseMessage(String command) {
        this(command, null, null);
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args != null ? Arrays.copyOf(args, args.length) : null;
    }

    public Object getData() {
        return data;
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
}
