# Исправление проблемы редактирования транзакций

## Проблема
Редактирование транзакций работало нестабильно - из-за клика только иногда удавалось перейти к редактированию.

## Внесённые изменения

### 1. EditTransactionViewModel.kt
**Файл**: `feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/edit/EditTransactionViewModel.kt`

**Проблема**: Метод `loadTransactionForEditById` использовал задержку (500ms) для ожидания загрузки транзакции, что было ненадёжно.

**Решение**:
- Убрана задержка `kotlinx.coroutines.delay(500)`
- Загрузка транзакции теперь выполняется синхронно в одном suspend-блоке
- Добавлена детальная обработка ошибок и логирование
- Транзакция загружается напрямую через `sharedFacade.loadTransactions()` и `getTransactionById()`
- Добавлена очистка ошибок при начале загрузки (`error = null`)
- Флаг `editMode` устанавливается явно в `false` при ошибке

**Ключевые изменения**:
```kotlin
// Было: использование устаревшего метода loadTransaction + delay
loadTransaction(transactionId)
kotlinx.coroutines.delay(500)
val transaction = _state.value.transactionToEdit

// Стало: прямая загрузка с обработкой результата
val allTransactions = sharedFacade.loadTransactions()
val transaction = sharedFacade.getTransactionById(allTransactions, transactionId)
if (transaction != null) {
    val domainTransaction = transaction.toDomain()
    // Устанавливаем состояние и загружаем данные
}
```

### 2. EditTransactionScreen.kt
**Файл**: `feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/edit/EditTransactionScreen.kt`

**Проблема**: Форма редактирования показывалась даже если данные ещё не загружены.

**Решение**:
- Добавлена проверка состояния перед отображением формы
- Показывается `CircularProgressIndicator` пока `isLoading && !editMode`
- Показывается сообщение об ошибке если `state.error != null`
- Форма показывается только когда `editMode && transactionToEdit != null`
- `LaunchedEffect` теперь зависит от `transactionId` вместо `Unit` для правильной перезагрузки

### 3. HomeViewModel.kt
**Файл**: `feature/home/src/main/java/com/davidbugayov/financeanalyzer/presentation/home/HomeViewModel.kt`

**Проблема**: Недостаточно логирования для отладки навигации.

**Решение**:
- Добавлено детальное логирование в обработчике `HomeEvent.EditTransaction`
- Логируются: ID транзакции, маршрут навигации, сумма и категория

### 4. HomeScreen.kt
**Файл**: `feature/home/src/main/java/com/davidbugayov/financeanalyzer/presentation/home/HomeScreen.kt`

**Проблема**: Диалог закрывался сразу после клика на "Редактировать".

**Решение**:
- Добавлено логирование клика на редактирование
- Диалог закрывается после вызова события (навигация обрабатывается асинхронно через NavigationManager)

## Технические детали

### Почему это работает надёжнее:
1. **Синхронная загрузка**: Вместо запуска отдельной корутины и ожидания с задержкой, теперь всё выполняется в одной suspend-функции
2. **Явное управление состоянием**: Флаги `isLoading`, `editMode`, `error` устанавливаются явно на каждом этапе
3. **Условный рендеринг**: Форма показывается только когда данные действительно загружены
4. **Улучшенное логирование**: Позволяет отследить весь путь от клика до загрузки данных

### Зависимости между изменениями:
```
HomeScreen (клик)
  → HomeViewModel (событие EditTransaction)
    → NavigationManager (навигация)
      → EditTransactionScreen (рендеринг)
        → EditTransactionViewModel.loadTransactionForEditById (загрузка)
          → baseTransactionScreen (форма редактирования)
```

## Обновление зависимостей

В файле `gradle/libs.versions.toml`:
- junit: 4.13.1 → 4.13.2
- firebasePerfPlugin: 2.0.0 → 2.1.0 (для совместимости с AGP 9.0)

## Тестирование

После применения исправлений рекомендуется:
1. Очистить кеш: `./gradlew clean`
2. Пересобрать проект: `./gradlew assembleRustoreDebug`
3. Проверить логи при клике на транзакцию (тег "ТРАНЗАКЦИЯ")
4. Убедиться что форма редактирования загружается с данными

## Логи для отладки

При успешной работе в логах должна быть последовательность:
```
HomeScreen: Редактирование транзакции ID=<id>
HomeViewModel: Навигация к редактированию транзакции - ID=<id>, route=edit/<id>
ТРАНЗАКЦИЯ-ЭКРАН: Загрузка транзакции с ID: <id>
ТРАНЗАКЦИЯ: Начало загрузки транзакции ID=<id>
ТРАНЗАКЦИЯ: Всего транзакций загружено: <count>
ТРАНЗАКЦИЯ: Транзакция найдена - id=<id>, сумма=<amount>, категория=<category>
ТРАНЗАКЦИЯ: Загрузка завершена успешно - editMode=true, сумма=<amount>
ТРАНЗАКЦИЯ-ЭКРАН: editMode=true, transactionToEdit=<id>, amount=<amount>
```
