package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * System presenter account class
 */
public class Presenter extends Client {

    public Presenter(ObjectOutputStream out, ObjectInputStream in) {
        super(out, in);
    }

    @Override
    protected void handlingInput() {

    }

    @Override
    protected void handlingOutput() {

    }
}
