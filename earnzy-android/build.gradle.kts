plugins {
    id("com.android.application") version "8.1.2"
    kotlin("android") version "1.9.10"
    kotlin("kapt") version "1.9.10"
}

android {
    namespace = "com.earnzy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.earnzy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Androidx
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")

    // Serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
