plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.akshaglobal.smartcallshield"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akshaglobal.smartcallshield"
        minSdk = 29
        targetSdk = 35
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
        viewBinding = true
        mlModelBinding = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // Hilt - Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.tensorflow.lite.metadata)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room - Database
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // DataStore - Preferences
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // TensorFlow Lite - ML
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.play.review)
    implementation(libs.play.services.ads)

    // Retrofit & OkHttp - Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Gson - JSON
    implementation(libs.gson)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel)

    // Permissions - Accompanist
    implementation(libs.accompanist.permissions)

    // Test Dependencies
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Dialer and Activity Result APIs
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.core:core-ktx:1.12.0")

    // MPAndroidChart - Charting Library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Apache POI - Excel export functionality
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // iText PDF - PDF export functionality
    // implementation("com.itextpdf:itext7-core:7.2.5")
    // implementation("com.itextpdf:itext7-io:7.2.5")
    // implementation("com.itextpdf:itext7-layout:7.2.5")

    // OpenPDF - PDF export functionality (alternative to iText)
    implementation("com.github.librepdf:openpdf:1.3.30")

    implementation("com.airbnb.android:lottie-compose:6.0.0")
}
