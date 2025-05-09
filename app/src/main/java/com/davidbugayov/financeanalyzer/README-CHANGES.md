# Улучшения в работе с транзакциями

## 05.04.2025

### Исправлено:

1. **Ввод десятичной точки в поле суммы:**
    - Добавлена возможность использовать как запятую, так и точку при вводе суммы транзакции
    - Любой из этих символов автоматически преобразуется в корректный формат (внутренний формат -
      точка, отображение - запятая)

2. **Улучшения при работе с диалогом успешного завершения:**
    - При добавлении новой транзакции показывается диалог успеха, затем происходит автоматический
      возврат
    - При редактировании транзакции диалог успеха не показывается - сразу происходит возврат на
      предыдущий экран
    - При нажатии на текст "Транзакция успешно добавлена" также происходит возврат на предыдущий
      экран
    - При нажатии кнопки "Готово" происходит немедленный возврат на предыдущий экран
    - Добавлена задержка (800мс) для автоматического возврата при добавлении, чтобы пользователь
      успел увидеть сообщение

### Как это работает:

1. При добавлении/редактировании транзакции:
    - При добавлении новой транзакции: После успешного сохранения показывается диалог успеха
    - При редактировании транзакции: После успешного сохранения происходит немедленный возврат
    - Пользователь может нажать "Добавить еще" для добавления новой транзакции (только при
      добавлении)
    - Пользователь может нажать "Готово" для немедленного возврата на предыдущий экран

2. При вводе суммы:
    - Пользователь может использовать как точку (.) так и запятую (,) для отделения дробной части
    - Поддерживается форматирование с разделением групп разрядов

### Ближайшие планы по улучшению:

1. Добавить больше валидаций при вводе суммы
2. Улучшить форматирование для больших сумм
3. Оптимизировать отображение для различных размеров экранов 