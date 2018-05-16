package com.example.moamen.locationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import static android.location.LocationManager.GPS_PROVIDER;


public class MainActivity extends AppCompatActivity {

    int PERMISSION_ALL = 10;
    String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    LocationManager locationManager;
    public GeofencingClient mGeofencingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasPermissions(getApplicationContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        Button saveNoteBtn = findViewById(R.id.saveNoteBtn);
        final EditText notes = findViewById(R.id.notes);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        saveNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String notesFromLocation= notes.getText().toString();
                if(!notesFromLocation.isEmpty()){

                    locationRoutine(notesFromLocation);

                    notes.setText("");
                }
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void locationRoutine(String notesFromLocation) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location last = locationManager.getLastKnownLocation(GPS_PROVIDER);
            final TextView mTextView = findViewById(R.id.location);
            double longitude, latitude;
            longitude= last.getLongitude();
            latitude=last.getLatitude();
            mTextView.setText("Longitude: " + longitude + "\nLatitude: " + latitude);

            /// Get preferences
            SharedPreferences sp= getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
            sp.edit().putString("latitude", String.valueOf(latitude)).apply();
            sp.edit().putString("longitude", String.valueOf(longitude)).apply();
            sp.edit().putString("NOTE", notesFromLocation).apply();

            addLocationAlert(latitude,longitude,notesFromLocation);
     }


//     Notification
    public void notificationRoutine() {

        /// Get preferences
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        double longitude = Double.parseDouble(sp.getString("longitude", ""));
        double latitude = Double.parseDouble(sp.getString("latitude", ""));
        String note = sp.getString("NOTE", "");

        System.out.println("Longitude: " + longitude + "\nLatitude " + latitude);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Location notification")
                        .setContentText(note);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    @SuppressLint("MissingPermission")
    private void addLocationAlert(double lat, double lng, String key){
        Log.d("addLocation", key);
        if (isLocationAccessPermitted()) {
            requestLocationAccessPermission();
            Log.d("if","we are in if case now");
        } else  {
            Log.d("else","in else");
            Geofence geofence = getGeofence(lat, lng, key);
            Log.d("tag","Longitude: "+ lng + "Latitude: " + lat);
            mGeofencingClient.addGeofences(getGeofencingRequest(geofence),
                    getGeofencePendingIntent());
            notificationRoutine();
        }
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER );
        // | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(geofence);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        Log.d("pendingIntent", "called");
        Intent intent = new Intent(getApplicationContext(), GeofenceTransitionsIntentService.class);
        Log.d("intent", intent.toString());
        return PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

    }
    private boolean isLocationAccessPermitted(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return true;
        }else{
            return false;
        }
    }
    private void requestLocationAccessPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                110);
    }
    private Geofence getGeofence(double lat, double lang, String key) {
        return new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(lat, lang, 10)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

}
