plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlinx-serialization")

    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.progetto"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.progetto"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {

    implementation("androidx.compose.ui:ui:1.5.0") // Sostituisci con la versione più recente
    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("com.mapbox.maps:android:11.9.0")
    // If you're using compose also add the compose extension
    implementation("com.mapbox.extension:maps-compose:11.9.0")
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Da copiare da qui alla fine
    // Core
    implementation(libs.ktor.ktor.client.core)
    implementation(libs.ktor.client.android)
    // Logging
    implementation(libs.ktor.client.logging)
    // JSON Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.io.ktor.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json.v210)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.1")
    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.1")
    implementation("androidx.datastore:datastore:1.1.1")
    // optional - RxJava2 support
    implementation("androidx.datastore:datastore-rxjava2:1.1.1")
    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-rxjava3:1.1.1")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")
    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
}