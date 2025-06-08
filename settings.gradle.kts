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
        
        // Репозиторий для RuStore (только для google flavor)
        val isFdroid = gradle.startParameter.taskRequests.any {
            it.args.any { arg -> arg.contains("fdroid", ignoreCase = true) } ||
            it.args.toString().contains("fdroid", ignoreCase = true)
        }
        
        if (!isFdroid) {
            maven { url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven") }
        }
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
