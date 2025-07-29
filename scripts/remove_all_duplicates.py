#!/usr/bin/env python3
"""
Агрессивный скрипт для удаления всех дублирующихся строк из других модулей,
оставляя их только в ui модуле.
"""
import os
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil
from collections import defaultdict
import re
import time

class AllDuplicatesConsolidator:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.ui_strings_file = self.project_root / "ui/src/main/res/values/strings.xml"
        self.backup_dir = None
        
    def create_backup(self):
        """Создает резервную копию всех файлов strings.xml"""
        timestamp = Path.cwd().name + "_backup_" + str(int(time.time()))
        self.backup_dir = self.project_root / "backup" / timestamp
        self.backup_dir.mkdir(parents=True, exist_ok=True)
        
        # Найти все файлы strings.xml
        pattern = "**/res/values/strings.xml"
        all_files = list(self.project_root.rglob(pattern))
        
        for file_path in all_files:
            if "backup" not in str(file_path):
                # Создать относительный путь для backup
                relative_path = file_path.relative_to(self.project_root)
                backup_path = self.backup_dir / relative_path
                backup_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(file_path, backup_path)
        
        print(f"Резервная копия создана в: {self.backup_dir}")
    
    def load_ui_strings(self):
        """Загружает все строки из ui модуля"""
        if not self.ui_strings_file.exists():
            print("Файл ui/src/main/res/values/strings.xml не найден!")
            return {}
        
        tree = ET.parse(self.ui_strings_file)
        root = tree.getroot()
        
        ui_strings = {}
        for string_elem in root.findall("string"):
            name = string_elem.get("name")
            value = string_elem.text or ""
            ui_strings[value] = name
        
        return ui_strings
    
    def remove_duplicates_from_file(self, file_path: Path, ui_strings: dict):
        """Удаляет дубликаты из файла, оставляя только уникальные строки"""
        if not file_path.exists():
            return 0
        
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        removed_count = 0
        strings_to_remove = []
        
        for string_elem in root.findall("string"):
            name = string_elem.get("name")
            value = string_elem.text or ""
            
            # Если значение уже есть в ui модуле, помечаем для удаления
            if value in ui_strings:
                strings_to_remove.append(string_elem)
                removed_count += 1
        
        # Удаляем помеченные строки
        for string_elem in strings_to_remove:
            root.remove(string_elem)
        
        # Сохраняем файл
        if removed_count > 0:
            tree.write(file_path, encoding="utf-8", xml_declaration=True)
            print(f"Удалено {removed_count} дубликатов из {file_path}")
        
        return removed_count
    
    def remove_all_duplicates(self):
        """Удаляет все дубликаты из всех модулей"""
        print("Начинаем агрессивное удаление дубликатов...")
        
        # Создаем резервную копию
        self.create_backup()
        
        # Загружаем строки из ui модуля
        ui_strings = self.load_ui_strings()
        print(f"Загружено {len(ui_strings)} строк из ui модуля")
        
        # Находим все файлы strings.xml
        pattern = "**/res/values/strings.xml"
        all_files = list(self.project_root.rglob(pattern))
        
        # Фильтруем файлы, исключая ui и backup
        files_to_process = []
        for file_path in all_files:
            if "backup" not in str(file_path) and "ui/src/main/res/values/strings.xml" not in str(file_path):
                files_to_process.append(file_path)
        
        total_removed = 0
        
        for file_path in files_to_process:
            removed = self.remove_duplicates_from_file(file_path, ui_strings)
            total_removed += removed
        
        print(f"\nВсего удалено {total_removed} дубликатов")
        print(f"Резервная копия сохранена в: {self.backup_dir}")
        
        return total_removed

def main():
    import sys
    
    if len(sys.argv) != 2:
        print("Использование: python3 scripts/remove_all_duplicates.py <project_root>")
        sys.exit(1)
    
    project_root = sys.argv[1]
    consolidator = AllDuplicatesConsolidator(project_root)
    
    try:
        consolidator.remove_all_duplicates()
        print("\nАгрессивное удаление дубликатов завершено!")
    except Exception as e:
        print(f"Ошибка: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 