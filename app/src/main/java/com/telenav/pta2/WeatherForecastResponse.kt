package com.telenav.pta2 // Pastikan package name Anda sesuai

// Jika Anda menggunakan Gson untuk parsing JSON, Anda mungkin perlu anotasi ini.
// Jika Anda menggunakan kotlinx.serialization, Anda akan menggunakan @SerialName("nama_json_field")
// import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    val list: List<WeatherForecastItem>,
    val city: City
)

data class City(
    val name: String,
    val country: String?, // Bisa ditambahkan jika API menyediakan dan Anda butuhkan
    val timezone: Int?    // Offset dari UTC dalam detik, jika ada
)

data class WeatherForecastItem(
    val dt: Long,                 // Timestamp UNIX (detik)
    val main: MainWeatherData,
    val weather: List<WeatherDescription>,
    val wind: WindData?,          // Bisa null jika API tidak selalu mengirimkan
    val clouds: CloudsData?,      // Data awan, jika ada
    val rain: PrecipitationData?, // Data hujan, bisa null
    val snow: PrecipitationData?, // Data salju, bisa null
    val pop: Double?,             // Probability of Precipitation (0-1), jika ada
    // @SerializedName("dt_txt")
    val dt_txt: String?           // Tanggal dan waktu dalam format teks, jika ada
)

data class MainWeatherData(
    val temp: Double,
    // @SerializedName("feels_like")
    val feels_like: Double?,
    // @SerializedName("temp_min")
    val temp_min: Double?,
    // @SerializedName("temp_max")
    val temp_max: Double?,
    val pressure: Int?,
    val humidity: Int
    // @SerializedName("sea_level")
    // val sea_level: Int?,
    // @SerializedName("grnd_level")
    // val grnd_level: Int?
)


data class WindData(
    val speed: Double?,          // Kecepatan angin. Unit default biasanya m/s.
    val deg: Int?,               // Arah angin, derajat (meteorologi)
    val gust: Double?            // Hembusan angin. Unit default biasanya m/s. (jika ada)
)

data class CloudsData(
    // @SerializedName("all")
    val all: Int?                // Persentase tutupan awan (%)
)

data class PrecipitationData(
    // Field di JSON bisa jadi "1h" atau "3h".
    // Jika menggunakan Gson: @SerializedName("1h")
    // Jika menggunakan kotlinx.serialization: @SerialName("1h")
    val oneHour: Double?,        // Volume presipitasi untuk 1 jam terakhir, mm.
    // Mungkin perlu anotasi jika nama field di JSON adalah "1h"

    // Jika menggunakan Gson: @SerializedName("3h")
    // Jika menggunakan kotlinx.serialization: @SerialName("3h")
    val threeHour: Double?       // Volume presipitasi untuk 3 jam terakhir, mm.
    // Mungkin perlu anotasi jika nama field di JSON adalah "3h"
)

// Contoh penggunaan @SerializedName dengan Gson (jika nama field JSON berbeda):
// data class PrecipitationData(
//    @SerializedName("1h")
//    val oneHour: Double?,
//    @SerializedName("3h")
//    val threeHour: Double?
// )