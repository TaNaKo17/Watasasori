package jp.ac.meijo.android.wata_whether;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {//基礎プログラム

    private TextView textWeather;
    private TimePicker timePicker;
    private Button buttonSetAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View取得
        textWeather = findViewById(R.id.textWeather);
        timePicker = findViewById(R.id.timePicker);
        buttonSetAlarm = findViewById(R.id.buttonSetAlarm);

        // 通知権限の確認（Android 13以上）
        requestNotificationPermission();

        // 天気取得
        fetchWeather();

        // 通知時刻設定ボタン
        buttonSetAlarm.setOnClickListener(v -> setWeatherAlarm());
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }

    /* -----------------------------
       天気API取得
     ----------------------------- */
    private void fetchWeather() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://weather.tsukumijima.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService service =
                retrofit.create(WeatherApiService.class);

        // 東京
        Call<WeatherResponse> call =
                service.getWeather("130010");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(
                    Call<WeatherResponse> call,
                    Response<WeatherResponse> response
            ) {
                if (!response.isSuccessful()) {
                    textWeather.setText("Error: " + response.code());
                    return;
                }

                WeatherResponse data = response.body();
                if (data == null || data.forecasts == null || data.forecasts.size() < 2) {
                    textWeather.setText("データ取得失敗");
                    return;
                }

                WeatherResponse.Forecast tomorrow =
                        data.forecasts.get(1);

                String maxTemp = "-";
                String minTemp = "-";

                if (tomorrow.temperature != null) {
                    if (tomorrow.temperature.max != null) {
                        maxTemp = tomorrow.temperature.max.celsius + "℃";
                    }
                    if (tomorrow.temperature.min != null) {
                        minTemp = tomorrow.temperature.min.celsius + "℃";
                    }
                }

                String result =
                        "■ " + tomorrow.dateLabel + " の天気\n" +
                                "天気：" + tomorrow.telop + "\n" +
                                "最高気温：" + maxTemp + "\n" +
                                "最低気温：" + minTemp;

                textWeather.setText(result);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                textWeather.setText("通信エラー");
                Log.e("API", "onFailure", t);
            }
        });
    }

    /* -----------------------------
       通知アラーム設定
     ----------------------------- */
    private void setWeatherAlarm() {

        int hour, minute;

        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 過去なら翌日
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent =
                new Intent(this, WeatherAlarmReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Toast.makeText(this,
                "通知を設定しました",
                Toast.LENGTH_SHORT).show();
    }
}
