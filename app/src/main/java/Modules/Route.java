package Modules;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
/**
 * Created by Vinesh on 27/06/17.
 */

public class Route {
    public String start_address;
    public String end_address;
    public LatLng start_location;
    public LatLng end_location;
    public Distance di;
    public Duration du;
    public LatLngBounds bounds;
    public boolean error;
}
