package com.example.akashic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.notification.StatusBarNotification;
import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.IOException;
import java.util.Calendar;

import static com.example.akashic.AlarmCheckerReceiver.UPDATE;
import static com.example.akashic.AlarmCheckerReceiver.now;

public class AlarmReceiver extends BroadcastReceiver {
    static MediaPlayer mediaPlayer;
    final public static int stopMediaRequestCode = 76;
    final public static int stopVibrationRequestCode = 77;

    int sunRise;
    int sunSet;
    int morningThirty;

    Vibrator vibrator;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEdit;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        sharedPrefEdit = sharedPref.edit();
        if (intent.getBooleanExtra("STOP_MEDIA_PLAYER", false)){
            if (mediaPlayer != null)
                mediaPlayer.release();
                vibrator.cancel();
                notificationManager.cancel(0);

            sharedPrefEdit.putBoolean("IS_ALARM_PLAYING", false).commit();

        } else if (intent.getBooleanExtra("IS_GENTLE", false)){
            buildGentleNotification(context, intent);
        } else if (intent.getBooleanExtra("STOP_VIBRATION", false)){
            vibrator.cancel();
            notificationManager.cancel(0);
            buildSecondaryNotification(context, intent);

        } else {
            buildNotification(context, intent);


            Intent intentRefresh = new Intent(context, AlarmCheckerReceiver.class);
            intentRefresh.putExtra("TIME_OF_DAY", intent.getStringExtra("TIME_OF_DAY"));
            intentRefresh.putExtra("MODE", UPDATE);
            intentRefresh.putExtra("FROM_ALARM_RECEIVER", true);
            context.sendBroadcast(intentRefresh);

        }



    }
    public void buildGentleNotification(Context context, Intent intent){

        String timeOfDay = intent.getStringExtra("TIME_OF_DAY");
        String title;
        String message;
        int id = 0;
        long timer;
        Calendar calendar = Calendar.getInstance();
        Calendar lastGentleAlarm = Calendar.getInstance();

        lastGentleAlarm.setTimeInMillis(sharedPref.getLong("LAST_GENTLE_" + timeOfDay + "_ALARM", 0));

        if (timeOfDay != "EXAMPLE")
            timer = AlarmCheckerReceiver.calculateSunEvent(context, timeOfDay, System.currentTimeMillis());
        else
            timer = AlarmCheckerReceiver.calculateSunEvent(context, "SUNSET", System.currentTimeMillis());
        calendar.setTimeInMillis(timer);
        if (timeOfDay.equals("SUNRISE")) {
            id = 1;
            title = "Gentle Sunrise Reminder";
            message = "The sun rises at " + MainActivity.calendarToTime(calendar) + ".";
        }
        else if (timeOfDay.equals("THIRTY")) {
            id = 2;
            title = "Gentle Reminder";
            message = "The sun reaches 30 degrees at " + MainActivity.calendarToTime(calendar) + ".";
        }
        else if (timeOfDay.equals("SUNSET")) {
            id = 3;
            title = "Gentle Sunset Reminder";
            message = "The sun sets at " + MainActivity.calendarToTime(calendar) + ".";

        }
        else if (timeOfDay.equals("FORTY")) {
            id = 4;
            title = "Gentle Reminder";
            message = "It will be forty minutes after the sun has set at " + MainActivity.calendarToTime(calendar) + ".";
        }
        else {
            title = "Example Gentle Alarm";
            message = "The sun sets at " + MainActivity.calendarToTime(calendar) + ".";

        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNotifications = notificationManager.getActiveNotifications();

            for (int i = 0; i < activeNotifications.length; i++) {
                Log.d("Active Notification",Integer.toString(activeNotifications[i].getId()));
                if (activeNotifications[i].getId() == id) return;
            }
        }


        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, MainActivity.silentnotificationChannelID);
        notifBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        notifBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        notifBuilder.setContentTitle(title);
        notifBuilder.setContentText(message);

        Notification notification = notifBuilder.build();

        notificationManager.notify(id, notification);

        sharedPrefEdit.putLong("LAST_GENTLE_" + timeOfDay + "_ALARM", calendar.getTimeInMillis());
    }
    private void buildSecondaryNotification(Context context, Intent receivedIntent){
        String timeOfDay = receivedIntent.getStringExtra("TIME_OF_DAY");
        Intent stopMediaPlayer = new Intent(context, AlarmReceiver.class);
        stopMediaPlayer.putExtra("STOP_MEDIA_PLAYER", true);
        PendingIntent stopMediaPlayerPending = PendingIntent.getBroadcast(context,stopMediaRequestCode,stopMediaPlayer,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent(context, Alarm.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, MainActivity.notifactionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);



        String message;
        String title;
        now = Calendar.getInstance();


        long timer = now.getTimeInMillis();

        if (timeOfDay != "EXAMPLE")
            timer = AlarmCheckerReceiver.calculateSunEvent(context, timeOfDay, Calendar.getInstance().getTimeInMillis());
        else
            timer = AlarmCheckerReceiver.calculateSunEvent(context, "SUNSET", Calendar.getInstance().getTimeInMillis());
        if (timeOfDay.equals("SUNRISE")) {
            title = "Sunrise";
            message = "The sun rises in: ";
        }
        else if (timeOfDay.equals("THIRTY")) {
            title = "Thirty";
            message = "The sun reaches 30 degrees in: ";
        }
        else if (timeOfDay.equals("SUNSET")) {
            title = "Sunset";
            message = "The sun sets in: ";
        }
        else if (timeOfDay.equals("FORTY")) {
            title = "Forty";
            message = "It will be forty minutes after the sun has set in: ";
        }
        else{
            title = "Example";
            message = "Example Alarm";
        }
        timeOfDay = timeOfDay.charAt(0) + timeOfDay.substring(1).toUpperCase();

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, MainActivity.silentnotificationChannelID);
        notifBuilder.setContentTitle(title + " Alarm");
        notifBuilder.setContentText(message);
        //notifBuilder.setNotificationSilent(); //Disables pop-up notification outside of Application
        notifBuilder.setShowWhen(true);
        notifBuilder.setWhen(timer);
        notifBuilder.setExtras(new Bundle());

        notifBuilder.setUsesChronometer(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notifBuilder.setChronometerCountDown(true);
        }
        notifBuilder.setCategory(NotificationCompat.CATEGORY_ALARM);
        notifBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        //notifBuilder.setContentIntent(stopMediaPlayerPending);
        notifBuilder.setFullScreenIntent(pendingIntent, true);
        //notifBuilder.setDeleteIntent(stopMediaPlayerPending);
        notifBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        notifBuilder.addAction(R.drawable.ic_notifications_black_24dp,"Stop Alarm", stopMediaPlayerPending);

        Notification notification = notifBuilder.build();
        notificationManager.notify(0,notification);

    }
    public void buildNotification(Context context, Intent receivedIntent){
        String timeOfDay = receivedIntent.getStringExtra("TIME_OF_DAY");
        Intent stopMediaPlayer = new Intent(context, AlarmReceiver.class);
        stopMediaPlayer.putExtra("STOP_MEDIA_PLAYER", true);
        PendingIntent stopMediaPlayerPending = PendingIntent.getBroadcast(context,stopMediaRequestCode,stopMediaPlayer,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent(context, Alarm.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, MainActivity.notifactionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = Uri.parse("android.resource://com.example.akashic/" + R.raw.brahmananda_swarupa);
        Intent stopVibration = new Intent(context, AlarmReceiver.class);
        stopVibration.putExtra("STOP_VIBRATION", true);

        stopVibration.putExtra("TIME_OF_DAY", timeOfDay);
        PendingIntent stopVibrationPending = PendingIntent.getBroadcast(context, stopVibrationRequestCode, stopVibration, PendingIntent.FLAG_UPDATE_CURRENT);


        String message;
        String title;
        now = Calendar.getInstance();


        long timer = now.getTimeInMillis();

        if (timeOfDay != "EXAMPLE")
            timer = AlarmCheckerReceiver.calculateSunEvent(context, timeOfDay, Calendar.getInstance().getTimeInMillis());
        else
            timer = AlarmCheckerReceiver.calculateSunEvent(context, "SUNSET", Calendar.getInstance().getTimeInMillis());
        if (timeOfDay.equals("SUNRISE")) {
            title = "Sunrise";
            message = "The sun rises in: ";
        }
        else if (timeOfDay.equals("THIRTY")) {
            title = "Thirty";
            message = "The sun reaches 30 degrees in: ";
        }
        else if (timeOfDay.equals("SUNSET")) {
            title = "Sunset";
            message = "The sun sets in: ";
        }
        else if (timeOfDay.equals("FORTY")) {
            title = "Forty";
            message = "It will be forty minutes after the sun has set in: ";
        }
        else{
            title = "Example";
            message = "Example Alarm";
        }
        timeOfDay = timeOfDay.charAt(0) + timeOfDay.substring(1).toUpperCase();




        long[] vibratePattern = new long[1500];
        for (int i = 0; i < 1500; i++){
            if (i%2 == 0)
                vibratePattern[i] = 866;
            else
                vibratePattern[i] = 866;
        }
        Intent snooze = new Intent(context, AlarmReceiver.class);
        snooze.putExtra("TIME_OF_DAY", timeOfDay);
        //PendingIntent snoozePending = PendingIntent.getActivity(context)


        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, MainActivity.silentnotificationChannelID);

        notifBuilder.setContentTitle(title + " Alarm");
        notifBuilder.setContentText(message);
        //notifBuilder.setNotificationSilent(); //Disables pop-up notification outside of Application
        notifBuilder.setShowWhen(true);
        notifBuilder.setWhen(timer);
        notifBuilder.setExtras(new Bundle());
        notifBuilder.setVibrate(vibratePattern);
        notifBuilder.setUsesChronometer(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notifBuilder.setChronometerCountDown(true);
        }
        notifBuilder.setCategory(NotificationCompat.CATEGORY_ALARM);
        notifBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        //notifBuilder.setContentIntent(stopMediaPlayerPending);

        notifBuilder.setDeleteIntent(stopMediaPlayerPending);
        notifBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notifBuilder.setFullScreenIntent(pendingIntent, true);
        notifBuilder.addAction(R.drawable.ic_notifications_black_24dp,"Stop Alarm", stopMediaPlayerPending);
        notifBuilder.addAction(R.drawable.ic_notifications_black_24dp, "Stop Vibration", stopVibrationPending);
        //notifBuilder.addAction(R.drawable.ic_notifications_black_24dp, "Snooze", snoozePending);
        Notification notification = notifBuilder.build();
        notificationManager.notify(0,notification);




        mediaPlayer = new MediaPlayer();
        AudioAttributes audioAttributes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_ALARM).setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build();
            mediaPlayer.setAudioAttributes(audioAttributes);
        }

        try{mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();}
        catch(IOException e){e.printStackTrace();}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
        } else {
            vibrator.vibrate(vibratePattern, 0);
        }
        float volume = 1.0f;
        mediaPlayer.setVolume(volume,volume);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                sharedPrefEdit.putBoolean("IS_ALARM_PLAYING", false).commit();
                mediaPlayer.release();
            }
        });

        SharedPreferences sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        sharedPrefEdit.putBoolean("IS_ALARM_PLAYING", true);
        sharedPrefEdit.putString("ALARM_PLAYING_IS", timeOfDay).commit();

    }
    public void calculate(){
        double[] anglesOfTheSun = new double[1500];
        Calendar rightNow = Calendar.getInstance();
        double latitude = sharedPref.getFloat("LATITUDE", (float)40.645710);
        double longitude = sharedPref.getFloat("LONGITUDE", (float)-77.943650);
        int dayOfYear = rightNow.get(Calendar.DAY_OF_YEAR);
        int year = rightNow.get(Calendar.YEAR);
        double zoneOffset = (rightNow.get(Calendar.ZONE_OFFSET) / 3600000.0) + (rightNow.get(Calendar.DST_OFFSET) / 3600000.0);

        boolean isAtmosphere;
        isAtmosphere = sharedPref.getBoolean("ATMOSPHERE_EFFECTS", false);
        if (isAtmosphere) {
            anglesOfTheSun = calculateAtmosphereAngles(zoneOffset, dayOfYear, year, longitude, latitude, anglesOfTheSun);
        } else {
            anglesOfTheSun = calculateAngles(zoneOffset, dayOfYear, year, longitude, latitude, anglesOfTheSun);
        }
        morningThirty = calculateMorningThirty(anglesOfTheSun);
        sunRise = calculateSunrise(anglesOfTheSun);
        sunSet = calculateSunset(anglesOfTheSun, morningThirty, sunRise);
    }
    public double[] calculateAngles(double timeZone, int day, int year, double longititude, double latitude, double[] angles) {
        int minute = 0;
        double minuteConversion;
        double julianDay;
        double julianCentury;
        double geomMeanLongSun;
        double geomMeanAnomSun;
        double eccentEarthOrbit;
        double sunEqOfCtr;
        double sunTrueLong;

        double sunAppLong;
        double meanObliqEcliptic;
        double obliqCorr;

        double sunDeclin;
        double varY;
        double eqOfTime;
        double trueSolarTime;
        double hourAngle;
        double solarZenithAngle;
        double solarElevation;

        int date = day + (year * 365) + ((year-2020) % 4) - 693470;
        while (minute != 1440){
            minuteConversion = minute * 0.000694444;
            julianDay = date + 2415018.5 + minuteConversion - (timeZone / 24.0);
            julianCentury = (julianDay - 2451545) / 36525.0;
            geomMeanLongSun = (280.46646 + (julianCentury * (36000.76983 + (julianCentury * 0.0003032)))) % 360;
            geomMeanAnomSun = 357.52911 + (julianCentury * (35999.05029 - (0.0001537 * julianCentury)));
            eccentEarthOrbit = 0.016708634 - (julianCentury * (0.000042037 +(0.0000001267 * julianCentury)));
            sunEqOfCtr = (Math.sin(Math.toRadians(geomMeanAnomSun)) * (1.914602-(julianCentury*(0.004817+(0.000014*julianCentury))))) + (Math.sin(Math.toRadians(2 * geomMeanAnomSun))*(0.019993-(0.000101*julianCentury))) + (Math.sin(Math.toRadians(3 *geomMeanAnomSun))*(0.000289));
            sunTrueLong = geomMeanLongSun + sunEqOfCtr;
            sunAppLong = sunTrueLong - 0.00569 - (0.00478 * (Math.sin(Math.toRadians(125.04-(1934.136*julianCentury)))));
            meanObliqEcliptic = 23 + (26+((21.448-(julianCentury*(46.815+(julianCentury*(0.00059-(julianCentury*0.001813)))))))/60)/60;
            obliqCorr = meanObliqEcliptic + (0.00256* Math.cos(Math.toRadians(125.04-(1934.136*julianCentury))));


            sunDeclin = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(obliqCorr))*Math.sin(Math.toRadians(sunAppLong))));
            varY = Math.tan(Math.toRadians(obliqCorr/2.0))*Math.tan(Math.toRadians(obliqCorr/2.0));
            eqOfTime = 4 * Math.toDegrees((varY*Math.sin(2*Math.toRadians(geomMeanLongSun)))-(2*eccentEarthOrbit*Math.sin(Math.toRadians(geomMeanAnomSun)))+(4*eccentEarthOrbit*varY*Math.sin(Math.toRadians(geomMeanAnomSun))*Math.cos(2*Math.toRadians(geomMeanLongSun)))-(0.5*varY*varY*Math.sin(4*Math.toRadians(geomMeanLongSun)))-(1.25*eccentEarthOrbit*eccentEarthOrbit*Math.sin(2* Math.toRadians(geomMeanAnomSun))));
            trueSolarTime = ((minuteConversion * 1440)+eqOfTime+(4*longititude)-(60*timeZone))%1440;
            if (trueSolarTime < 0){
                trueSolarTime = trueSolarTime + 1440;
            }
            if (trueSolarTime/4.0 < 0){
                hourAngle = (trueSolarTime/4.0) + 180;
            } else {
                hourAngle = (trueSolarTime/4.0) - 180;
            }
            solarZenithAngle = Math.toDegrees(Math.acos((Math.sin(Math.toRadians(latitude))*Math.sin(Math.toRadians(sunDeclin)))+(Math.cos(Math.toRadians(latitude))*Math.cos(Math.toRadians(sunDeclin))*Math.cos(Math.toRadians(hourAngle)))));
            solarElevation =  90 - solarZenithAngle;
            angles[minute] = solarElevation;


            minute = minute + 1;
        }
        return angles;
    }
    public double[] calculateAtmosphereAngles(double timeZone, int day, int year, double longititude, double latitude, double[] angles) {
        int minute = 0;
        double minuteConversion;
        double julianDay;
        double julianCentury;
        double geomMeanLongSun;
        double geomMeanAnomSun;
        double eccentEarthOrbit;
        double sunEqOfCtr;
        double sunTrueLong;

        double sunAppLong;
        double meanObliqEcliptic;
        double obliqCorr;

        double sunDeclin;
        double varY;
        double eqOfTime;
        double trueSolarTime;
        double hourAngle;
        double solarZenithAngle;
        double solarElevation;
        double atmosphericRefraction;

        int date = day + (year * 365) + ((year-2020) % 4) - 693470;
        while (minute != 1440){
            minuteConversion = minute * 0.000694444;
            julianDay = date + 2415018.5 + minuteConversion - (timeZone / 24.0);
            julianCentury = (julianDay - 2451545) / 36525.0;
            geomMeanLongSun = (280.46646 + (julianCentury * (36000.76983 + (julianCentury * 0.0003032)))) % 360;
            geomMeanAnomSun = 357.52911 + (julianCentury * (35999.05029 - (0.0001537 * julianCentury)));
            eccentEarthOrbit = 0.016708634 - (julianCentury * (0.000042037 +(0.0000001267 * julianCentury)));
            sunEqOfCtr = (Math.sin(Math.toRadians(geomMeanAnomSun)) * (1.914602-(julianCentury*(0.004817+(0.000014*julianCentury))))) + (Math.sin(Math.toRadians(2 * geomMeanAnomSun))*(0.019993-(0.000101*julianCentury))) + (Math.sin(Math.toRadians(3 *geomMeanAnomSun))*(0.000289));
            sunTrueLong = geomMeanLongSun + sunEqOfCtr;
            sunAppLong = sunTrueLong - 0.00569 - (0.00478 * (Math.sin(Math.toRadians(125.04-(1934.136*julianCentury)))));
            meanObliqEcliptic = 23 + (26+((21.448-(julianCentury*(46.815+(julianCentury*(0.00059-(julianCentury*0.001813)))))))/60)/60;
            obliqCorr = meanObliqEcliptic + (0.00256* Math.cos(Math.toRadians(125.04-(1934.136*julianCentury))));


            sunDeclin = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(obliqCorr))*Math.sin(Math.toRadians(sunAppLong))));
            varY = Math.tan(Math.toRadians(obliqCorr/2.0))*Math.tan(Math.toRadians(obliqCorr/2.0));
            eqOfTime = 4 * Math.toDegrees((varY*Math.sin(2*Math.toRadians(geomMeanLongSun)))-(2*eccentEarthOrbit*Math.sin(Math.toRadians(geomMeanAnomSun)))+(4*eccentEarthOrbit*varY*Math.sin(Math.toRadians(geomMeanAnomSun))*Math.cos(2*Math.toRadians(geomMeanLongSun)))-(0.5*varY*varY*Math.sin(4*Math.toRadians(geomMeanLongSun)))-(1.25*eccentEarthOrbit*eccentEarthOrbit*Math.sin(2* Math.toRadians(geomMeanAnomSun))));
            trueSolarTime = ((minuteConversion * 1440)+eqOfTime+(4*longititude)-(60*timeZone))%1440;
            if (trueSolarTime < 0){
                trueSolarTime = trueSolarTime + 1440;
            }
            if (trueSolarTime/4.0 < 0){
                hourAngle = (trueSolarTime/4.0) + 180;
            } else {
                hourAngle = (trueSolarTime/4.0) - 180;
            }
            solarZenithAngle = Math.toDegrees(Math.acos((Math.sin(Math.toRadians(latitude))*Math.sin(Math.toRadians(sunDeclin)))+(Math.cos(Math.toRadians(latitude))*Math.cos(Math.toRadians(sunDeclin))*Math.cos(Math.toRadians(hourAngle)))));
            solarElevation =  90 - solarZenithAngle;

            if (solarElevation > 85){
                atmosphericRefraction = 0;
            } else {
                if (solarElevation > 5) {
                    atmosphericRefraction = (58.1 / Math.tan(Math.toRadians(solarElevation))) - (0.07 / Math.pow(Math.tan(Math.toRadians(solarElevation)), 3)) + (0.000086 / Math.pow(Math.tan(Math.toRadians(solarElevation)), 5));
                } else {
                    if (solarElevation > -0.575) {
                        atmosphericRefraction = 1735 + (solarElevation * (-518.2 + solarElevation * (103.4 + solarElevation * (-12.79 + (solarElevation * 0.711)))));
                    } else {
                        atmosphericRefraction = -20.772/ Math.tan(Math.toRadians(solarElevation));
                    }
                }

            }

            atmosphericRefraction = atmosphericRefraction / 3600;
            angles[minute] = solarElevation + atmosphericRefraction;


            minute = minute + 1;
        }
        return angles;
    }
    public int calculateMorningThirty(double[] angles){
        double angle = angles[0];
        int minute = 1;

        while ((angle < 30) && (minute < 1500)){
            angle = angles[minute];
            minute = minute + 1;
        }
        if (angle < 30) return -1;
        return minute - 1;

    }
    public int calculateSunrise(double[] angles){
        double angle = angles[0];
        int minute = 1;

        while (angle < 0){
            angle = angles[minute];
            minute = minute + 1;
        }
        return minute - 1;

    }
    public int calculateSunset(double[] angles, int thirty, int sunrise){
        int minute;
        if (sunrise > thirty)
            minute = sunrise;
        else
            minute = thirty + 2;
        double angle = angles[minute];
        double lastangle = angles[minute - 1];

        while (angle > 0 || lastangle - angle < 0){
            lastangle = angle;
            minute = minute + 1;
            angle = angles[minute];

        }
        return minute - 1;

    }

}
