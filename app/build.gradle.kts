plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {




    namespace = "com.example.camerax"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.camerax"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:+")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    //cameraX
    implementation("androidx.camera:camera-camera2:1.0.1")
    implementation("androidx.camera:camera-lifecycle:1.0.1")
    implementation("androidx.camera:camera-view:1.0.0-alpha27")


    //retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0") // OkHttp Logging Interceptor (opcional para logs)



    //icon
//    implementation("androidx.compose.material:material-icons-extended:1.7.2")

    implementation("androidx.compose.material:material:1.6.0-alpha05") // substitua 1.x.x pela versão atual
    implementation("androidx.compose.material:material-icons-core:1.6.0-alpha05")
    implementation("androidx.compose.material:material-icons-extended:1.6.0-alpha05")

    // Coil
    implementation("io.coil-kt:coil-compose:1.4.0")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.1-alpha")
    implementation("com.google.accompanist:accompanist-permissions:0.33.1-alpha")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


}