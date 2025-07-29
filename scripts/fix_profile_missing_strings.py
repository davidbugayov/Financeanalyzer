#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/profile модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_profile():
    profile_file = Path("feature/profile/src/main/res/values/strings.xml")
    if not profile_file.exists():
        print(f"Файл {profile_file} не найден!")
        return
    tree = ET.parse(profile_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'total_transactions': 'Всего транзакций',
        'permission_required_title': 'Требуется разрешение',
        'profile_theme_light': 'Светлая',
        'profile_theme_dark': 'Темная',
        'profile_theme_system': 'Системная',
        'done': 'Готово',
        'ok': 'ОК',
        'profile_title': 'Профиль',
        'budget': 'Бюджет',
        'settings_theme_light': 'Светлая',
        'settings_theme_dark': 'Темная',
        'settings_theme_system': 'Системная',
        'analytics_title': 'Аналитика',
        'income': 'Доходы',
        'expenses': 'Расходы',
        'balance': 'Баланс',
        'savings_rate': 'Норма сбережений',
        'average_expense': 'Средний расход',
        'sources_used': 'Источники',
        'cd_done': 'Готово',
        'achievement_first_transaction_desc': 'Создана первая транзакция',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в profile")
    if added_count > 0:
        tree.write(profile_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в profile")
    else:
        print("Недостающих строк в profile не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_profile() 