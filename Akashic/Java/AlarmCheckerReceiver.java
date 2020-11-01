package com.example.akashic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;



public class AlarmCheckerReceiver extends BroadcastReceiver {
    public static double angles[] = new double[1500];
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor sharedPrefEdit;
    public static float latitude;
    public static float longitude;
    public static boolean isCalculating = false;
    public static final String FORTY = "FORTY";
    public static final String SUNSET = "SUNSET";
    public static final String THIRTY = "THIRTY";
    public static final String SUNRISE = "SUNRISE";
    public static final String ALL = "ALL";
    private static final String[] ALL_TIMES_OF_DAY = {SUNRISE, THIRTY, SUNSET, FORTY};
    final public static int UPDATE = 0;
    final public static int CANCEL = 1;
    final public static int SET = 2;
    public Calendar[] setAlarms = new Calendar[7];
    public static Calendar now = Calendar.getInstance();
    public static Calendar c = Calendar.getInstance();


    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        sharedPrefEdit = sharedPref.edit();
        latitude = sharedPref.getFloat("LATITUDE", 40.64571f);
        longitude = sharedPref.getFloat("LONGITUDE", -77.94365f);

        if (intent.getBooleanExtra("CANCEL_NEXT_ALARM", false)){
            cancelNextAlarm(context);


        } else {

            String timeOfDay = intent.getStringExtra("TIME_OF_DAY");

            int mode = intent.getIntExtra("MODE", CANCEL);

            if (timeOfDay.equals(ALL)) {
                for (int i = 0; i < 4; i++) {
                    timeOfDay = ALL_TIMES_OF_DAY[i];
                    checkMode(context, mode, timeOfDay);
                }
            } else {

                checkMode(context, mode, timeOfDay);
            }
        }
    }

    private void getSetAlarms(String timeOfDay){

        for (int i = 0; i < 7; i++) {
            c.setTimeInMillis(sharedPref.getLong(timeOfDay + intToSharedPrefString(i + 1) + "_ALARM", 0));
            //setAlarms[i] = c;
            setAlarms[i] = Calendar.getInstance();
            setAlarms[i].setTimeInMillis(sharedPref.getLong(timeOfDay + intToSharedPrefString(i + 1) + "_ALARM", 0));
        }

    }

    private void checkMode(Context context, int mode, String timeOfDay){
        if (mode == SET) {
            getSetAlarms(timeOfDay);
            resetAlarms(context, timeOfDay);
        } else if (mode == CANCEL) {
            getSetAlarms(timeOfDay);
            cancelAlarms(context, timeOfDay);
        } else if (mode == UPDATE) {
            if (sharedPref.getBoolean("IS_" + timeOfDay + "_ALARM_ON", false)) {
                getSetAlarms(timeOfDay);
                resetAlarms(context, timeOfDay);
            } else {
                getSetAlarms(timeOfDay);
                cancelAlarms(context, timeOfDay);
            }
        }
    }
    private void resetAlarms(Context context, String timeOfDay){
        int requestCodeBase;
        long offsetFromSunEvent;
        boolean setGentle = sharedPref.getBoolean("IS_" + timeOfDay + "_GENTLE", true);
        int lastRequestCode = sharedPref.getInt(timeOfDay + "_SEVEN_REQUEST_CODE", 0);
        long lastAlarm = setAlarms[6].getTimeInMillis();
        switch (timeOfDay){
            case SUNRISE:
                requestCodeBase = 0;
                break;
            case THIRTY:
                requestCodeBase = 7;
                break;
            case SUNSET:
                requestCodeBase = 14;
                break;
            case FORTY:
                requestCodeBase = 21;
                break;
            default:
                requestCodeBase = 100;
        }

        offsetFromSunEvent = sharedPref.getLong(timeOfDay + "_ALARM_OFFSET", -1000*60*10);


        boolean isAlarmPlaying = sharedPref.getBoolean("IS_ALARM_PLAYING", false);


        long nowMillis = now.getTimeInMillis();
        lastAlarm = calculateSunEvent(context, timeOfDay, nowMillis);
        if (lastAlarm > now.getTimeInMillis() && !isAlarmPlaying) {

                lastAlarm = lastAlarm + offsetFromSunEvent;
        }
        else {
            lastAlarm = lastAlarm + (24*60*60*1000);
            lastAlarm = calculateSunEvent(context, timeOfDay, lastAlarm) + offsetFromSunEvent;
        }
        boolean isAlarmGentle = sharedPref.getBoolean("IS_" + timeOfDay + "_GENTLE", false);
        //for (int i = 7-numPastAlarms; i < 7; i++){
        for (int i = 0; i < 7; i++){
            lastRequestCode = ((lastRequestCode + 1) % 7) + requestCodeBase;
            sharedPrefEdit.putLong(timeOfDay + intToSharedPrefString(i+1) +"_ALARM", lastAlarm);
            sharedPrefEdit.putInt(timeOfDay + intToSharedPrefString(i+1) +"_REQUEST_CODE", lastRequestCode);
            setAlarm(context, lastAlarm, lastRequestCode);
            lastAlarm = calculateGentleSunEvent(timeOfDay, lastAlarm);
            if (isAlarmGentle)
                setGentleAlarm(context, lastAlarm, lastRequestCode + 28, timeOfDay);
            lastAlarm = lastAlarm + (24*60*60*1000);
            lastAlarm = calculateSunEvent(context, timeOfDay, lastAlarm) + offsetFromSunEvent;
        }


        sharedPrefEdit.commit();
    }


    private String intToSharedPrefString(int i){
        String string;
        switch (i%10){
            case 0:
                string = "_ZERO";
                break;
            case 1:
                string = "_ONE";
                break;
            case 2:
                string = "_TWO";
                break;
            case 3:
                string = "_THREE";
                break;
            case 4:
                string = "_FOUR";
                break;
            case 5:
                string = "_FIVE";
                break;
            case 6:
                string = "_SIX";
                break;
            case 7:
                string = "_SEVEN";
                break;
            case 8:
                string = "_EIGHT";
                break;
            case 9:
                string = "_NINE";
                break;
            default:
                string = "_NEGATIVE";
        }
        return string;
    }
    private void cancelAlarms(Context context, String timeOfDay){
        int numPastAlarms = 0;
        int requestCodeBase;
        int lastRequestCode = sharedPref.getInt(timeOfDay + "_SEVEN_REQUEST_CODE", 0);
        long lastAlarm = setAlarms[6].getTimeInMillis();
        if (lastAlarm == 0) return;
        switch (timeOfDay){
            case SUNRISE:
                requestCodeBase = 0;
                break;
            case THIRTY:
                requestCodeBase = 7;
                break;
            case SUNSET:
                requestCodeBase = 14;
                break;
            case FORTY:
                requestCodeBase = 21;
                break;
            default:
                requestCodeBase = 100;
        }
        for (int i = 0; i < 7; i++){
            if (setAlarms[0].compareTo(now) < 0) numPastAlarms = i + 1;

        }


        for (int i = numPastAlarms; i < 7; i++){
            lastAlarm = 0;
            lastRequestCode = ((lastRequestCode + 1) % 7) + requestCodeBase;
            sharedPrefEdit.putLong(timeOfDay + intToSharedPrefString(i+1) +"_ALARM", lastAlarm);
            sharedPrefEdit.putInt(timeOfDay + intToSharedPrefString(i+1) +"_REQUEST_CODE", lastRequestCode);
            cancelAlarm(context, lastRequestCode);
            cancelAlarm(context, lastRequestCode + 28);
        }

        sharedPrefEdit.commit();
    }
    private void cancelAlarm(Context context, int requestCode){
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    private void setAlarm(Context context, long time, int requestCode){
        Intent intent = new Intent(context, AlarmReceiver.class);
        Intent infoIntent = new Intent(context, MainActivity.class);
        String timeOfDay = ALL_TIMES_OF_DAY[(int)(requestCode / 7.0)];
        intent.putExtra("TIME_OF_DAY", timeOfDay);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, MainActivity.infoAlarmRequestCode, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        AlarmManager.AlarmClockInfo alarmClockInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            alarmClockInfo = new AlarmManager.AlarmClockInfo(time, infoPendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }

    }

    private void setGentleAlarm(Context context, long time, int requestCode, String timeOfDay){
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("IS_GENTLE", true);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }
    private long calculateGentleSunEvent(String timeOfDay, long dayInMillis){
        c.setTimeInMillis(dayInMillis);
        if (sharedPref.getBoolean("ATMOSPHERE_EFFECTS", false)){
            calculateAtmosphereAngles(c);
        } else {
            calculateAngles(c);
        }
        switch (timeOfDay){
            case SUNRISE:
                calculateSunrise();
                break;
            case THIRTY:
                calculateSunrise();
                break;
            case SUNSET:
                calculateMorningThirty();
                break;
            case FORTY:
                calculateSunset();
                return c.getTimeInMillis();
            default:
                return c.getTimeInMillis();
        }
        return c.getTimeInMillis();
    }

    public static long calculateSunEvent(Context context, String timeOfDay, long dayInMillis){

        sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE);

        while (isCalculating == true){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isCalculating = true;
        c.setTimeInMillis(dayInMillis);
        if (sharedPref.getBoolean("ATMOSPHERE_EFFECTS", false)){
            calculateAtmosphereAngles(c);
        } else {
            calculateAngles(c);
        }
        switch (timeOfDay){
            case SUNRISE:
                calculateSunrise();
                break;
            case THIRTY:
                calculateMorningThirty();
                break;
            case SUNSET:
                calculateSunset();
                break;
            case FORTY:
                calculateSunset();
                isCalculating = false;
                return c.getTimeInMillis() + (40*60*1000);
            default:
                calculateSunset();
                isCalculating = false;
                return c.getTimeInMillis();
        }
        isCalculating = false;
        return c.getTimeInMillis();
    }

    private static void calculateAtmosphereAngles(Calendar calendar) {


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
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int timeZone = (calendar.get(Calendar.ZONE_OFFSET) / 3600000) + (calendar.get(Calendar.DST_OFFSET) / 3600000);

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
            trueSolarTime = ((minuteConversion * 1440)+eqOfTime+(4*longitude)-(60*timeZone))%1440;
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
    }
    private static void calculateAngles(Calendar calendar) {


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
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int timeZone = (calendar.get(Calendar.ZONE_OFFSET) / 3600000) + (calendar.get(Calendar.DST_OFFSET) / 3600000);

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
            trueSolarTime = ((minuteConversion * 1440)+eqOfTime+(4*longitude)-(60*timeZone))%1440;
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
    }

    private static void calculateMorningThirty(){
        double angle = angles[0];
        int minute = 1;
        int hour;

        while ((angle < 30) && (minute < 1500)){
            angle = angles[minute];
            minute = minute + 1;
        }
        minute = minute - 1;
        hour = minute / 60;
        minute = minute % 60;
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);

    }
    private static void calculateSunrise(){
        double angle = angles[0];
        int minute = 1;
        int hour;

        while (angle < 0){
            angle = angles[minute];
            minute = minute + 1;
        }
        minute = minute - 1;
        hour = minute / 60;
        minute = minute % 60;
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);


    }
    private static void calculateSunset(){
        double angle = angles[1381];
        int minute = 1380;
        int hour;

        while (angle < 0){
            angle = angles[minute];
            minute = minute - 1;
        }
        minute = minute + 1;
        hour = minute / 60;
        minute = minute % 60;
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
    }
    private void cancelNextAlarm(Context context){
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();
        long sunrise = calculateSunEvent(context, SUNRISE, nowMillis);
        long morning30 = calculateSunEvent(context, THIRTY, nowMillis);
        long sunset = calculateSunEvent(context, SUNSET, nowMillis);
        long forty = calculateSunEvent(context, FORTY, nowMillis);



        if (nowMillis < sunrise){

        } else if (nowMillis < morning30){
            getSetAlarms(THIRTY);
            for (int i = 0; i < 7; i++){
                if (setAlarms[i].get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && setAlarms[i].get(Calendar.YEAR) ==  setAlarms[i].get(Calendar.YEAR)){
                    int requestCode = sharedPref.getInt(THIRTY + intToSharedPrefString(i + 1) +"_REQUEST_CODE", 0);
                    cancelAlarm(context, requestCode);
                }
            }
        } else if (nowMillis < sunset){
            getSetAlarms(SUNSET);
            for (int i = 0; i < 7; i++){

                if (setAlarms[i].get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && setAlarms[i].get(Calendar.YEAR) == now.get(Calendar.YEAR)){
                    int requestCode = sharedPref.getInt(SUNSET + intToSharedPrefString(i + 1) +"_REQUEST_CODE", 0);
                    cancelAlarm(context, requestCode);

                }
            }
        } else if (nowMillis < forty){
            getSetAlarms(FORTY);
            for (int i = 0; i < 7; i++){
                if (setAlarms[i].get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && setAlarms[i].get(Calendar.YEAR) ==  setAlarms[i].get(Calendar.YEAR)){
                    int requestCode = sharedPref.getInt(FORTY + intToSharedPrefString(i + 1) +"_REQUEST_CODE", 0);
                    cancelAlarm(context, requestCode);
                }
            }
        }


    }
}
