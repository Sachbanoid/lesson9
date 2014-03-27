package com.ifmo.paraboloid;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class WeatherParser {
    private static final String ns = null;

    public WeatherData parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readWeatherData(parser);
        } finally {
            in.close();
        }
    }

    private WeatherData readWeatherData(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "data");
        String query = null;
        CurrentCondition currentCondition = null;
        FutureCondition[] futureConditions = new FutureCondition[5];
        int index = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("request")) {
                query = readRequest(parser);
            } else if (name.equals("current_condition")) {
                currentCondition = readCurrentCondition(parser);
            } else if (name.equals("weather")) {
                futureConditions[index] = readFutureCondition(parser);
                index++;
            } else {
                skip(parser);
            }
        }
        return new WeatherData(query, currentCondition, futureConditions);
    }

    public static class WeatherData {
        public final String query;
        public final CurrentCondition currentCondition;
        public final FutureCondition[] futureConditions;

        private WeatherData(String query, CurrentCondition currentCondition, FutureCondition[] futureConditions) {
            this.query = query;
            this.currentCondition = currentCondition;
            this.futureConditions = futureConditions;
        }
    }

    public static class CurrentCondition {
        public final String tempC;
        public final String tempF;
        public final String weatherIconUrl;
        public final String windspeedMiles;
        public final String windspeedKmph;
        public final String winddir16Point;
        public final String humidity;
        public final String pressure;

        private CurrentCondition(String tempC, String tempF, String weatherIconUrl,
                                 String windspeedMiles, String windspeedKmph, String winddir16Point,
                                 String humidity, String pressure) {
            this.tempC = tempC;
            this.tempF = tempF;
            this.weatherIconUrl = weatherIconUrl;
            this.windspeedMiles = windspeedMiles;
            this.windspeedKmph = windspeedKmph;
            this.winddir16Point = winddir16Point;
            this.humidity = humidity;
            this.pressure = pressure;
        }
    }

    public static class FutureCondition {
        public final String date;
        public final String tempMaxC;
        public final String tempMaxF;
        public final String tempMinC;
        public final String tempMinF;
        public final String weatherIconUrl;
        public final String windspeedMiles;
        public final String windspeedKmph;
        public final String winddir16Point;

        private FutureCondition(String date, String tempMaxC, String tempMaxF, String tempMinC, String tempMinF,
                                String weatherIconUrl, String windspeedMiles,
                                String windspeedKmph, String winddir16Point) {
            this.date = date;
            this.tempMaxC = tempMaxC;
            this.tempMaxF = tempMaxF;
            this.tempMinC = tempMinC;
            this.tempMinF = tempMinF;
            this.weatherIconUrl = weatherIconUrl;
            this.windspeedMiles = windspeedMiles;
            this.windspeedKmph = windspeedKmph;
            this.winddir16Point = winddir16Point;
        }
    }

    private String readRequest(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "request");
        String query = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("query")) {
                query = readQuery(parser);
            } else {
                skip(parser);
            }
        }
        return query;
    }

    private String readQuery(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "query");
        String query = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "query");
        return query;
    }

    private CurrentCondition readCurrentCondition(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "current_condition");
        String tempC = null;
        String tempF = null;
        String weatherIconUrl = null;
        String windspeedMiles = null;
        String windspeedKmph = null;
        String winddir16Point = null;
        String humidity = null;
        String pressure = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("temp_C")) {
                tempC = readTempC(parser);
            } else if (name.equals("temp_F")) {
                tempF = readTempF(parser);
            } else if (name.equals("weatherIconUrl")) {
                weatherIconUrl = readWeatherIconUrl(parser);
            } else if (name.equals("windspeedMiles")) {
                windspeedMiles = readWindspeedMiles(parser);
            } else if (name.equals("windspeedKmph")) {
                windspeedKmph = readWindspeedKmph(parser);
            } else if (name.equals("winddir16Point")) {
                winddir16Point = readWinddir16Point(parser);
            } else if (name.equals("humidity")) {
                humidity = readHumidity(parser);
            } else if (name.equals("pressure")) {
                pressure = readPressure(parser);
            } else {
                skip(parser);
            }
        }
        return new CurrentCondition(tempC, tempF, weatherIconUrl, windspeedMiles, windspeedKmph,
                winddir16Point, humidity, pressure);
    }

    private String readTempC(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "temp_C");
        String tempC = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "temp_C");
        return tempC;
    }

    private String readTempF(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "temp_F");
        String tempF = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "temp_F");
        return tempF;
    }

    private String readWeatherIconUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "weatherIconUrl");
        String weatherIconUrl = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "weatherIconUrl");
        return weatherIconUrl;
    }

    private String readWindspeedMiles(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "windspeedMiles");
        String windspeedMiles = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "windspeedMiles");
        return windspeedMiles;
    }

    private String readWindspeedKmph(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "windspeedKmph");
        String windspeedKmph = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "windspeedKmph");
        return windspeedKmph;
    }

    private String readWinddir16Point(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "winddir16Point");
        String winddir16Point = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "winddir16Point");
        return winddir16Point;
    }

    private String readHumidity(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "humidity");
        String humidity = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "humidity");
        return humidity;
    }

    private String readPressure(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pressure");
        String pressure = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pressure");
        return pressure;
    }

    private FutureCondition readFutureCondition(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "weather");
        String date = null;
        String tempMaxC = null;
        String tempMaxF = null;
        String tempMinC = null;
        String tempMinF = null;
        String weatherIconUrl = null;
        String windspeedMiles = null;
        String windspeedKmph = null;
        String winddir16Point = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("date")) {
                date = readDate(parser);
            } else if (name.equals("tempMaxC")) {
                tempMaxC = readTempMaxC(parser);
            } else if (name.equals("tempMaxF")) {
                tempMaxF = readTempMaxF(parser);
            } else if (name.equals("tempMinC")) {
                tempMinC = readTempMinC(parser);
            } else if (name.equals("tempMinF")) {
                tempMinF = readTempMinF(parser);
            } else if (name.equals("weatherIconUrl")) {
                weatherIconUrl = readWeatherIconUrl(parser);
            } else if (name.equals("windspeedMiles")) {
                windspeedMiles = readWindspeedMiles(parser);
            } else if (name.equals("windspeedKmph")) {
                windspeedKmph = readWindspeedKmph(parser);
            } else if (name.equals("winddir16Point")) {
                winddir16Point = readWinddir16Point(parser);
            } else {
                skip(parser);
            }
        }
        return new FutureCondition(date, tempMaxC, tempMaxF, tempMinC, tempMinF,
                weatherIconUrl, windspeedMiles, windspeedKmph, winddir16Point);
    }

    private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "date");
        String date = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "date");
        return date;
    }

    private String readTempMaxC(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tempMaxC");
        String tempMaxC = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "tempMaxC");
        return tempMaxC;
    }

    private String readTempMaxF(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tempMaxF");
        String tempMaxF = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "tempMaxF");
        return tempMaxF;
    }

    private String readTempMinC(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tempMinC");
        String tempMinC = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "tempMinC");
        return tempMinC;
    }

    private String readTempMinF(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "tempMinF");
        String tempMinF = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "tempMinF");
        return tempMinF;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}