# Настройка Lint Baseline для Деньги под Контролем

## Обзор
Lint Baseline позволяет зафиксировать текущие проблемы lint и фокусироваться только на новых проблемах в будущих изменениях кода.

## Конфигурация

### Глобальные настройки
- **`lint.xml`** - глобальная конфигурация для всего проекта
- **`build.gradle.kts`** - настройки на уровне проекта и пользовательские задачи

### Baseline файлы по модулям
Каждый модуль имеет свой baseline файл:
- `app/lint-baseline.xml` - основной модуль приложения
- `core/lint-baseline.xml` - ядро приложения
- `data/lint-baseline.xml` - слой данных
- `domain/lint-baseline.xml` - бизнес-логика
- `ui/lint-baseline.xml` - UI компоненты
- `feature/*/lint-baseline.xml` - feature модули

## Команды

### Генерация baseline для всех модулей
```bash
./gradlew generateLintBaseline
```

### Обновление всех baseline файлов
```bash
./gradlew updateLintBaseline
```

### Запуск lint для конкретного модуля
```bash
./gradlew :app:lintRustoreDebug
./gradlew :core:lintDebug
./gradlew :feature:transaction:lintDebug
```

### Генерация HTML отчета
```bash
./gradlew :app:lintRustoreDebug
# Отчет будет в app/build/reports/lint/lint-report.html
```

## Правила конфигурации

### Игнорируемые проблемы
- `Instantiatable` - проблемы инстанцирования классов
- `StringFormatMatches` - проблемы форматирования строк
- `InvalidPackage` - проблемы пакетов в зависимостях
- `ContentDescription` - описания для изображений
- `HardcodedText` - временно для постепенного перевода в ресурсы

### Предупреждения (не блокируют сборку)
- `MissingTranslation` - недостающие переводы
- `ExtraTranslation` - лишние переводы
- `TypographyFractions` - проблемы типографики
- `UnusedResources` - неиспользуемые ресурсы

### Ошибки (блокируют сборку)
- `HardcodedDebugMode` - захардкоженный debug режим
- `SecureRandom` - небезопасное использование Random
- `TrustAllX509TrustManager` - небезопасные сертификаты
- `Recycle` - проблемы с переработкой ресурсов

## Workflow

### При добавлении нового кода
1. Запустите lint для измененного модуля
2. Исправьте новые ошибки
3. Если нужно - добавьте новые проблемы в baseline

### При рефакторинге
1. Обновите baseline: `./gradlew updateLintBaseline`
2. Проведите рефакторинг
3. Запустите lint и исправьте новые проблемы
4. Обновите baseline при необходимости

### Периодическая очистка
1. Раз в месяц проверяйте baseline файлы
2. Исправляйте накопившиеся проблемы
3. Обновляйте baseline с исправлениями

## Интеграция с CI/CD

Добавьте в pipeline:
```bash
# Проверка lint без остановки на baseline проблемах
./gradlew lintRustoreDebug

# Проверка только новых проблем
./gradlew lintRustoreDebug --continue
```

## Советы

1. **Не игнорируйте критические проблемы** - baseline должен содержать только некритичные проблемы
2. **Регулярно обновляйте** - не позволяйте baseline "устареть"
3. **Документируйте исключения** - добавляйте комментарии к сложным правилам
4. **Используйте по модулям** - не создавайте один большой baseline для всего проекта

## Поддержка

При возникновении проблем:
1. Проверьте актуальность baseline файла
2. Убедитесь, что версия lint совместима
3. Очистите и пересоберите проект
4. При необходимости создайте новый baseline 