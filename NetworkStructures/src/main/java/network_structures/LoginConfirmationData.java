package network_structures;

import java.io.Serializable;

public class LoginConfirmationData implements Serializable {
    public boolean isLogged;
    public String message;

    public LoginConfirmationData(boolean isLogged, String message) {
        this.isLogged = isLogged;
        this.message = message;
    }
}
