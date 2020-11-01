package com.example.akashic;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class LocationReceiver extends BroadcastReceiver {

    final public static int geoFenceRequestCode = 78;

    private GeofencingClient geofencingClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent geoFenceIntent = new Intent(context, LocationReceiver.class);
        PendingIntent geoFencePending = PendingIntent.getBroadcast(context, geoFenceRequestCode, geoFenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        

        SharedPreferences sharedPref = context.getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        float latitude = sharedPref.getFloat("LATITUDE", 40.64571f);
        float longitude = sharedPref.getFloat("LONGITUDE", -77.94365f);
        geofencingClient = LocationServices.getGeofencingClient(context);

        Geofence.Builder geoBuilder = new Geofence.Builder();
        Geofence geofence = geoBuilder.setCircularRegion(latitude, longitude, 16093.44f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setRequestId("LOCATION")
                .setNotificationResponsiveness(300000)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(0)
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, geoFencePending);
    }
}
