// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.google.com/")
            name = "Google"
        }
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0")
        // Add the dependency for the Crashlytics Gradle plugin
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
        classpath("com.google.gms:google-services:4.4.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
        classpath("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.2.20")
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
}
