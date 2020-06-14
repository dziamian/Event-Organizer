package com.example.eventorganizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import network_structures.BaseMessage;

public class MainActivity extends AppCompatActivity {

    private EditText loginText, passwordText;
    private Button loginBtn;
    public static TaskManager taskManager = null;

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
                        new String[]{loginText.getText().toString(), passwordText.getText().toString()},
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
