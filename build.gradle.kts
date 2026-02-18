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
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
}
