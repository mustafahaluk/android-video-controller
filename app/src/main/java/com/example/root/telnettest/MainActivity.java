package com.example.root.telnettest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    TextView encoderStatus;
    TextView statusScreen;
    ProgressBar pb;
    List<MyTask> connectTasks;
    Button connectButton;
    ImageButton startRecordButton;
    ImageButton pauseRecordButton;
    ImageButton splitRecordButton;
    ImageButton stopRecordButton;

    String encoderName;

    //create new task each time.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initial declarations
        connectTasks = new ArrayList<>();
        encoderStatus = (TextView) findViewById(R.id.encoder_status);
        statusScreen = (TextView) findViewById(R.id.status_screen);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        connectButton = (Button) findViewById(R.id.btn_connect);
        startRecordButton = (ImageButton) findViewById(R.id.btn_start_rec);
        pauseRecordButton = (ImageButton) findViewById(R.id.btn_pause_rec);
        splitRecordButton = (ImageButton) findViewById(R.id.btn_split_rec);
        stopRecordButton = (ImageButton) findViewById(R.id.btn_stop_rec);

        SharedPreferences prefs = getSharedPreferences("addresses", MODE_PRIVATE);

        encoderName = prefs.getString("encoder", "Encoder #");
        encoderStatus.setText(getString(R.string.status, encoderName));

        pb.setVisibility(View.INVISIBLE);
        startRecordButton.setVisibility(View.INVISIBLE);
        pauseRecordButton.setVisibility(View.INVISIBLE);
        splitRecordButton.setVisibility(View.INVISIBLE);
        stopRecordButton.setVisibility(View.INVISIBLE);

        /**
         * Every time the app starts, check if settings exists, if not
         * direct user to settings activity. If there is a setting check
         * if wifi connection is present and if true display the status
         * of encoder, if not toast a warning message.
         */
        if (prefs.getString("port", "no text").equals("no text")) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else {
            if (isConnectedToWifi()) {
                MyTask task = new MyTask("connect");
                task.delegate = this;
                task.execute("encstatus \"" + encoderName + "\"");

            } else {
                Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToWifi()) {
                    MyTask task = new MyTask("connect");
                    task.delegate = MainActivity.this;
                    task.execute("encstatus \"" + encoderName + "\"");

                } else {
                    Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToWifi()) {
                    MyTask task = new MyTask("start");
                    task.delegate = MainActivity.this;
                    task.execute("encstatus \"" + encoderName + "\"");
                } else {
                    Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        pauseRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToWifi()) {
                    MyTask task = new MyTask("pause");
                    task.delegate = MainActivity.this;
                    task.execute("encstatus \"" + encoderName + "\"");
                } else {
                    Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        splitRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToWifi()) {
                    MyTask task = new MyTask("split");
                    task.delegate = MainActivity.this;
                    task.execute("encstatus \"" + encoderName + "\"");
                } else {
                    Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        stopRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToWifi()) {
                    MyTask task = new MyTask("stop");
                    task.delegate = MainActivity.this;
                    task.execute("encstatus \"" + encoderName + "\"");
                } else {
                    Toast.makeText(MainActivity.this, "Please check your Wifi Connection!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_web:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.metus.com/"));
                startActivity(i);
                return true;
            case R.id.action_about:
                Intent j=new Intent(MainActivity.this,AboutActivity.class);
                startActivity(j);
                return true;
            case R.id.action_close:
                finish();
                System.exit(0);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish(String output) {
        statusScreen.setText(output);
    }

    private class MyTask extends AsyncTask<String, String, String> {
        private String response;
        public AsyncResponse delegate = null;
        public String option;
        SharedPreferences prefs = getSharedPreferences("addresses", MODE_PRIVATE);
        String ipAdress = prefs.getString("ip", "Default");
        String portNumber = prefs.getString("port", "Default");

        MyTask(String _option)

        {
            this.option = _option;
        }

        @Override
        protected void onPreExecute() {
            if (connectTasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            connectTasks.add(this);
        }

        @Override
        protected String doInBackground(String... strings) {
            response = "";

            String command = "";
            for (int i = 0; i < strings.length; i++) {
                command += strings[i] + " ";
            }
            try {
                Socket socket;
                socket = new Socket(ipAdress, Integer.parseInt(portNumber));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out.println(command + "\r\n");

                response += in.readLine();
                response += in.readLine() + "\n";

            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "Could not connect to the device.";
            } catch (IOException e) {
                e.printStackTrace();
                return "Connection Failed!";
            }
            return response;

        }

        @Override
        protected void onPostExecute(String s) {
            if (s.contains("OK")) {
                // if connect button is pressed
                switch (option) {
                    case "connect":
                        startRecordButton.setVisibility(View.VISIBLE);
                        pauseRecordButton.setVisibility(View.VISIBLE);
                        splitRecordButton.setVisibility(View.VISIBLE);
                        stopRecordButton.setVisibility(View.VISIBLE);
                        break;
                    case "start":
                        if (s.contains("Prepared") || s.contains("Preparing") || s.contains("Stopped") || s.contains("Stopping")
                                || s.contains("Paused") || s.contains("Pausing")) {
                            MyTask task = new MyTask("");
                            task.delegate = MainActivity.this;
                            task.execute("start \"" + encoderName + "\"");
                        } else {
                            Toast.makeText(MainActivity.this, "Encoder is busy!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "pause":
                        if (s.contains("Runned") || s.contains("Running")) {
                            MyTask task = new MyTask("");
                            task.delegate = MainActivity.this;
                            task.execute("pause \"" + encoderName + "\"");
                        } else {
                            Toast.makeText(MainActivity.this, "No recording process found!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "split":
                        if (s.contains("Runned") || s.contains("Running")) {
                            MyTask task = new MyTask("");
                            task.delegate = MainActivity.this;
                            task.execute("split \"" + encoderName + "\"");
                        } else {
                            Toast.makeText(MainActivity.this, "No recording process found!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "stop":
                        if (s.contains("Runned") || s.contains("Running") || s.contains("Paused") || s.contains("Pausing")) {
                            MyTask task = new MyTask("");
                            task.delegate = MainActivity.this;
                            task.execute("stop \"" + encoderName + "\"");
                        } else {
                            Toast.makeText(MainActivity.this, "No recording process found!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            } else {
                Toast.makeText(MainActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
            }

            connectTasks.remove(this);
            if (connectTasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }
            delegate.processFinish(s);
        }

    }

    protected boolean isConnectedToWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI){
            return true;
        }
        return false;
    }

}