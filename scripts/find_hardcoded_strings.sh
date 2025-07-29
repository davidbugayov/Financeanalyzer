#!/bin/bash

# Скрипт для поиска хардкодных строк в проекте
# Использование: ./find_hardcoded_strings.sh

echo "🔍 Поиск хардкодных строк в проекте..."
echo "======================================"

# Поиск русских строк в кавычках
echo "📝 Русские строки:"
grep -r --include="*.kt" --include="*.java" '"[А-Яа-яЁё][^"]*"' . | grep -v "//" | grep -v "import" | head -20

echo ""
echo "📝 Английские строки:"
grep -r --include="*.kt" --include="*.java" '"[A-Za-z][^"]*"' . | grep -v "//" | grep -v "import" | grep -v "package" | grep -v "TODO" | head -20

echo ""
echo "📝 Строки с цифрами:"
grep -r --include="*.kt" --include="*.java" '"[0-9][^"]*"' . | grep -v "//" | grep -v "import" | head -10

echo ""
echo "======================================"
echo "💡 Рекомендации:"
echo "1. Вынесите найденные строки в ресурсы"
echo "2. Используйте stringResource() в Compose"
echo "3. Используйте StringResourceProvider в ViewModel'ях"
echo "4. Следуйте принципам из docs/STRING_ORGANIZATION.md" 