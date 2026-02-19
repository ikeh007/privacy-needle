plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.jaims.privacyneedle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jaims.privacyneedle"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation(libs.firebase.messaging) // Consider using the latest stable or RC version of Glide
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.0-rc01") // Match Glide version

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation ("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // Add Retrofit core library
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Or the latest version

    // Add Gson converter for Retrofit (since your RetrofitClient.java likely uses it)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Or the latest version, matching Retrofit

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
