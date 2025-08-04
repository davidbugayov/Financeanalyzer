# Решение проблемы с кастомными категориями

## Проблема
При сохранении кастомной категории она не отображалась в AddEditScreen при следующем заходе на экран.

## Причина
`AppCategoriesViewModel` загружал только дефолтные категории и не загружал кастомные категории из `CategoryPreferences`.

## Решение

### 1. Создана новая реализация CategoriesViewModel
Создан класс `PersistentCategoriesViewModel`, который:
- Загружает дефолтные категории из `CategoryProvider`
- Загружает кастомные категории из `CategoryPreferences`
- Объединяет их в один список
- Сохраняет новые кастомные категории в `CategoryPreferences`

### 2. Обновлен DI модуль
В `ViewModelModule.kt` заменена реализация:
```kotlin
// Было
viewModelOf(::AppCategoriesViewModel)
singleOf(::AppCategoriesViewModel) { bind<CategoriesViewModel>() }

// Стало
viewModelOf(::PersistentCategoriesViewModel)
singleOf(::PersistentCategoriesViewModel) { bind<CategoriesViewModel>() }
```

### 3. Добавлены недостающие методы в CategoryProvider
Добавлены методы для работы с иконками и цветами:
- `getIconByName(name: String)`
- `getIconName(icon: ImageVector)`
- `parseColorFromHex(hex: String)`
- `colorToHex(color: Color)`

### 4. Обновлены зависимости
Добавлена зависимость на `data` модуль в `presentation/build.gradle.kts`:
```kotlin
implementation(project(":data"))
```

## Как это работает

1. **При создании кастомной категории:**
   - Пользователь создает кастомную категорию через UI
   - `BaseTransactionViewModel` вызывает `categoriesViewModel.addCustomCategory()`
   - `PersistentCategoriesViewModel` сохраняет категорию в `CategoryPreferences`
   - Категория добавляется в UI

2. **При загрузке экрана:**
   - `AddTransactionScreen` вызывает `viewModel.initializeScreen()`
   - `AddTransactionViewModel.loadInitialData()` подписывается на изменения категорий
   - `PersistentCategoriesViewModel` загружает дефолтные и кастомные категории
   - `BaseTransactionViewModel` получает обновленный список категорий
   - UI отображает все категории, включая кастомные

## Результат
Теперь кастомные категории:
- ✅ Сохраняются в `CategoryPreferences`
- ✅ Загружаются при инициализации экрана
- ✅ Отображаются в списке категорий
- ✅ Сохраняются между сессиями приложения 