package xyz.multimikael.touch;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.*;
import java.net.*;


public class intro extends ActionBarActivity {
    private SharedPreferences SP;
    private Button leftBtn;
    private Button rightBtn;
    private LinearLayout bottomBtns;
    private Boolean isRunning = false;
    private float xOld = 0;
    private float yOld = 0;
    private float xNew = 0;
    private float yNew = 0;
    /* Timer
    private Boolean isReleased;
    private CountDownTimer timer;*/

    private ObjectOutputStream outputStream;
    private Socket connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        leftBtn = (Button) findViewById(R.id.btnLeft);
        rightBtn = (Button) findViewById(R.id.btnRight);
        bottomBtns = (LinearLayout) findViewById(R.id.bottomButtons);

        /* Timer
        timer = new CountDownTimer(100, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isReleased){
                    this.cancel();
                    try{
                        outputStream.writeObject("leftClick");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinish() {
                this.cancel();
            }
        };*/

        //Add listner to bottom buttons
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    outputStream.writeObject("leftClick");
                    outputStream.flush();
                } catch (IOException ioException) {
                    displayAlert("IOException", "Error writing obejct to outputstream");
                }
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputStream.writeObject("rightClick");
                    outputStream.flush();
                } catch (IOException ioException) {
                    displayAlert("IOException", "Error writing obejct to outputstream");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_powerToggle:
                if(isRunning){
                    bottomBtns.setVisibility(View.GONE);
                    isRunning = false;
                    try{
                        outputStream.close();
                        connection.close();
                    } catch (IOException ioException) {
                        displayAlert("IOException", ioException.toString());
                    }

                } else if(!isRunning) {
                    isRunning = true;
                    bottomBtns.setVisibility(View.VISIBLE);
                    new startRunning().execute();
                } else {
                    displayAlert("Error", "Error with isRunning variable.");
                }
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isRunning){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xOld = event.getX();
                    yOld = event.getY();
                    xNew = event.getX();
                    yNew = event.getY();
                    /* Timer
                    isReleased = false;
                    timer.start();

                case MotionEvent.ACTION_UP:
                    isReleased = true;
                    */
                case MotionEvent.ACTION_MOVE:
                    xNew = event.getX();
                    yNew = event.getY();
                    if(xOld == 0 && yOld == 0) {
                        xOld = xNew;
                        yOld = yNew;
                    }
                    float xDif = xNew - xOld;
                    float yDif = yNew - yOld;
                    /* Timer
                    if(xDif > 0 || yDif > 0) {
                        timer.cancel();
                    }*/

                    try{
                        outputStream.writeObject("x:" + xDif);
                        outputStream.writeObject("y:" + yDif);
                        outputStream.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    xOld = xNew;
                    yOld = yNew;
            }
        }

        return super.onTouchEvent(event);
    }

    private class startRunning extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("Starting...");
            String serverIp = SP.getString("serverIp", "NA");
            String _serverPort = SP.getString("serverPort", "4444");
            int serverPort = Integer.parseInt(_serverPort);
            System.out.println("Connecting...");
            try{
                connection = new Socket(serverIp, serverPort);
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                outputStream.writeObject("Ready!");
                outputStream.flush();
                System.out.println("Connected!");
            } catch (UnknownHostException unknownHostException) {
                displayAlert("UnknownHostException", "Can't connect to host.");
            } catch (IOException ioException) {
                displayAlert("IOException" , "Could not get I/O to: " + serverIp);
                ioException.printStackTrace();
            }
            return null;
        }
    }

    //Custom display an alert-dialog method
    private void displayAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
