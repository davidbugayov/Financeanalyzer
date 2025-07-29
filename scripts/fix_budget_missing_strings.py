#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/budget модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_budget():
    budget_file = Path("feature/budget/src/main/res/values/strings.xml")
    if not budget_file.exists():
        print(f"Файл {budget_file} не найден!")
        return
    tree = ET.parse(budget_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'period_settings_title': 'Настройки периода',
        'ok': 'ОК',
        'wallet_not_found': 'Кошелек не найден',
        'error_title': 'Ошибка',
        'wallet_name_label': 'Название кошелька',
        'wallet_name_hint': 'Введите название кошелька',
        'wallet_type_label': 'Тип кошелька',
        'wallet_spent_amount': 'Потрачено',
        'wallet_remaining_amount': 'Осталось',
        'wallet_budget_limit': 'Лимит бюджета',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в budget")
    if added_count > 0:
        tree.write(budget_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в budget")
    else:
        print("Недостающих строк в budget не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_budget() 