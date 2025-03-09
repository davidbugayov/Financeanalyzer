import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
}

fun getKeystoreProperties(): Properties {
    val properties = Properties()
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.exists()) {
        properties.load(FileInputStream(propertiesFile))
    }
    return properties
}

android {
    namespace = "com.davidbugayov.financeanalyzer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.davidbugayov.financeanalyzer"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Включаем поддержку R8
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    signingConfigs {
        create("release") {
            val keystoreProperties = getKeystoreProperties()
            storeFile = file(keystoreProperties.getProperty("keystore.file", "keystore/release.keystore"))
            storePassword = keystoreProperties.getProperty("keystore.password", "")
            keyAlias = keystoreProperties.getProperty("keystore.key.alias", "")
            keyPassword = keystoreProperties.getProperty("keystore.key.password", "")
            storeType = "PKCS12"

            // Проверка наличия всех необходимых свойств
            val requiredProperties = listOf("keystore.password", "keystore.key.alias", "keystore.key.password")
            requiredProperties.forEach { prop ->
                if (!keystoreProperties.containsKey(prop)) {
                    throw GradleException("Missing required keystore property: $prop")
                }
            }
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
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            multiDexEnabled = false
            buildConfigField("boolean", "DEBUG", "false")

            // Дополнительные оптимизации
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "DEBUG", "true")
            // Добавляем правила ProGuard и для debug сборки
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }
    }

    flavorDimensions += "environment"
    
    productFlavors {
        create("dev") {
            dimension = "environment"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Finance Analyzer Dev")
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.financeanalyzer.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            
            // Дополнительные настройки для dev
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
            manifestPlaceholders["analyticsCollectionEnabled"] = "false"
        }
        
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "Деньги под Контролем")
            buildConfigField("String", "API_BASE_URL", "\"https://api.financeanalyzer.com/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            
            // Дополнительные настройки для prod
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
            manifestPlaceholders["analyticsCollectionEnabled"] = "true"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Включаем оптимизации компилятора
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/proguard/**",
                "META-INF/versions/**",
                "META-INF/web-fragment.xml",
                "META-INF/androidx.*",
                "META-INF/services/kotlin.*"
            )
            pickFirsts += listOf(
                "META-INF/proguard/gson.pro"
            )
        }
        dex {
            useLegacyPackaging = false
        }
    }

    lint {
        disable += "FlowOperatorInvokedInComposition"
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)

    // Logging
    implementation(libs.timber)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // JSON
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}