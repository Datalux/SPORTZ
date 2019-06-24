package it.crisoftdev.sportz.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.SharedPreferencesManager;

public class TrackingService extends Service {
    private static final String TAG = TrackingService.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;

    protected BroadcastReceiver stopReceiver = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if(SharedPreferencesManager.getInstance(getBaseContext()).showNotification())
            buildNotification();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth != null)
            requestLocationUpdates();

        Log.d(TAG, "onCreate: start");

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        NotificationChannel chan = new
                NotificationChannel("sportz", "TrackingService", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return "sportz";
    }


    private void buildNotification() {

        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                sendMessageToActivity();
                Toast.makeText(context, "Tracciamento interrotto", Toast.LENGTH_SHORT).show();


                stopSelf();
            }
        };


        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = createNotificationChannel();
            Notification.Builder builder = new Notification.Builder(this, channelID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.tracking_enabled_notif))
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setContentIntent(broadcastIntent)
                    .setSmallIcon(R.drawable.ic_run_icon);
            startForeground(1, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.tracking_enabled_notif))
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setContentIntent(broadcastIntent)
                    .setSmallIcon(R.drawable.ic_run_icon);
            startForeground(1, builder.build());
        }

    }



    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        request.setInterval(10000);


        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestLocationUpdates: permission ok");

            locationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Location location = locationResult.getLastLocation();
                    if (location != null) {

                        sendMessageToActivity(location);

                    } else {
                        Toast.makeText(getBaseContext(), "GPS interrotto", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            client.requestLocationUpdates(request, locationCallback, null);


        }
    }

    private static void sendMessageToActivity() {
        Log.d(TAG, "sendMessageToActivity: stop sending...");
        Intent intent = new Intent("GPSLocationUpdates");
        intent.putExtra("Status", "stop");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void sendMessageToActivity(Location l) {
        Intent intent = new Intent("GPSLocationUpdates");
        intent.putExtra("Status", "true");
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        Log.d(TAG, "sendMessageToActivity: " + l.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        if(stopReceiver != null)
            unregisterReceiver(stopReceiver);
        client.removeLocationUpdates(locationCallback);
    }


}
