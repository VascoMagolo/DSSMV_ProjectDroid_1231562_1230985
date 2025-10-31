import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
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
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    implementation("org.asynchttpclient:async-http-client:3.0.3")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:*version*")
}