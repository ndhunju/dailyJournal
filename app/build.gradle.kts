import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp") version "2.2.20-2.0.2" // this version has to match with Kotlin version
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

    fun Packaging.() {
        resources {
            excludes += "META-INF/DEPENDENCIES"
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
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("androidx.legacy:legacy-support-v13:1.0.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("com.google.api-client:google-api-client-android:2.8.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20201130-1.31.0")

    // local repo downloaded with SDK Manager
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    // Fix "Duplicate class androidx.lifecycle.ViewModelLazy found in modules" error
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("com.google.android.material:material:1.13.0")
    // Local binary dependency
    //Even though android provides json library, it bring error while using it for JUnit testing
    //implementation(fileTree(mapOf("include" to kotlin.collections.listOf("*.jar"), "dir" to "libs")))
    implementation("com.google.android.gms:play-services-ads-lite:24.3.0")
    implementation("androidx.compose.runtime:runtime:1.9.1") // Use the latest stable version

    //http://tools.android.com/tech-docs/unit-testing-support
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.19.0")
    androidTestImplementation("androidx.annotation:annotation:1.9.1")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    // Set this dependency to use JUnit 4 rule
    androidTestImplementation("androidx.test:rules:1.7.0")
    // Set this dependency to build and run Espresso tests
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    // Set this dependency to build and run UI Automator tests
    //androidTestImplementation("com.android.support.test.uiautomator:uiautomator-v18:2.1.2")
    //In order for the Android Plug-in for Gradle to correctly build and run your instrumented
    // unit tests, you must specify the following libraries in the build.gradle file of your
    // Android app module:
    // Set this dependency if you want to use Hamcrest matching
    androidTestImplementation("org.hamcrest:hamcrest-library:3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation(project(":androidpdfwriter"))
}

// Apply this plugin at the bottom of the build.gradle file: https://developers.google.com/android/guides/google-services-plugin
apply(plugin = "com.google.gms.google-services")
