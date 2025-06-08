pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // Репозиторий для RuStore SDK - используется только для google flavor
        // Отключен для соответствия требованиям F-Droid
        // maven {
        //    url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven")
        // }
    }
}

// Build cache optimization
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

// Enable modern Gradle features
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Set project name
rootProject.name = "FinanceAnalyzer"
include(":app")
