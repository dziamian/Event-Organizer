package com.example.eventorganizer;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import network_structures.LoginData;

public class MainActivity extends AppCompatActivity {

    EditText loginText, passwordText;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginText = findViewById(R.id.loginText);
        passwordText = findViewById(R.id.passwordText);
        loginBtn = findViewById(R.id.loginBtn);

        ClientConnection.establishConnection("Connection established!",
                (msg) -> this.runOnUiThread(
                        () -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show())
        );

        loginBtn.setOnClickListener((v) -> ClientConnection.loginToServer(
                "No connection!",
                new LoginData(loginText.getText().toString(), passwordText.getText().toString()),

                (msg) -> this.runOnUiThread(
                        () -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()),

                () -> {
                    ClientConnection.handlingServer();

                    Intent newActivity = new Intent(MainActivity.this, HomeActivity.class);
                    MainActivity.this.startActivity(newActivity);

                    finish();
                }));

        /*loginBtn.setOnClickListener((v) -> {
            Intent newActivity = new Intent(MainActivity.this, HomeActivity.class);
            MainActivity.this.startActivity(newActivity);

            finish();
        });*/
    }
}
