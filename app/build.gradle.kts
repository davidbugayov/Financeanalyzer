import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Простое решение для копирования google-services.json
val debugGoogleServices = rootProject.file("google-services.debug.json")
val releaseGoogleServices = rootProject.file("google-services.release.json")
val targetGoogleServices = project.file("google-services.json")

tasks.register("copyDebugGoogleServices") {
    doLast {
        if (debugGoogleServices.exists()) {
            debugGoogleServices.copyTo(targetGoogleServices, overwrite = true)
            println("Copied debug google-services.json")
        } else {
            throw GradleException("Debug google-services.json not found at ${debugGoogleServices.absolutePath}")
        }
    }
}

tasks.register("copyReleaseGoogleServices") {
    doLast {
        if (releaseGoogleServices.exists()) {
            releaseGoogleServices.copyTo(targetGoogleServices, overwrite = true)
            println("Copied release google-services.json")
        } else {
            throw GradleException("Release google-services.json not found at ${releaseGoogleServices.absolutePath}")
        }
    }
}

// Задачи processGoogleServices создаются плагином google-services,
// поэтому нам нужно использовать afterEvaluate
afterEvaluate {
    tasks.matching { it.name == "processDebugGoogleServices" }.configureEach {
        dependsOn("copyDebugGoogleServices")
    }
    
    tasks.matching { it.name == "processReleaseGoogleServices" }.configureEach {
        dependsOn("copyReleaseGoogleServices")
    }
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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.davidbugayov.financeanalyzer"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 14
        versionName = "2.1"

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
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "DEBUG", "true")
            resValue("string", "app_name", "Finanalyzer Debug")

            // Включаем инспекцию Compose
            manifestPlaceholders["enableComposeCompilerReports"] = "true"
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
            "-Xcontext-receivers",
            // Добавляем флаги для улучшения работы Compose
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${layout.buildDirectory.asFile.get()}/compose_metrics",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${layout.buildDirectory.asFile.get()}/compose_reports",
            // Добавляем флаги для Layout Inspector
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${layout.buildDirectory.asFile.get()}/compose_stability.conf"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("META-INF/**")
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    // Явная зависимость для Layout Inspector
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.customview)
    debugImplementation(libs.androidx.customview.poolingcontainer)
    
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
    
    // PDF
    implementation(libs.pdfbox.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}