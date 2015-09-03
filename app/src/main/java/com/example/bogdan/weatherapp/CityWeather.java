package com.example.bogdan.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

//активити, отображающее текущую  погоду в выбранном городе

public class CityWeather extends Activity {
    private String cityName;
    private String json;
    private WeatherFragment weatherFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_discription);
        weatherFragment = (WeatherFragment)getFragmentManager().findFragmentById(R.id.fragment);
        Intent intent = getIntent();
        cityName = intent.getStringExtra(MainActivity.CITY_NAME);
        Thread downloadWeatherThread = new Thread(){
            public void run (){
                json = GetWeather.GetFullWeatherJSON(cityName);
            }
        };
        downloadWeatherThread.start();
        try {
            downloadWeatherThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences;
        /*если при обращении к json не было получено json объекта (нет подключенния к сети),
        то берем данные из SharedPreferences*/
        if (json.equals("no data")) {
            sharedPreferences = getSharedPreferences(MainActivity.APP_PREFERENCES, MODE_PRIVATE);
            json = sharedPreferences.getString(cityName, "");
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            String temp = String.format("%.1f",
                    Double.parseDouble(jsonObject.getJSONObject("main").getString("temp")));
            weatherFragment.setTemp(temp + " C");
            String pressure = String.format("%.2f",
                    Double.parseDouble(jsonObject.getJSONObject("main").getString("pressure"))
                            * 0.75006375541921);
            weatherFragment.setPressure(pressure + " mmHg");
            String humidity = jsonObject.getJSONObject("main").getString("humidity");
            weatherFragment.setHumidity(humidity + "%");
            String clouds = jsonObject.getJSONObject("clouds").getString("all");
            weatherFragment.setClouds(clouds);
            String date = jsonObject.getString("dt");
            weatherFragment.setDate(date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*сохранение json в SharedPreferences*/
        sharedPreferences = getSharedPreferences(MainActivity.APP_PREFERENCES, MODE_PRIVATE);
        sharedPreferences.edit().putString(cityName, json).apply();
    }

    public void threeDaysClick(View view) {
        Intent intent = new Intent(CityWeather.this, ThreeDaysWeather.class);
        intent.putExtra(MainActivity.CITY_NAME, cityName);
        startActivity(intent);
    }

    public void sevenDaysClick(View view) {
        Intent intent = new Intent(CityWeather.this, SevenDaysWeather.class);
        intent.putExtra(MainActivity.CITY_NAME, cityName);
        startActivity(intent);
    }
}