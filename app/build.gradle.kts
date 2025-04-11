plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Version catalog kullanıyorsanız aşağıdaki gibi değiştirin
    // alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.materialdesign.pomodoroapp"
    compileSdk = 35 // 35 yerine stabil olan 34 kullanmanızı öneririm

    defaultConfig {
        applicationId = "com.materialdesign.pomodoroapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
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
        viewBinding = true
        // Eğer Compose kullanıyorsanız:
        // compose = true
    }
    // Eğer Compose kullanıyorsanız:
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.3"
    // }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Eğer version catalog kullanmak isterseniz (libs.versions.toml tanımlıysa):
    // implementation(libs.androidx.core.ktx)
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material)
    // implementation(libs.androidx.constraintlayout)
}