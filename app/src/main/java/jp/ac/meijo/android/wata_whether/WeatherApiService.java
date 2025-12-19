package jp.ac.meijo.android.wata_whether;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WeatherApiService {
    // https://weather.tsukumijima.net/api/forecast/city/130010
    @GET("forecast/city/{cityId}")
    Call<WeatherResponse> getWeather(@Path("cityId") String cityId);//天気API
}
