#!/usr/bin/env python3
"""
Финальный скрипт для удаления дубликатов внутри ui модуля.
Удаляет дубликаты, оставляя только одно определение для каждого значения.
"""
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil
from typing import Dict, List, Set

def parse_ui_strings() -> Dict[str, List[str]]:
    """Парсит строки из ui модуля и группирует по значению"""
    ui_file = Path("ui/src/main/res/values/strings.xml")
    
    if not ui_file.exists():
        print(f"Файл {ui_file} не найден!")
        return {}
    
    tree = ET.parse(ui_file)
    root = tree.getroot()
    
    # Группируем строки по значению
    value_to_names = {}
    for string_elem in root.findall("string"):
        name = string_elem.get("name")
        value = string_elem.text or ""
        
        if value not in value_to_names:
            value_to_names[value] = []
        value_to_names[value].append(name)
    
    return value_to_names

def remove_ui_duplicates():
    """Удаляет дубликаты из ui модуля"""
    ui_file = Path("ui/src/main/res/values/strings.xml")
    
    if not ui_file.exists():
        print(f"Файл {ui_file} не найден!")
        return
    
    # Создаем резервную копию
    backup_file = ui_file.with_suffix('.xml.backup2')
    shutil.copy2(ui_file, backup_file)
    print(f"Создана резервная копия: {backup_file}")
    
    # Парсим строки
    value_to_names = parse_ui_strings()
    
    # Находим дубликаты
    duplicates = {value: names for value, names in value_to_names.items() 
                 if len(names) > 1}
    
    if not duplicates:
        print("Дубликаты в ui модуле не найдены!")
        return
    
    print(f"Найдено {len(duplicates)} дубликатов в ui модуле")
    
    # Читаем файл для редактирования
    tree = ET.parse(ui_file)
    root = tree.getroot()
    
    removed_count = 0
    
    # Удаляем дубликаты, оставляя первое вхождение
    for value, names in duplicates.items():
        print(f"\nОбрабатываем значение: '{value}'")
        print(f"  Найдены имена: {names}")
        
        # Оставляем первое имя, удаляем остальные
        keep_name = names[0]
        remove_names = names[1:]
        
        print(f"  Оставляем: {keep_name}")
        print(f"  Удаляем: {remove_names}")
        
        # Удаляем дубликаты
        for remove_name in remove_names:
            for string_elem in root.findall("string"):
                if string_elem.get("name") == remove_name:
                    root.remove(string_elem)
                    removed_count += 1
                    print(f"    Удалена строка: {remove_name}")
                    break
    
    # Сохраняем файл
    tree.write(ui_file, encoding="utf-8", xml_declaration=True)
    print(f"\nУдалено {removed_count} дубликатов из ui модуля")

def main():
    print("УДАЛЕНИЕ ДУБЛИКАТОВ В UI МОДУЛЕ")
    print("=" * 40)
    
    remove_ui_duplicates()
    print("\nОперация завершена!")

if __name__ == "__main__":
    main() 