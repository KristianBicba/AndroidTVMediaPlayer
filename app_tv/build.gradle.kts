plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "tpo.mediaplayer.app_tv"
    compileSdk = 33

    defaultConfig {
        applicationId = "tpo.mediaplayer.app_tv"
        minSdk = 28
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")

    implementation("com.google.android.exoplayer:exoplayer:2.18.2")

    implementation(project(":lib_communications"))
    implementation(project(":lib_vfs"))
}
