#!/usr/bin/env python3
"""
Полный скрипт для добавления всех недостающих строк во все модули.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_profile():
    """Добавляет недостающие строки в profile модуль"""
    file_path = Path("feature/profile/src/main/res/values/strings.xml")
    
    missing_strings = {
        'total_transactions': 'Всего транзакций',
        'achievement_first_transaction_desc': 'Создана первая транзакция'
    }
    
    add_strings_to_file(file_path, missing_strings, "profile")

def add_missing_strings_to_home():
    """Добавляет недостающие строки в home модуль"""
    file_path = Path("feature/home/src/main/res/values/strings.xml")
    
    missing_strings = {
        'current_balance': 'Текущий баланс',
        'financial_analyzer': 'Finance Analyzer',
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
        'feedback_error': 'Ошибка'
    }
    
    add_strings_to_file(file_path, missing_strings, "home")

def add_missing_strings_to_history():
    """Добавляет недостающие строки в history модуль"""
    file_path = Path("feature/history/src/main/res/values/strings.xml")
    
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
        'expenses': 'Расходы',
        'incomes': 'Доходы'
    }
    
    add_strings_to_file(file_path, missing_strings, "history")

def add_missing_strings_to_budget():
    """Добавляет недостающие строки в budget модуль"""
    file_path = Path("feature/budget/src/main/res/values/strings.xml")
    
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
    
    add_strings_to_file(file_path, missing_strings, "budget")

def add_strings_to_file(file_path: Path, strings_dict: dict, module_name: str):
    """Добавляет строки в файл"""
    if not file_path.exists():
        print(f"Файл {file_path} не найден!")
        return
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    # Проверяем, какие строки уже существуют
    existing_strings = set()
    for string_elem in root.findall("string"):
        name = string_elem.get("name")
        existing_strings.add(name)
    
    # Добавляем только недостающие строки
    added_count = 0
    for name, value in strings_dict.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
    
    if added_count > 0:
        tree.write(file_path, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} новых строк в {module_name} модуль")

def fix_all_missing_strings():
    """Исправляет все недостающие строки"""
    print("Начинаем исправление всех недостающих строк...")
    
    add_missing_strings_to_profile()
    add_missing_strings_to_home()
    add_missing_strings_to_history()
    add_missing_strings_to_budget()
    
    print("\nИсправление всех недостающих строк завершено!")

if __name__ == "__main__":
    fix_all_missing_strings() 