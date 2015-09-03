package com.example.bogdan.weatherapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

//Fragment для отображения показателей погоды
public class WeatherFragment extends Fragment {
    TextView tempView, humidityView, pressureView, cloudsView, dateView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_fragment, container, false);
        tempView = (TextView)view.findViewById(R.id.tvTempValue);
        humidityView = (TextView)view.findViewById(R.id.tvHumidityValue);
        pressureView = (TextView)view.findViewById(R.id.tvPressureValue);
        cloudsView = (TextView)view.findViewById(R.id.tvCloudsValue);
        dateView = (TextView)view.findViewById(R.id.tvDateValue);
        return view;
    }

    //методы, устанавливающие погодные показатели в соответствующие TextView фрагмента
    public void setTemp (String temp) {
        tempView.setText(temp);
    }

    public void setHumidity (String humidity) {
        humidityView.setText(humidity);
    }

    public void setPressure (String pressure) {
        pressureView.setText(pressure);
    }

    public void setClouds (String clouds) {
        cloudsView.setText(clouds);
    }

    public void setDate (String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = dateFormat.format(Long.parseLong(date)*1000);
        dateView.setText(date);
    }
}
