plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // 使用 kapt 插件处理 Room 编译器注解
    kotlin("kapt")
}

android {
    namespace = "com.sanshuiqimu.bill"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sanshuiqimu.bill"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.0Beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    }

    // 启用 Compose 与 buildFeatures
    buildFeatures {
        compose = true
    }

    composeOptions {
        // Kotlin 1.9.20 对应的 Compose Compiler 版本为 1.5.4
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM 统一管理 Compose 库版本
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose UI 与 Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Material Icons Extended（提供 TrendingUp/Down, BarChart 等扩展图标）
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // collectAsStateWithLifecycle 所需
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coil 图片加载
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Kotlin 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 单元测试
    testImplementation("junit:junit:4.13.2")
    // Android 仪器测试
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // Compose 调试工具
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
