package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.AppCompatTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.daimajia.swipe.SwipeLayout;
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
import com.google.android.gms.wallet.LineItem;
import com.itshareplus.googlemapdemo.BackService;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Step;
import Modules.Truck;
import Modules.Trucks;


public class MapsActivity extends AppCompatActivity {

    private List<SwipeLayout> truck_buttons = new ArrayList<SwipeLayout>();

    private Truck truck;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_add_truck:
                if(!isNetworkConnected()){
                    Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
                else{
                    int i = Trucks.trucks.size();
                    truck = new Truck("Truck " + i);

                    Trucks.trucks.add(truck);

                    Intent add_truck = new Intent(this,new_truck.class);
                    add_truck.putExtra("truck_no",i);
                    startActivityForResult(add_truck,1);
                    break;
                }
            case R.id.menu_view_all:
                if(!isNetworkConnected()){
                    Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(Trucks.trucks.isEmpty()){
                        Toast.makeText(this, "No trucks to show!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent view_all = new Intent(this, ViewAll.class);
                        startActivity(view_all);
                    }
                }


        }
        return true;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
//        startService(new Intent(this, BackService.class));

        Trucks.trucks.clear();

        BufferedReader input = null;
        File file = null;
        try {
            file = new File(getFilesDir(), "MyFile"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);

                String[] parts = line.split("_");
                truck = new Truck("");
                truck.Name = parts[0];
                truck.raw_source = parts[1];
                truck.raw_destination = parts[2];
                truck.driver_name = parts[3];
                truck.driver_number = parts[4];
                LatLng location = new LatLng(Double.parseDouble(parts[5]),Double.parseDouble(parts[6]));
                truck.remaining_time = parts[7];
                truck.remaining_distance = parts[8];
                truck.current_location = location;

                Trucks.trucks.add(truck);
                add_button(Trucks.trucks.size()-1);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

        String fileName = "MyFile";
        String content = "";
        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            for (Truck t : Trucks.trucks) {
                content = t.Name+"_"+t.raw_source+"_"+t.raw_destination+"_"+t.driver_name+"_"+t.driver_number+
                        "_"+t.current_location.latitude+"_"+t.current_location.longitude+"_"+t.remaining_time+
                        "_"+t.remaining_distance+"\n";
                outputStream.write(content.getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // TODO Extract the data returned from the child Activity.
            String returnValue = data.getStringExtra("some_key");
            int truck_number = data.getExtras().getInt("truck_no");
            if(data.getExtras().getBoolean("truck")){
                add_button(truck_number);
            }
            else{
                Trucks.trucks.remove(Trucks.trucks.size()-1);
            }
        }
        else if(requestCode == 2){

        }

    }

    public void add_button(int truck_number){
        LinearLayout ll = (LinearLayout)findViewById(R.id.truck_menu);

        SwipeLayout truck_button_swipe = new SwipeLayout(this);
        truck_button_swipe.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout truck_photo = new LinearLayout(this);
        LinearLayout truck_button_back = new LinearLayout(this);

        truck_button_back.setLayoutParams(
                new LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT));

        final ImageButton delete_truck = new ImageButton(this);
        delete_truck.setImageResource(R.drawable.delete_truck_icon);
        delete_truck.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        delete_truck.setBackgroundColor(0xFFe02f26);
        delete_truck.setTag("delete_button");
        delete_truck.setId(truck_number);

        delete_truck.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                delete_truck(v.getId());
            }
        });


        truck_button_back.addView(delete_truck);
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xFFFFFFFF);
        border.setCornerRadius(3);
        border.setStroke(3,0xFF000000);
        truck_photo.setBackground(border);

        truck_photo.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        truck_photo.setBackgroundColor(getResources().getColor(R.color.light_grey));
        truck_photo.setPadding(15,15,30,15);
        truck_photo.setId(truck_number);
        truck_photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawMap(v.getId());
            }
        });

        View image_border = new View(this);
        image_border.setLayoutParams(new ViewGroup.LayoutParams(5, ViewGroup.LayoutParams.MATCH_PARENT));
        image_border.setBackgroundColor(Color.BLACK);


        ImageView truck_icon = new ImageView(this);
        truck_icon.setImageResource(R.mipmap.truck_button_icon);
        truck_icon.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        truck_icon.setPadding(20,0,20,0);
        truck_photo.addView(truck_icon);
        truck_photo.addView(image_border);
        truck_photo.setTag("truck_button_top");

        LinearLayout truck_button_parent = new LinearLayout(this);

        truck_button_parent.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        truck_button_parent.setOrientation(LinearLayout.VERTICAL);
        truck_button_parent.setPadding(20,0,10,0);

        TextView truck_name = new TextView(this);
        truck_name.setTextColor(Color.BLACK);
        truck_name.setText(Trucks.trucks.get(truck_number).Name);
        truck_name.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        truck_name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        truck_name.setTextSize(20);

        ProgressBar truck_progress = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        truck_progress.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        truck_progress.setProgress(50);

        LinearLayout truck_info = new LinearLayout(this);
        truck_info.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        truck_info.setOrientation(LinearLayout.HORIZONTAL);
        truck_info.setPadding(5,5,5,0);

        ImageView clock = new ImageView(this);
        clock.setImageResource(R.drawable.clock);
        clock.setLayoutParams(new ViewGroup.LayoutParams((int)getResources().getDimension(R.dimen.clock_size),(int)getResources().getDimension(R.dimen.clock_size)));
        clock.setPadding(0,0,5,0);

        TextView truck_time = new TextView(this);
        truck_time.setText(Trucks.trucks.get(truck_number).remaining_time);
        truck_time.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        truck_time.setPadding(0,0,5,0);

        ImageView distance = new ImageView(this);
        distance.setImageResource(R.drawable.distance);
        distance.setLayoutParams(new ViewGroup.LayoutParams((int)getResources().getDimension(R.dimen.clock_size),(int)getResources().getDimension(R.dimen.clock_size)));
        distance.setPadding(0,0,5,0);

        TextView truck_distance = new TextView(this);
        truck_distance.setText(Trucks.trucks.get(truck_number).remaining_distance);
        truck_distance.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        truck_distance.setPadding(0,0,5,0);

        truck_info.addView(clock);
        truck_info.addView(truck_time);
        truck_info.addView(distance);
        truck_info.addView(truck_distance);
        truck_button_parent.addView(truck_name);
        truck_button_parent.addView(truck_progress);
        truck_button_parent.addView(truck_info);

        truck_photo.addView(truck_button_parent);

        truck_button_swipe.addView(truck_button_back);
        truck_button_swipe.addView(truck_photo);

        truck_button_swipe.setShowMode(SwipeLayout.ShowMode.LayDown);
        truck_button_swipe.addDrag(SwipeLayout.DragEdge.Left, truck_button_back);

        truck_buttons.add(truck_button_swipe);
        ll.addView(truck_button_swipe);
    }

    public void delete_truck(int delete_number){
        LinearLayout ll = (LinearLayout)findViewById(R.id.truck_menu);


        Trucks.trucks.remove(delete_number);

        ll.removeView(truck_buttons.get(delete_number));
        int i = delete_number + 1;
        while (i < truck_buttons.size()){
            LinearLayout button = (LinearLayout)truck_buttons.get(i).findViewWithTag("truck_button_top");
            ImageButton del_button = (ImageButton)truck_buttons.get(i).findViewWithTag("delete_button");

            int previd2 = del_button.getId();
            del_button.setId(previd2-1);
            int prevId = button.getId();
            button.setId(prevId-1);

            i = i+ 1;
        }
        truck_buttons.remove(delete_number);

    }


    public void drawMap(int truck_no){
        if(!isNetworkConnected()){
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent map = new Intent(this, SecondActivity.class);
            map.putExtra("truck_no",truck_no);
            startActivityForResult(map,2);
        }

    }


    public void view_all(View view) {
        if(Trucks.trucks.isEmpty()){
            Toast.makeText(this, "No trucks to show!", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent view_all = new Intent(this, ViewAll.class);
            startActivity(view_all);
        }

    }
}
