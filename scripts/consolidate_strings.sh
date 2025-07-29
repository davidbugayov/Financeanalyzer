#!/bin/bash
# Скрипт для автоматической консолидации дубликатов строк
# Сгенерирован автоматически

echo "Начинаем консолидацию дубликатов строк..."

# Создаем резервную копию
echo "Создаем резервную копию..."
cp -r app/src/main/res/values app/src/main/res/values.backup.$(date +%Y%m%d_%H%M%S)

# Обрабатываем дубликат: Загрузка данных......
echo 'Удаляем дубликат из feature/home/src/main/res/values/strings.xml'
# TODO: Удалить строку 'loading_data' из feature/home/src/main/res/values/strings.xml

echo "Консолидация завершена!"
echo "Не забудьте проверить результат и обновить импорты в коде."
