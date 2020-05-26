package com.example.eventorganizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import network_structures.BaseMessage;

public class MainActivity extends AppCompatActivity {

    private EditText loginText, passwordText;
    private Button loginBtn;
    public static ConnectionToServer connectionToServer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginText = findViewById(R.id.loginText);
        passwordText = findViewById(R.id.passwordText);
        loginBtn = findViewById(R.id.loginBtn);

        connectionToServer = new ConnectionToServer();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("Connection error")
                .setMessage("Unable to connect with the server. Check your internet connection and try again.");

        alertDialog.setNeutralButton(R.string.connection_error_button, ((dialog, which) -> {
            connectionToServer.addTask(new BaseMessage(
                    "connect",
                    null,
                    (Runnable) () -> runOnUiThread(alertDialog::show)
            ));
        }));

        new Thread(connectionToServer).start();

        connectionToServer.addTask(new BaseMessage(
                "connect",
                null,
                (Runnable) () -> runOnUiThread(alertDialog::show)
        ));

        loginBtn.setOnClickListener((v) -> {
            connectionToServer.addTask(new BaseMessage(
                    "login",
                    new String[] { loginText.getText().toString(), passwordText.getText().toString() },
                    new Runnable[] { () -> { // prawidlowe zalogowanie
                        runOnUiThread(() -> Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show());
                    }, () -> { // blad logowania
                        runOnUiThread(() -> Toast.makeText(this, "Invalid login or password!", Toast.LENGTH_SHORT).show());
                    }} // tutaj reakcja na odpowiedz serwera o danych logowania
            ));
        });








        /*ConnectionToServer.establishConnection("Connection established!",
                (msg) -> this.runOnUiThread(
                        () -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show())
        );

        loginBtn.setOnClickListener((v) -> ConnectionToServer.loginToServer(
                "No connection!",
                new LoginData(loginText.getText().toString(), passwordText.getText().toString()),

                (msg) -> this.runOnUiThread(
                        () -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()),

                () -> {
                    Intent newActivity = new Intent(MainActivity.this, HomeActivity.class);
                    MainActivity.this.startActivity(newActivity);

                    finish();
                }));*/
    }
}
