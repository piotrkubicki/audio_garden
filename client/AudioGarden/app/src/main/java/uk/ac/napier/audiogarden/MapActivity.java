package uk.ac.napier.audiogarden;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private GoogleMap mMap;
    MapView mMapView;
    final List<LatLng> points = new ArrayList<>();
    final List<String> names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setupActionBar();

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        getLocations();

    }

    private void getLocations() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.locations_storage), Context.MODE_PRIVATE);

        try {
            JSONObject jsonResponse = new JSONObject(prefs.getString(getString(R.string.locations_storage), ""));

            JSONArray jsonArray = jsonResponse.getJSONArray("locations");
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject obj = jsonArray.getJSONObject(i);
                final int location_id = obj.getInt("location_id");
                try {
                    JSONObject cord = obj.getJSONObject("position");
                    LatLng latLng = new LatLng(cord.getDouble("longitude"), cord.getDouble("latitude"));
                    points.add(latLng);
                    names.add(obj.getString("name"));

                } catch(JSONException e) {
                    Toast.makeText(getApplicationContext(), "Locations data cannot be found!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    // Add a marker at location and move the camera
                    for (LatLng point : points)
                    {
                        mMap.addMarker(new MarkerOptions().position(point).title(names.get(points.indexOf(point))));
                    }
                    moveCamera(points);
                }
            });
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Locations data cannot be found!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    //Update camera so all points are visible
    private void moveCamera(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points)
        {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
