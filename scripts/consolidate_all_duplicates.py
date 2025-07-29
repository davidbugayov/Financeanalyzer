#!/usr/bin/env python3
"""
Скрипт для автоматической консолидации всех дублирующихся строк в общий модуль ui.
"""

import os
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil
from collections import defaultdict

class AllDuplicatesConsolidator:
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
        
        # Копируем все файлы strings.xml
        for strings_file in self.project_root.rglob("**/strings.xml"):
            if "backup" not in str(strings_file):
                relative_path = strings_file.relative_to(self.project_root)
                backup_path = self.backup_dir / relative_path
                backup_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(strings_file, backup_path)
        
        print(f"Создана резервная копия в: {self.backup_dir}")
    
    def find_all_string_files(self):
        """Находит все файлы strings.xml в проекте."""
        pattern = "**/res/values/strings.xml"
        all_files = list(self.project_root.glob(pattern))
        # Исключаем резервные копии
        return [f for f in all_files if "backup" not in str(f)]
    
    def parse_strings_file(self, file_path):
        """Парсит файл strings.xml и возвращает словарь строк."""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            root = ET.fromstring(content)
            strings = {}
            
            for string_elem in root.findall('.//string'):
                name = string_elem.get('name')
                text = string_elem.text or ""
                strings[name] = text.strip()
            
            return strings
        except Exception as e:
            print(f"Ошибка при парсинге {file_path}: {e}")
            return {}
    
    def find_duplicates(self):
        """Находит все дублирующиеся строки."""
        string_files = self.find_all_string_files()
        all_strings = {}
        duplicates = defaultdict(list)
        
        # Собираем все строки
        for file_path in string_files:
            strings = self.parse_strings_file(file_path)
            for name, value in strings.items():
                all_strings[name] = value
                duplicates[value].append((file_path, name))
        
        # Фильтруем только дубликаты
        return {value: files for value, files in duplicates.items() if len(files) > 1}
    
    def consolidate_to_ui(self):
        """Консолидирует все дубликаты в модуль ui."""
        print("Поиск дубликатов...")
        duplicates = self.find_duplicates()
        
        if not duplicates:
            print("Дубликаты не найдены.")
            return
        
        print(f"Найдено {len(duplicates)} дублирующихся значений.")
        
        # Создаем резервную копию
        self.create_backup()
        
        # Читаем текущий файл ui
        ui_strings = self.parse_strings_file(self.ui_strings_file)
        
        # Собираем все уникальные строки для добавления в ui
        strings_to_add = {}
        
        for value, files in duplicates.items():
            # Находим лучшее имя для строки (предпочитаем из ui модуля)
            best_name = None
            for file_path, name in files:
                if "ui" in str(file_path):
                    best_name = name
                    break
            
            if not best_name:
                # Берем первое имя
                best_name = files[0][1]
            
            if best_name not in ui_strings:
                strings_to_add[best_name] = value
        
        # Добавляем строки в ui модуль
        if strings_to_add:
            self.add_strings_to_ui(strings_to_add)
            print(f"Добавлено {len(strings_to_add)} строк в ui модуль.")
        
        # Удаляем дубликаты из других модулей
        self.remove_duplicates_from_other_modules(duplicates)
    
    def add_strings_to_ui(self, strings_to_add):
        """Добавляет строки в ui модуль."""
        try:
            with open(self.ui_strings_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            root = ET.fromstring(content)
            
            # Добавляем новые строки
            for name, value in strings_to_add.items():
                # Проверяем, не существует ли уже строка
                existing = root.find(f".//string[@name='{name}']")
                if existing is None:
                    new_string = ET.SubElement(root, 'string')
                    new_string.set('name', name)
                    new_string.text = value
            
            # Сохраняем файл
            tree = ET.ElementTree(root)
            ET.indent(tree, space="    ")
            
            with open(self.ui_strings_file, 'w', encoding='utf-8') as f:
                f.write('<?xml version=\'1.0\' encoding=\'utf-8\'?>\n')
                tree.write(f, encoding='unicode', xml_declaration=False)
            
        except Exception as e:
            print(f"Ошибка при добавлении строк в ui: {e}")
    
    def remove_duplicates_from_other_modules(self, duplicates):
        """Удаляет дубликаты из других модулей."""
        ui_strings = self.parse_strings_file(self.ui_strings_file)
        removed_count = 0
        
        for value, files in duplicates.items():
            # Находим имя строки в ui модуле
            ui_name = None
            for file_path, name in files:
                if "ui" in str(file_path):
                    ui_name = name
                    break
            
            if not ui_name:
                continue
            
            # Удаляем дубликаты из других модулей
            for file_path, name in files:
                if "ui" not in str(file_path):
                    try:
                        with open(file_path, 'r', encoding='utf-8') as f:
                            content = f.read()
                        
                        root = ET.fromstring(content)
                        
                        # Находим и удаляем дублирующуюся строку
                        string_elem = root.find(f".//string[@name='{name}']")
                        if string_elem is not None and string_elem.text and string_elem.text.strip() == value:
                            root.remove(string_elem)
                            removed_count += 1
                        
                        # Сохраняем файл
                        tree = ET.ElementTree(root)
                        ET.indent(tree, space="    ")
                        
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write('<?xml version=\'1.0\' encoding=\'utf-8\'?>\n')
                            tree.write(f, encoding='unicode', xml_declaration=False)
                        
                    except Exception as e:
                        print(f"Ошибка при удалении дубликата из {file_path}: {e}")
        
        print(f"Удалено {removed_count} дублирующихся строк из других модулей.")
    
    def generate_report(self):
        """Генерирует отчет о консолидации."""
        duplicates = self.find_duplicates()
        
        report = f"""
# Отчет о консолидации строковых ресурсов

## Статистика
- Всего дублирующихся значений: {len(duplicates)}
- Файлов strings.xml: {len(self.find_all_string_files())}

## Дублирующиеся строки
"""
        
        for value, files in duplicates.items():
            report += f"\n### '{value}'\n"
            for file_path, name in files:
                report += f"- {file_path}: {name}\n"
        
        # Сохраняем отчет
        report_file = self.project_root / "docs/CONSOLIDATION_REPORT.md"
        report_file.parent.mkdir(exist_ok=True)
        
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write(report)
        
        print(f"Отчет сохранен в: {report_file}")

def main():
    consolidator = AllDuplicatesConsolidator(".")
    
    print("Начинаем консолидацию всех дублирующихся строк...")
    consolidator.consolidate_to_ui()
    consolidator.generate_report()
    
    print("\nКонсолидация завершена!")
    print("Проверьте отчет в docs/CONSOLIDATION_REPORT.md")

if __name__ == "__main__":
    main() 