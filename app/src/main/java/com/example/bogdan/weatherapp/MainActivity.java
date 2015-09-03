package com.example.bogdan.weatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//главное Activity, отображающее список городов и температур в них
public class MainActivity extends Activity {
    public static final String APP_PREFERENCES = "app_preferences";
    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;
    private static final String CITY_TABLE = "myTable";
    public static final String CITY_NAME = "city";
    public static final String CITY_TEMP = "temp";
    private static final String DB_CREATE = "create table " + CITY_TABLE + "("
             + CITY_NAME + " text, " + CITY_TEMP + " text" + ")";
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Map<String, String> cityMap;
    private SimpleAdapter adapter;
    private ArrayList<Map<String,String>> data;
    private String temp, tempInDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<>();
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        final Cursor c =  db.query(CITY_TABLE, null, null, null, null, null, null);
        //если база данных (sql) не пуста, то заполняем на ее основании список городов
        if (c.moveToFirst()){
            final int cityColIndex = c.getColumnIndex(CITY_NAME);
            final int tempColIndex = c.getColumnIndex(CITY_TEMP);
            do {
                cityMap = new HashMap<String, String>();
                cityMap.put(CITY_NAME, c.getString(cityColIndex));
                //вытягивает значение температуры из json
                Thread downloadTempThread = new Thread(){
                    public void run (){
                        temp = GetWeather.GetWeatherJSON(c.getString(cityColIndex));
                    }
                };
                downloadTempThread.start();
                try {
                    downloadTempThread.join();//ожидание завершения парсинга json
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*если нет подключения к сети, то берется значение из sql, сохраненное при крайнем
                парсинге json*/
                if (temp.equals("no data")){
                    temp = c.getString(tempColIndex);
                }
                cityMap.put("weather", temp);
                updateCityInDb(c.getString(cityColIndex), temp);
                data.add(cityMap);
            } while (c.moveToNext());

        }
        c.close();
        db.close();
        //заполнение ListView с помощью SimpleAdapter
        ListView listView = (ListView) findViewById(R.id.listView);
        String [] from = {CITY_NAME, "weather"};
        int [] to = {R.id.cityTextView, R.id.temperatureTextView};
        adapter = new SimpleAdapter(this, data, R.layout.list_item, from, to);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final String cityName = (data.get(arg2).get(CITY_NAME));
                Intent intent = new Intent(MainActivity.this, CityWeather.class);
                intent.putExtra(CITY_NAME, cityName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //добавление пункта  в контектное меню (удалить город из списка)
        menu.add(0, 1, 0, "Delete from list");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //обработка клика на контекстном меню:
        if (item.getItemId() == 1){
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            String cityToDelete =(data.get(acmi.position).get(CITY_NAME));
            db = dbHelper.getWritableDatabase();
            //удаление города из sql
            db.delete(CITY_TABLE, CITY_NAME + " = " + "'" + cityToDelete + "'", null);
            db.close();
            data.remove(acmi.position);
            adapter.notifyDataSetChanged();
            SharedPreferences sharedPreferences = getSharedPreferences
                    (APP_PREFERENCES, MODE_PRIVATE);
            //удаление города (json) из sharedPreferences
            sharedPreferences.edit().remove(cityToDelete).apply();
            sharedPreferences.edit().remove(cityToDelete + "_3").apply();
            sharedPreferences.edit().remove(cityToDelete + "_7").apply();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            //создагие AlertDialog для добавления города
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Type City here:");
            final EditText editCity = new EditText(this);
            dialog.setView(editCity);
            dialog.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final String enteredCity = editCity.getText().toString();
                    Boolean cityNotExist = true;
                    /*еслили введенного города еще нет в списке, то добавляем его в sql
                     и ListView*/
                    for (Map<String, String> map : data) {
                        if (map.containsValue(enteredCity)) {
                            cityNotExist = false;
                            break;
                        }
                    }
                    if (cityNotExist) {
                        Thread downloadTempThread = new Thread() {
                            public void run() {
                                tempInDb = GetWeather.GetWeatherJSON(enteredCity);
                            }
                        };
                        downloadTempThread.start();
                        try {
                            downloadTempThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!tempInDb.equals("no data")) {
                            cityMap = new HashMap<String, String>();
                            cityMap.put(CITY_NAME, enteredCity);
                            cityMap.put("weather", tempInDb);
                            data.add(cityMap);
                            adapter.notifyDataSetChanged();
                            putCityInDb(enteredCity, tempInDb);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No such city in our base(or maybe no connection)", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "City is already in your list", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //при создании базы, добавляются города "по умолчанию"
            db.execSQL(DB_CREATE);
            ContentValues startCv = new ContentValues();
            startCv.put(CITY_NAME, "St.Petersburg");
            db.insert(CITY_TABLE, null, startCv);
            startCv.put(CITY_NAME, "Moscow");
            db.insert(CITY_TABLE, null, startCv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    //добавляет город в sql
    private void putCityInDb (String city, String temp) {
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CITY_NAME, city);
        cv.put(CITY_TEMP, temp);
        db.insert(CITY_TABLE, null, cv);
        db.close();
    }

    //обновляет значение температуры в городе в sql
    private void updateCityInDb (String city, String temp) {
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CITY_NAME, city);
        cv.put(CITY_TEMP, temp);
        db.update(CITY_TABLE, cv, CITY_NAME + " = ?", new String[]{city});
        db.close();
    }
}
