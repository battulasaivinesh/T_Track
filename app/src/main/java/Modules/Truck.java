package Modules;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

/**
 * Created by Vinesh on 27/06/17.
 */

public class Truck {
    public String Name;
    public String raw_source;
    public String raw_destination;
    public Truck(String Name){
        this.Name = Name;
    }
    public LatLng current_location;
    public String driver_name;
    public String driver_number;
    public String remaining_time;
    public String remaining_distance;
    public float speed;
    public ArrayList<LatLng> tracking;
    public LatLngBounds bounds;
}
