package com.example.bogdan.weatherapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetWeather {

    //получает значение температуры из сети
    public static String GetWeatherJSON (String city) {
        HttpURLConnection httpURLConnection;
        BufferedReader bufferedReader;
        String temperature = "no data";
        String resultJson;

        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city
                    + "&units=metric");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            resultJson = stringBuilder.toString();
            try {
                JSONObject jsonObject = new JSONObject(resultJson);
                JSONObject main = jsonObject.getJSONObject("main");
                temperature = String.format("%.1f",
                        Double.parseDouble(main.getString("temp")));
            } catch (JSONException e) {
                e.printStackTrace();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temperature;
    }

    //получает строку, содержащую погодные показатели для текущего дня
    public static String GetFullWeatherJSON (String city) {
        HttpURLConnection httpURLConnection;
        BufferedReader bufferedReader;
        String resultJson = "no data";

        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city
            +"&units=metric");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            resultJson = stringBuilder.toString();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultJson;
    }


    //получает строку, содержащую прогнозные показатели на int days дней
    public static String GetFullForecastJSON (String city, int days) {
        HttpURLConnection httpURLConnection;
        BufferedReader bufferedReader;
        String resultJson = "no data";

        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city +
            "&units=metric&cnt=" + days);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            resultJson = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultJson;
    }
}
