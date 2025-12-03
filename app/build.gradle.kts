plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.luz.mylibrary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.luz.mylibrary"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Retrofit para API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Gson para JSON
    implementation("com.google.code.gson:gson:2.8.8")
    // Logging interceptor (opcional, para debugging)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

}