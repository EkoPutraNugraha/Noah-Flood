package com.telenav.pta2 // Pastikan package Anda benar

import retrofit2.Response // Gunakan Response Retrofit untuk error handling yg lebih baik
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Endpoint untuk cuaca saat ini
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String, // Kota yang dicari
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Minta suhu dalam Celsius
    ): Response<WeatherResponse> // Bungkus dengan Response<>

    // Anda bisa menambahkan endpoint lain di sini, misal untuk forecast 5 hari
    // @GET("data/2.5/forecast")
    // suspend fun getForecast(...)
}