package com.example.root.telnettest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText ipAdress;
    EditText portNumber;
    EditText encoderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ipAdress = (EditText) findViewById(R.id.ip_adress);
        portNumber = (EditText) findViewById(R.id.port_number);
        encoderName = (EditText) findViewById(R.id.encoder_name);

        SharedPreferences prefs = getSharedPreferences("addresses", MODE_PRIVATE);
        ipAdress.setText(prefs.getString("ip","10.7.10."));
        portNumber.setText(prefs.getString("port", "Port"));
        encoderName.setText(prefs.getString("encoder", "Encoder #"));

        Button saveButton = (Button) findViewById(R.id.save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("addresses", MODE_PRIVATE).edit();
                String ip = ipAdress.getText().toString();
                String port = portNumber.getText().toString();
                String encoder = encoderName.getText().toString();
                editor.putString("ip", ip);
                editor.putString("port", port);
                editor.putString("encoder", encoder);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
