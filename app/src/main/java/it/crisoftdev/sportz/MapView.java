package it.crisoftdev.sportz;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import it.crisoftdev.sportz.obj.MyLocation;

public class MapView extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        ArrayList<MyLocation> locations = getIntent().getParcelableArrayListExtra("locations");

        if(locations != null) {
            if (locations.size() > 0) {
                LatLng m = new LatLng(locations.get(0).getLatitude(), locations.get(0).getLongitude());
                mMap.addMarker(new MarkerOptions().position(m).title("Start"));

                LatLng end = new LatLng(locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude());
                mMap.addMarker(new MarkerOptions().position(end).title("End"));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(m));
                float zoom = 14.0f;
                if (locations.size() < 20)
                    zoom = 17.0f;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m, zoom));

                drawPrimaryLinePath(locations);
            } else {
                Toast.makeText(this, "Size 0", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Oops... qualcosa è andato storto", Toast.LENGTH_SHORT).show();
        }

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
