# План внедрения аналитики в приложение

## 1. Анализ неиспользуемых методов

### ErrorTracker
- `trackValidationError` - не используется
- `trackNetworkError` - не используется
- `trackDatabaseError` - не используется
- `trackException` - используется только в ProfileScreen

### PerformanceMetrics
- `trackBackgroundTask` - не используется
- `trackFrameMetrics` - не используется
- `trackNetworkCall` - не используется
- `trackMemoryUsage` - используется только в BaseFinanceApp

### UserEventTracker
- `trackAppRating` - не используется
- `trackUserFeedback` - не используется
- `sendSessionStats` - используется только в BaseFinanceApp

## 2. План внедрения

### Экраны приложения
1. **HomeScreen**:
   - `trackScreenOpen`/`trackScreenClose`
   - `startScreenLoadTiming`/`endScreenLoadTiming`
   - `trackFeatureUsage` для основных действий
   - `trackUserAction` для кнопок

2. **TransactionHistoryScreen**:
   - `trackScreenOpen`/`trackScreenClose`
   - `startScreenLoadTiming`/`endScreenLoadTiming`
   - `trackFeatureUsage` для фильтрации и поиска

3. **AddTransactionScreen**:
   - `trackScreenOpen`/`trackScreenClose`
   - `trackFeatureUsage` для добавления транзакции
   - `trackValidationError` для ошибок валидации

4. **EditTransactionScreen**:
   - Дополнить существующую аналитику
   - Добавить `trackValidationError`

5. **BudgetScreen**:
   - `trackScreenOpen`/`trackScreenClose`
   - `trackFeatureUsage` для управления бюджетом

6. **StatisticsScreen**:
   - `trackScreenOpen`/`trackScreenClose`
   - `trackFeatureUsage` для просмотра статистики
   - `trackAction` для измерения времени генерации графиков

### Слой данных
1. **Репозитории**:
   - `trackDatabaseError` для ошибок БД
   - `startDbOperation`/`endDbOperation` для измерения производительности

2. **Сетевые запросы**:
   - `trackNetworkCall` для измерения времени запросов
   - `trackNetworkError` для ошибок сети

3. **Фоновые задачи**:
   - `trackBackgroundTask` для длительных операций
   - `trackMemoryUsage` для мониторинга памяти

### Общие компоненты
1. **Диалоги**:
   - `trackUserAction` для действий в диалогах
   - `trackUserFeedback` для диалогов обратной связи

2. **Виджеты**:
   - `trackFeatureUsage` для использования виджетов
   - `trackAction` для измерения времени обновления

3. **Рейтинг приложения**:
   - `trackAppRating` для отслеживания оценок

## 3. Приоритеты внедрения
1. **Высокий приоритет**:
   - HomeScreen (основной экран приложения)
   - AddTransactionScreen (критическая функциональность)
   - Репозитории (для отслеживания ошибок БД)

2. **Средний приоритет**:
   - TransactionHistoryScreen
   - StatisticsScreen
   - Сетевые запросы

3. **Низкий приоритет**:
   - BudgetScreen
   - Диалоги
   - Виджеты

## 4. Методы, которые можно удалить при необходимости
- `trackFrameMetrics` - если не планируется отслеживание производительности UI
- `getFeatureLastUsed`/`getFeatureUsageCount` - если не используются для аналитики внутри приложения 