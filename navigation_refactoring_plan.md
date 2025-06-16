# План завершения рефакторинга навигации

В процессе переноса навигации из модуля app в модуль navigation мы столкнулись с рядом проблем. Вот план действий для завершения рефакторинга:

## 1. Обновление NavigationManager

1. NavigationManager был перемещен из `com.davidbugayov.financeanalyzer.presentation.navigation` в `com.davidbugayov.financeanalyzer.navigation`
2. Необходимо обновить все импорты NavigationManager в ViewModels и других классах

## 2. Обновление Screen

1. Screen был перемещен из `com.davidbugayov.financeanalyzer.presentation.navigation` в `com.davidbugayov.financeanalyzer.navigation`
2. Константы для аргументов навигации теперь находятся в companion object класса Screen
3. Необходимо обновить все импорты Screen в ViewModels и других классах

## 3. Обновление AppNavHostImpl

1. AppNavHostImpl в модуле app должен быть помечен как @Composable функция
2. Необходимо обновить импорты в AppNavHostImpl

## 4. Обновление PeriodType

1. PeriodType был перемещен из `com.davidbugayov.financeanalyzer.presentation.history.model` в `com.davidbugayov.financeanalyzer.navigation.model`
2. Необходимо обновить все импорты PeriodType в классах, которые его используют

## 5. Обновление AppModule

1. Необходимо обновить предоставление NavigationManager в DI модуле

## Рекомендации

1. Использовать Android Studio для автоматического обновления импортов
2. Запустить сборку проекта после исправления каждой группы ошибок для проверки прогресса
3. Рассмотреть возможность использования инструментов рефакторинга IDE для массового обновления импортов

После выполнения этих шагов навигация будет полностью перенесена из модуля app в модуль navigation, что улучшит архитектуру приложения и сделает его более модульным. 