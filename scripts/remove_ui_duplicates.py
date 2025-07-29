#!/usr/bin/env python3
"""
Скрипт для удаления дубликатов внутри ui модуля.
"""
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil
from collections import defaultdict
import time

class UIDuplicatesRemover:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.ui_strings_file = self.project_root / "ui/src/main/res/values/strings.xml"
        self.backup_dir = None
        
    def create_backup(self):
        """Создает резервную копию ui файла"""
        timestamp = Path.cwd().name + "_ui_backup_" + str(int(time.time()))
        self.backup_dir = self.project_root / "backup" / timestamp
        self.backup_dir.mkdir(parents=True, exist_ok=True)
        
        backup_path = self.backup_dir / "ui_strings_backup.xml"
        shutil.copy2(self.ui_strings_file, backup_path)
        print(f"Резервная копия ui файла создана в: {backup_path}")
    
    def remove_ui_duplicates(self):
        """Удаляет дубликаты внутри ui модуля"""
        print("Начинаем удаление дубликатов внутри ui модуля...")
        
        # Создаем резервную копию
        self.create_backup()
        
        if not self.ui_strings_file.exists():
            print("Файл ui/src/main/res/values/strings.xml не найден!")
            return 0
        
        tree = ET.parse(self.ui_strings_file)
        root = tree.getroot()
        
        # Собираем все строки и их значения
        string_values = {}
        duplicates = []
        
        for string_elem in root.findall("string"):
            name = string_elem.get("name")
            value = string_elem.text or ""
            
            if value in string_values:
                # Найден дубликат
                duplicates.append((string_elem, string_values[value]))
            else:
                string_values[value] = name
        
        # Удаляем дубликаты, оставляя первое вхождение
        removed_count = 0
        for duplicate_elem, original_name in duplicates:
            root.remove(duplicate_elem)
            removed_count += 1
            print(f"Удален дубликат: {duplicate_elem.get('name')} (значение: '{duplicate_elem.text}')")
        
        # Сохраняем файл
        if removed_count > 0:
            tree.write(self.ui_strings_file, encoding="utf-8", xml_declaration=True)
            print(f"\nУдалено {removed_count} дубликатов из ui модуля")
        else:
            print("Дубликатов в ui модуле не найдено")
        
        return removed_count

def main():
    import sys
    
    if len(sys.argv) != 2:
        print("Использование: python3 scripts/remove_ui_duplicates.py <project_root>")
        sys.exit(1)
    
    project_root = sys.argv[1]
    remover = UIDuplicatesRemover(project_root)
    
    try:
        remover.remove_ui_duplicates()
        print("\nУдаление дубликатов в ui модуле завершено!")
    except Exception as e:
        print(f"Ошибка: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 