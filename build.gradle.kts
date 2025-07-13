// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
}

subprojects {
    // Унифицируем JVM target и общие compilerArgs для всех Kotlin-проектов
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
            compilerOptions {
                // Используем версию Java из libs.versions.toml (21)
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(libs.versions.javaVersion.get()))
                freeCompilerArgs.addAll(
                    listOf(
                        "-Xcontext-parameters",
                        "-opt-in=kotlin.RequiresOptIn",
                        "-Xjvm-default=all",
                    )
                )
            }
        }
    }
    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            ignoreFailures.set(true)
        }
    }
}

// Добавляем задачу для запуска lint для всех модулей
tasks.register("lintAll") {
    group = "verification"
    description = "Runs lint for all modules"
    
    // Делаем задачу зависимой от lint задач всех модулей
    dependsOn(
        ":app:lintRustoreDebug",
        ":core:lintDebug", 
        ":data:lintDebug",
        ":domain:lintDebug",
        ":ui:lintDebug",
        ":utils:lintDebug",
        ":navigation:lintDebug",
        ":presentation:lintDebug",
        ":feature:lintDebug",
        ":feature:home:lintDebug",
        ":feature:budget:lintDebug",
        ":feature:transaction:lintDebug",
        ":feature:history:lintDebug",
        ":feature:statistics:lintDebug",
        ":feature:profile:lintDebug",
        ":feature:onboarding:lintDebug",
        ":feature:widget:lintDebug"
    )
    
    doLast {
        println("✅ Lint проверка завершена для всех модулей!")
    }
}

// Добавляем задачу для очистки всех baseline файлов и их пересоздания
tasks.register("resetLintBaseline") {
    group = "verification"
    description = "Resets all lint baseline files by deleting them and running fresh lint"
    
    doLast {
        println("Resetting lint baseline files...")
        
        val modules = listOf(
            "app", "core", "data", "domain", "ui", "utils", "navigation", "presentation",
            "feature", "feature:home", "feature:budget", "feature:transaction", 
            "feature:history", "feature:statistics", "feature:profile", 
            "feature:onboarding", "feature:widget"
        )
        
        modules.forEach { module ->
            val baselineFile = file("$module/lint-baseline.xml")
            if (baselineFile.exists()) {
                baselineFile.delete()
                println("Deleted baseline for $module")
            }
        }
        
        println("All lint baseline files have been reset!")
        println("Run './gradlew lintAll' to generate fresh baselines")
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
    }
    dependencies {
        classpath(libs.agconnect.gradle.plugin)
        // AGConnect plugin expects explicit AGP classpath
        classpath("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}")
    }
}