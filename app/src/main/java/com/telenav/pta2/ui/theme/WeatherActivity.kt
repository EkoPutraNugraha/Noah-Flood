package com.telenav.pta2 // Sesuaikan package

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
// Import data class cuaca
import com.telenav.pta2.databinding.ActivityHomePageBinding
import com.telenav.pta2.WeatherResponse
import com.telenav.pta2.WeatherForecastResponse
import com.telenav.pta2.WeatherForecastItem
// Import untuk BottomNavigationView Listener
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Ekstensi String (tetap sama)
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

class HomePageActivity : ComponentActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var auth: FirebaseAuth
    // Hapus variabel database, ref, listener

    private val weatherService by lazy { RetrofitClient.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // Hapus inisialisasi database, ref

        if (auth.currentUser == null) {
            goToLoginActivity()
            return
        }

        binding.textViewWelcome.text = "Hai, Selamat datang 👋"

        // === LISTENER TOMBOL LOGOUT DIHAPUS ===

        // Listener Tombol Cari Cuaca
        binding.buttonSearchCity.setOnClickListener {
            val city = binding.editTextCity.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchDefaultWeatherData(city)
            } else {
                Toast.makeText(this, "Masukkan nama kota", Toast.LENGTH_SHORT).show()
            }
        }

        // Panggil setup bottom navigation
        setupBottomNavigation()

        // Pemanggilan startDistanceListener() DIHAPUS

        // Muat data cuaca default
        fetchDefaultWeatherData("Jakarta")
    }

    // setupBottomNavigation() (tetap sama)
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> { true }
                R.id.navigation_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    // fetchDefaultWeatherData() (tetap sama)
    private fun fetchDefaultWeatherData(city: String) {
        binding.textViewWeatherCity.text = "Memuat..."
        binding.textViewForecastError.visibility = View.GONE
        binding.textViewWeatherDesc.text = "..."
        binding.textViewWeatherTemp.text = "--°C"
        binding.imageViewWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder)
        binding.textViewWeatherHumidity.text = "Kelembapan: --%"
        binding.textViewWeatherWind.text = "Angin: -- km/h"
        binding.textViewWeatherPrecipitation.text = "Presipitasi: --"
        hideForecastCards()

        fetchCurrentWeatherByCity(city)
        fetchWeatherForecast(city)
    }

    // Fungsi startDistanceListener() DIHAPUS SELURUHNYA

    // fetchCurrentWeatherByCity()
    private fun fetchCurrentWeatherByCity(city: String) {
        lifecycleScope.launch {
            try {
                // --- FIX 1: Deklarasikan 'key' di sini ---
                val key = BuildConfig.WEATHER_API_KEY
                if (key.isEmpty() || key.contains("MASUKKAN_API_KEY") || key == "YOUR_API_KEY_HERE") {
                    showWeatherError("API Key belum dikonfigurasi")
                    return@launch
                }
                // -----------------------------------------

                // Gunakan endpoint yang sesuai untuk cuaca saat ini
                val response = weatherService.getCurrentWeatherByCity(city, key, "metric", "id") // Gunakan key
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        Log.d("WeatherAPI", "Current Weather Response: $weatherData")
                        if (weatherData.responseCode == 200 || weatherData.responseCode == null) {
                            updateCurrentWeatherUI(weatherData)
                        } else {
                            showWeatherError("Kota '${city}' tidak ditemukan atau error API (${weatherData.responseCode})")
                        }
                    } ?: showWeatherError("Data cuaca saat ini kosong untuk '${city}'")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WeatherAPI", "Current Weather API Error: ${response.code()} - ${response.message()}. Body: $errorBody")
                    if (response.code() == 404) {
                        showWeatherError("Kota '${city}' tidak ditemukan (404)")
                    } else {
                        showWeatherError("Cuaca Saat Ini: Error ${response.code()} untuk '${city}'")
                    }
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

    // fetchWeatherForecast()
    private fun fetchWeatherForecast(city: String) {
        lifecycleScope.launch {
            try {
                // --- FIX 2: Deklarasikan 'key' di sini juga ---
                val key = BuildConfig.WEATHER_API_KEY
                if (key.isEmpty() || key.contains("MASUKKAN_API_KEY") || key == "YOUR_API_KEY_HERE") {
                    showForecastError("API Key belum dikonfigurasi")
                    return@launch
                }
                // -------------------------------------------

                // Gunakan endpoint yang sesuai untuk perkiraan
                val response = weatherService.getWeatherForecast(city, key, "metric", "id") // Gunakan key
                if (response.isSuccessful) {
                    response.body()?.let { forecastResponse ->
                        Log.d("WeatherAPI", "Forecast Response: $forecastResponse")
                        processAndDisplayDailyForecast(forecastResponse)
                    } ?: showForecastError("Data perkiraan kosong untuk '${city}'")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WeatherAPI", "Forecast API Error: ${response.code()} - ${response.message()}. Body: $errorBody")
                    if (response.code() == 404) {
                        showForecastError("Perkiraan untuk kota '${city}' tidak ditemukan (404)")
                    } else {
                        showForecastError("Perkiraan: Error ${response.code()} untuk '${city}'")
                    }
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

    // updateCurrentWeatherUI() (tetap sama)
    private fun updateCurrentWeatherUI(data: WeatherResponse) {
        with(binding) {
            textViewWeatherCity.text = data.cityName?.capitalizeWords() ?: "Kota Tidak Ditemukan"
            data.main?.temperature?.let { textViewWeatherTemp.text = "%.1f°C".format(it) } ?: run { textViewWeatherTemp.text = "--°C" }
            val weatherDescInfo = data.weather?.firstOrNull()
            textViewWeatherDesc.text = weatherDescInfo?.description?.capitalizeWords() ?: "Tidak ada deskripsi"
            data.main?.humidity?.let { textViewWeatherHumidity.text = "Kelembapan: $it%" } ?: run { textViewWeatherHumidity.text = "Kelembapan: --%" }
            data.wind?.speed?.let { speedMs ->
                val speedKmh = speedMs * 3.6
                var windText = "Angin: ${speedKmh.roundToInt()} km/h"
                // Periksa null untuk degree sebelum memanggil getWindDirection
                data.wind.degree?.let { degrees -> windText += " (${getWindDirection(degrees)})" }
                textViewWeatherWind.text = windText
            } ?: run { textViewWeatherWind.text = "Angin: -- km/h" }
            var precipitationText = "Presipitasi: --"
            data.rain?.oneHour?.let { volume -> precipitationText = "Hujan: %.1f mm/jam".format(volume) }
            data.snow?.oneHour?.let { volume ->
                if (precipitationText == "Presipitasi: --" || data.rain?.oneHour == null) {
                    precipitationText = "Salju: %.1f mm/jam".format(volume)
                } else {
                    precipitationText += "\nSalju: %.1f mm/jam".format(volume)
                }
            }
            textViewWeatherPrecipitation.text = precipitationText
            val iconCode = weatherDescInfo?.icon
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
    }

    // processAndDisplayDailyForecast() (tetap sama)
    private fun processAndDisplayDailyForecast(forecast: WeatherForecastResponse) {
        // ... implementasi ...
        with(binding) {
            val dailyForecasts = mutableListOf<WeatherForecastItem>()
            val processedDates = mutableSetOf<String>()
            val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayDateFormat = SimpleDateFormat("EEEE, dd MMM HH:mm", Locale("id", "ID"))

            val todayCalendar = Calendar.getInstance()
            val todayDateString = dateOnlyFormat.format(todayCalendar.time)
            Log.d("ForecastLogic", "Hari ini (string): $todayDateString")

            forecast.list.forEach { item ->
                val itemDate = Date(item.dt * 1000L)
                val itemDateString = dateOnlyFormat.format(itemDate)

                if (itemDateString == todayDateString) {
                    Log.d("ForecastLogic", "Mengabaikan item untuk hari ini: ${item.dt_txt}")
                    return@forEach
                }

                if (!processedDates.contains(itemDateString)) {
                    val targetItem = forecast.list.find {
                        val d = Date(it.dt * 1000L)
                        dateOnlyFormat.format(d) == itemDateString
                    }
                    if (targetItem != null) {
                        Log.d("ForecastLogic", "Menambahkan entri untuk $itemDateString: ${targetItem.dt_txt}")
                        dailyForecasts.add(targetItem)
                        processedDates.add(itemDateString)
                    }
                }
            }

            dailyForecasts.sortBy { it.dt }
            val finalDailyDisplay = dailyForecasts.take(3)

            hideForecastCards()
            textViewForecastError.visibility = View.GONE

            if (finalDailyDisplay.isEmpty()) {
                showForecastError("Data perkiraan untuk hari berikutnya tidak tersedia.")
                return@with
            }

            Log.d("ForecastDisplay", "--- Mulai Proses Tampilan Perkiraan (mulai besok) ---")

            finalDailyDisplay.forEachIndexed { index, dailyItem ->
                val date = Date(dailyItem.dt * 1000L)
                val formattedDisplayDate = displayDateFormat.format(date)
                val weatherInfo = dailyItem.weather.firstOrNull()
                val description = weatherInfo?.description?.capitalizeWords() ?: "Tidak ada deskripsi"
                val iconCode = weatherInfo?.icon
                val tempText = dailyItem.main.temp.let { "%.1f°C".format(it) }

                Log.d("ForecastDisplay", "Hari ke-${index + 1} (besok dst.): ${dailyItem.dt_txt}, Icon: $iconCode, Desc: $description")

                val cardView: CardView
                val iconView: ImageView
                val dateView: TextView
                val descView: TextView
                val tempView: TextView
                val titleView: TextView

                when (index) {
                    0 -> { cardView = binding.cardViewDay1; iconView = binding.imageViewDay1Icon; dateView = binding.textViewDay1Date; descView = binding.textViewDay1Desc; tempView = binding.textViewDay1Temp; titleView = binding.textViewDay1Title }
                    1 -> { cardView = binding.cardViewDay2; iconView = binding.imageViewDay2Icon; dateView = binding.textViewDay2Date; descView = binding.textViewDay2Desc; tempView = binding.textViewDay2Temp; titleView = binding.textViewDay2Title }
                    2 -> { cardView = binding.cardViewDay3; iconView = binding.imageViewDay3Icon; dateView = binding.textViewDay3Date; descView = binding.textViewDay3Desc; tempView = binding.textViewDay3Temp; titleView = binding.textViewDay3Title }
                    else -> return@forEachIndexed
                }

                cardView.visibility = View.VISIBLE
                titleView.text = "Perkiraan ${index + 1}"
                dateView.text = formattedDisplayDate
                descView.text = description
                tempView.text = "Suhu: $tempText"

                if (iconCode != null) {
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                    Glide.with(this@HomePageActivity).load(iconUrl)
                        .placeholder(R.drawable.ic_weather_placeholder)
                        .error(R.drawable.ic_weather_error)
                        .into(iconView)
                } else {
                    iconView.setImageResource(R.drawable.ic_weather_placeholder)
                }
            }
        }
    }

    // getWindDirection()
    private fun getWindDirection(degrees: Int): String {
        val directions = arrayOf("U", "UTL", "TL", "TTL", "T", "TGR", "GR", "BBL", "B", "BBD", "BD", "SBD", "S", "SBL", "BL", "UBL")
        // --- FIX 3: Tambahkan 'return' di sini ---
        return directions[((degrees % 360) / 22.5).roundToInt() % 16]
        // -----------------------------------------
    }

    // showWeatherError() (tetap sama)
    private fun showWeatherError(message: String) {
        with(binding) {
            Toast.makeText(this@HomePageActivity, message, Toast.LENGTH_LONG).show()
            textViewWeatherCity.text = "Gagal Memuat"
            textViewWeatherTemp.text = "--°C"
            textViewWeatherDesc.text = "Error: $message"
            imageViewWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder)
            textViewWeatherHumidity.text = "Kelembapan: --%"
            textViewWeatherWind.text = "Angin: -- km/h"
            textViewWeatherPrecipitation.text = "Presipitasi: --"
        }
    }

    // showForecastError() (tetap sama)
    private fun showForecastError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        hideForecastCards()
        binding.textViewForecastError.text = message
        binding.textViewForecastError.visibility = View.VISIBLE
    }

    // hideForecastCards() (tetap sama)
    private fun hideForecastCards() {
        binding.cardViewDay1.visibility = View.GONE
        binding.cardViewDay2.visibility = View.GONE
        binding.cardViewDay3.visibility = View.GONE
    }

    // goToLoginActivity() (tetap sama)
    private fun goToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // onDestroy() (tidak perlu menghapus listener jarak)
    override fun onDestroy() {
        super.onDestroy()
        // Tidak ada listener yang perlu dihapus
    }
}