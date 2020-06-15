package com.example.eventorganizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import network_structures.BaseMessage;

/**
 * Main activity of application including login form.
 */
public class MainActivity extends AppCompatActivity {

    /** Object that contains field to fill login information */
    private EditText loginText;
    /** Object that contains field to fill password information */
    private EditText passwordText;
    /** Object that contains button which performs attempt to login into the server */
    private Button loginBtn;
    /** Contains reference to {@link TaskManager} instance */
    public static TaskManager taskManager = null;

    /**
     * Initializes setup of activity. Creates all views related with login form (<b>loginText</b>, <b>passwordText</b>
     * and <b>loginBtn</b>) and pop-up message if connection with server was not established.
     * Initializes {@link TaskManager}. After log in, it creates new intent of {@link HomeActivity} and closes current Activity.
     * @param savedInstanceState Contains the data which was supplied in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     *                           (currently not in use)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginText = findViewById(R.id.loginText);
        passwordText = findViewById(R.id.passwordText);
        loginBtn = findViewById(R.id.loginBtn);

        taskManager = new TaskManager();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("Connection error")
                .setMessage("Unable to connect with the server. Check your internet connection and try again.")
                .setCancelable(false);

        alertDialog.setNeutralButton(R.string.connection_error_button, ((dialog, which) -> {
            taskManager.addIncomingMessage(new BaseMessage(
                    "connect",
                    null,
                    (Runnable) () -> runOnUiThread(alertDialog::show)
            ));
        }));

        new Thread(taskManager).start();

        taskManager.addIncomingMessage(new BaseMessage(
                "connect",
                null,
                (Runnable) () -> runOnUiThread(alertDialog::show)
        ));

        loginBtn.setOnClickListener((v) -> {
            boolean error = false;
            if (loginText.length() == 0) {
                loginText.setError("Login field cannot be empty!");
                error = true;
            }
            if (passwordText.length() == 0) {
                passwordText.setError("Password field cannot be empty!");
                error = true;
            }
            if (!error) {
                taskManager.addIncomingMessage(new BaseMessage(
                        "login",
                        new String[]{ loginText.getText().toString(), passwordText.getText().toString()},
                        new Runnable[]{() -> { // prawidlowe zalogowanie
                            runOnUiThread(() -> Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show());

                            Intent newActivity = new Intent(MainActivity.this, HomeActivity.class);
                            MainActivity.this.startActivity(newActivity);

                            finish();
                        }, () -> { // blad logowania
                            runOnUiThread(() -> Toast.makeText(this, "Invalid login or password!", Toast.LENGTH_SHORT).show());
                        }},// tutaj reakcja na odpowiedz serwera o danych logowania
                        TaskManager.nextCommunicationStream()
                ));
            }
        });
    }
}
