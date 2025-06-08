import java.io.FileInputStream
import java.util.Properties
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.configurationcache.extensions.capitalized

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    
    // Применяем Firebase плагины только для google flavor
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

fun getKeystoreProperties(): Properties {
    val properties = Properties()
    val propertiesFile = rootProject.file("keystore/keystore.properties")
    if (propertiesFile.exists()) {
        properties.load(FileInputStream(propertiesFile))
    }
    return properties
}

android {
    namespace = "com.davidbugayov.financeanalyzer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.davidbugayov.financeanalyzer"
        minSdk = 26
        targetSdk = 35
        versionCode = 35
        versionName = "2.16.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Добавляем глобальные флаги для Firebase и RuStore
        buildConfigField("boolean", "USE_FIREBASE", "true")
        buildConfigField("boolean", "USE_RUSTORE", "true")

        // Enable R8 support
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    // Добавляем flavors для разных магазинов приложений
    flavorDimensions += "store"
    productFlavors {
        create("google") {
            dimension = "store"
            // Google версия использует Firebase и RuStore
            buildConfigField("boolean", "USE_FIREBASE", "true")
            buildConfigField("boolean", "USE_RUSTORE", "true")
            
            // Применяем плагины только для Google flavor
            plugins.apply("com.google.gms.google-services")
            plugins.apply("com.google.firebase.crashlytics")
        }
        create("fdroid") {
            dimension = "store"
            // F-Droid версия не использует Firebase и RuStore
            buildConfigField("boolean", "USE_FIREBASE", "false")
            buildConfigField("boolean", "USE_RUSTORE", "false")
        }
    }

    // Room schema location
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            val keystoreProperties = getKeystoreProperties()
            storeFile = file(keystoreProperties.getProperty("keystore.file", "keystore/release.keystore"))
            storePassword = keystoreProperties.getProperty("keystore.password", "")
            keyAlias = keystoreProperties.getProperty("keystore.key.alias", "")
            keyPassword = keystoreProperties.getProperty("keystore.key.password", "")
            storeType = "PKCS12"

            // Check that all required properties are present
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

            // Additional optimizations
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "DEBUG", "true")
            resValue("string", "app_name", "Finanalyzer Debug")

            // Enable Compose inspection
            manifestPlaceholders["enableComposeCompilerReports"] = "true"
        }
    }

    // Указываем исходные директории для каждого флейвора
    sourceSets {
        getByName("google") {
            java.srcDirs("src/google/java")
            res.srcDirs("src/google/res")
            manifest.srcFile("src/google/AndroidManifest.xml")
        }
        getByName("fdroid") {
            java.srcDirs("src/fdroid/java")
            res.srcDirs("src/fdroid/res")
            manifest.srcFile("src/fdroid/AndroidManifest.xml")
        }
    }

    // Compilation optimizations to speed up build
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = false
    }

    // Disable underused instrumentation tests to speed up builds
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        // Disable unused features
        aidl = false
        renderScript = false
        shaders = false
        resValues = true
    }

    // Exclude POI and related libraries from minification
    packaging {
        resources {
            excludes += "META-INF/**"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        // baseline = file("lint-baseline.xml") // Раскомментируйте и создайте файл для использования baseline
        htmlReport = true
        htmlOutput = layout.buildDirectory.file("reports/lint/lint-report.html").get().asFile
        xmlReport = true
        xmlOutput = layout.buildDirectory.file("reports/lint/lint-report.xml").get().asFile

        // Пример отключения конкретной проверки Lint (если необходимо)
        // disable.add("TypographyFractions") // Замените "TypographyFractions" на ID нужной проверки

        // Пример установки уровня серьезности для проверки
        // warning.add("ObsoleteLintCustomCheck")
    }
    buildToolsVersion = "36.0.0"
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.material)
    
    // AppMetrica для всех флейворов
    implementation(libs.appmetrica.sdk)
    
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

    // Accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    
    // Explicit dependency for Layout Inspector
    debugImplementation(libs.androidx.customview)
    debugImplementation(libs.androidx.customview.poolingcontainer)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Firebase - только для google flavor
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.analytics.ktx)
    "googleImplementation"(libs.firebase.crashlytics.ktx)
    "googleImplementation"(libs.firebase.perf.ktx)
    
    // RuStore SDK только для Google flavor
    "googleImplementation"(libs.rustore.review)
    "googleImplementation"(libs.rustore.appupdate)

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
    
    // Excel - Keep POI and related libraries from being minified
    implementation(libs.poi.core) {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "javax.xml.stream", module = "stax-api")
    }
    implementation(libs.poi.ooxml) {
        exclude(group = "stax", module = "stax-api")
        exclude(group = "javax.xml.stream", module = "stax-api")
    }

    // Testing
    testImplementation(libs.junit)

    // Add kotlinx-datetime
    implementation(libs.kotlinx.datetime)

    // Add kotlinx-serialization for kotlinx-datetime
    implementation(libs.kotlinx.serialization.core)

    // PDFBox for Android (tom_roush)
    implementation(libs.pdfbox.android)

    // Exp4j
    implementation(libs.exp4j)
}

composeCompiler {
    // Включаем генерацию отчетов компилятора Compose (полезно для анализа рекомпозиций)
    reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")

    // Включаем генерацию метрик компилятора Compose (полезно для анализа производительности)
    metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")

    // Позволяет указать файл конфигурации стабильности для классов, которые компилятор не может вывести автоматически.
    // Это может помочь уменьшить количество ненужных рекомпозиций.
    // Создайте файл stability_config.conf в корне проекта и перечислите стабильные классы,
    // например:
    // com.example.MyStableClass
    // com.example.MyOtherStableClass
    // stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_stability.conf")

    // Включение "strong skipping mode" может улучшить производительность, пропуская больше ненужных рекомпозиций,
    // но требует, чтобы все Composable функции с нестабильными параметрами были помечены как @NonRestartableComposable или @Composable (с умом).
    // strongSkipping = true // Используйте с осторожностью и после тщательного тестирования

    // Если вы используете enableComposeCompilerMetrics или enableComposeCompilerReports в BuildConfig (старый способ),
    // их можно удалить, так как эти настройки теперь здесь.
}