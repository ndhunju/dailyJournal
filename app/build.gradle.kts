import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.ksp) // this version has to match with Kotlin version
    id("org.jetbrains.kotlin.plugin.compose")
}

configurations {
    configureEach {
        exclude(module = "httpclient")
        exclude(module = "commons-logging")
    }
}

android {
    // each version of the Android Gradle Plugin now has a default version of the build tools.
    // buildToolsVersion = "25.0.3"

    defaultConfig {
        // The values in defaultConfig override those in the manifest file.
        applicationId = "com.ndhunju.dailyjournalplus"
        compileSdk = 36
        minSdk = 23
        targetSdk = 36
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //work around for Error:Error: error in parsing "g/"
        //generatedDensities = emptyList<String>()
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }

    // Disable all kinds of splitting of APK file since it is causing
    // ResourceNotFound exception probably because of side loading the
    // app i.e. one user downloaded the apk for one device and shared
    // with another user with a different device. Also, our app isn't
    // big to lose much for not splitting the apk to save space
    // See https://stackoverflow.com/a/53082301
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = false
        }
        abi {
            enableSplit = false
        }
    }

    buildTypes {
        //By default, the build system defines two build types: debug and release
        getByName("release") {
            //shrink the code to save space
            isMinifyEnabled = true
            //use proGaurd to optimize, obfuscate
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
        getByName("debug") {
            isDebuggable = true
        }
    }

    // Specifies one flavor dimension.
    flavorDimensions += "version" // Use += for adding to list

    productFlavors {
        create("local") {
            dimension = "version"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"
        }
        create("prod") {
            dimension = "version"
        }
    }

    testOptions {
        //All methods throw exceptions (by default). This is to make sure your unit tests only
        // test your code and do not depend on any particular behaviour of the Android platform
        // (that you have not explicitly mocked e.g. using Mockito). If that proves problematic,
        // you can add the snippet below to your build.gradle to change this behavior:
        // unitTests.isReturnDefaultValues = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "src/main/assets/")
            java.srcDirs("src/main/java")
        }
        getByName("androidTest") {
            setRoot("src/androidTest")
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    namespace = "com.ndhunju.dailyjournal"

    lint {
        disable.add("MissingTranslation")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

}

//The dependencies element is outside and after the android element. This element declares the
// dependencies for this module. The build system adds all the compile dependencies to the
// compilation classpath and includes them in the final package.
dependencies {
    // Remote binary dependency
    implementation(libs.firebase.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.play.services.drive)
    implementation(libs.androidx.legacy.support.v13)

    implementation(libs.androidx.multidex)

    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.crashlytics)

    // Fix "Duplicate class androidx.lifecycle.ViewModelLazy found in modules" error
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.google.material)
    implementation(libs.firebase.ui.auth)

    implementation(libs.play.services.ads.lite)
    implementation(libs.androidx.compose.runtime)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.annotation)
    androidTestImplementation(libs.androidx.test.ext.junit)
    // Set this dependency to use JUnit 4 rule
    androidTestImplementation(libs.androidx.test.rules)
    // Set this dependency to build and run Espresso tests
    androidTestImplementation(libs.androidx.test.espresso.core)
    // Set this dependency if you want to use Hamcrest matching
    androidTestImplementation(libs.hamcrest.library)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(project(":androidpdfwriter"))
}

// Apply this plugin at the bottom of the build.gradle file: https://developers.google.com/android/guides/google-services-plugin
apply(plugin = "com.google.gms.google-services")
