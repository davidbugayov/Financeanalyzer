import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

// Функция для определения, является ли текущая сборка F-Droid
fun isNotFDroidBuild(): Boolean {
    return System.getenv("FDROID_BUILD") != "1"
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
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.davidbugayov.financeanalyzer"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = 36
        versionName = "2.17.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Добавляем глобальные флаги для Firebase и RuStore
        buildConfigField("boolean", "USE_FIREBASE", "true")
        buildConfigField("boolean", "USE_RUSTORE", "true")
        buildConfigField(
            "boolean",
            "IS_RUSTORE_FLAVOR",
            "false",
        ) // По умолчанию false, переопределим для rustore flavor

        // Enable R8 support
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
    }

    // Добавляем flavors для разных магазинов приложений
    flavorDimensions += "store"
    productFlavors {
        create("google") {
            dimension = "store"
            // Google Play версия использует Firebase
            buildConfigField("boolean", "USE_FIREBASE", "true")
            buildConfigField("boolean", "USE_RUSTORE", "false")

            // Суффиксы для Google Play
            versionNameSuffix = ".gp"
            resValue("string", "app_name", "Деньги под Контролем")
            resValue("string", "app_store", "Google Play")
        }

        create("rustore") {
            dimension = "store"
            // RuStore версия использует Firebase и RuStore SDK
            buildConfigField("boolean", "USE_FIREBASE", "true")
            buildConfigField("boolean", "USE_RUSTORE", "true")
            buildConfigField("boolean", "IS_RUSTORE_FLAVOR", "true") // Переопределяем для rustore flavor

            // Суффиксы для RuStore
            versionNameSuffix = ".rs"
            resValue("string", "app_name", "Деньги под Контролем")
            resValue("string", "app_store", "RuStore")
        }

        create("fdroid") {
            dimension = "store"
            // F-Droid версия не использует Firebase и RuStore (в соответствии с требованиями F-Droid)
            buildConfigField("boolean", "USE_FIREBASE", "false")
            buildConfigField("boolean", "USE_RUSTORE", "false")

            // Суффиксы для F-Droid
            versionNameSuffix = ".fd"
            resValue("string", "app_name", "Деньги под Контролем (F-Droid)")
            resValue("string", "app_store", "F-Droid")
        }
    }

    // Room schema location
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            val keystoreProperties = getKeystoreProperties()
            storeFile = file(
                keystoreProperties.getProperty("keystore.file", "keystore/release.keystore"),
            )
            storePassword = keystoreProperties.getProperty("keystore.password", "")
            keyAlias = keystoreProperties.getProperty("keystore.key.alias", "")
            keyPassword = keystoreProperties.getProperty("keystore.key.password", "")
            storeType = "PKCS12"

            // Check that all required properties are present
            val requiredProperties = listOf(
                "keystore.password",
                "keystore.key.alias",
                "keystore.key.password",
            )
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
                "proguard-rules.pro",
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
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        isCoreLibraryDesugaringEnabled = false
    }

    // Disable underused instrumentation tests to speed up builds
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
        // Enable compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xcontext-receivers",
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

    // Исключаем тестовые классы из релизной сборки
    packagingOptions {
        resources {
            excludes += listOf(
                "**/*Test.class",
                "**/*Tests.class",
                "**/test/**",
                "**/androidTest/**",
                "**/*.navigation.test*",
            )
        }
    }

    buildToolsVersion = "36.0.0"
}

