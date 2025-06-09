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
        
        // Репозиторий для RuStore SDK - используется только для rustore flavor
        maven {
            url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven")
            content {
                // Включаем репозиторий только для RuStore зависимостей
                // и только для rustore flavor
                includeGroup("ru.rustore")
                includeGroupByRegex("ru\\.rustore\\..*")
                
                // Исключаем использование для F-Droid и Google flavor
                excludeGroupByRegex(".*fdroid.*")
                excludeGroupByRegex(".*google.*")
            }
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
