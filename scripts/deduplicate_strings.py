#!/usr/bin/env python3
"""
Скрипт для поиска и объединения дубликатов строковых ресурсов в Android проекте.
Находит одинаковые значения строк в разных файлах strings.xml и предлагает их объединение.
"""

import os
import xml.etree.ElementTree as ET
import argparse
from collections import defaultdict
from pathlib import Path
import re
from typing import Dict, List, Tuple, Set

class StringDeduplicator:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.string_files = []
        self.duplicates = defaultdict(list)
        self.common_strings = set()
        
    def find_string_files(self) -> List[Path]:
        """Находит все файлы strings.xml в проекте, исключая резервные копии."""
        pattern = "**/res/values/strings.xml"
        all_files = list(self.project_root.glob(pattern))
        
        # Исключаем файлы из резервных копий
        string_files = [f for f in all_files if 'backup' not in f.parts]
        
        print(f"Найдено {len(string_files)} файлов strings.xml:")
        for file in string_files:
            print(f"  - {file.relative_to(self.project_root)}")
        return string_files
    
    def parse_strings_file(self, file_path: Path) -> Dict[str, str]:
        """Парсит файл strings.xml и возвращает словарь {name: value}."""
        strings = {}
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            for string_elem in root.findall('string'):
                name = string_elem.get('name')
                value = string_elem.text or ""
                if name:
                    strings[name] = value
                    
        except ET.ParseError as e:
            print(f"Ошибка парсинга {file_path}: {e}")
        except Exception as e:
            print(f"Ошибка чтения {file_path}: {e}")
            
        return strings
    
    def find_duplicates(self) -> Dict[str, List[Tuple[Path, str]]]:
        """Находит дубликаты строк по значению."""
        string_files = self.find_string_files()
        value_to_files = defaultdict(list)
        
        for file_path in string_files:
            strings = self.parse_strings_file(file_path)
            for name, value in strings.items():
                # Нормализуем значение (убираем лишние пробелы)
                normalized_value = value.strip()
                if normalized_value:
                    value_to_files[normalized_value].append((file_path, name))
        
        # Оставляем только дубликаты (больше одного файла)
        duplicates = {value: files for value, files in value_to_files.items() 
                     if len(files) > 1}
        
        return duplicates
    
    def find_common_strings(self) -> Set[str]:
        """Находит строки, которые используются во всех модулях."""
        string_files = self.find_string_files()
        if not string_files:
            return set()
        
        # Получаем все строки из первого файла
        first_file = string_files[0]
        first_strings = set(self.parse_strings_file(first_file).values())
        
        # Находим пересечение со всеми остальными файлами
        common_strings = first_strings.copy()
        for file_path in string_files[1:]:
            file_strings = set(self.parse_strings_file(file_path).values())
            common_strings &= file_strings
        
        return common_strings
    
    def analyze_duplicates(self):
        """Анализирует дубликаты и выводит отчет."""
        print("\n" + "="*60)
        print("АНАЛИЗ ДУБЛИКАТОВ СТРОК")
        print("="*60)
        
        duplicates = self.find_duplicates()
        
        if not duplicates:
            print("Дубликаты не найдены! 🎉")
            return
        
        print(f"Найдено {len(duplicates)} дублирующихся значений:")
        print()
        
        for i, (value, files) in enumerate(duplicates.items(), 1):
            print(f"{i}. Значение: '{value[:50]}{'...' if len(value) > 50 else ''}'")
            print(f"   Найдено в {len(files)} файлах:")
            for file_path, name in files:
                rel_path = file_path.relative_to(self.project_root)
                print(f"     - {rel_path} (name='{name}')")
            print()
    
    def suggest_consolidation(self):
        """Предлагает план консолидации дубликатов."""
        print("\n" + "="*60)
        print("ПЛАН КОНСОЛИДАЦИИ")
        print("="*60)
        
        duplicates = self.find_duplicates()
        if not duplicates:
            return
        
        # Группируем по модулям
        module_groups = defaultdict(list)
        for value, files in duplicates.items():
            modules = set()
            for file_path, _ in files:
                # Извлекаем имя модуля из пути
                parts = file_path.parts
                if 'src' in parts:
                    src_index = parts.index('src')
                    if src_index > 0:
                        module_name = parts[src_index - 1]
                        modules.add(module_name)
            
            if modules:
                module_groups[tuple(sorted(modules))].append((value, files))
        
        print("Рекомендации по консолидации:")
        print()
        
        for modules, items in module_groups.items():
            print(f"Модули: {', '.join(modules)}")
            print(f"Количество дубликатов: {len(items)}")
            print("Действия:")
            
            # Определяем основной модуль для консолидации
            if 'ui' in modules:
                main_module = 'ui'
            elif 'common-ui' in modules:
                main_module = 'common-ui'
            else:
                main_module = modules[0]
            
            print(f"  1. Перенести все общие строки в модуль '{main_module}'")
            print(f"  2. Удалить дубликаты из других модулей")
            print(f"  3. Обновить импорты в коде")
            print()
    
    def find_common_strings_report(self):
        """Выводит отчет о общих строках."""
        print("\n" + "="*60)
        print("ОБЩИЕ СТРОКИ ВО ВСЕХ МОДУЛЯХ")
        print("="*60)
        
        common_strings = self.find_common_strings()
        
        if not common_strings:
            print("Общих строк не найдено.")
            return
        
        print(f"Найдено {len(common_strings)} общих строк:")
        print()
        
        for i, value in enumerate(sorted(common_strings), 1):
            print(f"{i}. '{value[:50]}{'...' if len(value) > 50 else ''}'")
        
        print()
        print("Рекомендация: Эти строки должны быть в модуле 'ui' или 'common-ui'")
    
    def generate_consolidation_script(self):
        """Генерирует скрипт для автоматической консолидации."""
        print("\n" + "="*60)
        print("ГЕНЕРАЦИЯ СКРИПТА КОНСОЛИДАЦИИ")
        print("="*60)
        
        duplicates = self.find_duplicates()
        if not duplicates:
            print("Нет дубликатов для консолидации.")
            return
        
        script_content = """#!/bin/bash
# Скрипт для автоматической консолидации дубликатов строк
# Сгенерирован автоматически

echo "Начинаем консолидацию дубликатов строк..."

# Создаем резервную копию
echo "Создаем резервную копию..."
cp -r app/src/main/res/values app/src/main/res/values.backup.$(date +%Y%m%d_%H%M%S)

"""
        
        # Добавляем команды для каждого дубликата
        for value, files in duplicates.items():
            if len(files) > 1:
                # Берем первый файл как основной
                main_file, main_name = files[0]
                script_content += f"# Обрабатываем дубликат: {value[:30]}...\n"
                
                for file_path, name in files[1:]:
                    rel_path = file_path.relative_to(self.project_root)
                    script_content += f"echo 'Удаляем дубликат из {rel_path}'\n"
                    script_content += f"# TODO: Удалить строку '{name}' из {rel_path}\n"
                
                script_content += "\n"
        
        script_content += """echo "Консолидация завершена!"
echo "Не забудьте проверить результат и обновить импорты в коде."
"""
        
        script_path = self.project_root / "scripts" / "consolidate_strings.sh"
        with open(script_path, 'w', encoding='utf-8') as f:
            f.write(script_content)
        
        os.chmod(script_path, 0o755)
        print(f"Скрипт консолидации сохранен в: {script_path}")
    
    def run_full_analysis(self):
        """Запускает полный анализ."""
        print("АНАЛИЗ ДУБЛИКАТОВ СТРОКОВЫХ РЕСУРСОВ")
        print("="*60)
        
        self.analyze_duplicates()
        self.find_common_strings_report()
        self.suggest_consolidation()
        self.generate_consolidation_script()

def main():
    parser = argparse.ArgumentParser(description='Анализ дубликатов строковых ресурсов Android')
    parser.add_argument('--project-root', default='.', 
                       help='Корневая папка проекта (по умолчанию: текущая папка)')
    parser.add_argument('--action', choices=['analyze', 'consolidate', 'common'], 
                       default='analyze',
                       help='Действие для выполнения')
    
    args = parser.parse_args()
    
    deduplicator = StringDeduplicator(args.project_root)
    
    if args.action == 'analyze':
        deduplicator.run_full_analysis()
    elif args.action == 'common':
        deduplicator.find_common_strings_report()
    elif args.action == 'consolidate':
        deduplicator.generate_consolidation_script()

if __name__ == '__main__':
    main() 