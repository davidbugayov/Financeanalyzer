#!/usr/bin/env python3
"""
Улучшенный скрипт для консолидации строковых ресурсов в Android проекте.
Основан на лучших практиках модульной архитектуры.
"""
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil
from typing import Dict, List, Set, Tuple
import argparse

def parse_strings_file(file_path: Path) -> Dict[str, str]:
    """Парсит файл strings.xml и возвращает словарь {name: value}"""
    if not file_path.exists():
        return {}
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    strings = {}
    for string_elem in root.findall("string"):
        name = string_elem.get("name")
        value = string_elem.text or ""
        strings[name] = value
    
    return strings

def find_duplicates() -> Dict[str, List[Tuple[Path, str]]]:
    """Находит все дубликаты строк в проекте"""
    strings_files = list(Path(".").rglob("**/strings.xml"))
    
    # Собираем все строки
    all_strings = {}
    for file_path in strings_files:
        strings = parse_strings_file(file_path)
        for name, value in strings.items():
            if value not in all_strings:
                all_strings[value] = []
            all_strings[value].append((file_path, name))
    
    # Фильтруем только дубликаты
    duplicates = {value: files for value, files in all_strings.items() 
                 if len(files) > 1}
    
    return duplicates

def categorize_strings(duplicates: Dict[str, List[Tuple[Path, str]]]) -> Dict[str, List[str]]:
    """Категоризирует строки по типам для лучшей организации"""
    categories = {
        'ui_common': [],      # Общие UI элементы (кнопки, диалоги)
        'navigation': [],     # Навигация
        'categories': [],     # Категории транзакций
        'achievements': [],   # Достижения
        'errors': [],         # Ошибки
        'periods': [],        # Периоды времени
        'wallet_types': [],   # Типы кошельков
        'transaction_types': [], # Типы транзакций
        'statistics': [],     # Статистика
        'other': []           # Остальное
    }
    
    for value, files in duplicates.items():
        # Определяем категорию на основе содержимого и контекста
        if any(keyword in value.lower() for keyword in ['добавить', 'отмена', 'ок', 'закрыть', 'применить']):
            categories['ui_common'].append(value)
        elif any(keyword in value.lower() for keyword in ['навигация', 'история', 'статистика', 'профиль', 'бюджет']):
            categories['navigation'].append(value)
        elif any(keyword in value.lower() for keyword in ['продукты', 'услуги', 'транспорт', 'развлечения', 'одежда']):
            categories['categories'].append(value)
        elif any(keyword in value.lower() for keyword in ['достижение', 'мастер', 'первый', 'аналитик']):
            categories['achievements'].append(value)
        elif any(keyword in value.lower() for keyword in ['ошибка', 'неизвестная']):
            categories['errors'].append(value)
        elif any(keyword in value.lower() for keyword in ['день', 'неделя', 'месяц', 'год', 'период']):
            categories['periods'].append(value)
        elif any(keyword in value.lower() for keyword in ['наличные', 'карта', 'сбережения', 'инвестиции']):
            categories['wallet_types'].append(value)
        elif any(keyword in value.lower() for keyword in ['доход', 'расход', 'транзакция']):
            categories['transaction_types'].append(value)
        elif any(keyword in value.lower() for keyword in ['статистика', 'средний', 'норма', 'баланс']):
            categories['statistics'].append(value)
        else:
            categories['other'].append(value)
    
    return categories

def consolidate_to_ui_module(duplicates: Dict[str, List[Tuple[Path, str]]], 
                           categories: Dict[str, List[str]]) -> None:
    """Консолидирует общие строки в ui модуль"""
    ui_file = Path("ui/src/main/res/values/strings.xml")
    
    if not ui_file.exists():
        print(f"Файл {ui_file} не найден!")
        return
    
    # Читаем существующие строки в ui
    ui_strings = parse_strings_file(ui_file)
    tree = ET.parse(ui_file)
    root = tree.getroot()
    
    # Создаем резервную копию
    backup_file = ui_file.with_suffix('.xml.backup')
    shutil.copy2(ui_file, backup_file)
    print(f"Создана резервная копия: {backup_file}")
    
    consolidated_count = 0
    
    # Консолидируем строки по категориям
    for category, values in categories.items():
        if not values:
            continue
            
        print(f"\nОбрабатываем категорию: {category}")
        
        for value in values:
            if value not in duplicates:
                continue
                
            files = duplicates[value]
            
            # Выбираем лучшее имя для строки
            best_name = select_best_name(files, category)
            
            # Добавляем в ui если еще нет
            if best_name not in ui_strings:
                string_elem = ET.SubElement(root, "string")
                string_elem.set("name", best_name)
                string_elem.text = value
                ui_strings[best_name] = value
                consolidated_count += 1
                print(f"  Добавлена: {best_name} = '{value}'")
            
            # Удаляем дубликаты из других файлов
            for file_path, name in files:
                if file_path != ui_file:
                    remove_string_from_file(file_path, name)
                    print(f"  Удалена из {file_path}: {name}")
    
    # Сохраняем ui файл
    tree.write(ui_file, encoding="utf-8", xml_declaration=True)
    print(f"\nКонсолидировано {consolidated_count} строк в ui модуль")

