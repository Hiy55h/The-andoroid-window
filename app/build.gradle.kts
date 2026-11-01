plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.hiy55h.astraos"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.hiy55h.astraos"
        minSdk = 30
        targetSdk = 30
        versionCode = 2
        versionName = "0.2.0"
        ndk { abiFilters += listOf("armeabi-v7a") }
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
