// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
}

buildscript {
    val kotlin_version by extra("2.2.0")
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
    repositories {
        mavenCentral()
    }
}
