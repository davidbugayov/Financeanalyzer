#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/home модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_home():
    home_file = Path("feature/home/src/main/res/values/strings.xml")
    if not home_file.exists():
        print(f"Файл {home_file} не найден!")
        return
    tree = ET.parse(home_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'current_balance': 'Текущий баланс',
        'profile': 'Профиль',
        'add_transaction': 'Добавить транзакцию',
        'loading_data': 'Загрузка данных...',
        'filter_today': 'Сегодня',
        'filter_week': 'Неделя',
        'filter_month': 'Месяц',
        'filter_all_time': 'За все время',
        'balance': 'Баланс',
        'expense_categories': 'Категории расходов',
        'income_categories': 'Категории доходов',
        'hide': 'Скрыть',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в home")
    if added_count > 0:
        tree.write(home_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в home")
    else:
        print("Недостающих строк в home не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_home() 