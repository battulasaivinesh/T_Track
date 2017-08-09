package Modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import Modules.Place;
import Modules.PlaceFinderListener;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class PlaceFinder {
    private static final String PlACES_URL_API = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBemzB0Mcyi3U4-TwuXwFHYFhYaSURlyeU";
    private PlaceFinderListener listener;
    private LatLng current_location;
    private String type;
    private int radius;

    public PlaceFinder(PlaceFinderListener listener, LatLng current_location, String type, int radius) {
        this.listener = listener;
        this.current_location = current_location;
        this.type = type;
        this.radius = radius;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onPlaceFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String type2 = URLEncoder.encode(type, "utf-8");
        return PlACES_URL_API    + "location=" + current_location.latitude+","+ current_location.longitude + "&query=" + type2 + "&radius=" + radius  +"&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
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
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Place> places = new ArrayList<Place>();

        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonResults = jsonData.getJSONArray("results");
        for (int i = 0; i < jsonResults.length(); i++) {
            JSONObject jsonPlace = jsonResults.getJSONObject(i);

            Place place = new Place();
            JSONObject geometry = jsonPlace.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            String name = jsonPlace.getString("name");

            place.location = new LatLng(location.getDouble("lat"),location.getDouble("lng"));
            place.name = name;

            places.add(place);

        }

        listener.onPlaceFinderSuccess(places);
    }



}
