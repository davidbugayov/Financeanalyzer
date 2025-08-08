#!/usr/bin/env python3
"""
Продвинутый скрипт для автоматической консолидации дубликатов строковых ресурсов.
Поддерживает интерактивный режим и автоматическое объединение.
"""

import os
import xml.etree.ElementTree as ET
import argparse
from collections import defaultdict
from pathlib import Path
import shutil
import re
from typing import Dict, List, Tuple, Set
import json

class AdvancedStringConsolidator:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.backup_dir = None
        self.consolidation_plan = {}
        
    def create_backup(self):
        """Создает резервную копию всех файлов strings.xml."""
        timestamp = os.popen('date +%Y%m%d_%H%M%S').read().strip()
        self.backup_dir = self.project_root / f"backup_strings_{timestamp}"
        self.backup_dir.mkdir(exist_ok=True)
        
        string_files = self.find_string_files()
        for file_path in string_files:
            backup_path = self.backup_dir / file_path.relative_to(self.project_root)
            backup_path.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(file_path, backup_path)
        
        print(f"Резервная копия создана в: {self.backup_dir}")
    
    def find_string_files(self) -> List[Path]:
        """Находит все файлы strings*.xml в проекте, исключая бэкапы."""
        patterns = [
            "**/res/values/strings.xml",
            "**/res/values/strings_*.xml",
        ]
        files: List[Path] = []
        for pattern in patterns:
            files.extend(self.project_root.glob(pattern))

        def is_backup(p: Path) -> bool:
            return any(part.startswith("backup_strings_") for part in p.parts)

        # Убираем дубликаты и бэкапы
        unique_files = []
        seen = set()
        for f in files:
            if is_backup(f):
                continue
            key = str(f.resolve())
            if key not in seen:
                seen.add(key)
                unique_files.append(f)
        return unique_files
    
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
    
    def _normalize_value(self, value: str) -> str:
        """Нормализация значений для поиска дублей: трим, схлопывание пробелов."""
        if value is None:
            return ""
        # Схлопываем пробелы и неразрывные пробелы
        return " ".join((value.replace("\u00A0", " ").split()))

    def find_duplicates(self) -> Dict[str, List[Tuple[Path, str]]]:
        """Находит дубликаты строк по значению (с нормализацией)."""
        string_files = self.find_string_files()
        value_to_files = defaultdict(list)
        
        for file_path in string_files:
            strings = self.parse_strings_file(file_path)
            for name, value in strings.items():
                normalized_value = self._normalize_value(value)
                if normalized_value:
                    value_to_files[normalized_value].append((file_path, name))
        
        return {value: files for value, files in value_to_files.items() 
                if len(files) > 1}
    
    def get_module_name(self, file_path: Path) -> str:
        """Извлекает имя модуля из пути к файлу."""
        parts = file_path.parts
        if 'src' in parts:
            src_index = parts.index('src')
            if src_index > 0:
                return parts[src_index - 1]
        return "unknown"
    
    def determine_target_module(self, files: List[Tuple[Path, str]]) -> str:
        """Определяет целевой модуль для консолидации."""
        modules = [self.get_module_name(file_path) for file_path, _ in files]
        
        # Приоритет модулей для консолидации
        priority_modules = ['ui', 'common-ui', 'core', 'app']
        
        for module in priority_modules:
            if module in modules:
                return module
        
        # Если нет приоритетных модулей, выбираем первый
        return modules[0] if modules else "ui"
    
    def create_consolidation_plan(self) -> Dict:
        """Создает план консолидации дубликатов."""
        duplicates = self.find_duplicates()
        plan = {
            'target_modules': {},
            'duplicates': {},
            'summary': {
                'total_duplicates': len(duplicates),
                'files_to_modify': set(),
                'strings_to_move': 0
            }
        }
        
        for value, files in duplicates.items():
            target_module = self.determine_target_module(files)
            
            if target_module not in plan['target_modules']:
                plan['target_modules'][target_module] = []
            
            # Находим файл в целевом модуле
            target_file = None
            other_files = []
            
            for file_path, name in files:
                if self.get_module_name(file_path) == target_module:
                    target_file = (file_path, name)
                else:
                    other_files.append((file_path, name))
            
            if target_file and other_files:
                plan['target_modules'][target_module].append({
                    'value': value,
                    'target_file': target_file,
                    'other_files': other_files
                })
                
                plan['summary']['strings_to_move'] += len(other_files)
                for file_path, _ in other_files:
                    plan['summary']['files_to_modify'].add(str(file_path))
        
        return plan

    def _scan_module_usages(self, module_dir: Path) -> Dict[str, int]:
        """Сканирует код модуля и возвращает счетчик локальных обращений к R.string.<key>.

        Ключ: имя строки, Значение: количество вхождений 'R.string.key' в пределах модуля.
        """
        usage: Dict[str, int] = defaultdict(int)
        code_roots = [
            module_dir / "src" / "main" / "java",
            module_dir / "src" / "main" / "kotlin",
        ]
        import re
        pattern = re.compile(r"\bR\.string\.(\w+)\b")
        for root in code_roots:
            if not root.exists():
                continue
            for file in root.rglob("**/*"):
                if file.suffix not in (".kt", ".java"):
                    continue
                try:
                    text = file.read_text(encoding="utf-8", errors="ignore")
                except Exception:
                    continue
                for m in pattern.finditer(text):
                    usage[m.group(1)] += 1
        return usage

    def build_usage_index(self) -> Dict[str, Set[str]]:
        """Строит индекс локальных использований ключей по модулям.

        Возвращает dict: key -> set(modules_with_local_R_usage)
        """
        index: Dict[str, Set[str]] = defaultdict(set)
        # Соберем уникальные модули из файлов строк
        modules: Set[str] = set()
        files = self.find_string_files()
        for file_path in files:
            modules.add(self.get_module_name(file_path))
        # Просканируем каждый модуль
        for module in modules:
            if module in ("unknown",):
                continue
            module_path = self.project_root / module
            if not module_path.exists():
                continue
            local_usages = self._scan_module_usages(module_path)
            for key, count in local_usages.items():
                if count > 0:
                    index[key].add(module)
        return index
    
    def consolidate_to_module(self, target_module: str, consolidations: List[Dict], safe_remove: bool = True):
        """Консолидирует строки в указанный модуль.

        safe_remove=True: не удалять строку из модуля, если она используется локально как R.string.<key>.
        """
        print(f"\nКонсолидация в модуль '{target_module}':")
        
        # Находим файл strings.xml в целевом модуле
        target_file = None
        for file_path in self.find_string_files():
            if self.get_module_name(file_path) == target_module:
                target_file = file_path
                break
        
        if not target_file:
            print(f"Файл strings.xml не найден в модуле {target_module}")
            return
        
        # Читаем существующие строки
        existing_strings = self.parse_strings_file(target_file)
        
        # Добавляем новые строки
        added_strings = []
        for consolidation in consolidations:
            value = consolidation['value']
            target_name = consolidation['target_file'][1]
            
            if target_name not in existing_strings:
                existing_strings[target_name] = value
                added_strings.append(target_name)
        
        # Записываем обновленный файл
        self.write_strings_file(target_file, existing_strings)
        
        print(f"  Добавлено {len(added_strings)} строк в {target_file}")
        
        # Удаляем дубликаты из других файлов (с учетом safe_remove)
        usage_index = self.build_usage_index() if safe_remove else {}
        for consolidation in consolidations:
            for file_path, name in consolidation['other_files']:
                source_module = self.get_module_name(file_path)
                if safe_remove and name in usage_index and source_module in usage_index[name]:
                    print(f"  Пропущено удаление '{name}' из {file_path} — локально используется в модуле '{source_module}'")
                    continue
                self.remove_string_from_file(file_path, name)
    
    def remove_string_from_file(self, file_path: Path, string_name: str):
        """Удаляет строку из файла strings.xml."""
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            # Находим и удаляем элемент
            for string_elem in root.findall('string'):
                if string_elem.get('name') == string_name:
                    root.remove(string_elem)
                    break
            
            # Записываем обновленный файл
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print(f"  Удалена строка '{string_name}' из {file_path}")
            
        except Exception as e:
            print(f"Ошибка при удалении строки из {file_path}: {e}")
    
    def write_strings_file(self, file_path: Path, strings: Dict[str, str]):
        """Записывает строки в файл strings.xml."""
        try:
            # Создаем корневой элемент
            root = ET.Element('resources')
            
            # Добавляем строки в алфавитном порядке
            for name in sorted(strings.keys()):
                string_elem = ET.SubElement(root, 'string')
                string_elem.set('name', name)
                string_elem.text = strings[name]
            
            # Создаем дерево и записываем
            tree = ET.ElementTree(root)
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            
        except Exception as e:
            print(f"Ошибка при записи файла {file_path}: {e}")
    
    def run_automatic_consolidation(self, safe_remove: bool = True):
        """Запускает автоматическую консолидацию.

        safe_remove=True включает безопасный режим удаления дубликатов.
        """
        print("Начинаем автоматическую консолидацию дубликатов...")
        
        # Создаем резервную копию
        self.create_backup()
        
        # Создаем план консолидации
        plan = self.create_consolidation_plan()
        
        print(f"\nПлан консолидации:")
        print(f"Всего дубликатов: {plan['summary']['total_duplicates']}")
        print(f"Строк для перемещения: {plan['summary']['strings_to_move']}")
        print(f"Файлов для изменения: {len(plan['summary']['files_to_modify'])}")
        
        # Выполняем консолидацию по модулям
        for target_module, consolidations in plan['target_modules'].items():
            if consolidations:
                self.consolidate_to_module(target_module, consolidations, safe_remove=safe_remove)
        
        print(f"\nКонсолидация завершена!")
        print(f"Резервная копия сохранена в: {self.backup_dir}")
    
    def generate_report(self):
        """Генерирует отчет о дубликатах и неиспользуемых строках."""
        duplicates = self.find_duplicates()
        plan = self.create_consolidation_plan()
        
        def _to_jsonable(obj):
            from pathlib import Path as _P
            if isinstance(obj, _P):
                return str(obj)
            if isinstance(obj, set):
                return list(obj)
            if isinstance(obj, tuple):
                return [_to_jsonable(i) for i in obj]
            if isinstance(obj, list):
                return [_to_jsonable(i) for i in obj]
            if isinstance(obj, dict):
                return {k: _to_jsonable(v) for k, v in obj.items()}
            return obj

        report = {
            'timestamp': os.popen('date').read().strip(),
            'total_duplicates': len(duplicates),
            'consolidation_plan': _to_jsonable(plan),
            'duplicates_by_module': defaultdict(list)
        }
        
        # Группируем дубликаты по модулям
        for value, files in duplicates.items():
            modules = set()
            for file_path, _ in files:
                modules.add(self.get_module_name(file_path))
            
            module_key = ",".join(sorted(modules)) if modules else ""
            report['duplicates_by_module'][module_key].append({
                'value': value[:100] + '...' if len(value) > 100 else value,
                'files': [(str(file_path), name) for file_path, name in files]
            })
        
        # Неиспользуемые строки по модулям
        unused_by_module: Dict[str, List[str]] = defaultdict(list)
        usage_index = self.build_usage_index()
        # Собираем все ключи из файлов и отмечаем те, что нигде не используются локально и через UiR
        all_files = self.find_string_files()
        all_keys_by_module: Dict[str, Set[str]] = defaultdict(set)
        for file_path in all_files:
            module = self.get_module_name(file_path)
            strings = self.parse_strings_file(file_path)
            for key in strings.keys():
                all_keys_by_module[module].add(key)
        # Локально не используемые (по модулю)
        for module, keys in all_keys_by_module.items():
            for key in keys:
                # Если ключ не используется локально как R.string.key
                if module not in usage_index.get(key, set()):
                    unused_by_module[module].append(key)
        report['unused_by_module'] = unused_by_module

        # Сохраняем отчет
        report_path = self.project_root / "scripts" / "duplicates_report.json"
        # Превращаем defaultdict в обычный dict
        report['duplicates_by_module'] = {k: v for k, v in report['duplicates_by_module'].items()}
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(_to_jsonable(report), f, indent=2, ensure_ascii=False)
        
        print(f"Отчет сохранен в: {report_path}")
        return report
    
    def interactive_consolidation(self):
        """Интерактивная консолидация с выбором пользователя."""
        print("Интерактивная консолидация дубликатов")
        print("="*50)
        
        duplicates = self.find_duplicates()
        if not duplicates:
            print("Дубликаты не найдены!")
            return
        
        # Создаем резервную копию
        self.create_backup()
        
        # Группируем дубликаты по модулям
        module_groups = defaultdict(list)
        for value, files in duplicates.items():
            modules = set()
            for file_path, _ in files:
                modules.add(self.get_module_name(file_path))
            
            module_groups[tuple(sorted(modules))].append((value, files))
        
        # Показываем группы и предлагаем выбор
        for i, (modules, items) in enumerate(module_groups.items(), 1):
            print(f"\nГруппа {i}: Модули {', '.join(modules)}")
            print(f"Количество дубликатов: {len(items)}")
            
            # Показываем первые несколько дубликатов
            for j, (value, files) in enumerate(items[:3], 1):
                print(f"  {j}. '{value[:50]}{'...' if len(value) > 50 else ''}'")
                print(f"     Файлы: {len(files)}")
            
            if len(items) > 3:
                print(f"  ... и еще {len(items) - 3} дубликатов")
            
            # Спрашиваем пользователя
            response = input(f"\nКонсолидировать группу {i}? (y/n/s - показать все): ").lower()
            
            if response == 's':
                # Показываем все дубликаты в группе
                for j, (value, files) in enumerate(items, 1):
                    print(f"  {j}. '{value}'")
                    for file_path, name in files:
                        rel_path = file_path.relative_to(self.project_root)
                        print(f"     - {rel_path} (name='{name}')")
                
                response = input(f"Консолидировать группу {i}? (y/n): ").lower()
            
            if response == 'y':
                # Определяем целевой модуль
                target_module = self.determine_target_module([file for _, files in items for file in files])
                print(f"Консолидируем в модуль '{target_module}'...")
                
                # Создаем план для этой группы
                group_plan = []
                for value, files in items:
                    target_file = None
                    other_files = []
                    
                    for file_path, name in files:
                        if self.get_module_name(file_path) == target_module:
                            target_file = (file_path, name)
                        else:
                            other_files.append((file_path, name))
                    
                    if target_file and other_files:
                        group_plan.append({
                            'value': value,
                            'target_file': target_file,
                            'other_files': other_files
                        })
                
                # Выполняем консолидацию
                if group_plan:
                    self.consolidate_to_module(target_module, group_plan)
        
        print(f"\nИнтерактивная консолидация завершена!")
        print(f"Резервная копия сохранена в: {self.backup_dir}")

def main():
    parser = argparse.ArgumentParser(description='Продвинутая консолидация дубликатов строк')
    parser.add_argument('--project-root', default='.', 
                       help='Корневая папка проекта')
    parser.add_argument('--mode', choices=['auto', 'interactive', 'report'], 
                       default='auto',
                       help='Режим работы')
    parser.add_argument('--unsafe-remove', action='store_true',
                        help='Отключить безопасное удаление (по умолчанию безопасный режим включен)')
    parser.add_argument('--backup', action='store_true',
                       help='Создать резервную копию перед изменениями')
    
    args = parser.parse_args()
    
    consolidator = AdvancedStringConsolidator(args.project_root)
    
    if args.mode == 'auto':
        consolidator.run_automatic_consolidation(safe_remove=not args.unsafe_remove)
    elif args.mode == 'interactive':
        consolidator.interactive_consolidation()
    elif args.mode == 'report':
        consolidator.generate_report()

if __name__ == '__main__':
    main() 