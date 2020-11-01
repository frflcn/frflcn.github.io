package com.example.akashic;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;

import android.widget.Switch;
import android.widget.TextView;




import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Alarms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Alarms extends Fragment {

    public static TextView isEmpty;
    public static AlarmListener alarmListener;
    public static TextView submitText;
    public static String sunsetAlarmState = "IS_SUNSET_ALARM_ON";
    public static String sunriseAlarmState = "IS_SUNRISE_ALARM_ON";
    public static String thirtyAlarmState = "IS_THIRTY_ALARM_ON";
    public static String fortyAlarmState = "IS_FORTY_ALARM_ON";

    private boolean sunsetValue;
    private boolean sunriseValue;
    private boolean thirtyValue;
    private boolean fortyValue;
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor sharedPrefEdit;
    private AlarmManager.OnAlarmListener sunsetAlarmListener;

    private Intent intent;
    private PendingIntent pendingIntent;
    public static Switch sunsetAlarm;
    public static Switch thirtyAlarm;
    public static Switch fortyAlarm;
    public static Switch sunriseAlarm;
    public static Switch gentleFortyAlarm;
    public static Switch gentleSunsetAlarm;
    public static Switch gentleThirtyAlarm;
    public static Switch gentleSunriseAlarm;

    public Alarms() {
        // Required empty public constructor
    }

    public interface AlarmListener{
        void setAlarm();
        void setFullAlarm();
        void cancelAlarm();
    }

    // TODO: Rename and change types and number of parameters
    public static Alarms newInstance() {
        Alarms fragment = new Alarms();
        return fragment;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof AlarmListener) {
            alarmListener = (AlarmListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentAListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        alarmListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_alarms, container, false);
        sunsetAlarm = v.findViewById(R.id.sunset_alarm_switch);
        thirtyAlarm = v.findViewById(R.id.thirty_alarm_switch);
        fortyAlarm = v.findViewById(R.id.forty_switch_alarm);
        sunriseAlarm = v.findViewById(R.id.sunrise_alarm_switch);
        gentleFortyAlarm = v.findViewById(R.id.gentle_forty_alarm_switch);
        gentleSunsetAlarm = v.findViewById(R.id.gentle_sunset_alarm_switch);
        gentleThirtyAlarm = v.findViewById(R.id.gentle_thirty_alarm_switch);
        gentleSunriseAlarm = v.findViewById(R.id.gentle_sunrise_alarm_switch);
        //sharedPrefEdit.clear().commit();
        sharedPref = getActivity().getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        sharedPrefEdit = sharedPref.edit();





        sunsetValue = sharedPref.getBoolean(sunsetAlarmState, false);
        sunsetAlarm.setChecked(sunsetValue);
        if (sunsetValue) {
            gentleSunsetAlarm.setVisibility(View.VISIBLE);
        } else {
            gentleSunsetAlarm.setVisibility(View.GONE);
        }

        sunriseValue = sharedPref.getBoolean(sunriseAlarmState, false);
        sunriseAlarm.setChecked(sunriseValue);
        if (sunriseValue) {
            gentleSunriseAlarm.setVisibility(View.VISIBLE);
        } else {
            gentleSunriseAlarm.setVisibility(View.GONE);
        }

        thirtyValue = sharedPref.getBoolean(thirtyAlarmState, false);
        thirtyAlarm.setChecked(thirtyValue);
        if (thirtyValue) {
            gentleThirtyAlarm.setVisibility(View.VISIBLE);
        } else {
            gentleThirtyAlarm.setVisibility(View.GONE);
        }

        fortyValue = sharedPref.getBoolean(fortyAlarmState, false);
        fortyAlarm.setChecked(fortyValue);
        if (fortyValue) {
            gentleFortyAlarm.setVisibility(View.VISIBLE);
        } else {
            gentleFortyAlarm.setVisibility(View.GONE);
        }

        fortyAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    setAlarms(AlarmCheckerReceiver.FORTY);
                    sharedPrefEdit.putBoolean(fortyAlarmState, true).commit();
                    gentleFortyAlarm.setVisibility(View.VISIBLE);
                } else {
                    cancelAlarms(AlarmCheckerReceiver.FORTY);
                    sharedPrefEdit.putBoolean(fortyAlarmState, false).commit();
                    gentleFortyAlarm.setVisibility(View.GONE);
                }
            }
        });

        thirtyAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    setAlarms(AlarmCheckerReceiver.THIRTY);
                    sharedPrefEdit.putBoolean(thirtyAlarmState, true).commit();
                    gentleThirtyAlarm.setVisibility(View.VISIBLE);
                } else {
                    cancelAlarms(AlarmCheckerReceiver.THIRTY);
                    sharedPrefEdit.putBoolean(thirtyAlarmState, false).commit();
                    gentleThirtyAlarm.setVisibility(View.GONE);
                }
            }
        });

        sunriseAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    setAlarms(AlarmCheckerReceiver.SUNRISE);
                    sharedPrefEdit.putBoolean(sunriseAlarmState, true).commit();
                    gentleSunriseAlarm.setVisibility(View.VISIBLE);
                } else {
                    cancelAlarms(AlarmCheckerReceiver.SUNRISE);
                    sharedPrefEdit.putBoolean(sunriseAlarmState, false).commit();
                    gentleSunriseAlarm.setVisibility(View.GONE);
                }
            }
        });


        sunsetAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    setAlarms(AlarmCheckerReceiver.SUNSET);
                    sharedPrefEdit.putBoolean(sunsetAlarmState, true).commit();
                    gentleSunsetAlarm.setVisibility(View.VISIBLE);
                } else {
                    cancelAlarms(AlarmCheckerReceiver.SUNSET);
                    sharedPrefEdit.putBoolean(sunsetAlarmState, false).commit();
                    gentleSunsetAlarm.setVisibility(View.GONE);
                }
            }
        });
        gentleSunriseAlarm.setChecked(sharedPref.getBoolean("IS_SUNRISE_GENTLE", false));
        gentleSunriseAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {sharedPrefEdit.putBoolean("IS_SUNRISE_GENTLE", true).commit();}
                else {sharedPrefEdit.putBoolean("IS_SUNRISE_GENTLE", false).commit();}
            }
        });

        gentleSunsetAlarm.setChecked(sharedPref.getBoolean("IS_SUNSET_GENTLE", false));
        gentleSunsetAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {sharedPrefEdit.putBoolean("IS_SUNSET_GENTLE", true).commit();}
                else {sharedPrefEdit.putBoolean("IS_SUNSET_GENTLE", false).commit();}
            }
        });

        gentleThirtyAlarm.setChecked(sharedPref.getBoolean("IS_THIRTY_GENTLE", false));
        gentleThirtyAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {sharedPrefEdit.putBoolean("IS_THIRTY_GENTLE", true).commit();}
                else {sharedPrefEdit.putBoolean("IS_THIRTY_GENTLE", false).commit();}
            }
        });

        gentleFortyAlarm.setChecked(sharedPref.getBoolean("IS_FORTY_GENTLE", false));
        gentleFortyAlarm.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {sharedPrefEdit.putBoolean("IS_FORTY_GENTLE", true).commit();}
                else {sharedPrefEdit.putBoolean("IS_FORTY_GENTLE", false).commit();}
            }
        });

        return v;
    }
    public void checkDrawPermissions(){

        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SYSTEM_ALERT_WINDOW)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                //createDialog();
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, MainActivity.drawRequestCode);

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW}, MainActivity.drawRequestCode);

            }
        } else alarmListener.setAlarm();
    }
    public void setGentleAlarms(String timeOfDay){
        Intent intent = new Intent(getActivity(), AlarmCheckerReceiver.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        intent.putExtra("MODE", AlarmCheckerReceiver.SET);
        getActivity().sendBroadcast(intent);
    }

    public void setAlarms(String timeOfDay){
        Intent intent = new Intent(getActivity(), AlarmCheckerReceiver.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        intent.putExtra("MODE", AlarmCheckerReceiver.SET);
        getActivity().sendBroadcast(intent);

    }
    public void updateAlarms(String timeOfDay){
        Intent intent = new Intent(getActivity(), AlarmCheckerReceiver.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        intent.putExtra("MODE", AlarmCheckerReceiver.UPDATE);
        getActivity().sendBroadcast(intent);

    }
    public void cancelAlarms(String timeOfDay){
        Intent intent = new Intent(getActivity(), AlarmCheckerReceiver.class);
        intent.putExtra("TIME_OF_DAY", timeOfDay);
        intent.putExtra("MODE", AlarmCheckerReceiver.CANCEL);
        getActivity().sendBroadcast(intent);

    }


}