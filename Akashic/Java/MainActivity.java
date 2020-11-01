package com.example.akashic;

import android.Manifest;
import android.app.AlarmManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;





import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;



import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import us.dustinj.timezonemap.TimeZoneMap;

import java.text.DecimalFormat;
import java.util.Calendar;

import java.util.List;
import java.util.TimeZone;


import static com.example.akashic.AlarmCheckerReceiver.UPDATE;


public class MainActivity extends AppCompatActivity implements IveBowedDialog.IveBowed, Alarms.AlarmListener, Update.UpdateListener, AppCompatCallback {

    public TimeZoneMap timeZoneMap;
    private PendingIntent pendingIntent;
    public static String notificationChannelID = "com.example.Akashic.Sunset_Alarm";
    public static String oldnotificationChannelID = "com.example.Akashic.test123";
    public static String testnotificationChannelID = "com.example.Akashic.test1";
    public static String silentnotificationChannelID = "com.example.Akashic.silent1";

    //public static MediaPlayer mediaPlayer;
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor sharedPrefEdit;

    //public TimeZoneMap map;
    public static Calendar rightNow;
    public java.util.TimeZone timeZone;
    public Marker marker;
    public Button getLocation;
    final public static int locationRequestCode = 70;
    final public static int drawRequestCode = 71;
    final public static int sunsetAlarmRequestCode = 72;
    final public static int infoAlarmRequestCode = 73;
    final public static int alarmCheckerRequestCode = 74;
    final public static int notifactionRequestCode = 75;
    public static double longitude = -77.943650;
    public static double latitude = 40.645710;
    public static double[] anglesOfTheSun = new double[1500];



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (getLocation != null)
                        getLocation.setVisibility(Button.GONE);
                    if (selectedFragment != null)
                        getSupportFragmentManager().beginTransaction().remove(selectedFragment).commit();

