package it.crisoftdev.sportz.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.SharedPreferencesManager;
import it.crisoftdev.sportz.adapter.RecyclerViewAdapter;
import it.crisoftdev.sportz.obj.MyLocation;
import it.crisoftdev.sportz.obj.Post;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    SwipeRefreshLayout mSwipeRefreshLayout;

    private static final int PERMISSIONS_REQUEST = 100;


    private RecyclerView.Adapter mAdapter;

    private ArrayList<Post> posts = new ArrayList<>();

    private DatabaseReference db_posts = FirebaseDatabase.getInstance().getReference("posts");

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(SharedPreferencesManager.getInstance(getBaseContext()).getSensorFlag())
            checkForSensor();

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            posts.clear();
            mAdapter.notifyDataSetChanged();
            loadData();

            mSwipeRefreshLayout.setRefreshing(false);
        });

        loadData();

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        mAdapter = new RecyclerViewAdapter(getBaseContext(), posts, getIntent().getStringExtra("uid"));
        recyclerView.setAdapter(mAdapter);




        findViewById(R.id.startRecording).setOnClickListener(v -> {

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getBaseContext(), "Attivare il GPS", Toast.LENGTH_SHORT).show();
            } else {

                int permission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    startTrackerActivity();
                }  else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST);
            }
        }

        });

        findViewById(R.id.profile).setOnClickListener(v -> {
                Intent i = new Intent(getBaseContext(), ProfileActivity.class);
                i.putExtra("uid", getIntent().getStringExtra("uid"));
                i.putExtra("show_settings", true);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


        });


        findViewById(R.id.home).setOnClickListener(v -> {
            recyclerView.smoothScrollToPosition(0);
        });



    }

    private void checkForSensor() {

        PackageManager manager = getPackageManager();
        boolean hasStepDetector = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
        boolean hasAccelerometer = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        if(!hasStepDetector || !hasAccelerometer) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Attenzione!")
                    .setMessage("Sembra che il tuo dispositivo non sia dotato dei sensori necessari per il tracciamento, pertanto i dati forniti saranno delle stime.")
                    .setCancelable(false)
                    .setPositiveButton("Ho capito", (dialog, which) -> dialog.cancel())
                    .setNegativeButton("Non mostrare più", (dialog, which) -> {
                        SharedPreferencesManager.getInstance(getBaseContext()).setNoSensorFlag();
                    })
                    .setIcon(R.drawable.ic_warning)
                    .show();
        }



    }

    private void startTrackerActivity() {
        Intent i = new Intent(getBaseContext(), TrackerActivity.class);
        i.putExtra("uid", getIntent().getStringExtra("uid"));
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadData() {
        db_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                mAdapter.notifyDataSetChanged();
                int i = 0;

                for(DataSnapshot d : dataSnapshot.getChildren()){
                    ArrayList<MyLocation> l = new ArrayList<>();
                    for(DataSnapshot dd : d.child("path").getChildren()) {
                        l.add(new MyLocation(dd.child("latitude").getValue(Double.class), dd.child("longitude").getValue(Double.class)));
                    }

                    boolean flag = false;
                    for(DataSnapshot dd : d.child("likes").getChildren()) {
                        if (Objects.requireNonNull(dd.getValue(String.class)).equals(getIntent().getStringExtra("uid"))) {
                            flag = true;
                        }
                    }

                    double dis = 0;
                    if(d.child("distance").getValue(Double.class) != null){
                        dis = d.child("distance").getValue(Double.class);
                    }
                    int speed = 0;
                    if(d.child("speed").getValue(Integer.class) != null){
                        speed = d.child("speed").getValue(Integer.class);
                    }
                    double maxal = 0;
                    if(d.child("maxAltitude").getValue(Double.class) != null){
                        maxal = d.child("maxAltitude").getValue(Double.class);
                    }


                    Post p = new Post(
                            d.getKey(),
                            l,
                            d.child("step").getValue(Long.class),
                            dis,
                            speed,
                            maxal,
                            d.child("time").getValue(String.class),
                            d.child("uid").getValue(String.class)
                    );


                    p.setAuthorName(d.child("authorName").getValue(String.class));
                    p.setDate(d.child("date").getValue(String.class));
                    p.setUserPhoto(d.child("userPhoto").getValue(String.class));
                    p.setLike(flag);
                    posts.add(0,p);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Errore nella lettura del database", Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int REQUEST_SELECT_ACTIVITY = 1;
        if (requestCode == REQUEST_SELECT_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                int result = data.getIntExtra("result", -1);
                if(result != -1){
                    startNewActivity(result);
                } else {
                    Toast.makeText(this, "Errore", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startNewActivity(int type){
        Log.d(TAG, "startNewActivity: " + type);
        Intent i = new Intent(getBaseContext(), TrackerActivity.class);
        i.putExtra("uid", getIntent().getStringExtra("uid"));
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }




}
