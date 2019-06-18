package it.crisoftdev.sportz.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.adapter.RecyclerViewAdapter;
import it.crisoftdev.sportz.obj.MyLocation;
import it.crisoftdev.sportz.obj.Post;

public class ProfileActivity extends AppCompatActivity {


    ImageView photoProfile;
    TextView userName;
    ImageButton settingsButton;


    DatabaseReference db_posts = FirebaseDatabase.getInstance().getReference("posts");



    RecyclerView recyclerView;
    RecyclerView.Adapter mAdapter;
    ArrayList<Post> posts = new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        photoProfile = findViewById(R.id.photo_profile);
        userName = findViewById(R.id.user_name_tv);

        settingsButtonInit();

        changeToolbarFont(findViewById(R.id.collapsing_toolbar), this);


        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(getIntent().getStringExtra("uid"));

        ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String url, name;

                url = dataSnapshot.child("photoUrl").getValue(String.class);
                name = dataSnapshot.child("name").getValue(String.class);

                if (url != null) {
                    Glide.with(photoProfile)
                            .asBitmap()
                            .load(url.replace("s96", "s400"))
                            .listener(new RequestListener<Bitmap>() {

                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.dismiss();
                                    return false;
                                }

                            })
                            .into(photoProfile);
                }

                CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
                collapsingToolbarLayout.setCollapsedTitleTypeface(Typeface.create(ResourcesCompat.getFont(getBaseContext(), R.font.quicksand_bold), Typeface.NORMAL));
                collapsingToolbarLayout.setExpandedTitleTypeface(Typeface.create(ResourcesCompat.getFont(getBaseContext(), R.font.quicksand_bold), Typeface.NORMAL));
                collapsingToolbarLayout.setTitle(name);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadData();

        recyclerView = findViewById(R.id.profile_rv);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        mAdapter = new RecyclerViewAdapter(getBaseContext(), posts, getIntent().getStringExtra("uid"));
        recyclerView.setAdapter(mAdapter);


    }

    private void settingsButtonInit() {
        settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), SettingsActivity.class);
            i.putExtra("uid", getIntent().getStringExtra("uid"));
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        if(!getIntent().getBooleanExtra("show_settings", false))
            settingsButton.setVisibility(View.GONE);
    }

    public static void changeToolbarFont(Toolbar toolbar, Activity context) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (tv.getText().equals(toolbar.getTitle())) {
                    applyFont(tv, context);
                    break;
                }
            }
        }
    }

    public static void applyFont(TextView tv, Activity context) {
        tv.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_bold), Typeface.NORMAL);
    }

    private void loadData() {

        db_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                mAdapter.notifyDataSetChanged();
                int i = 0;

                for(DataSnapshot d : dataSnapshot.getChildren()) {

                    if (Objects.requireNonNull(d.child("uid").getValue()).equals(getIntent().getStringExtra("uid"))) {

                        ArrayList<MyLocation> l = new ArrayList<>();
                        for (DataSnapshot dd : d.child("path").getChildren()) {
                            l.add(new MyLocation(dd.child("latitude").getValue(Double.class), dd.child("longitude").getValue(Double.class)));
                        }

                        boolean flag = false;
                        for (DataSnapshot dd : d.child("likes").getChildren()) {
                            if (Objects.requireNonNull(dd.getValue(String.class)).equals(getIntent().getStringExtra("uid"))) {
                                flag = true;
                            }
                        }

                        double dis = 0;
                        if (d.child("distance").getValue(Double.class) != null) {
                            dis = d.child("distance").getValue(Double.class);
                        }
                        int speed = 0;
                        if (d.child("speed").getValue(Integer.class) != null) {
                            speed = d.child("speed").getValue(Integer.class);
                        }
                        double maxal = 0;
                        if (d.child("maxAltitude").getValue(Double.class) != null) {
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
                        posts.add(0, p);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Errore nella lettura del database", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
