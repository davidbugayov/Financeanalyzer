plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.davidbugayov.financeanalyzer.feature.transaction"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Expose flavor constant so library can check at runtime
        buildConfigField("boolean", "IS_RUSTORE_FLAVOR", "false")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget
                    .fromTarget(libs.versions.javaVersion.get()),
            )
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xjvm-default=all",
                ),
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        // Отключаем правила, которые конфликтуют с ktlint
        disable += "ComposableNaming"
        disable += "UnusedResources"
        disable += "StringFormatMatches"
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":ui"))
    implementation(project(":navigation"))
    implementation(project(":utils"))
    implementation(project(":feature"))
    implementation(project(":feature:widget"))
    implementation(project(":presentation"))
    implementation(project(":shared"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.ui.tooling)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Logging
    implementation(libs.timber)

    // PDF
    implementation(libs.pdfbox.android)

    // Excel
    implementation(libs.poi.core)
    implementation(libs.poi.ooxml)

    // Document Parsing
    implementation(libs.apache.commons.csv)
    implementation(libs.commons.io)

    // Kotlin Flow

    // Kotlin DateTime
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)

    // Math expression parser
    implementation(libs.exp4j)
}
