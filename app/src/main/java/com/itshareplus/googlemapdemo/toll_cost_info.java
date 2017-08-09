package com.itshareplus.googlemapdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.test.suitebuilder.TestMethod;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.webkit.WebHistoryItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import java.util.Iterator;

import Modules.Truck;
import Modules.Trucks;

/**
 * Created by Vinesh on 15/07/17.
 */

public class toll_cost_info extends Activity {

    private int truck_number;
    private String origin;
    private String destination;
    private static final String DIRECTION_URL_API = "http://54.69.218.208/";
    TableLayout.LayoutParams tableParams;
    TableRow.LayoutParams rowParams;
    TableLayout tb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.toll_popup);

        Intent popup = getIntent();
        truck_number = popup.getExtras().getInt("truck_number");

        tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);


        tb = (TableLayout)findViewById(R.id.toll_table);

        origin = Trucks.trucks.get(truck_number).raw_source;
        destination = Trucks.trucks.get(truck_number).raw_destination;
        try{
            new get_toll_cost().execute(createUrl());
        }
        catch (UnsupportedEncodingException e){

        }


    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return DIRECTION_URL_API + urlOrigin + "_" + urlDestination;
    }

    private class get_toll_cost extends AsyncTask<String, Void, String> {

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
        JSONArray tolltable = jsonData.getJSONArray("tolltable");
        int i=0;
        while (i<tolltable.length()){
            JSONObject toll = tolltable.getJSONObject(i);
            Iterator<String> places = toll.keys();
            while (places.hasNext()){
                String key = (String) places.next();
                String cost = (String) toll.getString(key);
                TableRow row= new TableRow(this);
                row.setLayoutParams(rowParams);
                LinearLayout row_linear = new LinearLayout(this);
                row_linear.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextView key_text = new TextView(this);
                key_text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                key_text.setText(key);
                TextView cost_text =  new TextView(this);
                cost_text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                cost_text.setText(cost);
                row_linear.addView(key_text);
                row_linear.addView(cost_text);
                row.addView(row_linear);
                tb.addView(row);
            }
        }

    }
}
