package com.itshareplus.googlemapdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;


import Modules.Trucks;

/**
 * Created by Vinesh on 15/07/17.
 */

public class track_history extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int truck_number;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
//        mMap.setMyLocationEnabled(true);
        draw();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.track_history_layout);

        Intent popup = getIntent();
        truck_number = popup.getExtras().getInt("truck_number");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);
    }

    private void draw(){
        PolylineOptions track_poly = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);
        int i=0;
        LatLng new_cord;

        while(i< Trucks.trucks.get(truck_number).tracking.size()){

            new_cord = new LatLng(Trucks.trucks.get(truck_number).tracking.get(i).latitude - 0.3, Trucks.trucks.get(truck_number).tracking.get(i).longitude);
            PolylineOptions line = new PolylineOptions().color(Color.BLACK).width(4);
            line.add(Trucks.trucks.get(truck_number).tracking.get(i));
            line.add(new_cord);
            line.zIndex(0);
            CircleOptions circle = new CircleOptions().center(new_cord).fillColor(Color.GREEN).strokeColor(Color.GREEN).radius(2500);
            circle.zIndex(1);
            mMap.addCircle(circle);
            mMap.addPolyline(line);
            i = i+Trucks.trucks.get(truck_number).tracking.size()/10 ;
        }

        i=0;

        while(i<Trucks.trucks.get(truck_number).tracking.size()){
            new_cord = new LatLng(Trucks.trucks.get(truck_number).tracking.get(i).latitude + 0.3, Trucks.trucks.get(truck_number).tracking.get(i).longitude);
            PolylineOptions line = new PolylineOptions().color(Color.BLACK).width(4);
            line.add(Trucks.trucks.get(truck_number).tracking.get(i));
            line.add(new_cord);
            line.zIndex(0);
            CircleOptions circle = new CircleOptions().center(new_cord).fillColor(Color.RED).strokeColor(Color.RED).radius(2500);
            circle.zIndex(1);
            mMap.addCircle(circle);
            mMap.addPolyline(line);
            i = i+Trucks.trucks.get(truck_number).tracking.size()/10 + 50 ;
        }

        i=0;
        while(i<Trucks.trucks.get(truck_number).tracking.size()){
            track_poly.add(Trucks.trucks.get(truck_number).tracking.get(i));
            i = i+1;
        }

        final LinearLayout layout = (LinearLayout)findViewById(R.id.track);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(Trucks.trucks.get(truck_number).bounds, 0));
            }
        });
        mMap.addPolyline(track_poly);
    }
}
