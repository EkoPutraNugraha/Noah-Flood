plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.telenav.pta2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.telenav.pta2"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // HAPUS baris 'val apiKey = project.findProperty...' jika ada sebelumnya

        // !!! TIDAK DIREKOMENDASIKAN - MASALAH KEAMANAN !!!
        // Ganti "2b5a01ed755293b789135c52d86b6334" dengan API KEY ANDA YANG ASLI
        buildConfigField("String", "WEATHER_API_KEY", "\"2b5a01ed755293b789135c52d86b6334\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation("junit:junit:4.13.2")
    // Firebase dependencies

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Tambahkan dependensi Firebase Database TANPA versi
    implementation("com.google.firebase:firebase-database-ktx")

    // Pastikan dependensi Firebase lain juga TANPA versi (contoh)
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.compose.material3:material3:1.2.0")

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Atau versi terbaru
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Atau versi terbaru
    implementation("com.google.code.gson:gson:2.10.1") // Atau versi terbaru

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Atau versi terbaru
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Atau versi terbaru


    // Tambahkan Glide untuk memuat gambar
    implementation("com.github.bumptech.glide:glide:4.16.0") // Cek versi terbaru jika perlu
    // annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Jika pakai Java/Kapt

    // CardView & Material Components
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:18.0.0")

}