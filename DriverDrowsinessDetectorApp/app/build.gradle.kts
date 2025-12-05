plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.driverdrowsinessdetectorapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.driverdrowsinessdetectorapp"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


        // ═══════════════════════════════════════════════════════════
        // 🌐 CONFIGURACIÓN DE URL DEL BACKEND
        // ═══════════════════════════════════════════════════════════
        // Cambiar esta IP según tu red local
        // Para encontrar tu IP en Linux: hostname -I | awk '{print $1}'
        // ═══════════════════════════════════════════════════════════
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.17:8000/\"")

        // Configuración de timeouts (en segundos)
        buildConfigField("long", "CONNECT_TIMEOUT", "30L")
        buildConfigField("long", "READ_TIMEOUT", "30L")
        buildConfigField("long", "WRITE_TIMEOUT", "30L")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false

            // URL de desarrollo (red local)
            // Esta sobrescribe la de defaultConfig para debug
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.17:8000/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ========================================
    // ⭐ MEDIAPIPE - CORE DE DETECCIÓN
    // ========================================
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // ========================================
    // ⭐ CAMERAX - CAPTURA DE VIDEO
    // ========================================
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // ========================================
    // ⭐ KOTLIN COROUTINES - PROCESAMIENTO ASÍNCRONO
    // ========================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    
    // Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Permissions (Compose)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // WorkManager (para sync en background)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ========================================
    // ⭐ DATASTORE - PREFERENCES
    // ========================================
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // MediaPipe
    implementation("com.google.mediapipe:tasks-vision:0.10.9")

    // Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Permissions (Compose)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // WorkManager (para sync en background)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}