                    return true;
                case R.id.navigation_dashboard:
                    selectedFragment = new Update();
                    if (getLocation != null)
                        getLocation.setVisibility(Button.GONE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                case R.id.navigation_notifications:
                    selectedFragment = new Alarms();
                    if (getLocation != null)
                        getLocation.setVisibility(Button.GONE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
            }

            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == locationRequestCode){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) getSingleLocation();
        } else if (requestCode == drawRequestCode){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {Alarms.alarmListener.setAlarm();}
            else {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED){

                } else {
                    setFullAlarm();
                }

                }
        }

    }

    @Override
    public void setFullAlarm() {

        Alarms.sharedPrefEdit.putBoolean(Alarms.sunsetAlarmState, true).commit();
        Intent intent = new Intent(this, AlarmReceiver.class);
        Intent infoIntent = new Intent(this, MainActivity.class);
        PendingIntent infoPendingIntent = PendingIntent.getActivity(this, infoAlarmRequestCode, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent = PendingIntent.getBroadcast(this, sunsetAlarmRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long millis = 4000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            alarmClockInfo = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + millis, infoPendingIntent);

            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millis, pendingIntent);
        }


        /*
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationRequestCode = 4;
        intent = new Intent(this, Alarm2.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, MainActivity.notificationChannelID).setSmallIcon(R.drawable.ic_notifications_black_24dp).setPriority(NotificationCompat.PRIORITY_HIGH).setFullScreenIntent(pendingIntent, true);
        Notification notification = notifBuilder.build();
        notificationManager.notify(0,notification);*/
    }

    private void setsAlarm(){
        Intent stopMediaPlayer = new Intent(this, AlarmReceiver.class);
        stopMediaPlayer.putExtra("STOP_MEDIA_PLAYER", true);
        PendingIntent stopMediaPlayerPending = PendingIntent.getBroadcast(this, AlarmReceiver.stopMediaRequestCode,stopMediaPlayer,PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, MainActivity.notifactionRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = Uri.parse("android.resource://com.example.akashic/" + R.raw.brahmananda_swarupa);

        String timeOfDay = "SUNSET";
        String message;
        AlarmCheckerReceiver.now = Calendar.getInstance();
        /*
        Alarms.sharedPrefEdit.putBoolean(Alarms.sunsetAlarmState, true).commit();
        Intent intent = new Intent(this, Alarm.class);
        pendingIntent = PendingIntent.getActivity(this, sunsetAlarmRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long millis = 3000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            alarmClockInfo = new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + millis, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millis, pendingIntent);
        }
*/
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, MainActivity.silentnotificationChannelID);
        notifBuilder.setContentTitle("Full Screen" + " Alarm");

        notifBuilder.setShowWhen(true);
        //notifBuilder.setWhen(timer);
        notifBuilder.setExtras(new Bundle());
        //notifBuilder.setUsesChronometer(true);
        //notifBuilder.setChronometerCountDown(true);
        notifBuilder.setCategory(NotificationCompat.CATEGORY_ALARM);
        notifBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        notifBuilder.setContentIntent(stopMediaPlayerPending);
        notifBuilder.setDeleteIntent(stopMediaPlayerPending);
        notifBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notifBuilder.setFullScreenIntent(pendingIntent, true);
        notifBuilder.addAction(R.drawable.ic_notifications_black_24dp,"Stop Alarm", stopMediaPlayerPending);
        Notification notification = notifBuilder.build();
        notificationManager.notify(0,notification);
    }


    public void fullScreenAlarm(){
        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {

                createDialog();
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, drawRequestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, drawRequestCode);

            }
        } else {

            setFullAlarm();
        }
    }
    @Override
    public void setAlarm() {




    }
    @Override
    public void cancelAlarm() {
        Alarms.submitText.setText("Off");
        Alarms.sharedPrefEdit.putBoolean(Alarms.sunsetAlarmState, false).commit();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (pendingIntent != null) alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onSubmitLatitudeLongitude(double submitLatitude, double submitLongitude) {

        longitude = submitLongitude;
        latitude = submitLatitude;
        calculate(longitude, latitude);


    }
    @Override
    public void onGetLocation() {

        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                createDialog();
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);

            }
        } else {

            getSingleLocation();
            timeZone = TimeZone.getDefault();
            rightNow.setTimeZone(timeZone);
        }
    }
    @Override
    public void onAtmosphereSwitch(){
        calculate(longitude, latitude);
    }

    @Override
    public void cancelNextAlarm() {
        Intent intent = new Intent(this, AlarmCheckerReceiver.class);
        intent.putExtra("CANCEL_NEXT_ALARM", true);
        sendBroadcast(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        sharedPref = getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("IS_ALARM_PLAYING", false)) {
            Intent intent = new Intent(this, Alarm.class);
            intent.putExtra("TIME_OF_DAY", sharedPref.getString("ALARM_PLAYING_IS", "SUNSET"));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String tag = "MainActivity: ";


        super.onCreate(savedInstanceState);


        Runnable runnable = () -> timeZoneMap = TimeZoneMap.forEverywhere();
        Thread thread = new Thread(runnable);



        rightNow = Calendar.getInstance();
        timeZone = rightNow.getTimeZone();

        thread.start();




        //Update Alarms

        Intent intent = new Intent(this, AlarmCheckerReceiver.class);
        intent.putExtra("TIME_OF_DAY", AlarmCheckerReceiver.ALL);
        intent.putExtra("MODE", UPDATE);
        sendBroadcast(intent);




        //Create Notification Channels
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);



            NotificationChannel notificationChannel = new NotificationChannel(testnotificationChannelID, "Test Alarm", NotificationManager.IMPORTANCE_HIGH);
            Uri uri = Uri.parse("android.resource://com.example.akashic/" + R.raw.brahmananda_swarupa);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_ALARM).setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build();
            notificationChannel.setSound(uri, audioAttributes);
            notificationChannel.setBypassDnd(true);

            notificationManager.createNotificationChannel(notificationChannel);

            notificationChannel = new NotificationChannel(silentnotificationChannelID, "Silent Alarm", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setBypassDnd(true);
            uri = Uri.parse("android.resource://com.example.akashic/" + R.raw.silence);
            audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_ALARM).setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED).build();
            notificationChannel.setSound(uri, audioAttributes);
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);




        }

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        Button requestDraw = findViewById(R.id.drawPermission);
        requestDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenAlarm();
            }
        });

        Button notificationButton = findViewById(R.id.newNotification);
        notificationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(view.getContext(), MainActivity.testnotificationChannelID);
                notifBuilder.setContentTitle("EXAMPLE NOTIFICATION");
                notifBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
                Notification notification = notifBuilder.build();
                notificationManager.notify(-1, notification);
            }
        });

        Button mapButton = findViewById(R.id.map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                ConstraintLayout constraintLayout = findViewById(R.id.button_nav);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(10.978036, 76.735309);
                        markerOptions.position(latLng);
                        markerOptions.draggable(true);
                        constraintLayout.setVisibility(View.VISIBLE);
                        marker = googleMap.addMarker(markerOptions);
                        if (getLocation != null)
                            getLocation.setVisibility(Button.VISIBLE);
                        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
                            @Override
                            public void onMapLongClick(LatLng latLng) {
                                marker.setPosition(latLng);
                            }
                        });
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
                            @Override
                            public void onMapClick(LatLng latLng) {
                                marker.setPosition(latLng);
                            }
                        });
                        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override
                            public void onMarkerDragStart(Marker marker) {

                            }

                            @Override
                            public void onMarkerDrag(Marker marker) {

                            }

                            @Override
                            public void onMarkerDragEnd(Marker marker1) {
                                marker = marker1;
                            }
                        });

                    }
                });

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        mapFragment).commit();


                }
        });

        getLocation = findViewById(R.id.getLocation2);
        getLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LatLng latLng = marker.getPosition();
                getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
                ConstraintLayout constraintLayout = findViewById(R.id.button_nav);
                constraintLayout.setVisibility(View.GONE);
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                try {
                    thread.join();
                    String TZString = timeZoneMap.getOverlappingTimeZone(latitude, longitude).getZoneId();

                    timeZone = java.util.TimeZone.getTimeZone(TZString);
                    rightNow.setTimeZone(timeZone);
                    calculate(longitude, latitude);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                    Toast toast = Toast.makeText(MainActivity.this, "Error: TimeZoneMap Could Not Be Created", Toast.LENGTH_LONG);
                    toast.show();

                }
            }
        });

        Button angleAnalysis = findViewById(R.id.angleAnalysis);
        angleAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = new AngleAnalysis();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });
        Button dateToy = findViewById(R.id.dateToy);
        dateToy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = new DateToy();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });
        Button newAlarm = findViewById(R.id.newAlarm);
        newAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Thread.sleep(3000);
                }
                catch (InterruptedException e) {

                }
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.putExtra("TIME_OF_DAY", "THIRTY");
                sendBroadcast(intent);

            }
        });



        sharedPref = getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        sharedPrefEdit = sharedPref.edit();
        //Calculate Sunrise
        float longitude = sharedPref.getFloat("LONGITUDE",-77.94365f);
        float latitude = sharedPref.getFloat("LATITUDE", 40.64571f);
        calculate(longitude, latitude);


    }

    public void createDialog(){
            LocationDialog locationdialog = new LocationDialog();
            locationdialog.show(getSupportFragmentManager(), "location dialog");
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
    public String minuteToString12Hour(int minute){
        if (minute == -1) return "The sun doesn't reach 30 degrees in this location on this day.";
        int hour = (minute/60);
        boolean isAM = false;
        String timeString;
        if (hour < 12) isAM = true;
        hour = hour%12;

        if (hour == 0){
            hour = hour + 12;
    }
        if (minute%60 >= 10) {
            if (isAM) timeString = hour + ":" + minute % 60 + " AM";
            else timeString = hour + ":" + minute % 60 + " PM";
        } else {
            if (isAM) timeString = hour + ":0" + minute % 60 + " AM";
            else timeString = hour + ":0" + minute % 60 + " PM";
        }

        return timeString;
    }
    public String CalendarToString(Calendar date){
            String dateString = (date.get(date.MONTH)+1) + "/" + date.get(date.DATE) + "/" + date.get(date.YEAR);

            return dateString;
    }
    public void calculate(double longitude, double latitude){

        TextView sunsetText = findViewById(R.id.sunset);
        TextView dateText = findViewById(R.id.date);
        TextView longitudeText = findViewById(R.id.longitude);
        TextView latitudeText = findViewById(R.id.latitude);
        TextView morningThirtyText = findViewById(R.id.morning_thirty);
        TextView sunriseText = findViewById(R.id.sunrise);
        TextView timeZoneText = findViewById(R.id.TimeZone);
        TextView timeText = findViewById(R.id.time);

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
        int morningThirty = calculateMorningThirty(anglesOfTheSun);
        int sunRise = calculateSunrise(anglesOfTheSun);
        int sunSet = calculateSunset(anglesOfTheSun, morningThirty, sunRise);



        dateText.setText(CalendarToString(rightNow));
        DecimalFormat decimalFormat = new DecimalFormat("#.000###");
        longitudeText.setText(decimalFormat.format(longitude));
        latitudeText.setText(decimalFormat.format(latitude));
        sunriseText.setText(minuteToString12Hour(sunRise));
        morningThirtyText.setText(minuteToString12Hour(morningThirty));
        sunsetText.setText(minuteToString12Hour(sunSet));
        timeZoneText.setText(rightNow.getTimeZone().getDisplayName());

        timeText.setText(calendarToTime(rightNow));

    }
    public static String calendarToTime(Calendar calendar){
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR);
        String minuteString;
        String amPM;
        if (minute < 10) minuteString = "0" + minute;
        else minuteString = Integer.toString(minute);
        if (calendar.get(Calendar.HOUR_OF_DAY) < 13) amPM = " AM";
        else amPM = " PM";
        if (hour == 0) hour = 12;
        return hour + ":" + minuteString + amPM;


    }
    public void getSingleLocation(){
        Criteria crit = new Criteria();

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String provider = lm.getBestProvider(crit, true);
        //Location location = lm.getLastKnownLocation(provider);
        try {
            lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    float longitude = (float) location.getLongitude();
                    float latitude = (float) location.getLatitude();
                    sharedPrefEdit.putFloat("LATITUDE", latitude);
                    sharedPrefEdit.putFloat("LONGITUDE", longitude);
                    sharedPrefEdit.commit();
                    calculate(longitude, latitude);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            }, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public void checkAlarmsSet(){
        long lastUpdate = sharedPref.getLong("Last Update", 0);
        Calendar cLastUpdate = Calendar.getInstance();
        cLastUpdate.setTimeInMillis(lastUpdate);
        Calendar cNow = Calendar.getInstance();


    }
    }

