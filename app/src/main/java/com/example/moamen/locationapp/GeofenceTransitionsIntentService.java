package com.example.moamen.locationapp;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String NAME = "MOAMEN";

    public GeofenceTransitionsIntentService() {
        super(NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("oncreate", ">>>>onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.d("Local service", "received" + startId + " : " + intent);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent", intent.getDataString());
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e("Error", ""+geofencingEvent.getErrorCode());
            return;
        }


        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        SharedPreferences sp = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
        String note = sp.getString("NOTE", "");

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER|geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.d("hello", "now were here");
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                if (geofence.getRequestId().equalsIgnoreCase(note)) {
                    notificationRoutine();
                    Log.d("key", geofence.getRequestId());
                } else {
                    Log.d("key", "id did not match");
                }
            }
        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
        }
    }

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
        assert mNotifyMgr != null;
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
