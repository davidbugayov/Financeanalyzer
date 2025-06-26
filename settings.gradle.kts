pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()

        // Репозиторий для RuStore SDK - используется только для rustore flavor
        // Исключаем для F-Droid сканера
        if (System.getenv("FDROID_BUILD") != "1") {
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
}

// Build cache optimization
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

// Enable modern Gradle features
@Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Set project name
rootProject.name = "FinanceAnalyzer"
include(":app")
include(":data")
include(":domain")
include(":core")
include(":navigation")
include(":utils")
include(":ui")

// Feature modules
include(":feature")
include(":feature:home")
include(":feature:profile")
include(":feature:history")
include(":feature:statistics")
include(":feature:transaction")
include(":feature:widget")

// Presentation module
include(":presentation")
