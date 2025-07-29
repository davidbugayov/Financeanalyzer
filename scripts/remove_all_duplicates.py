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

class AggressiveDuplicateRemover:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.ui_strings_file = self.project_root / "ui/src/main/res/values/strings.xml"
        self.backup_dir = None
        
    def create_backup(self):
        """Создает резервную копию всех файлов strings.xml."""
        import subprocess
        timestamp = subprocess.check_output(['date', '+%Y%m%d_%H%M%S']).decode().strip()
        self.backup_dir = self.project_root / f"backup_strings_{timestamp}"
        self.backup_dir.mkdir(exist_ok=True)
        
        # Находим все файлы strings.xml
        string_files = list(self.project_root.rglob("**/res/values/strings.xml"))
        
        for file_path in string_files:
            if "backup" not in str(file_path):
                relative_path = file_path.relative_to(self.project_root)
                backup_path = self.backup_dir / relative_path
                backup_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(file_path, backup_path)
        
        print(f"✅ Резервная копия создана: {self.backup_dir}")
        
    def load_ui_strings(self):
        """Загружает все строки из ui модуля."""
        if not self.ui_strings_file.exists():
            print("❌ Файл ui/src/main/res/values/strings.xml не найден!")
            return {}
            
        tree = ET.parse(self.ui_strings_file)
        root = tree.getroot()
        
        ui_strings = {}
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""
            ui_strings[value] = name
            
        return ui_strings
        
    def remove_duplicates_from_file(self, file_path: Path, ui_strings: dict):
        """Удаляет дублирующиеся строки из файла."""
        if not file_path.exists():
            return 0
            
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        removed_count = 0
        strings_to_remove = []
        
        for string_elem in root.findall('string'):
            name = string_elem.get('name')
            value = string_elem.text or ""
            
            # Если значение уже есть в ui модуле, удаляем его
            if value in ui_strings and ui_strings[value] != name:
                strings_to_remove.append(string_elem)
                removed_count += 1
                
        # Удаляем найденные дубликаты
        for string_elem in strings_to_remove:
            root.remove(string_elem)
            
        # Сохраняем файл
        if removed_count > 0:
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            
        return removed_count
        
    def remove_all_duplicates(self):
        """Удаляет все дублирующиеся строки из всех модулей."""
        print("🔄 Начинаю удаление всех дубликатов...")
        
        # Создаем резервную копию
        self.create_backup()
        
        # Загружаем строки из ui модуля
        ui_strings = self.load_ui_strings()
        print(f"📋 Загружено {len(ui_strings)} строк из ui модуля")
        
        # Находим все файлы strings.xml
        string_files = list(self.project_root.rglob("**/res/values/strings.xml"))
        
        total_removed = 0
        processed_files = 0
        
        for file_path in string_files:
            # Пропускаем ui модуль и резервные копии
            if "ui/src/main/res/values/strings.xml" in str(file_path) or "backup" in str(file_path):
                continue
                
            removed = self.remove_duplicates_from_file(file_path, ui_strings)
            if removed > 0:
                print(f"🗑️  Удалено {removed} дубликатов из {file_path.relative_to(self.project_root)}")
                total_removed += removed
            processed_files += 1
            
        print(f"\n✅ Удаление завершено!")
        print(f"📁 Обработано файлов: {processed_files}")
        print(f"🗑️  Всего удалено дубликатов: {total_removed}")
        print(f"💾 Резервная копия: {self.backup_dir}")
        
        return total_removed

def main():
    import sys
    
    if len(sys.argv) != 2:
        print("Использование: python3 scripts/remove_all_duplicates.py <project_root>")
        sys.exit(1)
        
    project_root = sys.argv[1]
    remover = AggressiveDuplicateRemover(project_root)
    remover.remove_all_duplicates()

if __name__ == "__main__":
    main() 