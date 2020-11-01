package com.example.akashic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Alarm extends AppCompatActivity {
    boolean isLoopRunning;
    Thread thread;
    String timeOfDay;
    TextView time;
    long now;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Log.d("OnCreate","Something");
        TextView alarmDescription = findViewById(R.id.alarmDescriptor);
        time = findViewById(R.id.timeUntil);

        Intent intent = getIntent();

        timeOfDay = intent.getStringExtra("TIME_OF_DAY");
        if (timeOfDay == null) timeOfDay = "SUNSET";
        setTurnScreenOn(true);
        setShowWhenLocked(true);



        if (timeOfDay.equals("SUNRISE")){
            alarmDescription.setText("The sun rises in:");

        } else if (timeOfDay.equals("THIRTY")){
            alarmDescription.setText("The morning sun reaches thirty degrees in:");

        } else if (timeOfDay.equals("SUNSET")){
            alarmDescription.setText("The sun sets in:");
        } else if (timeOfDay.equals("FORTY")) {
            alarmDescription.setText("It will be 40 minutes after the sun has set in::");
        } else {
            alarmDescription.setText("The sun sets in:");
        }




    }

    @Override
    protected void onStop() {
        super.onStop();
        isLoopRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ONRESUME","Something");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Onstart","Something");
        isLoopRunning = true;
        now = System.currentTimeMillis();
        long sunEvent = AlarmCheckerReceiver.calculateSunEvent(this, timeOfDay, now);
        Runnable runnable = new Runnable (){
            @Override
            public void run() {
                Log.d("THREAD", Long.toString(sunEvent));
                while (isLoopRunning) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            time.setText(timeToString(sunEvent - System.currentTimeMillis()));
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    public void stop(View v) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("STOP_MEDIA_PLAYER", true);
        sendBroadcast(intent);
    }

    public String timeToString(long time){

        boolean isNegative;
        if (time < 0) {
            isNegative = true;
            time = time * -1;
        } else {
            isNegative = false;
        }

        time = time / 1000;
        String seconds;
        String minutes;
        String string;
        if (time % 60 < 10) seconds = "0" + time%60;
        else seconds = Long.toString(time%60);
        time = time / 60;
        if (time >= 60){

            if (time % 60 < 10) minutes = "0" + time % 60;
            else minutes = Long.toString(time  % 60);
            string = (time/60) + ":" + minutes + ":" + seconds;
        } else {
            if (time % 60 < 10) minutes = "0" + time % 60;
            else minutes = Long.toString(time  % 60);
            string = minutes + ":" + seconds;
        }

        if (isNegative){
            string = "-" + string;
        }

        return string;
    }



}