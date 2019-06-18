package it.crisoftdev.sportz.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.crisoftdev.sportz.R;
import it.crisoftdev.sportz.activities.ProfileActivity;
import it.crisoftdev.sportz.obj.MyLocation;
import it.crisoftdev.sportz.obj.Post;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<Post> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String uid;
    private DatabaseReference db_posts;


    public RecyclerViewAdapter(Context context, ArrayList<Post> data, String uid) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.uid = uid;
        db_posts = FirebaseDatabase.getInstance().getReference("posts");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.posts_row, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    private String getDistanceParsed(double m){
        if(m > 1000) return String.format("%.2f km", (m/1000));
        else return  String.format("%.2f m", (m));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mData.get(position);

        holder.steps.setText(String.valueOf(post.getStep()));
        holder.distance.setText(getDistanceParsed(post.getDistance()));
        holder.speed.setText(String.format("%d s/m", post.getSpeed()));
        holder.altitude.setText(String.format("%.2f m", post.getMaxAltitude()));
        holder.time.setText(post.getTime());

        holder.author.setText(post.getAuthorName());
        holder.date.setText(post.getDate());


        holder.author.setOnClickListener(v -> openProfile(post));

        holder.userPhoto.setOnClickListener(v -> openProfile(post));

        String url = post.getUserPhoto();
        if(url != null) {

            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(url.replace("s96", "s400"))
                    .into(holder.userPhoto);
        }

        if(post.isLike()){
            holder.likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite));
        } else {
            holder.likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_no_favorite));
        }

        setLikeCounter(post, holder.likeCounter);

        holder.likeButton.setOnClickListener(v ->

                db_posts.child(post.getId()).child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean flag = true;
                        for(DataSnapshot d : dataSnapshot.getChildren()){
                            if(Objects.requireNonNull(d.getValue(String.class)).equals(uid)) {
                                d.getRef().removeValue();
                                flag = false;
                                holder.likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_no_favorite));
                            }
                        }

                        if(flag){
                            dataSnapshot.getRef().push().setValue(uid);
                            holder.likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_favorite));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                })
        );


        if (holder.map != null){
            holder.map.onCreate(null);
            holder.map.onResume();
            holder.map.getMapAsync(holder);
        }
    }

    private void openProfile(Post post) {
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra("uid", post.getUid());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private void setLikeCounter(Post post, TextView tv){
        db_posts.child(post.getId()).child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long counter = dataSnapshot.getChildrenCount();
                if(counter > 0) {
                    tv.setText(String.valueOf(counter));
                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setmClickListener(ItemClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnMapReadyCallback {
        GoogleMap mapCurrent;
        MapView map;
        TextView steps, distance, speed, altitude, author, date, likeCounter, time;
        ImageButton likeButton;
        ImageView userPhoto;

        ViewHolder(View view) {
            super(view);
            map = view.findViewById(R.id.mapView);

            steps = view.findViewById(R.id.steps);
            steps.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular), Typeface.NORMAL);

            distance = view.findViewById(R.id.distance);
            distance.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular), Typeface.NORMAL);
            speed = view.findViewById(R.id.speed);
            speed.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular), Typeface.NORMAL);
            altitude = view.findViewById(R.id.altitude);
            altitude.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular), Typeface.NORMAL);
            time = view.findViewById(R.id.time);
            time.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_regular), Typeface.NORMAL);

            author = view.findViewById(R.id.author);
            author.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_bold), Typeface.NORMAL);
            likeButton = view.findViewById(R.id.like);
            date = view.findViewById(R.id.date_tv);
            date.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand_medium), Typeface.NORMAL);
            likeCounter = view.findViewById(R.id.like_counter);
            likeCounter.setTypeface(ResourcesCompat.getFont(context, R.font.orbitronbold), Typeface.NORMAL);

            userPhoto = view.findViewById(R.id.profile_image);

        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(context);
            mapCurrent = googleMap;

            ArrayList<MyLocation> locations = mData.get(getAdapterPosition()).getPath();

            if(locations.size() > 0) {

                LatLng m = new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongitude());
                mapCurrent.addMarker(new MarkerOptions().position(m).title("Start"));

                LatLng end = new LatLng(locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude());
                mapCurrent.addMarker(new MarkerOptions().position(end).title("End"));

                mapCurrent.moveCamera(CameraUpdateFactory.newLatLng(m));
                float zoom = 14.0f;
                if (locations.size() < 20)
                    zoom = 17.0f;
                mapCurrent.animateCamera(CameraUpdateFactory.newLatLngZoom(m, zoom));


                drawPrimaryLinePath(mData.get(getAdapterPosition()).getPath());
            }

            mapCurrent.setOnMapClickListener(latLng -> {
                Intent i = new Intent(context, it.crisoftdev.sportz.MapView.class);
                i.putExtra("locations", mData.get(getAdapterPosition()).getPath());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

            });

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        private void drawPrimaryLinePath(ArrayList<MyLocation> listLocsToDraw) {
            if (mapCurrent == null) return;

            if (listLocsToDraw.size() < 2) return;

            PolylineOptions options = new PolylineOptions();

            options.color(Color.parseColor("#AA0000FF"));
            options.width(20);
            options.visible(true);

            for (MyLocation locRecorded : listLocsToDraw){
                options.add(new LatLng(locRecorded.getLatitude(),
                        locRecorded.getLongitude()));
            }
            mapCurrent.addPolyline(options);

        }


    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
