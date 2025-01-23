plugins {
    id("com.android.library") // Change this to 'com.android.library'
    id("maven-publish")

}

android {
    namespace = "com.ahs.easymediahelper"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.exifinterface:exifinterface:1.3.6")
}