def select_best_name(files: List[Tuple[Path, str]], category: str) -> str:
    """Выбирает лучшее имя для строки на основе контекста"""
    # Приоритет: ui > core > domain > feature modules
    priority_order = ['ui', 'core', 'domain', 'data', 'presentation']
    
    # Сортируем файлы по приоритету
    sorted_files = sorted(files, key=lambda x: get_module_priority(x[0]))
    
    # Возвращаем имя из файла с наивысшим приоритетом
    return sorted_files[0][1]

def get_module_priority(file_path: Path) -> int:
    """Возвращает приоритет модуля (меньше = выше приоритет)"""
    path_str = str(file_path)
    
    if 'ui/src' in path_str:
        return 0
    elif 'core/src' in path_str:
        return 1
    elif 'domain/src' in path_str:
        return 2
    elif 'data/src' in path_str:
        return 3
    elif 'presentation/src' in path_str:
        return 4
    elif 'feature/' in path_str:
        return 5
    else:
        return 6

def remove_string_from_file(file_path: Path, string_name: str) -> None:
    """Удаляет строку из файла strings.xml"""
    if not file_path.exists():
        return
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    # Находим и удаляем строку
    for string_elem in root.findall("string"):
        if string_elem.get("name") == string_name:
            root.remove(string_elem)
            break
    
    # Сохраняем файл
    tree.write(file_path, encoding="utf-8", xml_declaration=True)

def create_consolidation_report(duplicates: Dict[str, List[Tuple[Path, str]]], 
                              categories: Dict[str, List[str]]) -> None:
    """Создает отчет о консолидации"""
    report_file = Path("docs/STRING_CONSOLIDATION_REPORT.md")
    report_file.parent.mkdir(exist_ok=True)
    
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write("# Отчет о консолидации строковых ресурсов\n\n")
        f.write(f"Дата: {Path().cwd().name}\n\n")
        
        f.write("## Статистика\n\n")
        f.write(f"- Всего дубликатов: {len(duplicates)}\n")
        f.write(f"- Файлов strings.xml: {len(set(file for _, files in duplicates.items() for file, _ in files))}\n\n")
        
        f.write("## Категории строк\n\n")
        for category, values in categories.items():
            if values:
                f.write(f"### {category}\n")
                f.write(f"- Количество: {len(values)}\n")
                f.write("- Примеры:\n")
                for value in values[:5]:  # Показываем первые 5 примеров
                    f.write(f"  - `{value}`\n")
                if len(values) > 5:
                    f.write(f"  - ... и еще {len(values) - 5}\n")
                f.write("\n")
        
        f.write("## Рекомендации\n\n")
        f.write("1. Все общие строки консолидированы в ui модуль\n")
        f.write("2. Специфичные для модулей строки оставлены в соответствующих модулях\n")
        f.write("3. Используйте ui.R.string для доступа к общим строкам\n")
        f.write("4. Регулярно проверяйте новые дубликаты\n\n")
    
    print(f"Отчет сохранен: {report_file}")

def main():
    parser = argparse.ArgumentParser(description="Консолидация строковых ресурсов")
    parser.add_argument("--action", choices=["analyze", "consolidate", "report"], 
                       default="consolidate", help="Действие для выполнения")
    args = parser.parse_args()
    
    print("АНАЛИЗ И КОНСОЛИДАЦИЯ СТРОКОВЫХ РЕСУРСОВ")
    print("=" * 50)
    
    # Находим дубликаты
    duplicates = find_duplicates()
    print(f"Найдено {len(duplicates)} дубликатов")
    
    if args.action == "analyze":
        # Только анализ
        categories = categorize_strings(duplicates)
        print("\nКатегории дубликатов:")
        for category, values in categories.items():
            if values:
                print(f"  {category}: {len(values)} строк")
        return
    
    if args.action == "report":
        # Создаем отчет
        categories = categorize_strings(duplicates)
        create_consolidation_report(duplicates, categories)
        return
    
    # Консолидация
    if duplicates:
        categories = categorize_strings(duplicates)
        consolidate_to_ui_module(duplicates, categories)
        create_consolidation_report(duplicates, categories)
        print("\nКонсолидация завершена!")
    else:
        print("Дубликаты не найдены!")

if __name__ == "__main__":
    main() 