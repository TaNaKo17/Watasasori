plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "jp.ac.meijo.android.wata_whether"
    compileSdk = 36

    defaultConfig {
        applicationId = "jp.ac.meijo.android.wata_whether"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    // Java desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // 基本ライブラリ
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit + Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson 本体（必要）
    implementation("com.google.code.gson:gson:2.11.0")

    // OkHttp（Retrofit が内部で使う）
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Unit Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}