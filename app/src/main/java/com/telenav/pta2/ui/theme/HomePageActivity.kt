package com.telenav.pta2

// Android Core & UI
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope

// Firebase (Auth & Realtime Database)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// Coroutines
import kotlinx.coroutines.launch

// Image Loading (Glide)
import com.bumptech.glide.Glide

// Utilities
import java.io.IOException
import java.util.Locale

class HomePageActivity : ComponentActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var distanceRef: DatabaseReference
    private var distanceListener: ValueEventListener? = null

    // Weather API
    private val weatherService = RetrofitClient.instance

    // UI Views
    private lateinit var textViewWelcome: TextView
    private lateinit var buttonLogout: Button

    private lateinit var textViewDistanceLabel: TextView
    private lateinit var textViewDistanceValue: TextView

    private lateinit var textViewWeatherCity: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private lateinit var textViewWeatherTemp: TextView
    private lateinit var textViewWeatherDesc: TextView
    private lateinit var textViewWeatherPrecipitation: TextView
    private lateinit var textViewWeatherHumidity: TextView
    private lateinit var textViewWeatherWind: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page_activity)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        distanceRef = database.getReference("/sensor/jarak_cm")

        // Inisialisasi Views
        textViewWelcome = findViewById(R.id.textViewWelcome)
        buttonLogout = findViewById(R.id.buttonLogout)

        textViewDistanceLabel = findViewById(R.id.textViewDistanceLabel)
        textViewDistanceValue = findViewById(R.id.textViewDistanceValue)

        textViewWeatherCity = findViewById(R.id.textViewWeatherCity)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)
        textViewWeatherTemp = findViewById(R.id.textViewWeatherTemp)
        textViewWeatherDesc = findViewById(R.id.textViewWeatherDesc)
        textViewWeatherPrecipitation = findViewById(R.id.textViewWeatherPrecipitation)
        textViewWeatherHumidity = findViewById(R.id.textViewWeatherHumidity)
        textViewWeatherWind = findViewById(R.id.textViewWeatherWind)

        // Cek apakah user sudah login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            textViewWelcome.text = "Welcome!"
        } else {
            goToLoginActivity()
            return
        }

        // Tombol Logout
        buttonLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
            goToLoginActivity()
        }

        // Mulai listener data sensor dan ambil data cuaca
        startDistanceListener()
        fetchWeatherData("Jakarta")
    }

    // --- SENSOR JARAK (Firebase) ---
    private fun startDistanceListener() {
        if (distanceListener == null) {
            distanceListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val distanceValue = snapshot.getValue(Float::class.java)
                        if (distanceValue != null) {
                            val distanceText = String.format(Locale.getDefault(), "%.1f cm", distanceValue)
                            textViewDistanceValue.text = distanceText
                            Log.d("HomePageActivity", "Jarak diterima: $distanceText")
                        } else {
                            textViewDistanceValue.text = "Data N/A"
                            Log.w("HomePageActivity", "Data jarak null di Firebase")
                        }
                    } else {
                        textViewDistanceValue.text = "Path Kosong"
                        Log.w("HomePageActivity", "Path ${distanceRef.path} tidak ditemukan di Firebase")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomePageActivity", "Gagal membaca data: ${error.message}")
                    textViewDistanceValue.text = "Error DB!"
                    Toast.makeText(baseContext, "Error reading distance DB: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            distanceRef.addValueEventListener(distanceListener!!)
            Log.d("HomePageActivity", "Listener Jarak Firebase ditambahkan.")
        } else {
            Log.d("HomePageActivity", "Listener Jarak Firebase sudah berjalan.")
        }
    }

    // --- CUACA (OpenWeatherMap API) ---
    private fun fetchWeatherData(cityName: String) {
        lifecycleScope.launch {
            try {
                val apiKey = BuildConfig.WEATHER_API_KEY
                if (apiKey.isEmpty() || apiKey.contains("MASUKKAN_API_KEY")) {
                    Log.e("HomePageActivity", "API Key belum diatur dengan benar.")
                    showWeatherError("API Key Error")
                    return@launch
                }

                val response = weatherService.getCurrentWeather(cityName, apiKey, "metric")
                if (response.isSuccessful && response.body() != null) {
                    updateWeatherUI(response.body()!!)
                } else {
                    val errorCode = response.code()
                    val errorMsg = response.errorBody()?.string() ?: "Unknown API error"
                    Log.e("HomePageActivity", "Error API Cuaca ($errorCode): $errorMsg")
                    showWeatherError(if (errorCode == 401) "API Key Salah" else "Gagal: $errorCode")
                }
            } catch (e: IOException) {
                Log.e("HomePageActivity", "Network Error: ${e.message}")
                showWeatherError("Network Error")
            } catch (e: Exception) {
                Log.e("HomePageActivity", "Error: ${e.message}")
                showWeatherError("Error")
            }
        }
    }

    private fun updateWeatherUI(data: WeatherResponse) {
        textViewWeatherCity.text = data.cityName ?: "Unknown"
        textViewWeatherTemp.text = data.main?.temperature?.let {
            String.format(Locale.getDefault(), "%.0f°C", it)
        } ?: "--°C"

        val description = data.weather?.firstOrNull()?.description
        textViewWeatherDesc.text = description?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        } ?: "No description"

        textViewWeatherHumidity.text = "Kelembapan: ${data.main?.humidity ?: "--"}%"
        val windSpeed = data.wind?.speed?.times(3.6)
        textViewWeatherWind.text = if (windSpeed != null) {
            String.format(Locale.getDefault(), "Angin: %.1f km/h", windSpeed)
        } else {
            "Angin: -- km/h"
        }

        textViewWeatherPrecipitation.text = when {
            data.rain?.oneHour != null -> String.format(Locale.getDefault(), "Hujan: %.1f mm/1j", data.rain.oneHour)
            data.snow?.oneHour != null -> String.format(Locale.getDefault(), "Salju: %.1f mm/1j", data.snow.oneHour)
            else -> "Presipitasi: 0 mm/1j"
        }

        val iconCode = data.weather?.firstOrNull()?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"
            Glide.with(this)
                .load(iconUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageViewWeatherIcon)
        } else {
            imageViewWeatherIcon.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    private fun showWeatherError(message: String) {
        textViewWeatherCity.text = "Error Cuaca"
        imageViewWeatherIcon.setImageResource(R.drawable.ic_launcher_foreground)
        textViewWeatherTemp.text = "--°C"
        textViewWeatherDesc.text = message
        textViewWeatherPrecipitation.text = "Presipitasi: --"
        textViewWeatherHumidity.text = "Kelembapan: --%"
        textViewWeatherWind.text = "Angin: -- km/h"
        Toast.makeText(this, "Weather Error: $message", Toast.LENGTH_SHORT).show()
    }

    // --- Navigasi ---
    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- Lifecycle ---
    override fun onDestroy() {
        super.onDestroy()
        if (distanceListener != null) {
            distanceRef.removeEventListener(distanceListener!!)
            Log.d("HomePageActivity", "Listener Jarak Firebase dihapus.")
        }
    }
}
