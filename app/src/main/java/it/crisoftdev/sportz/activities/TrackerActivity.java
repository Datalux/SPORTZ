package it.crisoftdev.sportz.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.SharedPreferencesManager;
import it.crisoftdev.sportz.obj.MyLocation;
import it.crisoftdev.sportz.obj.Post;
import it.crisoftdev.sportz.services.TrackingService;

public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    private static final String TAG = TrackerActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST = 100;

    private GoogleMap mMap;
    Location lastKnownLoc;
    private TextView stepView, magView, timeView, distanceView, speedView;
    Button stopTracking;

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] accelValues;
    private float[] magnetValues;

    private Handler handler = new Handler(); //Used to update the time in the UI

    private int stepCount = 0;
    private long stepTimestamp = 0;
    private long startTime = 0;
    long timeInMilliseconds = 0;
    long elapsedTime = 0;
    long updatedTime = 0;

    boolean useStepCounter = false;

    float estimatedDistance = 0;

    float stepSize = 0;

    ArrayList<MyLocation> path = new ArrayList<>();

    private double distance = 0;
    private int maxSpeed = 0;
    private double maxAltitude = 0;
    private String timeString;

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("posts");

    private boolean shareState = false;


    private Runnable timerRunnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            if(!shareState) {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

                updatedTime = elapsedTime + timeInMilliseconds;

                int seconds = (int) (updatedTime / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                minutes = minutes % 60;

                timeString = String.format("%d:%s:%s", hours, String.format("%02d", minutes), String.format("%02d", seconds));
                timeView.setText(timeString);

                handler.postDelayed(this, 0);
            }
        }

    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        stepView = findViewById(R.id.step);
        //accelView = findViewById(R.id.accelleremeter);
        magView = findViewById(R.id.magnetomer);
        timeView = findViewById(R.id.time);
        distanceView = findViewById(R.id.distance);
        speedView = findViewById(R.id.speed);

        stopTracking = findViewById(R.id.stopTracker);

        stepSize = SharedPreferencesManager.getInstance(getBaseContext()).getStepSize();



        stopTracking.setOnClickListener(v -> {
            if(shareState){
                Log.d("TrackerActivity", "sharing post...");
                Post p = new Post(path, stepCount, distance, maxSpeed, maxAltitude, getIntent().getStringExtra("uid"));
                //p.setAuthor(getIntent().getStringExtra("uid"));
                p.setAuthorName(SharedPreferencesManager.getInstance(getBaseContext()).getName());
                p.setUserPhoto(SharedPreferencesManager.getInstance(getBaseContext()).getPhotoUrl());
                Date currentTime = Calendar.getInstance().getTime();
                p.setDate(parseDateToddMMyyyy(currentTime));
                p.setTime(timeString);
                db.push().setValue(p);
                stopTracking.setClickable(false);
                Toast.makeText(this, "Post condiviso.", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(this::finish, 1000);


            } else {
                Intent myService = new Intent(TrackerActivity.this, TrackingService.class);
                stopService(myService);

                //sensorManager.unregisterListener(this);

                sensorManager.unregisterListener(TrackerActivity.this, stepDetectorSensor);
                sensorManager.unregisterListener(TrackerActivity.this, accelerometer);
                sensorManager.unregisterListener(TrackerActivity.this, magnetometer);
                elapsedTime += timeInMilliseconds;
                handler.removeCallbacks(timerRunnable);
                stopTracking.setText(getString(R.string.condividi));
                shareState = true;

            }

        });

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getBaseContext(), "Attivare il GPS", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onCreate: activated");

            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permission == PackageManager.PERMISSION_GRANTED) {

                PackageManager manager = getPackageManager();
                boolean hasStepDetector = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
                if(!hasStepDetector){
                    useStepCounter = true;
                    Toast.makeText(this, "Sembra che il tuo dispositivo non possa contare i passi", Toast.LENGTH_SHORT).show();
                }
                //sSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

                sensorManager = (SensorManager) getBaseContext().getSystemService(Context.SENSOR_SERVICE);

                if(!useStepCounter)
                    stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                else
                    stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                sensorManager.registerListener(TrackerActivity.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(TrackerActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(TrackerActivity.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(timerRunnable, 0);

                //sensorManager.registerListener(TrackerActivity.this, sSensor, SensorManager.SENSOR_DELAY_FASTEST);

                LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                        mMessageReceiver, new IntentFilter("GPSLocationUpdates"));

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }

                Log.d(TAG, "onCreate: ok");


                startTrackerService();
            } else {
                Log.d(TAG, "onCreate: È QUI IL PRB");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }

    }



    public String parseDateToddMMyyyy(Date time) {
        String outputPattern = "dd-MM-yyyy HH:mm";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        String str = null;

        try {
            str = outputFormat.format(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            if(message != null && message.equals("stop")){
                Log.d(TAG, "onReceive: stop received");
                stopTracking.setText(getString(R.string.condividi));
                shareState = true;
            } else {
                Log.d(TAG, "onReceive: received loc");
                Bundle b = intent.getBundleExtra("Location");
                lastKnownLoc = b.getParcelable("Location");
                if (lastKnownLoc != null) {
                    if (path.size() == 0) {
                        LatLng m = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(m).title("Start"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(m));
                        float zoom = 14.0f;
                        if (path.size() < 20)
                            zoom = 17.0f;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m, zoom));
                    }
                    path.add(new MyLocation(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude()));

                    if(useStepCounter) {
                        if (path.size() > 0) {
                            Location l = new Location("");
                            l.setLatitude(path.get(path.size() - 1).getLatitude());
                            l.setLongitude(path.get(path.size() - 1).getLongitude());

                            float distanceInMeters = lastKnownLoc.distanceTo(l);
                            estimatedDistance += distanceInMeters;
                            Toast.makeText(context, "d " + distanceInMeters + "\ne " + estimatedDistance, Toast.LENGTH_SHORT).show();
                            distanceView.setText(String.format("%.2f m", estimatedDistance));
                            stepView.setText(String.valueOf(estimatedDistance%stepSize));
                        }
                    }

                    drawPrimaryLinePath(path);
                }

                double currentAltituide = Objects.requireNonNull(lastKnownLoc).getAltitude();
                Log.d(TAG, "onReceive: " + currentAltituide);
                if(currentAltituide > maxAltitude){
                    maxAltitude = currentAltituide;
                }
            }
        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: READY!");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        //Get sensor values
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER):
                accelValues = event.values;
                break;
            case (Sensor.TYPE_MAGNETIC_FIELD):
                magnetValues = event.values;
                break;
            case (Sensor.TYPE_STEP_DETECTOR):
                countSteps(event.values[0]);
                calculateSpeed(event.timestamp);
                break;
        }

        if (accelValues != null && magnetValues != null) {
            float[] rotation = new float[9];
            float[] orientation = new float[3];
            if (SensorManager.getRotationMatrix(rotation, null, accelValues, magnetValues)) {
                SensorManager.getOrientation(rotation, orientation);
                float azimuthDegree = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
                float orientationDegree = Math.round(azimuthDegree);
                getOrientation(orientationDegree);
            }
        }
    }


    private void countSteps(float step) {
        stepCount += (int) step;
        stepView.setText(String.valueOf(stepCount));

        distance = stepCount * stepSize;
        @SuppressLint("DefaultLocale") String distanceString = String.format("%.2f" , distance);
        distanceView.setText(distanceString);

    }

    private void calculateSpeed(long eventTimeStamp) {
        long timestampDifference = eventTimeStamp - stepTimestamp;
        stepTimestamp = eventTimeStamp;
        double stepTime = timestampDifference / 1000000000.0;
        int speed = (int) (60 / stepTime);
        if(speed > maxSpeed) maxSpeed = speed;
        speedView.setText(String.valueOf(speed));
    }

    private void getOrientation(float orientationDegree) {
        String compassOrientation;
        if (orientationDegree >= 0 && orientationDegree < 90) {
            compassOrientation = "Nord";
        } else if (orientationDegree >= 90 && orientationDegree < 180) {
            compassOrientation = "Est";
        } else if (orientationDegree >= 180 && orientationDegree < 270) {
            compassOrientation = "Sud";
        } else {
            compassOrientation = "Ovest";
        }
        magView.setText(compassOrientation);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startTrackerService();
        } else {

            Toast.makeText(this, "Abilita i servizi di localizzazione", Toast.LENGTH_SHORT).show();
        }
    }


    private void startTrackerService() {
        startService(new Intent(this, TrackingService.class));

    }

    private void drawPrimaryLinePath(ArrayList<MyLocation> listLocsToDraw) {
        if (mMap == null) return;

        if (listLocsToDraw.size() < 2) return;

        PolylineOptions options = new PolylineOptions();

        options.color(Color.parseColor("#AA0000FF"));
        options.width(20);
        options.visible(true);

        for (MyLocation locRecorded : listLocsToDraw){
            options.add(new LatLng(locRecorded.getLatitude(),
                    locRecorded.getLongitude()));
        }
        mMap.addPolyline(options);

    }
}
