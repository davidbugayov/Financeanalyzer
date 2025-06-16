# Следующие шаги для завершения рефакторинга навигации

Мы успешно перенесли основные классы навигации из модуля app в модуль navigation:

1. Создали класс `Screen` в модуле navigation
2. Создали класс `NavigationManager` в модуле navigation
3. Создали `AppNavHost` и `AppNavigation` в модуле navigation
4. Создали `AppNavHostImpl` в модуле app
5. Перенесли `PeriodType` в модуль navigation

Однако, есть еще несколько шагов, которые нужно выполнить для завершения рефакторинга:

## Шаги для завершения рефакторинга

1. **Обновить импорты во всех файлах**:
   - Заменить `import com.davidbugayov.financeanalyzer.presentation.navigation.NavigationManager` на `import com.davidbugayov.financeanalyzer.navigation.NavigationManager`
   - Заменить `import com.davidbugayov.financeanalyzer.presentation.navigation.Screen` на `import com.davidbugayov.financeanalyzer.navigation.Screen`
   - Заменить `import com.davidbugayov.financeanalyzer.presentation.history.model.PeriodType` на `import com.davidbugayov.financeanalyzer.navigation.model.PeriodType`

2. **Обновить DI модуль**:
   - Обновить предоставление `NavigationManager` в `AppModule`

3. **Обновить ссылки на константы**:
   - Использовать константы из `Screen.companion object` вместо констант из конкретных объектов Screen

## Как это сделать

Лучше всего использовать инструменты рефакторинга Android Studio:

1. Открыть Android Studio
2. Использовать функцию "Find in Path" (Ctrl+Shift+F или Cmd+Shift+F)
3. Найти все импорты старых классов и заменить их на новые
4. Запустить сборку проекта и исправить оставшиеся ошибки

## Что уже сделано

- Создана структура классов в модуле navigation
- Удален дублирующийся класс Screen из модуля app
- Создан класс PeriodType в модуле navigation
- Реализован AppNavHostImpl в модуле app

После выполнения этих шагов навигация будет полностью перенесена из модуля app в модуль navigation, что улучшит архитектуру приложения и сделает его более модульным. 