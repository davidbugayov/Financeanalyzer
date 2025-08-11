import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    // For iOS frameworks publishing
    id("com.android.library")
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    androidTarget()

    val iosX64Target = iosX64()
    val iosArm64Target = iosArm64()
    val iosSimArm64Target = iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "FinanceAnalyzer shared logic"
        homepage = "https://github.com/davidbugayov/FinanceAnalyzer"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "shared"
            isStatic = false
        }
    }

    // Configure iOS frameworks + XCFramework
    val xcf = XCFramework()
    listOf(iosX64Target, iosArm64Target, iosSimArm64Target).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = false
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines.android)
            }
        }
        val androidUnitTest by getting
        // iOS source sets hierarchy is configured by default template
    }
}

android {
    namespace = "com.davidbugayov.financeanalyzer.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

