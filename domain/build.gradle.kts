plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.davidbugayov.financeanalyzer.domain"
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
    }
}

dependencies {
    // Modules
    implementation(project(":core"))
    
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Koin
    implementation(libs.koin.android)

    // Logging
    implementation(libs.timber)

    // Document parsing & PDF/Excel libraries
    implementation(libs.paging.runtime)
}
