package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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
import com.google.android.gms.wallet.LineItem;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Step;
import Modules.Truck;
import Modules.Trucks;

public class new_truck extends Activity{
    private int truck_number;
    private EditText source;
    private EditText dest;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_truck);
        Intent main = getIntent();
        truck_number = main.getExtras().getInt("truck_no");

    }

    public void Clicked(View view) {
        source = (EditText)findViewById(R.id.source_input);
        dest = (EditText)findViewById(R.id.dest_input);
        EditText name = (EditText)findViewById(R.id.name_input);
        EditText driver_name = (EditText)findViewById(R.id.driver_name_input);
        EditText driver_number = (EditText)findViewById(R.id.phone_input);

        if (source.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
        }
        else if (dest.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
        }
        else {

            Trucks.trucks.get(truck_number).raw_source = source.getText().toString();

            Trucks.trucks.get(truck_number).raw_destination = dest.getText().toString();

            Trucks.trucks.get(truck_number).Name = name.getText().toString();

            Trucks.trucks.get(truck_number).driver_name=  driver_name.getText().toString();

            Trucks.trucks.get(truck_number).driver_number = driver_number.getText().toString();

            Intent maps = new Intent(this, SecondActivity.class);
            maps.putExtra("truck_no", truck_number);
            startActivityForResult(maps,2);


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                TextView origin = (TextView)findViewById(R.id.dest_input);

                origin.setText(place.getName());


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.


            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        else if (requestCode == 3){
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                TextView origin = (TextView)findViewById(R.id.source_input);

                origin.setText(place.getName());


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.


            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        else if (requestCode == 2) {
            if(data.getExtras().getBoolean("check")){
                Intent resultIntent = new Intent();
                resultIntent.putExtra("truck", Boolean.TRUE);
                resultIntent.putExtra("truck_no",truck_number);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            else{
                source.setText("");
                dest.setText("");
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("truck",Boolean.FALSE);
        resultIntent.putExtra("truck_no",truck_number);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void pos_auto_search(int i) {
        int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
        if(i==1){
            PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;
        }
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void source_pos_auto_search(View view){
        pos_auto_search(1);
    }

    public void dest_pos_auto_search(View view){
        pos_auto_search(0);
    }


}
