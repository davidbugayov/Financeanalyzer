#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в ui модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_ui():
    """Добавляет недостающие строки в ui модуль"""
    ui_file = Path("ui/src/main/res/values/strings.xml")

    if not ui_file.exists():
        print(f"Файл {ui_file} не найден!")
        return

    tree = ET.parse(ui_file)
    root = tree.getroot()

    existing_strings = set()
    for string_elem in root.findall("string"):
        name = string_elem.get("name")
        existing_strings.add(name)

    missing_strings = {
        'statistics': 'Статистика',
        'add_button': 'Добавить',
        'rec_critical_emergency_title': 'Создайте резервный фонд',
        'rec_critical_emergency_desc': 'У вас нет резервного фонда. Это критически важно для финансовой безопасности.',
        'rec_critical_emergency_impact': 'Создайте фонд на 3-6 месяцев расходов',
        'rec_high_savings_impact': 'Цель: 15-20% от доходов',
        'rec_normal_invest_title': 'Рассмотрите инвестиции',
        'rec_onboarding_stats_title': 'Статистика',
        'nav_budget': 'Бюджет',
        'nav_history': 'История',
        'transaction': 'Транзакция',
        'transaction_amount': 'Сумма',
        'transaction_category': 'Категория',
        'transaction_date': 'Дата',
        'transaction_note': 'Заметка',
        'transaction_type_income': 'Доход',
        'transaction_type_expense': 'Расход',
        'transaction_transfer': 'Перевод',
        'wallet_balance': 'Баланс',
        'wallet_type_savings': 'Сбережения',
        'wallet_type_investment': 'Инвестиции',
        'wallet_type_other': 'Другое',
        'error_linking_categories': 'Ошибка привязки категорий',
        'add_transaction_long': 'Добавить транзакцию',
        'expenses_category': 'Расходы',
        'emergency_fund_category': 'Резервный фонд',
        'study_achievements_title': 'Изучите достижения',
        'stat_income_transactions': 'Доходы',
        'stat_expense_transactions': 'Расходы',
        'stat_avg_expense': 'Средний расход',
        'stat_savings_rate': 'Норма сбережений',
        'insight_expense_pattern_desc': 'Наиболее активный день: %s',
        'insight_expense_pattern_metric': '%s',
        'insight_high_activity_desc': '%d транзакций за период',
        'insight_high_activity_metric': '%d транзакций',
        'dialog_cancel': 'Отмена',
        'period_all_time': 'За все время',
        'period_day': '%s',
        'period_week': '%s - %s',
        'period_month': '%s - %s',
        'period_quarter': '%s - %s',
        'period_year': '%s - %s',
        'expense': 'Расход'
    }

    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в ui")

    if added_count > 0:
        tree.write(ui_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в ui")
    else:
        print("Недостающих строк в ui не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_ui() 