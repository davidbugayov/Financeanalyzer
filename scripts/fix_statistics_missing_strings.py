#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/statistics модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_statistics():
    statistics_file = Path("feature/statistics/src/main/res/values/strings.xml")
    if not statistics_file.exists():
        print(f"Файл {statistics_file} не найден!")
        return
    tree = ET.parse(statistics_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'select_period': 'Выберите период',
        'all_time': 'За все время',
        'day': 'День',
        'week': 'Неделя',
        'month': 'Месяц',
        'period_quarter': 'Квартал',
        'year': 'Год',
        'start_date': 'Дата начала',
        'end_date': 'Дата окончания',
        'apply': 'Применить',
        'statistics': 'Статистика',
        'tips': 'Советы',
        'income': 'Доходы',
        'expense': 'Расходы',
        'average_expenses_title': 'Средний расход',
        'savings_rate_title': 'Норма сбережений',
        'financial_cushion_title': 'Финансовая подушка',
        'financial_health_title': 'Финансовое здоровье',
        'financial_health_savings_rate_title': 'Норма сбережений',
        'financial_health_average_expenses_title': 'Средний расход',
        'financial_health_financial_cushion_title': 'Финансовая подушка',
        'financial_health_ok_button': 'ОК',
        'chart_title_income': 'Доходы',
        'chart_title_expense': 'Расходы',
        'total_transactions': 'Всего транзакций',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в statistics")
    if added_count > 0:
        tree.write(statistics_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в statistics")
    else:
        print("Недостающих строк в statistics не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_statistics() 