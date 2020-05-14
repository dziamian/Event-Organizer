package network_structures;

import java.io.Serializable;

public class LoginData implements Serializable {
    public String login;
    public String password;

    public LoginData(String login, String password) {
        this.login = login;
        this.password = password;
    }
}