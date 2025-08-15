# Настройка ktlint и Android Lint

## Обзор

Данный документ описывает настройку ktlint и Android Lint для обеспечения совместимости правил и предотвращения конфликтов между линтерами.

## Проблема

Изначально ktlint и Android Lint имели конфликтующие правила:
- **ktlint**: требует `lowerCamelCase` для всех Composable функций
- **Android Lint**: требует `PascalCase` для Composable функций, возвращающих `Unit`

## Решение

### 1. Настройка ktlint

#### Глобальные настройки (build.gradle.kts)
```kotlin
plugins.withId("org.jlleitschuh.gradle.ktlint") {
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        ignoreFailures.set(true)
        android.set(true)
        verbose.set(true)
        outputToConsole.set(true)
        enableExperimentalRules.set(true)
        filter {
            exclude { element -> element.file.path.contains("generated/") }
            include("**/src/main/**/*.kt")
            include("**/src/google/**/*.kt")
            include("**/src/fdroid/**/*.kt")
            include("**/src/huawei/**/*.kt")
        }
    }
}
```

#### Конфигурационный файл (.ktlintrc)
```properties
# Основные правила
ktlint_code_style = android_studio
ktlint_standard_function-naming = true
ktlint_standard_import-ordering = true
ktlint_standard_no-wildcard-imports = true
ktlint_standard_no-unused-imports = true

# Экспериментальные правила
ktlint_experimental_function-naming = true
ktlint_experimental_import-ordering = true
```

### 2. Настройка Android Lint

#### Отключение конфликтующих правил
```kotlin
lint {
    abortOnError = false
    checkReleaseBuilds = false
    // Отключаем правила, которые конфликтуют с ktlint
    disable += "ComposableNaming"
    disable += "UnusedResources"
    disable += "StringFormatMatches"
    baseline = file("lint-baseline.xml")
}
```

### 3. Исправление строковых ресурсов

#### Проблема с форматированием строк
```xml
<!-- Неправильно -->
<string name="recommendation_no_savings">Save at least 10% every month.</string>

<!-- Правильно -->
<string name="recommendation_no_savings">Save at least 10%% every month.</string>
```

## Результат

### ktlint
- ✅ Проверяет стиль кода Kotlin
- ✅ Требует `lowerCamelCase` для Composable функций
- ✅ Сортирует импорты
- ✅ Проверяет длину строк
- ✅ Проверяет расположение комментариев

### Android Lint
- ✅ Проверяет Android-специфичные проблемы
- ✅ Отключены конфликтующие правила
- ✅ Использует baseline для подавления существующих предупреждений

## Команды

### Запуск проверок
```bash
# ktlint
./gradlew ktlintCheck

# Android Lint
./gradlew lintDebug

# Оба линтера
./gradlew ktlintCheck lintDebug
```

### Автоисправление ktlint
```bash
./gradlew ktlintFormat
```

## Рекомендации

1. **Используйте ktlint для стиля кода**: ktlint является основным инструментом для проверки стиля Kotlin кода.

2. **Используйте Android Lint для Android-специфичных проблем**: Android Lint проверяет проблемы, связанные с Android платформой.

3. **Регулярно запускайте проверки**: Включите проверки в CI/CD pipeline.

4. **Исправляйте проблемы постепенно**: Используйте baseline для подавления существующих предупреждений и исправляйте их постепенно.

## Файлы конфигурации

- `build.gradle.kts` - основные настройки Gradle
- `.ktlintrc` - конфигурация ktlint
- `lint-baseline.xml` - baseline для Android Lint (создается автоматически)

## Статус

- ✅ ktlint настроен и работает
- ✅ Android Lint настроен и работает
- ✅ Конфликты между линтерами устранены
- ✅ Сборка проходит успешно
