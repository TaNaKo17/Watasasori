package jp.ac.meijo.android.wata_whether;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.ac.meijo.android.wata_whether.databinding.ActivityMain2Binding;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.buttonHome.setOnClickListener(View -> {
            var intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // -------- 通知権限チェック --------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1000
                );
            }
        }

        // -------- 通知チャンネル --------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default", "通知テスト",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // -------- 天気 API 呼び出し --------
        binding.tuchibutton.setOnClickListener(view -> {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://weather.tsukumijima.net/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            WeatherApiService api = retrofit.create(WeatherApiService.class);

            Call<WeatherResponse> call = api.getWeather("130010");

            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        sendNotification("天気情報を取得できませんでした。");
                        return;
                    }

                    WeatherResponse data = response.body();
                    WeatherResponse.Forecast tomorrow = data.forecasts.get(1);

                    String weather = tomorrow.telop;
                    String maxTemp = (tomorrow.temperature.max != null)
                            ? tomorrow.temperature.max.celsius : "-";
                    String minTemp = (tomorrow.temperature.min != null)
                            ? tomorrow.temperature.min.celsius : "-";

                    String message = "明日の天気: " + weather +
                            " / 最高 " + maxTemp + "℃ 最低 " + minTemp + "℃";

                    sendNotification(message);
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    sendNotification("天気情報の取得に失敗しました。");
                }
            });
        });
    }

    // -------- 通知を送るメソッド --------
    private void sendNotification(String text) {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("お天気通知")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1, builder.build());
    }
}
