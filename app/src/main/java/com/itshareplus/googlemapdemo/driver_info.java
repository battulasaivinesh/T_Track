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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Step;
import Modules.Truck;
import Modules.Trucks;


public class driver_info extends Activity{

    private int truck_number;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_info_layout);

        Intent new_truck = getIntent();
        truck_number = new_truck.getExtras().getInt("truck_no");

        EditText driver_name = (EditText)findViewById(R.id.driver_name);
        EditText contact = (EditText)findViewById(R.id.phone_number);

        driver_name.setText(Trucks.trucks.get(truck_number).driver_name);
        contact.setText(Trucks.trucks.get(truck_number).driver_number);
    }

    public void call_driver(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);

        callIntent.setData(Uri.parse("tel:" + Trucks.trucks.get(truck_number).driver_number));

        if (ActivityCompat.checkSelfPermission(driver_info.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startActivity(callIntent);
    }
}
