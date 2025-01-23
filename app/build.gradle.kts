plugins {
    alias(libs.plugins.android.application)
    id("maven-publish")
}

android {
    namespace = "com.ahs.easymediahelper"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.ahs.easymediahelper"
        minSdk = 24
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
    dataBinding{
        enable = true
    }



}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.areebhussainsiddiqui"
            artifactId = "EasyMediaHelper"
            version = "1.1"
        }
    }
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
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

    implementation( project(":easymediahelper"))
}