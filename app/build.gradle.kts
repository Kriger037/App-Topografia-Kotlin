import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()){
    localPropertiesFile.inputStream().use {stream ->
        localProperties.load(stream)
    }
}

android {
    namespace = "com.felipe.topografiaapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures{
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.felipe.topografiaapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"${localProperties.getProperty("SERVER_BASE_URL", "http://localhost/")}\"")
        manifestPlaceholders["mapApiKey"] = localProperties.getProperty("MAPS_API_KEY", "")
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
    //Retrofit (para conectar a Internet)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //GSON converter (para traducir retrofit a Kotlin)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Libreria de Google maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}