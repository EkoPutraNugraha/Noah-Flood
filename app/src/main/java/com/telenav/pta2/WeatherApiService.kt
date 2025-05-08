package com.telenav.pta2 // Sesuaikan package

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Import model data Anda
import com.telenav.pta2.WeatherResponse // Untuk cuaca saat ini
import com.telenav.pta2.WeatherForecastResponse // Untuk perkiraan cuaca

interface WeatherApiService {

    // Endpoint untuk CUACA SAAT INI
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Response<WeatherResponse>

    // Endpoint untuk PERKIRAAN CUACA (misal, 5 hari / 3 jam)
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): Response<WeatherForecastResponse>
}