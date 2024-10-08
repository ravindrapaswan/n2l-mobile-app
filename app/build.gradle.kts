@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

apply(plugin = "kotlin-kapt")
apply(plugin = "kotlin-parcelize")

android {
    namespace = "practice.english.n2l"
    compileSdk = 34

    defaultConfig {
        applicationId = "practice.english.n2l"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjvm-default=all"
    }
    buildFeatures {
        dataBinding = true // for data binding
        viewBinding = true // for view binding
    }
    splits{
        abi{
            isEnable=true
            reset()
            include("armeabi", "armeabi-v7a", "arm64-v8a", "mips", "x86", "x86_64", "arm64-v8a")
            isUniversalApk=true
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.signalr.messagepack)
    implementation(libs.signalr)
    implementation(libs.eventbus)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.adapter.rxjava3)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    "kapt"(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.glide)
    implementation(libs.mobile.ffmpeg.full.gpl)
    implementation(libs.commons.codec)
    //annotationProcessor(libs.compiler)
    "kapt"(libs.compiler)
    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)
}