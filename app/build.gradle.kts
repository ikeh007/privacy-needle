// AdMob production App ID comes from ~/.gradle/gradle.properties (never guess the '~' suffix).
// The publisher prefix must match the production ad units listed in the AdMob console.
val verifiedAppId = "ca-app-pub-7008705915557363~9635177396"
val liveAppId = providers.gradleProperty("PRIVACY_NEEDLE_ADMOB_APP_ID")
    .orNull?.trim()?.takeIf { it.isNotEmpty() } ?: verifiedAppId
val hasLiveAppId = Regex("ca-app-pub-7008705915557363~[0-9]{10}").matches(liveAppId)
val enableReleaseRewarded = providers.gradleProperty("PRIVACY_NEEDLE_ENABLE_REWARDED_ADS")
    .orNull?.equals("true", ignoreCase = true) == true
val sampleAppId = "ca-app-pub-3940256099942544~3347511713"

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.jaims.privacyneedle"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.jaims.privacyneedle"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["ADMOB_APP_ID"] = sampleAppId
            buildConfigField("boolean", "ADMOB_LIVE_ENABLED", "false")
            buildConfigField("boolean", "ADMOB_REWARDED_ENABLED", "true")
        }
        release {
            // An unconfigured release never sends live ad requests.
            manifestPlaceholders["ADMOB_APP_ID"] = if (hasLiveAppId) liveAppId else sampleAppId
            buildConfigField("boolean", "ADMOB_LIVE_ENABLED", hasLiveAppId.toString())
            // During policy remediation, the optional quiz reward stays off in production.
            buildConfigField("boolean", "ADMOB_REWARDED_ENABLED",
                (hasLiveAppId && enableReleaseRewarded).toString())
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
    // Google Mobile Ads SDK (legacy APIs) and User Messaging Platform privacy consent.
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
