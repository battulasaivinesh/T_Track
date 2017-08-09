package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.PlaceFinderListener;
import Modules.Route;
import Modules.Step;
import Modules.Step;
import Modules.Trucks;
import Modules.Truck;
import Modules.PlaceFinder;
import Modules.Place;
import Modules.Distance;
import Modules.Duration;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;


public class ViewAll extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap2;

    private List<Marker> truckMarkers = new ArrayList<>();

    private SlidingUpPanelLayout mLayout;
    private ScrollView mScroll;
    private ImageView slide_arrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all);





        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);

        mapFragment.getMapAsync(this);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mScroll = (ScrollView)findViewById(R.id.scroll_view);
        slide_arrow = (ImageView)findViewById(R.id.slide_arrow);
        mLayout.setScrollableView(mScroll);

        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {

               if (newState == PanelState.EXPANDED){
                   slide_arrow.setRotation(180);
               }
               else {
                   slide_arrow.setRotation(0);
               }
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return Boolean.FALSE;
    }

    private void mark_all_trucks(){

        if (truckMarkers != null) {
            for (Marker marker : truckMarkers) {
                marker.remove();
            }
        }
        truckMarkers = new ArrayList<>();
        int i=0;
        for (Truck truck : Trucks.trucks) {
            truckMarkers.add(mMap2.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.truck_marker))
                            .title(truck.driver_name)
                            .position(truck.current_location)
            ));
            add_button(i);
            i += 1;
        }
    }

    public void add_button(int truck_number){
        Button b_truck = new Button(this);
        b_truck.setText(Trucks.trucks.get(truck_number).Name);
        LinearLayout ll = (LinearLayout)findViewById(R.id.truck_list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        b_truck.setId(truck_number);
        b_truck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                center_camera(v.getId());
            }
        });
        ll.addView(b_truck);
    }

    public void center_camera(int truck_number){
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        }
        mMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(Trucks.trucks.get(truck_number).current_location,15));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap2 = googleMap;


//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling

//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        mMap2.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        mark_all_trucks();
//        mMap2.setMyLocationEnabled(true);

        LatLng center_india = new LatLng(27.8913 ,78.0792);



        mMap2.animateCamera(CameraUpdateFactory.newLatLngZoom(center_india,4));
    }

}

