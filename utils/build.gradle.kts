plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.davidbugayov.financeanalyzer.utils"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
            "-Xcontext-receivers",
        )
    }

    buildFeatures {
        compose = true
    }

    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }
}

dependencies {
    // Modules
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":navigation"))
    implementation(project(":ui"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.splashscreen)
    testImplementation(libs.junit)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.ui.tooling)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // JSON
    implementation(libs.gson)

    // Logging
    implementation(libs.timber)

    // Exp4j для математических выражений
    implementation(libs.exp4j)
}
