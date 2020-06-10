package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * System administrator account class
 */
public class Administrator extends Client{

    public Administrator(ObjectOutputStream out, ObjectInputStream in) {
        super(out, in);
    }

    @Override
    protected void handlingInput() {

    }

    @Override
    protected void handlingOutput() {

    }

}
