package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * System moderator account class
 */
public class Moderator extends Client {

    public Moderator(ObjectOutputStream out, ObjectInputStream in) {
        super(out, in);
    }

    @Override
    protected void handlingInput() {

    }

    @Override
    protected void handlingOutput() {

    }

}
