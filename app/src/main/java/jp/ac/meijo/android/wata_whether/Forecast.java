package jp.ac.meijo.android.wata_whether;

public class Forecast {// 天気情報
    public String date;//日付
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