dependencies {
    // Modules
    val modules = listOf(
        ":domain",
        ":data",
        ":core",
        ":navigation",
        ":utils",
        ":ui",
        ":feature",
        ":feature:home",
        ":feature:profile",
        ":feature:history",
        ":feature:statistics",
        ":feature:transaction",
        ":feature:widget",
    )

    modules.forEach { module ->
        implementation(project(module))
        add("googleImplementation", project(module))
        add("rustoreImplementation", project(module))
        add("fdroidImplementation", project(module))
    }

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

    // Firebase
    // Используем Firebase BOM (Bill of Materials) для управления версиями
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    "googleImplementation"(firebaseBom)
    "rustoreImplementation"(firebaseBom)

    // Firebase Analytics (если используется)
    "googleImplementation"(libs.firebase.analytics)
    "rustoreImplementation"(libs.firebase.analytics)

    // Firebase Crashlytics
    "googleImplementation"(libs.firebase.crashlytics)
    "rustoreImplementation"(libs.firebase.crashlytics)

    // Firebase Performance
    "googleImplementation"(libs.firebase.perf)
    "rustoreImplementation"(libs.firebase.perf)

    // RuStore SDK только для RuStore флейвора
    "rustoreImplementation"(libs.rustore.review)
    "rustoreImplementation"(libs.rustore.appupdate)

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

// Настройка ktlint
ktlint {
    android.set(true)
    verbose.set(true)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
    filter {
        exclude { element -> element.file.path.contains("generated/") }
        include("**/src/main/**/*.kt")
        include("**/src/google/**/*.kt")
        include("**/src/fdroid/**/*.kt")
    }
}

// Отключаем Google Services для F-Droid флейвора
tasks.whenTaskAdded {
    if (name.contains("fdroid", ignoreCase = true) &&
        (
            name.contains("ProcessGoogleServices", ignoreCase = true) ||
                name.contains("FirebaseCrashlytics", ignoreCase = true) ||
                name.contains("FirebasePerformance", ignoreCase = true)
            )
    ) {
        enabled = false
    }
}

// Исключаем все проприетарные зависимости из F-Droid сборки
configurations.all {
    if (name.contains("fdroid", ignoreCase = true)) {
        exclude(group = "com.google.firebase")
        exclude(group = "com.google.android.gms")
        // AppMetrica разрешена в F-Droid сборке
        // exclude(group = "io.appmetrica")
        exclude(group = "ru.rustore")
    }
    // Исключаем RuStore зависимости из Google flavor
    if (name.contains("google", ignoreCase = true)) {
        exclude(group = "ru.rustore")
    }
}

// Добавляем задачу для создания релиза для RuStore
tasks.register<Copy>("prepareRuStoreRelease") {
    dependsOn("bundleRustoreRelease")

    // Получаем путь к сгенерированному AAB файлу
    val aabFile = layout.buildDirectory.file("outputs/bundle/rustoreRelease/app-rustore-release.aab")

    // Создаем новую директорию для RuStore релизов
    val ruStoreDir = layout.buildDirectory.dir("outputs/rustore")

    // Копируем AAB файл в директорию RuStore с добавлением версии
    from(aabFile) {
        rename {
            "financeanalyzer-rustore-v${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "rustore",
            ).versionNameSuffix}.aab"
        }
    }

    into(ruStoreDir)

    doLast {
        println("==========================================")
        println("RuStore Release подготовлен:")
        println(
            "Версия: ${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "rustore",
            ).versionNameSuffix} (${android.defaultConfig.versionCode})",
        )
        println("Расположение: ${ruStoreDir.get()}")
        println("==========================================")
    }
}

// Добавляем задачу для создания релиза для F-Droid
tasks.register<Copy>("prepareFDroidRelease") {
    dependsOn("bundleFdroidRelease")

    // Получаем путь к сгенерированному AAB файлу
    val aabFile = layout.buildDirectory.file("outputs/bundle/fdroidRelease/app-fdroid-release.aab")
    val apkFile = layout.buildDirectory.file("outputs/apk/fdroid/release/app-fdroid-release.apk")

    // Создаем новую директорию для F-Droid релизов
    val fdroidDir = layout.buildDirectory.dir("outputs/fdroid")

    // Копируем AAB и APK файлы в директорию F-Droid с добавлением версии
    from(aabFile) {
        rename {
            "financeanalyzer-fdroid-v${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "fdroid",
            ).versionNameSuffix}.aab"
        }
    }

    from(apkFile) {
        rename {
            "financeanalyzer-fdroid-v${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "fdroid",
            ).versionNameSuffix}.apk"
        }
    }

    into(fdroidDir)

    doLast {
        println("==========================================")
        println("F-Droid Release подготовлен:")
        println(
            "Версия: ${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "fdroid",
            ).versionNameSuffix} (${android.defaultConfig.versionCode})",
        )
        println("Расположение: ${fdroidDir.get()}")
        println("==========================================")
    }
}

// Добавляем задачу для создания релиза для Google Play
tasks.register<Copy>("prepareGooglePlayRelease") {
    dependsOn("bundleGoogleRelease")

    // Получаем путь к сгенерированному AAB файлу
    val aabFile = layout.buildDirectory.file("outputs/bundle/googleRelease/app-google-release.aab")

    // Создаем новую директорию для Google Play релизов
    val googleDir = layout.buildDirectory.dir("outputs/googleplay")

    // Копируем AAB файл в директорию Google Play с добавлением версии
    from(aabFile) {
        rename {
            "financeanalyzer-googleplay-v${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "google",
            ).versionNameSuffix}.aab"
        }
    }

    into(googleDir)

    doLast {
        println("==========================================")
        println("Google Play Release подготовлен:")
        println(
            "Версия: ${android.defaultConfig.versionName}${android.productFlavors.getByName(
                "google",
            ).versionNameSuffix} (${android.defaultConfig.versionCode})",
        )
        println("Расположение: ${googleDir.get()}")
        println("==========================================")
    }
}

// Добавляем задачу для создания всех релизов одновременно
tasks.register("prepareAllReleases") {
    dependsOn("prepareGooglePlayRelease", "prepareRuStoreRelease", "prepareFDroidRelease")

    doLast {
        println("==========================================")
        println("Все релизы подготовлены!")
        println("Версия: ${android.defaultConfig.versionName} (${android.defaultConfig.versionCode})")
        println("==========================================")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        // Add any Kotlin compiler options here
        freeCompilerArgs += listOf(
            "-Xskip-prerelease-check",
            "-Xallow-unstable-dependencies",
        )
    }
}
