package com.telenav.pta2 // Sesuaikan package

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherDescription>?,
    @SerializedName("main") val main: MainStats?,
    @SerializedName("wind") val wind: Wind?, // <-- TAMBAHKAN INI
    @SerializedName("name") val cityName: String?,
    @SerializedName("cod") val responseCode: Int,
    // Tambahkan Rain/Snow jika ingin menampilkan volume presipitasi
    @SerializedName("rain") val rain: Precipitation?,
    @SerializedName("snow") val snow: Precipitation?
)

data class WeatherDescription(
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String? // Kode ikon
)

data class MainStats(
    @SerializedName("temp") val temperature: Float?,
    @SerializedName("feels_like") val feelsLike: Float?,
    @SerializedName("temp_min") val tempMin: Float?,
    @SerializedName("temp_max") val tempMax: Float?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

// <-- TAMBAHKAN DATA CLASS BARU INI -->
data class Wind(
    @SerializedName("speed") val speed: Float?, // Kecepatan (m/s default, bisa berbeda tergantung unit)
    @SerializedName("deg") val degree: Int? // Arah angin (derajat)
)

// <-- TAMBAHKAN DATA CLASS BARU INI (Opsional, untuk volume) -->
data class Precipitation(
    @SerializedName("1h") val oneHour: Float? // Volume dalam 1 jam terakhir (mm)
    // @SerializedName("3h") val threeHours: Float? // Volume dalam 3 jam terakhir (mm) - jika API menyediakan
)