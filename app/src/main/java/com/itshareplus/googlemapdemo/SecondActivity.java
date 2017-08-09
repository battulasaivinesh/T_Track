package com.itshareplus.googlemapdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.view.ViewGroup.LayoutParams;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.PlaceFinderListener;

import Modules.Route;
import Modules.Step;
import Modules.Truck;
import Modules.Trucks;
import Modules.PlaceFinder;
import Modules.Place;

public class SecondActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener, PlaceFinderListener{
    LinearLayout layoutOfPopup;
    PopupWindow popupMessage;
    Button popupButton, insidePopupButton;
    TextView popupText;

    private int truck_number;
    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private List<Marker> gasStationMarkers = new ArrayList<>();
    private Boolean gasStationSet;
    private ProgressDialog progressDialog;
    private LatLng truck_position = new LatLng(0,0);
    private LatLng start_position;
    private MarkerOptions start_marker_options;
    private Marker start_marker;
    private TextView truck_speeed;
    private Handler handler;
    private Runnable runnable;
    private List<Step> routes;
    private Route route;
    private Boolean ViewLoaded;
    private static final String URL_API = "https://api.thingspeak.com/channels/300455/feeds.json?api_key=41VM86KOB3I20J1R&results=2";
    private ArrayList<LatLng> track_points;
    private LatLng hard_location;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_view_bar, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.driver_info:
                Intent driver = new Intent(this, driver_info.class);
                driver.putExtra("truck_no",truck_number);
                startActivity(driver);
                break;
            case R.id.gas_button:
                if(gasStationSet){
                    for (Marker marker : gasStationMarkers) {
                        marker.remove();
                    }
                }
                else{
                    try {
                        new PlaceFinder(this, truck_position, "gas+station", 2000).execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.toll_road:
//                Intent toll = new Intent(SecondActivity.this, toll_cost_info.class);
//                toll.putExtra("truck_number",truck_number);
//                startActivity(toll);
                break;
            case R.id.tracking_history:
                Intent track_history = new Intent(SecondActivity.this, track_history.class);
                track_history.putExtra("truck_number",truck_number);
                startActivity(track_history);
                Log.i("bounds", Trucks.trucks.get(truck_number).bounds.toString());

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewLoaded = Boolean.FALSE;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Intent new_truck = getIntent();
        truck_number = new_truck.getExtras().getInt("truck_no");
        gasStationSet = Boolean.FALSE;


        sendRequest();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void sendRequest() {
        String origin = Trucks.trucks.get(truck_number).raw_source;
        String destination = Trucks.trucks.get(truck_number).raw_destination;
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

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
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }



    @Override
    public void onDirectionFinderSuccess(List<Step> routes2, Route route2, ArrayList<LatLng> tracking) {
        progressDialog.setMessage("Finding truck location...");
        routes = routes2;
        route = route2;

        Trucks.trucks.get(truck_number).tracking = tracking;

        if (route.error) {
            Toast.makeText(this, "Please enter valid address", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("check", Boolean.FALSE);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            truck_speeed = (TextView)findViewById(R.id.truck_speed);
            new UpdateLocation().execute(URL_API);
//
        }
    }

    private void generateView(){
        progressDialog.dismiss();
        if(ViewLoaded){
            truck_speeed.setText(Float.toString(Trucks.trucks.get(truck_number).speed));
            start_marker.setPosition(Trucks.trucks.get(truck_number).current_location);
        }
        else{
            polylinePaths = new ArrayList<>();
            originMarkers = new ArrayList<>();
            destinationMarkers = new ArrayList<>();
            truck_position = route.start_location;
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dest_marker))
                    .title(route.end_address)
                    .position(route.end_location));
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.source_marker))
                    .title(route.start_address)
                    .position(route.start_location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(route.bounds, 0));
            Trucks.trucks.get(truck_number).bounds = route.bounds;
            Trucks.trucks.get(truck_number).remaining_time = route.du.text;
            Trucks.trucks.get(truck_number).remaining_distance = route.di.text;

//            ((TextView) findViewById(R.id.tvDuration)).setText(route.du.text);
//            ((TextView) findViewById(R.id.tvDistance)).setText(route.di.text);
//            for (Step step : routes) {
//                PolylineOptions polylineOptions = new PolylineOptions().
//                        geodesic(true).
//                        color(Color.BLUE).
//                        width(10);
//
//                if (step.Toll) {
//                    polylineOptions.color(Color.RED);
//                }
//
//                for (int i = 0; i < step.points.size(); i++)
//                    polylineOptions.add(step.points.get(i));
//
//                polylinePaths.add(mMap.addPolyline(polylineOptions));
//            }


            PolylineOptions track_poly = new PolylineOptions().geodesic(true).color(Color.BLUE).width(10);
            int i=0;
            hard_location = new LatLng(0,0);
            while(i<Trucks.trucks.get(truck_number).tracking.size()){
                track_poly.add(Trucks.trucks.get(truck_number).tracking.get(i));
                if(i==(int)Trucks.trucks.get(truck_number).tracking.size()/2){
                    hard_location = Trucks.trucks.get(truck_number).tracking.get(i);
                }
                i = i+1;
            }

            Trucks.trucks.get(truck_number).current_location = hard_location;

            mMap.addPolyline(track_poly);

            truck_speeed.setText("25 km/hr");
            truck_speeed = (TextView)findViewById(R.id.truck_speed);
            start_marker_options = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.truck_marker))
                    .title(route.start_address)
                    .position(hard_location);
            start_position = route.start_location;
            start_marker = mMap.addMarker(start_marker_options);
            ViewLoaded = Boolean.TRUE;
//            TimerUpdate();
        }

    }

    public void TimerUpdate(){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                new UpdateLocation().execute(URL_API);
                handler.postDelayed(this, 2 * 1000);
            }
        };
        handler.postDelayed(runnable, 2 * 1000);
    }



    private class UpdateLocation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String URL_API = params[0];
            try {
                URL url = new URL(URL_API);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {

            try {
                parseJSON(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void parseJSON(String data) throws JSONException{

        JSONObject jsonData = new JSONObject(data);
        JSONArray feeds = jsonData.getJSONArray("feeds");
        JSONObject previous_data = feeds.getJSONObject(0);
        JSONObject current_data = feeds.getJSONObject(1);

        String lat = current_data.getString("field1");
        String lng = current_data.getString("field2");
        String speed = current_data.getString("field3");

        LatLng current_location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        Trucks.trucks.get(truck_number).current_location = current_location;
        Trucks.trucks.get(truck_number).speed = Float.parseFloat(speed);

        generateView();
    }


    @Override
    public void onBackPressed() {
//        handler.removeCallbacks(runnable);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("check",Boolean.TRUE);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onPlaceFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding places..!", true);

    }

    @Override
    public void onPlaceFinderSuccess(List<Place> places){
        progressDialog.dismiss();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(truck_position,14));

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.gas_station);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        for (Place place : places) {
            gasStationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    .title(place.name)
                    .position(place.location)));
        }
        gasStationSet = Boolean.TRUE;
    }




    public void center_truck(View view) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(hard_location));

    }

    public void driver_info(View view) {
        Intent driver = new Intent(this, driver_info.class);
        driver.putExtra("truck_no",truck_number);

        startActivity(driver);
    }
}

