plugins {
    alias(libs.plugins.android.application) // 'com.android.application' 플러그인
    alias(libs.plugins.kotlin.android) // 'kotlin-android' 플러그인
    alias(libs.plugins.google.gms.google.services) // 'com.google.gms.google-services' 플러그인
}


android {
    namespace = "com.example.parklog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.parklog"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Firebase BoM을 통해 호환되는 버전을 자동으로 관리
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    // Firebase Storage 라이브러리 추가
    implementation("com.google.firebase:firebase-storage")
    // Firebase Realtime Database 라이브러리 추가
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.firebase.database)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation ("com.jakewharton.timber:timber:5.0.1")
    implementation ("com.github.weliem:blessed-kotlin:3.0.7")
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}