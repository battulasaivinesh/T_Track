package com.itshareplus.googlemapdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Modules.Truck;
import Modules.Trucks;

/**
 * Created by Vinesh on 11/07/17.
 */

public class BackService extends Service {


    private LatLng current_location;
    private float truck_speed;

    private static final String URL_API = "https://api.thingspeak.com/channels/300455/feeds.json?api_key=41VM86KOB3I20J1R&results=2";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        if(data==null){
            return;
        }

        JSONObject jsonData = new JSONObject(data);
        JSONArray feeds = jsonData.getJSONArray("feeds");
        JSONObject previous_data = feeds.getJSONObject(0);
        JSONObject current_data = feeds.getJSONObject(1);

        String lat = current_data.getString("field1");
        String lng = current_data.getString("field2");
        String speed = current_data.getString("field3");

        current_location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        truck_speed = Float.parseFloat(speed);

        if(truck_speed>1){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(BackService.this)
                            .setSmallIcon(R.drawable.cast_ic_notification_0)
                            .setContentTitle("OverSpeed Alert")
                            .setContentText("Truck 1 is over speeding at" + truck_speed);

            Intent resultIntent = new Intent(BackService.this, new_truck.class);


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(BackService.this);

            stackBuilder.addParentStack(new_truck.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.i("Running","yes");
                new UpdateLocation().execute(URL_API);
                handler.postDelayed(this, 3 * 1000);

            }
        };
        handler.postDelayed(runnable, 3 * 1000);



        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
