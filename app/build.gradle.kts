plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    kotlin("plugin.serialization") version "1.9.0"

}

android {
    namespace = "com.ugraks.project1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ugraks.project1"
        minSdk = 29
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.navigation:navigation-compose:2.8.0-beta01")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation ("io.coil-kt:coil-compose:2.2.2")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.material:material:1.1.0")

    implementation ("com.google.accompanist:accompanist-permissions:0.27.0")

    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")



}