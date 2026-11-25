// Name                 Rishi Sahonta
// Student ID           S2533401
// Programme of Study   BSc (Hons) Software Development

package com.example.sahonta_rishi_s2533401;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<Situation> unplannedEvents;
    private ArrayList<Situation> currentRoadworks;
    private CameraPosition cameraPosition;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable("cameraPosition");
        }

        unplannedEvents = (ArrayList<Situation>) getIntent().getSerializableExtra("unplannedEvents");
        currentRoadworks = (ArrayList<Situation>) getIntent().getSerializableExtra("currentRoadworks");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (cameraPosition != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {
            LatLng glasgow = new LatLng(55.861670, -4.258335);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(glasgow, 12));
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        for (Situation situation : unplannedEvents) {

            if (!situation.getFromLatitude().isEmpty() && !situation.getFromLongitude().isEmpty()) {
                double fromLatitude = Double.parseDouble(situation.getFromLatitude());
                double fromLongitude = Double.parseDouble(situation.getFromLongitude());
                LatLng fromPosition = new LatLng(fromLatitude, fromLongitude);
                googleMap.addMarker(new MarkerOptions().position(fromPosition).title("Unplanned Event").snippet(situation.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                if (!situation.getToLatitude().isEmpty() && !situation.getToLongitude().isEmpty()) {
                    double toLatitude = Double.parseDouble(situation.getToLatitude());
                    double toLongitude = Double.parseDouble(situation.getToLongitude());
                    LatLng toPosition = new LatLng(toLatitude, toLongitude);
                    googleMap.addPolyline(new PolylineOptions().add(fromPosition, toPosition).color(Color.RED).width(8));
                    googleMap.addMarker(new MarkerOptions().position(toPosition).title("Unplanned Event - End").snippet(situation.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }
        }

        for (Situation situation : currentRoadworks) {

            if (!situation.getFromLatitude().isEmpty() && !situation.getFromLongitude().isEmpty()) {
                double fromLatitude = Double.parseDouble(situation.getFromLatitude());
                double fromLongitude = Double.parseDouble(situation.getFromLongitude());
                LatLng fromPosition = new LatLng(fromLatitude, fromLongitude);
                googleMap.addMarker(new MarkerOptions().position(fromPosition).title("Current Roadworks").snippet(situation.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                if (!situation.getToLatitude().isEmpty() && !situation.getToLongitude().isEmpty()) {
                    double toLatitude = Double.parseDouble(situation.getToLatitude());
                    double toLongitude = Double.parseDouble(situation.getToLongitude());
                    LatLng toPosition = new LatLng(toLatitude, toLongitude);
                    googleMap.addPolyline(new PolylineOptions().add(fromPosition, toPosition).color(Color.BLUE).width(8));
                    googleMap.addMarker(new MarkerOptions().position(toPosition).title("Current Roadworks - End").snippet(situation.getLocation()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (googleMap != null) {
            savedInstanceState.putParcelable("cameraPosition", googleMap.getCameraPosition());
        }
    }
}
