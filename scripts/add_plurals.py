#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

def add_plurals(file_path):
    """Добавляет множественные числа в XML файлы."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Добавляем plurals перед </resources>
    plurals_content = '''
    <!-- Plurals -->
    <plurals name="transactions_count">
        <item quantity="one">%d transaction</item>
        <item quantity="other">%d transactions</item>
    </plurals>
    
    <plurals name="important_count">
        <item quantity="one">%d important</item>
        <item quantity="other">%d important</item>
    </plurals>
    
    <plurals name="large_count">
        <item quantity="one">%d large</item>
        <item quantity="other">%d large</item>
    </plurals>
    
    <plurals name="small_count">
        <item quantity="one">%d small</item>
        <item quantity="other">%d small</item>
    </plurals>
    
    <plurals name="month_count">
        <item quantity="one">%d month</item>
        <item quantity="other">%d months</item>
    </plurals>
    
    <plurals name="day_count">
        <item quantity="one">%d day</item>
        <item quantity="other">%d days</item>
    </plurals>
    
    <plurals name="week_count">
        <item quantity="one">%d week</item>
        <item quantity="other">%d weeks</item>
    </plurals>
    
    <plurals name="year_count">
        <item quantity="one">%d year</item>
        <item quantity="other">%d years</item>
    </plurals>
'''
    
    # Добавляем plurals если их нет
    if '<plurals' not in content:
        content = content.replace('</resources>', plurals_content + '\n</resources>')
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Added plurals to {file_path}")
        return True
    return False

def main():
    """Основная функция для добавления множественных чисел во все XML файлы."""
    strings_files = [
        ROOT / "ui/src/main/res/values/strings.xml",
        ROOT / "ui/src/main/res/values-en/strings.xml", 
        ROOT / "ui/src/main/res/values-zh-rCN/strings.xml"
    ]
    
    fixed_count = 0
    for file_path in strings_files:
        if file_path.exists():
            if add_plurals(file_path):
                fixed_count += 1
    
    print(f"Added plurals to {fixed_count} files")

if __name__ == "__main__":
    main()
