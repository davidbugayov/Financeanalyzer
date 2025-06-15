# Проблемы после рефакторинга domain модуля

## Ошибки компиляции

1. Ошибки в аналитических классах (CalculateBalanceMetricsUseCase, CalculateCategoryStatsUseCase, GetCategoriesWithAmountUseCase)
2. Ошибки в классах экспорта (ExportTransactionsToCSVUseCase)
3. Несоответствие параметров в вызовах методов

## Задачи

1. Исправить ошибки компиляции в классах аналитики
2. Исправить ошибки компиляции в классах экспорта
3. Обновить вызовы методов в ViewModel классах
