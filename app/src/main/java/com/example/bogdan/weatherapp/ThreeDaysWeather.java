package com.example.bogdan.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//активити, отображающее прогноз на 3 дня
public class ThreeDaysWeather extends Activity {
    private String cityName;
    private String json;
    private ArrayList<View> fragmentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.three_days_layout);
        Intent intent = getIntent();
        cityName = intent.getStringExtra(MainActivity.CITY_NAME);
        Thread downloadWeatherThread = new Thread(){
            public void run (){
                json = GetWeather.GetFullForecastJSON(cityName, 3);
            }
        };
        downloadWeatherThread.start();
        try {
            downloadWeatherThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("myLogs", json);
        fragmentsList = new ArrayList<>();
        LinearLayout layout = (LinearLayout)findViewById(R.id.linear_layout);
        Log.d("myLogs"," size of layout = " + String.valueOf(layout.getChildCount()));
        for (int i = 0; i < layout.getChildCount(); i++) {
            fragmentsList.add(layout.getChildAt(i));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences;
        if (json.equals("no data")){
            sharedPreferences = getSharedPreferences(MainActivity.APP_PREFERENCES, MODE_PRIVATE);
            json = sharedPreferences.getString(cityName + "_3", "");
        }
        try {
            JSONObject jsonData = new JSONObject(json);
            JSONArray jsonArray = jsonData.getJSONArray("list");
            for (int i = 0; i < 3; i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                WeatherFragment weatherFragment = (WeatherFragment) getFragmentManager()
                        .findFragmentById(fragmentsList.get(i).getId());
                weatherFragment.setTemp(String.format("%.1f",
                        Double.parseDouble(jsonObject.getJSONObject("temp").getString("day")))
                        + " C");
                weatherFragment.setClouds(jsonObject.getString("clouds"));
                weatherFragment.setPressure((String.format("%.2f",
                        Double.parseDouble(jsonObject.getString("pressure")) * 0.75006375541921))
                        + " mmHg");
                weatherFragment.setHumidity(jsonObject.getString("humidity") + "%");
                weatherFragment.setDate(jsonObject.getString("dt"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        sharedPreferences = getSharedPreferences(MainActivity.APP_PREFERENCES, MODE_PRIVATE);
        sharedPreferences.edit().putString(cityName + "_3", json).apply();
    }
}
