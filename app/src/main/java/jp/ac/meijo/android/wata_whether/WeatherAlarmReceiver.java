package jp.ac.meijo.android.wata_whether;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherAlarmReceiver extends BroadcastReceiver {//アラームを受け取る

    @Override
    public void onReceive(Context context, Intent intent) {

        // ★ Receiver の寿命を延ばす（重要）
        PendingResult pendingResult = goAsync();

        // ===== 通知チャンネル作成（Android 8+ 必須）=====
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",
                    "お天気通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://weather.tsukumijima.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService api = retrofit.create(WeatherApiService.class);

        api.getWeather("230010").enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                try {
                    if (!response.isSuccessful() || response.body() == null ||
                            response.body().forecasts == null ||
                            response.body().forecasts.size() < 2) {
                        return;
                    }

                    WeatherResponse.Forecast tomorrow =
                            response.body().forecasts.get(1);

                    String maxTemp = "-";
                    if (tomorrow.temperature != null &&
                            tomorrow.temperature.max != null) {
                        maxTemp = tomorrow.temperature.max.celsius;
                    }

                    String message =
                            "明日の天気: " + tomorrow.telop +
                                    " / 最高気温: " + maxTemp + "℃";

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context, "default")
                                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                                    .setContentTitle("お天気通知")
                                    .setContentText(message)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true);

                    // ===== 通知権限チェック =====
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED) {

                        NotificationManagerCompat
                                .from(context)
                                .notify(1, builder.build());
                    }

                } finally {
                    // ★ 必ず終了を通知
                    pendingResult.finish();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                pendingResult.finish();
            }
        });
    }
}
