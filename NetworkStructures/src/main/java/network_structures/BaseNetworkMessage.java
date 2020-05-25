package network_structures;

import java.io.Serializable;
import java.util.Arrays;

public class BaseNetworkMessage implements Serializable {
    private final String command;
    private final String[] args; //Optional
    private final Serializable data;

    public BaseNetworkMessage(String command, String[] args, Serializable data) {
        this.command = command;
        this.args = args;
        this.data = data;
    }

    public BaseNetworkMessage(String command, String[] args) {
        this(command, args, null);
    }

    public BaseNetworkMessage(String command, Serializable data) {
        this(command, null, data);
    }

    public BaseNetworkMessage(String command) {
        this(command, null, null);
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args != null ? Arrays.copyOf(args, args.length) : null;
    }

    public Serializable getData() {
        return data;
    }
}
