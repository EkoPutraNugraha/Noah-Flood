package com.telenav.pta2 // Sesuaikan package

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.telenav.pta2.databinding.ActivityHomePageBinding
// Import model data Anda (pastikan path dan nama file benar)
import com.telenav.pta2.WeatherResponse
import com.telenav.pta2.WeatherForecastResponse
import com.telenav.pta2.WeatherForecastItem // Mungkin tidak perlu jika di package yang sama dengan WeatherForecastResponse
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Ekstensi untuk membuat setiap kata dalam string menjadi huruf kapital di awal
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

class HomePageActivity : ComponentActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var distanceRef: DatabaseReference
    private var distanceListener: ValueEventListener? = null

    private val weatherService by lazy { RetrofitClient.instance }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        distanceRef = database.getReference("/sensor/jarak_cm")

        if (auth.currentUser == null) {
            goToLoginActivity()
            return
        }

        binding.textViewWelcome.text = "Hai, Selamat datang 👋"
        // Anda bisa memuat cuaca kota default di sini jika diinginkan
        // fetchDefaultWeatherData("Jakarta") // Misalnya

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
            goToLoginActivity()
        }

        binding.buttonSearchCity.setOnClickListener {
            val city = binding.editTextCity.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchDefaultWeatherData(city)
            } else {
                Toast.makeText(this, "Masukkan nama kota", Toast.LENGTH_SHORT).show()
            }
        }
        startDistanceListener()
    }

    private fun fetchDefaultWeatherData(city: String) {
        fetchCurrentWeatherByCity(city)
        fetchWeatherForecast(city)
    }

    private fun startDistanceListener() {
        distanceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val distance = snapshot.getValue(Float::class.java)
                val text = distance?.let { "%.1f cm".format(it) } ?: "Tidak ada data"
                binding.textViewDistanceValue.text = text
            }
            override fun onCancelled(error: DatabaseError) {
                binding.textViewDistanceValue.text = "Error DB"
                Toast.makeText(this@HomePageActivity, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        distanceRef.addValueEventListener(distanceListener!!)
    }

    private fun fetchCurrentWeatherByCity(city: String) {
        lifecycleScope.launch {
            try {
                val key = BuildConfig.WEATHER_API_KEY
                if (key.isEmpty() || key.contains("MASUKKAN_API_KEY") || key == "YOUR_API_KEY_HERE") {
                    showWeatherError("API Key belum dikonfigurasi")
                    return@launch
                }
                val response = weatherService.getCurrentWeatherByCity(city, key, "metric", "id")
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        Log.d("WeatherAPI", "Current Weather Response: $weatherData")
                        if (weatherData.responseCode == 200) {
                            updateCurrentWeatherUI(weatherData)
                        } else {
                            showWeatherError("API Cuaca Saat Ini: ${weatherData.responseCode}")
                        }
                    } ?: showWeatherError("Data cuaca saat ini kosong")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WeatherAPI", "Current Weather API Error: ${response.code()} - ${response.message()}. Body: $errorBody")
                    showWeatherError("Cuaca Saat Ini: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("WeatherAPI", "Current Weather Network Error", e)
                showWeatherError("Jaringan Cuaca Saat Ini Error")
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Current Weather Unexpected Error", e)
                showWeatherError("Error Cuaca Saat Ini: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchWeatherForecast(city: String) {
        lifecycleScope.launch {
            try {
                val key = BuildConfig.WEATHER_API_KEY
                if (key.isEmpty() || key.contains("MASUKKAN_API_KEY") || key == "YOUR_API_KEY_HERE") {
                    showForecastError("API Key belum dikonfigurasi") // Pesan error spesifik untuk forecast
                    return@launch
                }
                val response = weatherService.getWeatherForecast(city, key, "metric", "id")
                if (response.isSuccessful) {
                    response.body()?.let { forecastResponse ->
                        Log.d("WeatherAPI", "Forecast Response: $forecastResponse")
                        processAndDisplayDailyForecast(forecastResponse)
                    } ?: showForecastError("Data perkiraan kosong")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WeatherAPI", "Forecast API Error: ${response.code()} - ${response.message()}. Body: $errorBody")
                    showForecastError("Perkiraan: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("WeatherAPI", "Forecast Network Error", e)
                showForecastError("Jaringan Perkiraan Error")
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Forecast Unexpected Error", e)
                showForecastError("Error Perkiraan: ${e.localizedMessage}")
            }
        }
    }

    private fun updateCurrentWeatherUI(data: WeatherResponse) = with(binding) {
        textViewWeatherCity.text = data.cityName?.capitalizeWords() ?: "Kota Tidak Ditemukan"
        data.main?.temperature?.let { textViewWeatherTemp.text = "%.1f°C".format(it) } ?: run { textViewWeatherTemp.text = "--°C" }
        val weatherDesc = data.weather?.firstOrNull()
        textViewWeatherDesc.text = weatherDesc?.description?.capitalizeWords() ?: "Tidak ada deskripsi"
        data.main?.humidity?.let { textViewWeatherHumidity.text = "Kelembapan: $it%" } ?: run { textViewWeatherHumidity.text = "Kelembapan: --%" }

        data.wind?.speed?.let { speedMs ->
            val speedKmh = speedMs * 3.6
            var windText = "Angin: ${speedKmh.roundToInt()} km/h"
            data.wind.degree?.let { degrees -> windText += " (${getWindDirection(degrees)})" }
            textViewWeatherWind.text = windText
        } ?: run { textViewWeatherWind.text = "Angin: -- km/h" }

        var precipitationText = "Presipitasi: --"
        data.rain?.oneHour?.let { volume -> precipitationText = "Hujan: %.1f mm/jam".format(volume) }
        data.snow?.oneHour?.let { volume ->
            if (precipitationText != "Presipitasi: --" && data.rain?.oneHour == null) {
                precipitationText = "Salju: %.1f mm/jam".format(volume)
            } else if (precipitationText != "Presipitasi: --") {
                precipitationText += "\nSalju: %.1f mm/jam".format(volume)
            } else {
                precipitationText = "Salju: %.1f mm/jam".format(volume)
            }
        }
        textViewWeatherPrecipitation.text = precipitationText

        val iconCode = weatherDesc?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            Glide.with(this@HomePageActivity).load(iconUrl)
                .placeholder(R.drawable.ic_weather_placeholder)
                .error(R.drawable.ic_weather_error)
                .into(imageViewWeatherIcon)
        } else {
            imageViewWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder)
        }
    }

    private fun processAndDisplayDailyForecast(forecast: WeatherForecastResponse) = with(binding) {
        val dailyForecasts = mutableListOf<WeatherForecastItem>()
        val processedDates = mutableSetOf<String>()
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("EEEE, dd MMM", Locale("id", "ID"))

        forecast.list.forEach { item ->
            val date = Date(item.dt * 1000L)
            val dateString = dateOnlyFormat.format(date)
            if (!processedDates.contains(dateString)) {
                // Coba pilih item sekitar tengah hari jika memungkinkan (misal, antara jam 11 dan 14)
                // Ini adalah heuristik sederhana, bisa disesuaikan
                val calendar = Calendar.getInstance().apply { time = date }
                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                if (dailyForecasts.none { dateOnlyFormat.format(Date(it.dt * 1000L)) == dateString } ||
                    (hourOfDay in 11..14) ) { // Prioritaskan jam tengah hari jika belum ada untuk hari itu
                    // Jika sudah ada untuk hari itu tapi ini jam tengah hari, ganti. (Logika bisa lebih kompleks)
                    dailyForecasts.removeAll { dateOnlyFormat.format(Date(it.dt * 1000L)) == dateString } // Hapus entri lama untuk hari ini
                    dailyForecasts.add(item)
                    processedDates.add(dateString)
                } else if (dailyForecasts.none{ dateOnlyFormat.format(Date(it.dt * 1000L)) == dateString }){
                    // Jika tidak ada jam tengah hari & belum ada entri untuk hari itu, ambil saja yang ini.
                    dailyForecasts.add(item)
                    processedDates.add(dateString)
                }
            }
        }
        // Sortir berdasarkan tanggal untuk memastikan urutan yang benar
        dailyForecasts.sortBy { it.dt }

        // Ambil maksimal 3 hari
        val finalDailyDisplay = dailyForecasts.take(3)

        textViewWeatherDay1.text = "Perkiraan 1: Data tidak tersedia"
        textViewWeatherDay2.text = "Perkiraan 2: Data tidak tersedia"
        textViewWeatherDay3.text = "Perkiraan 3: Data tidak tersedia"

        finalDailyDisplay.forEachIndexed { index, dailyItem ->
            val date = Date(dailyItem.dt * 1000L)
            val formattedDisplayDate = displayDateFormat.format(date)
            // Pastikan MainData dan WeatherInfo tidak null dan memiliki data
            val tempText = dailyItem.main?.temp?.let { "%.1f°C".format(it) } ?: "--°C"
            val description = dailyItem.weather?.firstOrNull()?.description?.capitalizeWords() ?: "-"

            val dayText = "Perkiraan ${index + 1}:\n" +
                    "$formattedDisplayDate\n" +
                    "$description\n" +
                    "Suhu: $tempText"
            when (index) {
                0 -> textViewWeatherDay1.text = dayText
                1 -> textViewWeatherDay2.text = dayText
                2 -> textViewWeatherDay3.text = dayText
            }
        }
    }

    private fun getWindDirection(degrees: Int): String {
        val directions = arrayOf("U", "UTL", "TL", "TTL", "T", "TGR", "GR", "BBL", "B", "BBD", "BD", "SBD", "S", "SBL", "BL", "UBL")
        return directions[((degrees % 360) / 22.5).roundToInt() % 16]
    }

    private fun showWeatherError(message: String) = with(binding) {
        Toast.makeText(this@HomePageActivity, "Cuaca Saat Ini: $message", Toast.LENGTH_SHORT).show()
        textViewWeatherCity.text = "Gagal Memuat"
        textViewWeatherTemp.text = "--°C"
        textViewWeatherDesc.text = "Error: $message"
        imageViewWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder)
        textViewWeatherHumidity.text = "Kelembapan: --%"
        textViewWeatherWind.text = "Angin: -- km/h"
        textViewWeatherPrecipitation.text = "Presipitasi: --"
    }

    private fun showForecastError(message: String) {
        Toast.makeText(this, "Perkiraan: $message", Toast.LENGTH_SHORT).show()
        binding.textViewWeatherDay1.text = "Perkiraan 1: Error"
        binding.textViewWeatherDay2.text = "Perkiraan 2: Error"
        binding.textViewWeatherDay3.text = "Perkiraan 3: Error"
    }

    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        distanceListener?.let { distanceRef.removeEventListener(it) }
    }
}