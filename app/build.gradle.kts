plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ecommerce"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.ecommerce"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 10.0.2.2 = mÃ¡y host nhÃ¬n tá»« emulator; Ä‘á»•i sang IP mÃ¡y tÃ­nh khi cháº¡y mÃ¡y tháº­t
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")

        // true = dÃ¹ng dá»¯ liá»‡u giáº£ Ä‘á»ƒ dá»±ng giao diá»‡n khi backend chÆ°a cháº¡y (xem repository/MockDataSource.java)
        // false = gá»i backend tháº­t. Äá»•i xong báº¥m Sync Project.
        buildConfigField("boolean", "USE_MOCK_DATA", "true")
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.glide)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
