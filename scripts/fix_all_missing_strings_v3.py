#!/usr/bin/env python3
"""
Скрипт для добавления всех недостающих строк в модули home, history и budget.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_strings_to_file(file_path: Path, strings_dict: dict):
    """Добавляет строки в файл strings.xml"""
    if not file_path.exists():
        print(f"Файл {file_path} не найден!")
        return
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    existing_strings = set()
    for string_elem in root.findall("string"):
        name = string_elem.get("name")
        existing_strings.add(name)
    
    added_count = 0
    for name, value in strings_dict.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в {file_path.parent.parent.parent.name}")
    
    if added_count > 0:
        tree.write(file_path, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в {file_path.parent.parent.parent.name}")

def add_missing_strings_to_home():
    """Добавляет недостающие строки в home модуль"""
    home_file = Path("feature/home/src/main/res/values/strings.xml")
    
    missing_strings = {
        'current_balance': 'Текущий баланс',
        'financial_analyzer': 'Финансовый Анализатор',
        'profile': 'Профиль',
        'add_transaction': 'Добавить транзакцию',
        'loading_data': 'Загрузка данных...',
        'filter_today': 'Сегодня',
        'filter_week': 'Неделя',
        'filter_month': 'Месяц',
        'filter_all_time': 'Все время',
        'balance': 'Баланс',
        'expense_categories': 'Категории расходов',
        'income_categories': 'Категории доходов',
        'hide': 'Скрыть',
        'feedback_error': 'Ошибка обратной связи'
    }
    
    add_strings_to_file(home_file, missing_strings)

def add_missing_strings_to_history():
    """Добавляет недостающие строки в history модуль"""
    history_file = Path("feature/history/src/main/res/values/strings.xml")
    
    missing_strings = {
        'select_category': 'Выбрать категорию',
        'select_period': 'Выбрать период',
        'apply': 'Применить',
        'close': 'Закрыть',
        'group_by_days': 'По дням',
        'group_by_weeks': 'По неделям',
        'group_by_months': 'По месяцам',
        'collapse': 'Свернуть',
        'expand': 'Развернуть',
        'transaction_history': 'История транзакций',
        'all_time': 'Все время',
        'day': 'День',
        'week': 'Неделя',
        'month': 'Месяц',
        'quarter': 'Квартал',
        'year': 'Год',
        'start_date': 'Начальная дата',
        'end_date': 'Конечная дата',
        'delete': 'Удалить',
        'add_transaction': 'Добавить транзакцию',
        'loading_data': 'Загрузка данных...',
        'expenses': 'Расходы',
        'incomes': 'Доходы'
    }
    
    add_strings_to_file(history_file, missing_strings)

def add_missing_strings_to_budget():
    """Добавляет недостающие строки в budget модуль"""
    budget_file = Path("feature/budget/src/main/res/values/strings.xml")
    
    missing_strings = {
        'period_settings_title': 'Настройки периода',
        'ok': 'ОК',
        'wallet_not_found': 'Кошелек не найден',
        'transactions_section': 'Секция транзакций',
        'link_categories_title': 'Привязать категории',
        'error_title': 'Ошибка',
        'wallet_name_label': 'Название кошелька',
        'wallet_name_hint': 'Введите название кошелька',
        'wallet_type_label': 'Тип кошелька',
        'wallet_spent_amount': 'Потрачено',
        'wallet_remaining_amount': 'Осталось',
        'wallet_budget_limit': 'Лимит бюджета',
        'wallet_spend_action': 'Тратить'
    }
    
    add_strings_to_file(budget_file, missing_strings)

def main():
    """Основная функция"""
    print("Добавляем недостающие строки в модули...")
    
    add_missing_strings_to_home()
    add_missing_strings_to_history()
    add_missing_strings_to_budget()
    
    print("Все недостающие строки добавлены!")

if __name__ == "__main__":
    main() 