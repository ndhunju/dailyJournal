// build.gradle.kts for :androidpdfwriter module

plugins {
    id("com.android.library")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    namespace = "crl.android.pdfwriter"
}

dependencies {
    api(fileTree(kotlin.collections.mapOf("dir" to "libs", "include" to kotlin.collections.listOf("*.jar"))))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
