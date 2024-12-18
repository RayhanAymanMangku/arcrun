plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.arcrun"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.newarcrun"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }





    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
    }
}

dependencies {
    // Android and Firebase libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase and Google Play Services libraries
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.0.2")  // Firebase Authentication
    implementation("com.google.android.gms:play-services-auth:21.2.0")  // Google Sign-In
    implementation("com.google.android.gms:play-services-identity:17.0.0")  // Google Identity Services

    // Kotlin and testing dependencies
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.android.material:material:1.7.0")
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.wear)
    implementation(libs.volley)
    implementation(libs.androidx.tools.core)
    implementation(libs.billing)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.midtrans:uikit:2.0.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
}
