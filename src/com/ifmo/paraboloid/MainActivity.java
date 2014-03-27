package com.ifmo.paraboloid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {
    private TextView cityCountry;
    private TextView dateTime;
    private TextView temperature;
    private TextView wind;
    private TextView pressure;
    private TextView humidity;
    private ImageView image;
    private TextView sync;
    private SharedPreferences preferences;
    private SharedPreferences sp;
    private Calendar calendar;
    private SimpleDateFormat formatter;

    final int MENU_PREFS = 0;
    final int MENU_SYNC = 1;

    String getQuery(String city) {
        return "http://api.worldweatheronline.com/free/v1/weather.ashx?" +
                "q=" + city + "&format=xml&num_of_days=5&key=esxe5qwud6kphy4phrjygky2";
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (preferences.getString("sync", "").equals("")) {
            String city = sp.getString("list", "Sankt-Peterburg");
            new DownloadXmlTask().execute(getQuery(city));
        }
        calendar = Calendar.getInstance();
        formatter = new SimpleDateFormat("dd.MM k:mm");
        dateTime = (TextView) findViewById(R.id.dateTime);
        dateTime.setText(formatter.format(calendar.getTime()));
        cityCountry = (TextView) findViewById(R.id.cityCountry);
        cityCountry.setText(preferences.getString("cityCountry", ""));
        temperature = (TextView) findViewById(R.id.temperature);
        temperature.setText(preferences.getString("temperature", ""));
        wind = (TextView) findViewById(R.id.wind);
        wind.setText(preferences.getString("wind", ""));
        pressure = (TextView) findViewById(R.id.pressure);
        pressure.setText(preferences.getString("pressure", ""));
        humidity = (TextView) findViewById(R.id.humidity);
        humidity.setText(preferences.getString("humidity", ""));
        sync = (TextView) findViewById(R.id.sync);
        sync.setText(preferences.getString("sync", ""));
        if (preferences.getString("sync", "").equals("")) {
            sync.setText("syncing...");
        }
        image = (ImageView) findViewById(R.id.image);
        String previouslyEncodedImage = preferences.getString("image", "");
        if (!previouslyEncodedImage.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            image.setImageBitmap(bitmap);
        }
        sync = (TextView) findViewById(R.id.sync);
        Weather[] weatherData = new Weather[5];
        Bitmap bitmap = null;
        for (int i = 0; i < 5; i++) {
            previouslyEncodedImage = preferences.getString("image" + i, "");
            if (!previouslyEncodedImage.equalsIgnoreCase("")) {
                byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            }
            weatherData[i] = new Weather(preferences.getString("date" + i, ""),
                    preferences.getString("temperature" + i, ""),
                    bitmap,
                    preferences.getString("wind" + i, ""));
        }
        WeatherAdapter adapter = new WeatherAdapter(this, weatherData);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        sync.setText("syncing...");
        String city = sp.getString("list", "Sankt-Peterburg");
        new DownloadXmlTask().execute(getQuery(city));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SYNC, 0, "Sync");
        MenuItem menuItem = menu.add(0, MENU_PREFS, 0, "Preferences");
        menuItem.setIntent(new Intent(this, SettingsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SYNC) {
            sync.setText("syncing...");
            String city = sp.getString("list", "Sankt-Peterburg");
            new DownloadXmlTask().execute(getQuery(city));
        }
        return super.onOptionsItemSelected(item);
    }

    private static class Data {
        final WeatherParser.WeatherData weatherData;
        final Bitmap bitmap;

        public Data(WeatherParser.WeatherData weatherData, Bitmap bitmap) {
            this.weatherData = weatherData;
            this.bitmap = bitmap;
        }
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, Data> {
        @Override
        protected Data doInBackground(String... urls) {
            Data result = null;
            try {
                result = loadXmlFromNetwork(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Data result) {
            setContentView(R.layout.main);
            preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
            calendar = Calendar.getInstance();
            formatter = new SimpleDateFormat("dd.MM k:mm");
            cityCountry = (TextView) findViewById(R.id.cityCountry);
            cityCountry.setText(result.weatherData.query);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("cityCountry", cityCountry.getText().toString());
            dateTime = (TextView) findViewById(R.id.dateTime);
            dateTime.setText(formatter.format(calendar.getTime()));
            temperature = (TextView) findViewById(R.id.temperature);
            String degree = sp.getString("degree", "Celsius");
            if (degree.equals("Celsius")) {
                temperature.setText(result.weatherData.currentCondition.tempC + "°C");
            } else {
                temperature.setText(result.weatherData.currentCondition.tempF + "°F");
            }
            editor.putString("temperature", temperature.getText().toString());
            wind = (TextView) findViewById(R.id.wind);
            String distance = sp.getString("distance", "Kilometers");
            String windBegin;
            if (distance.equals("Miles")) {
                windBegin = result.weatherData.currentCondition.windspeedMiles + " miles per hour, ";
            } else {
                windBegin = result.weatherData.currentCondition.windspeedKmph + " km per hour, ";
            }
            wind.setText("wind " + windBegin + result.weatherData.currentCondition.winddir16Point);
            editor.putString("wind", wind.getText().toString());
            pressure = (TextView) findViewById(R.id.pressure);
            pressure.setText("pressure " + result.weatherData.currentCondition.pressure + " millibars");
            editor.putString("pressure", pressure.getText().toString());
            humidity = (TextView) findViewById(R.id.humidity);
            humidity.setText("humidity " + result.weatherData.currentCondition.humidity + "%");
            editor.putString("humidity", humidity.getText().toString());
            image = (ImageView) findViewById(R.id.image);
            image.setImageBitmap(result.bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            result.bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            editor.putString("image", encodedImage);
            sync = (TextView) findViewById(R.id.sync);
            sync.setText("last sync: " + formatter.format(calendar.getTime()));
            editor.putString("sync", sync.getText().toString());
            editor.commit();
            Weather weatherData[] = new Weather[5];
            Bitmap bitmap = null;
            for (int i = 0; i < 5; i++) {
                String previouslyEncodedImage = preferences.getString("image" + i, "");
                if (!previouslyEncodedImage.equalsIgnoreCase("")) {
                    b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                }
                weatherData[i] = new Weather(preferences.getString("date" + i, ""),
                        preferences.getString("temperature" + i, ""),
                        bitmap,
                        preferences.getString("wind" + i, ""));
            }

            WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, weatherData);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        }
    }

    private Data loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        WeatherParser weatherParser = new WeatherParser();
        WeatherParser.WeatherData weatherData = null;
        try {
            stream = downloadUrl(urlString);
            weatherData = weatherParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Bitmap bitmap = null;
        try {
            stream = downloadUrl(weatherData.currentCondition.weatherIconUrl);
            bitmap = BitmapFactory.decodeStream(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < weatherData.futureConditions.length; i++) {
            editor.putString("date" + i, String.valueOf(weatherData.futureConditions[i].date.charAt(8)) +
                    weatherData.futureConditions[i].date.charAt(9) + "." +
                    weatherData.futureConditions[i].date.charAt(5) +
                    weatherData.futureConditions[i].date.charAt(6));
            String degree = sp.getString("degree", "Celsius");
            if (degree.equals("Celsius")) {
                editor.putString("temperature" + i, weatherData.futureConditions[i].tempMinC + "..." +
                        weatherData.futureConditions[i].tempMaxC + "°C");
            } else {
                editor.putString("temperature" + i, weatherData.futureConditions[i].tempMinF + "..." +
                        weatherData.futureConditions[i].tempMaxF + "°F");
            }
            String distance = sp.getString("distance", "Kilometers");
            if (distance.equals("Miles")) {
                editor.putString("wind" + i, weatherData.futureConditions[i].windspeedMiles + " miles per hour, " +
                        weatherData.futureConditions[i].winddir16Point);
            } else {
                editor.putString("wind" + i, weatherData.futureConditions[i].windspeedKmph + " km per hour, " +
                        weatherData.futureConditions[i].winddir16Point);
            }
            try {
                stream = downloadUrl(weatherData.futureConditions[i].weatherIconUrl);
                bitmap = BitmapFactory.decodeStream(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            editor.putString("image" + i, encodedImage);
        }
        editor.commit();
        return new Data(weatherData, bitmap);
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    public class Weather {
        public final String date;
        public final String temperature;
        public final Bitmap image;
        public final String wind;

        public Weather(String date, String temperature, Bitmap image, String wind) {
            this.date = date;
            this.temperature = temperature;
            this.image = image;
            this.wind = wind;
        }
    }

    public class WeatherAdapter extends ArrayAdapter<Weather> {
        final Context context;
        final int layoutResourceId;
        Weather data[] = null;

        public WeatherAdapter(Context context, Weather[] data) {
            super(context, R.layout.list_item, data);
            this.layoutResourceId = R.layout.list_item;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            WeatherHolder holder;
            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new WeatherHolder();
                holder.date = (TextView) row.findViewById(R.id.date);
                holder.temperature = (TextView) row.findViewById(R.id.temperature);
                holder.image = (ImageView) row.findViewById(R.id.image);
                holder.wind = (TextView) row.findViewById(R.id.wind);
                row.setTag(holder);
            } else {
                holder = (WeatherHolder)row.getTag();
            }
            Weather weather = data[position];
            holder.date.setText(weather.date);
            holder.image.setImageBitmap(weather.image);
            holder.temperature.setText(weather.temperature);
            holder.wind.setText(weather.wind);
            return row;
        }

        class WeatherHolder {
            TextView date;
            TextView temperature;
            ImageView image;
            TextView wind;
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}