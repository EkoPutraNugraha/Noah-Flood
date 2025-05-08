// File: WeatherModels.kt (atau nama file model Anda)
package com.telenav.pta2 // Sesuaikan package

import com.google.gson.annotations.SerializedName

// Model untuk CUACA SAAT INI
data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherDescription>?,
    @SerializedName("main") val main: MainStats?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("name") val cityName: String?,
    @SerializedName("cod") val responseCode: Int, // Biasanya 200 untuk sukses
    @SerializedName("rain") val rain: Precipitation?, // Data hujan, bisa null
    @SerializedName("snow") val snow: Precipitation?  // Data salju, bisa null
)

data class WeatherDescription(
    // @SerializedName("id") val id: Int?, // Jika API Anda mengirimkan ID
    @SerializedName("main") val mainCondition: String?, // Ganti nama agar tidak bentrok jika ada var 'main' lain
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class MainStats(
    @SerializedName("temp") val temperature: Float?,
    @SerializedName("humidity") val humidity: Int?,
    // @SerializedName("feels_like") val feels_like: Float?, // Jika API mengirimkan
    // @SerializedName("temp_min") val temp_min: Float?,   // Jika API mengirimkan
    // @SerializedName("temp_max") val temp_max: Float?,   // Jika API mengirimkan
    // @SerializedName("pressure") val pressure: Int?      // Jika API mengirimkan
)

data class Wind(
    @SerializedName("speed") val speed: Float?, // Biasanya m/s
    @SerializedName("deg") val degree: Int?    // Derajat
    // @SerializedName("gust") val gust: Float? // Jika API mengirimkan
)

data class Precipitation(
    // API bisa mengirim "1h" atau "3h", pastikan @SerializedName cocok
    @SerializedName("1h") val oneHour: Float?,
    // @SerializedName("3h") val threeHour: Float? // Jika API mengirimkan data 3 jam juga
)