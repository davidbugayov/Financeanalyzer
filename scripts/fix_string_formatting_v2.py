#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

def fix_string_formatting_v2(file_path):
    """Исправляет проблемы с форматированием строк в XML файлах."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Исправляем проблемы с форматированием
    # 1. Исправляем неправильные форматы с множественными параметрами
    content = re.sub(r'%1\$s\s*-\s*%1\$s', r'%1$s - %2$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s', r'%1$s, %2$s', content)
    
    # 2. Исправляем проблемы с процентами
    content = re.sub(r'%(\d+)\$s\s*%%', r'%1$s%%', content)
    content = re.sub(r'%%%', r'%%', content)
    
    # 3. Исправляем проблемы с пробелами в форматировании
    content = re.sub(r'%(\d+)\$\s*s', r'%1$s', content)
    content = re.sub(r'%(\d+)\$\s*d', r'%1$d', content)
    
    # 4. Исправляем проблемы с форматированием месяцев
    content = re.sub(r'%1\$s\s+month\.', r'%1$d month.', content)
    
    # 5. Исправляем проблемы с множественными параметрами в логах
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*,\s*%1\$s', r'%1$s, %2$s, %3$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s', r'%1$s, %2$s', content)
    
    # 6. Исправляем проблемы с форматированием в CSV
    content = re.sub(r'%1\$s\s*,\s*size:\s*%1\$s', r'%1$s, size: %2$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*missed', r'%1$s, %2$s missed', content)
    
    # 7. Исправляем проблемы с форматированием в рекомендациях
    content = re.sub(r'%1\$s\.\s*Current\s*progress:\s*%1\$s', r'%1$s. Current progress: %2$s', content)
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed formatting in {file_path}")
        return True
    return False

def main():
    """Основная функция для исправления форматирования во всех XML файлах."""
    strings_files = [
        ROOT / "ui/src/main/res/values/strings.xml",
        ROOT / "ui/src/main/res/values-en/strings.xml", 
        ROOT / "ui/src/main/res/values-zh-rCN/strings.xml"
    ]
    
    fixed_count = 0
    for file_path in strings_files:
        if file_path.exists():
            if fix_string_formatting_v2(file_path):
                fixed_count += 1
    
    print(f"Fixed formatting in {fixed_count} files")

if __name__ == "__main__":
    main()

