package Modules;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

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
import java.util.regex.Pattern;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyBemzB0Mcyi3U4-TwuXwFHYFhYaSURlyeU";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private ArrayList<LatLng> tracking = new ArrayList<>();

    public DirectionFinder(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
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

        List<Step> routes = new ArrayList<Step>();
        JSONObject jsonData = new JSONObject(data);
        Route route = new Route();
        Log.i("check-status",jsonData.getString("status"));

        if (!jsonData.getString("status").toString().equals("OK")){
            Log.i("input_error","yes");
            route.error = Boolean.TRUE;
        }
        else {
            Log.i("input_error","no");
            route.error = Boolean.FALSE;
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for (int i = 0; i < jsonRoutes.length(); i++) {

                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                JSONObject bounds_map = jsonRoute.getJSONObject("bounds");
                JSONObject southwest = bounds_map.getJSONObject("southwest");
                JSONObject northeast = bounds_map.getJSONObject("northeast");

                LatLng southwest_l = new LatLng(southwest.getDouble("lat")-0.5, southwest.getDouble("lng")-0.5);
                LatLng northeast_l = new LatLng(northeast.getDouble("lat")+0.5, northeast.getDouble("lng")+0.5);
                route.bounds = new LatLngBounds(southwest_l, northeast_l);
                JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                JSONArray jsonSteps = jsonLeg.getJSONArray("steps");
                route.start_address = jsonLeg.getString("start_address");
                route.end_address = jsonLeg.getString("end_address");
                JSONObject start = jsonLeg.getJSONObject("start_location");
                JSONObject end = jsonLeg.getJSONObject("end_location");
                route.start_location = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
                route.end_location = new LatLng(end.getDouble("lat"), end.getDouble("lng"));
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                route.di = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                route.du = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                for (int j = 0; j < jsonSteps.length(); j++) {
                    Step step = new Step();

                    JSONObject jsonStep = jsonSteps.getJSONObject(j);

                    JSONObject jsonEndLocation = jsonStep.getJSONObject("end_location");
                    JSONObject jsonStartLocation = jsonStep.getJSONObject("start_location");
                    JSONObject polyline = jsonStep.getJSONObject("polyline");

                    step.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                    step.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                    step.points = decodePolyLine(polyline.getString("points"));

                    int k;
                    LatLng new_cord;
                    for(k=0;k<step.points.size();k++){
                        tracking.add(step.points.get(k));
                    }

                    if (jsonStep.getString("html_instructions").toLowerCase().contains("Toll".toLowerCase())) {
                        step.Toll = Boolean.TRUE;
                    } else {
                        step.Toll = Boolean.FALSE;
                    }
                    routes.add(step);
                }

            }
        }

        listener.onDirectionFinderSuccess(routes,route, tracking);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
