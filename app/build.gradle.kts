plugins {
    id("com.google.gms.google-services")

    alias(libs.plugins.android.application)

}

android {
    namespace = "com.leandroboari.autonomousvehiclesimulation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.leandroboari.autonomousvehiclesimulation"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    // Implementação da biblioteca
    implementation(project(":autonomousvehicle"))

    // Implementação do Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.junit.junit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    testImplementation("org.mockito:mockito-core:4.6.1")

}