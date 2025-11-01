import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}
val keysPropertiesFile = rootProject.file("keys.properties")
val keysProperties = Properties().apply {
    load(keysPropertiesFile.inputStream())
}

android {
    namespace = "rttc.dssmv_projectdroid_1231562_1230985"
    compileSdk = 34

    defaultConfig {
        applicationId = "rttc.dssmv_projectdroid_1231562_1230985"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TranslateAPI_KEY", "\"${keysProperties["TranslateAPI_KEY"]}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${keysProperties["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${keysProperties["SUPABASE_KEY"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        resources {
            pickFirst("META-INF/INDEX.LIST")
            pickFirst("META-INF/io.netty.versions.properties")
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.17.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    implementation("org.asynchttpclient:async-http-client:3.0.3")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:*version*")
}