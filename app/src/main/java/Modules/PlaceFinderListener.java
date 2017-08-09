package Modules;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import Modules.Place;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public interface PlaceFinderListener {
    void onPlaceFinderStart();
    void onPlaceFinderSuccess(List<Place> places);
}
