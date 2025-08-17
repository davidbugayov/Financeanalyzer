#!/usr/bin/env python3
"""
Скрипт для исправления аннотаций и других ktlint предупреждений
"""

import os
import re

def fix_annotations(file_path):
    """Добавляет перенос строки после последней аннотации"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Паттерн для поиска аннотаций перед функцией
    pattern = r'(@[A-Za-z0-9_]+(?:\([^)]*\))?)\s*\n(@Composable\s+fun)'
    replacement = r'\1\n\n\2'
    content = re.sub(pattern, replacement, content)
    
    # Паттерн для поиска аннотаций перед функцией без @Composable
    pattern2 = r'(@[A-Za-z0-9_]+(?:\([^)]*\))?)\s*\n(fun\s+[A-Z])'
    replacement2 = r'\1\n\n\2'
    content = re.sub(pattern2, replacement2, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_parameter_spacing(file_path):
    """Исправляет пробелы в параметрах"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Исправляем пробелы в параметрах
    pattern = r'(\w+:\s*[A-Za-z<>,\s]+)(\s*,\s*)(\w+:\s*[A-Za-z<>,\s]+)'
    replacement = r'\1, \3'
    content = re.sub(pattern, replacement, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_unused_imports(file_path):
    """Удаляет неиспользуемые импорты"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Удаляем неиспользуемые импорты (простая эвристика)
    lines = content.split('\n')
    filtered_lines = []
    
    for line in lines:
        if line.strip().startswith('import ') and 'unused' not in line.lower():
            # Проверяем, используется ли импорт в коде
            import_name = line.split('import ')[1].split(' as ')[0].split('.')[-1]
            if import_name in content.replace(line, ''):
                filtered_lines.append(line)
        else:
            filtered_lines.append(line)
    
    content = '\n'.join(filtered_lines)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    """Основная функция"""
    # Список файлов с проблемами аннотаций
    annotation_files = [
        'feature/home/src/main/java/com/davidbugayov/financeanalyzer/presentation/home/components/HomeTipsCard.kt',
        'feature/home/src/main/java/com/davidbugayov/financeanalyzer/presentation/home/components/NotificationPermissionDialog.kt',
        'feature/history/src/main/java/com/davidbugayov/financeanalyzer/presentation/history/TransactionHistoryScreen.kt',
        'feature/budget/src/main/java/com/davidbugayov/financeanalyzer/presentation/budget/BudgetScreen.kt',
        'feature/budget/src/main/java/com/davidbugayov/financeanalyzer/presentation/budget/setup/WalletSetupScreen.kt',
        'feature/budget/src/main/java/com/davidbugayov/financeanalyzer/presentation/budget/subwallets/SubWalletsScreen.kt',
        'feature/budget/src/main/java/com/davidbugayov/financeanalyzer/presentation/budget/subwallets/components/SubWalletComponents.kt',
        'feature/budget/src/main/java/com/davidbugayov/financeanalyzer/presentation/budget/wallet/WalletTransactionsScreen.kt',
        'feature/profile/src/main/java/com/davidbugayov/financeanalyzer/feature/profile/components/NotificationSettingsDialog.kt',
        'feature/profile/src/main/java/com/davidbugayov/financeanalyzer/feature/profile/components/TimePickerDialog.kt',
        'feature/profile/src/main/java/com/davidbugayov/financeanalyzer/feature/profile/libraries/LibrariesScreen.kt',
        'feature/statistics/src/main/java/com/davidbugayov/financeanalyzer/presentation/chart/detail/FinancialDetailStatisticsScreen.kt',
        'feature/statistics/src/main/java/com/davidbugayov/financeanalyzer/presentation/chart/statistic/FinancialStatisticsScreen.kt',
        'ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/AppTopBar.kt',
        'ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/DatePickerDialog.kt',
        'ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/card/AdviceCard.kt',
        'ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/dialog/DatePickerDialog.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/base/components/CategorySection.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/base/components/ColorPickerDialog.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/base/components/SourceItem.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/base/components/SourcePickerDialog.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/base/components/WalletSelectorDialog.kt',
    ]
    
    # Список файлов с проблемами пробелов в параметрах
    spacing_files = [
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/add/model/AddTransactionState.kt',
        'feature/transaction/src/main/java/com/davidbugayov/financeanalyzer/feature/transaction/edit/model/EditTransactionState.kt',
    ]
    
    # Список файлов с неиспользуемыми импортами
    unused_imports_files = [
        'ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/NumberTextField.kt',
    ]
    
    print("Исправляю аннотации...")
    for file_path in annotation_files:
        if os.path.exists(file_path):
            print(f"Обрабатываю: {file_path}")
            try:
                fix_annotations(file_path)
            except Exception as e:
                print(f"Ошибка при обработке {file_path}: {e}")
    
    print("Исправляю пробелы в параметрах...")
    for file_path in spacing_files:
        if os.path.exists(file_path):
            print(f"Обрабатываю: {file_path}")
            try:
                fix_parameter_spacing(file_path)
            except Exception as e:
                print(f"Ошибка при обработке {file_path}: {e}")
    
    print("Исправляю неиспользуемые импорты...")
    for file_path in unused_imports_files:
        if os.path.exists(file_path):
            print(f"Обрабатываю: {file_path}")
            try:
                fix_unused_imports(file_path)
            except Exception as e:
                print(f"Ошибка при обработке {file_path}: {e}")

if __name__ == "__main__":
    main()
