package com.telenav.pta2 // Sesuaikan package

import com.google.gson.annotations.SerializedName // Jika Anda menggunakan Gson

// === MODEL UNTUK CUACA SAAT INI (CURRENT WEATHER) ===
data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherDescription>?,
    @SerializedName("main") val main: MainWeatherData?,      // Menggunakan MainWeatherData
    @SerializedName("wind") val wind: WindData?,            // Menggunakan WindData
    @SerializedName("rain") val rain: PrecipitationData?,    // Menggunakan PrecipitationData
    @SerializedName("snow") val snow: PrecipitationData?,    // Menggunakan PrecipitationData
    @SerializedName("clouds") val clouds: CloudsData?,
    @SerializedName("dt") val dt: Long,                     // Timestamp untuk current weather
    @SerializedName("name") val cityName: String?,
    @SerializedName("cod") val responseCode: Int?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("id") val cityId: Long?
)

// === MODEL UNTUK PERKIRAAN CUACA (FORECAST) ===
data class WeatherForecastResponse(
    @SerializedName("list") val list: List<WeatherForecastItem>,
    @SerializedName("city") val city: CityData // Nama diubah agar tidak bentrok
)

data class CityData( // Sebelumnya City
    @SerializedName("name") val name: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("id") val id: Long?,
    @SerializedName("population") val population: Long?,
    @SerializedName("coord") val coordinates: Coordinates?
)

data class Coordinates(
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?
)

data class WeatherForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainWeatherData, // Menggunakan MainWeatherData
    @SerializedName("weather") val weather: List<WeatherDescription>, // Menggunakan WeatherDescription
    @SerializedName("clouds") val clouds: CloudsData?, // Menggunakan CloudsData
    @SerializedName("wind") val wind: WindData?,    // Menggunakan WindData
    @SerializedName("rain") val rain: PrecipitationData?, // Menggunakan PrecipitationData
    @SerializedName("snow") val snow: PrecipitationData?, // Menggunakan PrecipitationData
    @SerializedName("pop") val pop: Double?,
    @SerializedName("dt_txt") val dt_txt: String?
)

// === KOMPONEN DATA YANG DIGUNAKAN BERSAMA ===
// (WeatherDescription, MainWeatherData, WindData, CloudsData, PrecipitationData)

data class WeatherDescription(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val mainCondition: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class MainWeatherData( // Digunakan oleh WeatherResponse & WeatherForecastItem
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feels_like: Double?,
    @SerializedName("temp_min") val temp_min: Double?,
    @SerializedName("temp_max") val temp_max: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

data class WindData( // Digunakan oleh WeatherResponse & WeatherForecastItem
    @SerializedName("speed") val speed: Double?,
    @SerializedName("deg") val deg: Int?,
    @SerializedName("gust") val gust: Double?
)

data class CloudsData( // Digunakan oleh WeatherResponse & WeatherForecastItem
    @SerializedName("all") val all: Int?
)

data class PrecipitationData( // Digunakan oleh WeatherResponse & WeatherForecastItem
    @SerializedName("1h") val oneHour: Double?,
    @SerializedName("3h") val threeHour: Double? // Jadikan nullable
)