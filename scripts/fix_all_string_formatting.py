#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

def fix_string_formatting(file_path):
    """Исправляет все проблемы с форматированием строк в XML файлах."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Исправляем проблемы с форматированием
    # 1. Исправляем неправильные форматы с множественными параметрами
    content = re.sub(r'%1\$s\s*-\s*%1\$s', r'%1$s - %2$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s', r'%1$s, %2$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*,\s*%1\$s', r'%1$s, %2$s, %3$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*,\s*%1\$s\s*,\s*%1\$s', r'%1$s, %2$s, %3$s, %4$s', content)
    
    # 2. Исправляем проблемы с процентами
    content = re.sub(r'%(\d+)\$s\s*%%', r'%1$s%%', content)
    content = re.sub(r'%%%', r'%%', content)
    
    # 3. Исправляем проблемы с пробелами в форматировании
    content = re.sub(r'%(\d+)\$\s*s', r'%1$s', content)
    content = re.sub(r'%(\d+)\$\s*d', r'%1$d', content)
    
    # 4. Исправляем проблемы с форматированием месяцев
    content = re.sub(r'%1\$s\s+month\.', r'%1$d month.', content)
    
    # 5. Исправляем проблемы с форматированием в CSV
    content = re.sub(r'%1\$s\s*,\s*size:\s*%1\$s', r'%1$s, size: %2$s', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*missed', r'%1$s, %2$s missed', content)
    
    # 6. Исправляем проблемы с форматированием в рекомендациях
    content = re.sub(r'%1\$s\.\s*Current\s*progress:\s*%1\$s', r'%1$s. Current progress: %2$s', content)
    
    # 7. Исправляем проблемы с форматированием в логах
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*byte', r'%1$s, %2$s byte', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*from', r'%1$s, %2$s from', content)
    
    # 8. Исправляем проблемы с форматированием в импорте
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*missed\s*from', r'%1$s, %2$s missed from', content)
    content = re.sub(r'%1\$s\s*,\s*%1\$s\s*missed', r'%1$s, %2$s missed', content)
    
    # 9. Исправляем проблемы с форматированием в неделях
    content = re.sub(r'Week\s*%1\$s\s*,\s*%1\$s', r'Week %1$s, %2$s', content)
    
    # 10. Исправляем проблемы с форматированием в CSV ошибках
    content = re.sub(r'Line:\s*%1\$s', r'Line: %2$s', content)
    content = re.sub(r'Line:\s*%1\$s\s*\.', r'Line: %2$s.', content)
    
    # 11. Исправляем проблемы с форматированием в достижениях
    content = re.sub(r'%1\$s%%\s*вашего\s*дохода\s*в\s*течение\s*%1\$d', r'%1$s%% вашего дохода в течение %2$d', content)
    content = re.sub(r'%1\$s\s*различных\s*категорий', r'%1$s различных категорий', content)
    content = re.sub(r'%1\$d\s*транзакций', r'%1$d транзакций', content)
    content = re.sub(r'%1\$s\s*в\s*общей\s*сложности', r'%1$s в общей сложности', content)
    content = re.sub(r'%1\$d\s*раз', r'%1$d раз', content)
    content = re.sub(r'%1\$d\s*финансовых\s*целей', r'%1$d финансовых целей', content)
    content = re.sub(r'%1\$s\s*долга', r'%1$s долга', content)
    content = re.sub(r'%1\$s\s*в\s*размере\s*%1\$s', r'%1$s в размере %2$s', content)
    content = re.sub(r'%1\$d\s*месяцев', r'%1$d месяцев', content)
    content = re.sub(r'%1\$d\s*источников\s*дохода', r'%1$d источников дохода', content)
    content = re.sub(r'%1\$d\s*дней', r'%1$d дней', content)
    content = re.sub(r'%1\$s%%', r'%1$s%%', content)
    content = re.sub(r'%1\$d\s*финансовых\s*советов', r'%1$d финансовых советов', content)
    content = re.sub(r'%1\$s\s*страховое\s*покрытие\s*на\s*%1\$s', r'%1$s страховое покрытие на %2$s', content)
    
    # 12. Исправляем проблемы с форматированием в китайском
    content = re.sub(r'％s\s*%%%', r'%1$s%%', content)
    content = re.sub(r'％d\s*%%%', r'%1$d%%', content)
    content = re.sub(r'__p_0\s*__\s*%%%', r'%1$s%%', content)
    content = re.sub(r'__p_1\s*__\s*%%%', r'%2$s%%', content)
    
    # 13. Исправляем проблемы с форматированием в английском
    content = re.sub(r'%1\$s\s*%%%', r'%1$s%%', content)
    content = re.sub(r'%1\$d\s*%%%', r'%1$d%%', content)
    
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
            if fix_string_formatting(file_path):
                fixed_count += 1
    
    print(f"Fixed formatting in {fixed_count} files")

if __name__ == "__main__":
    main()
