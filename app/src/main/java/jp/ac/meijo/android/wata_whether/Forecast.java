package jp.ac.meijo.android.wata_whether;

public class Forecast {
    public String date;
    public String dateLabel;
    public String telop;
    public Temperature temperature;

    public static class Temperature {
        public Temp min;
        public Temp max;
    }

    public static class Temp {
        public String celsius;
        public String fahrenheit;
    }
}
