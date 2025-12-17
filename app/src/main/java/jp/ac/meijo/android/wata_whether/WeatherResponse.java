package jp.ac.meijo.android.wata_whether;

import java.util.List;

public class WeatherResponse {

    public List<Forecast> forecasts;

    // --- Forecast ---
    public static class Forecast {
        public String dateLabel;
        public String telop;
        public Temperature temperature;
    }

    // --- Temperature ---
    public static class Temperature {
        public Temp min;
        public Temp max;
    }

    // --- Temp ---
    public static class Temp {
        public String celsius;
    }
}

