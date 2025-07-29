#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/history модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_history():
    history_file = Path("feature/history/src/main/res/values/strings.xml")
    if not history_file.exists():
        print(f"Файл {history_file} не найден!")
        return
    tree = ET.parse(history_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'select_category': 'Выберите категорию',
        'select_period': 'Выберите период',
        'apply': 'Применить',
        'close': 'Закрыть',
        'group_by_days': 'По дням',
        'group_by_weeks': 'По неделям',
        'group_by_months': 'По месяцам',
        'collapse': 'Свернуть',
        'expand': 'Развернуть',
        'transaction_history': 'История транзакций',
        'all_time': 'За все время',
        'day': 'День',
        'week': 'Неделя',
        'month': 'Месяц',
        'quarter': 'Квартал',
        'year': 'Год',
        'start_date': 'Дата начала',
        'end_date': 'Дата окончания',
        'delete': 'Удалить',
        'add_transaction': 'Добавить транзакцию',
        'loading_data': 'Загрузка данных...',
        'expenses': 'Расходы',
        'incomes': 'Доходы',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в history")
    if added_count > 0:
        tree.write(history_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в history")
    else:
        print("Недостающих строк в history не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_history() 