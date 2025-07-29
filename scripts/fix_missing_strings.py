#!/usr/bin/env python3
"""
Скрипт для автоматического добавления отсутствующих строк в соответствующие модули.
"""

import os
import xml.etree.ElementTree as ET
from pathlib import Path
import re

class MissingStringsFixer:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        
    def find_missing_strings(self):
        """Находит отсутствующие строки в модулях."""
        missing_strings = {
            'feature/onboarding': ['onboarding_welcome_title'],
            'feature/profile': [
                'permission_required_title', 'edit_time', 'done', 'ok', 'profile_title', 
                'budget', 'analytics_title', 'income', 'expenses', 'balance', 'savings_rate',
                'average_expense', 'sources_used'
            ],
            'feature/statistics': [
                'statistics', 'tips', 'income', 'expense', 'savings_rate_title',
                'financial_cushion_title', 'financial_health_title', 'financial_health_savings_rate_title',
                'financial_health_financial_cushion_title', 'financial_health_ok_button',
                'chart_title_income', 'chart_title_expense'
            ],
            'feature/transaction': [
                'add_button', 'select_category', 'add_category', 'category_name', 'select_icon',
                'category', 'add_custom_category', 'select_color', 'add_custom_source', 'source_name',
                'date', 'select_date_button', 'note_optional', 'select_source', 'delete_source',
                'source', 'income_type', 'expense_type', 'deduct_from_wallets', 'add_to_wallets',
                'header_date', 'header_amount', 'csv_expense_value', 'back', 'import_section_title',
                'import_transactions_title', 'add_button_text', 'error_unknown', 'category_other',
                'category_transfers', 'source_cash', 'source_card', 'edit_transaction_title',
                'save_button_text', 'error_title', 'dialog_cancel', 'delete_category_title',
                'dialog_delete', 'delete_source_title', 'import_button', 'import_transactions_content_description',
                'import_unknown_error'
            ]
        }
        
        return missing_strings
    
    def get_string_values(self):
        """Возвращает значения для отсутствующих строк."""
        return {
            'onboarding_welcome_title': 'Добро пожаловать',
            'permission_required_title': 'Требуется разрешение',
            'edit_time': 'Изменить время',
            'done': 'Готово',
            'ok': 'ОК',
            'profile_title': 'Профиль',
            'budget': 'Бюджет',
            'analytics_title': 'Аналитика',
            'income': 'Доходы',
            'expenses': 'Расходы',
            'balance': 'Баланс',
            'savings_rate': 'Норма сбережений',
            'average_expense': 'Средний расход',
            'sources_used': 'Используемые источники',
            'statistics': 'Статистика',
            'tips': 'Советы',
            'savings_rate_title': 'Норма сбережений',
            'financial_cushion_title': 'Финансовая подушка',
            'financial_health_title': 'Финансовое здоровье',
            'financial_health_savings_rate_title': 'Норма сбережений',
            'financial_health_financial_cushion_title': 'Финансовая подушка',
            'financial_health_ok_button': 'Понятно',
            'chart_title_income': 'Доходы',
            'chart_title_expense': 'Расходы',
            'add_button': 'Добавить',
            'select_category': 'Выбрать категорию',
            'add_category': 'Добавить категорию',
            'category_name': 'Название категории',
            'select_icon': 'Выбрать иконку',
            'category': 'Категория',
            'add_custom_category': 'Добавить категорию',
            'select_color': 'Выбрать цвет',
            'add_custom_source': 'Добавить источник',
            'source_name': 'Название источника',
            'date': 'Дата',
            'select_date_button': 'Выбрать дату',
            'note_optional': 'Заметка (необязательно)',
            'select_source': 'Выбрать источник',
            'delete_source': 'Удалить источник',
            'source': 'Источник',
            'income_type': 'Доход',
            'expense_type': 'Расход',
            'deduct_from_wallets': 'Списать с кошельков',
            'add_to_wallets': 'Добавить в кошельки',
            'header_date': 'Дата',
            'header_amount': 'Сумма',
            'csv_expense_value': 'Расход',
            'back': 'Назад',
            'import_section_title': 'Импорт',
            'import_transactions_title': 'Импорт транзакций',
            'add_button_text': 'Добавить',
            'error_unknown': 'Неизвестная ошибка',
            'category_other': 'Другое',
            'category_transfers': 'Переводы',
            'source_cash': 'Наличные',
            'source_card': 'Карта',
            'edit_transaction_title': 'Редактировать транзакцию',
            'save_button_text': 'Сохранить',
            'error_title': 'Ошибка',
            'dialog_cancel': 'Отмена',
            'delete_category_title': 'Удалить категорию',
            'dialog_delete': 'Удалить',
            'delete_source_title': 'Удалить источник',
            'import_button': 'Импортировать',
            'import_transactions_content_description': 'Импорт транзакций',
            'import_unknown_error': 'Неизвестная ошибка импорта'
        }
    
    def add_missing_strings_to_module(self, module_path: str, missing_strings: list):
        """Добавляет отсутствующие строки в модуль."""
        strings_file = self.project_root / module_path / "src/main/res/values/strings.xml"
        
        if not strings_file.exists():
            print(f"Файл {strings_file} не найден")
            return
        
        # Читаем существующий файл
        with open(strings_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Парсим XML
        root = ET.fromstring(content)
        
        # Получаем значения строк
        string_values = self.get_string_values()
        
        # Добавляем отсутствующие строки
        added_count = 0
        for string_name in missing_strings:
            if string_name in string_values:
                # Проверяем, не существует ли уже строка
                existing = root.find(f".//string[@name='{string_name}']")
                if existing is None:
                    # Добавляем новую строку
                    new_string = ET.SubElement(root, 'string')
                    new_string.set('name', string_name)
                    new_string.text = string_values[string_name]
                    added_count += 1
                    print(f"Добавлена строка: {string_name} = {string_values[string_name]}")
        
        if added_count > 0:
            # Сохраняем файл
            tree = ET.ElementTree(root)
            ET.indent(tree, space="    ")
            
            with open(strings_file, 'w', encoding='utf-8') as f:
                f.write('<?xml version=\'1.0\' encoding=\'utf-8\'?>\n')
                tree.write(f, encoding='unicode', xml_declaration=False)
            
            print(f"Добавлено {added_count} строк в {module_path}")
        else:
            print(f"Все строки уже существуют в {module_path}")
    
    def fix_all_modules(self):
        """Исправляет все модули."""
        missing_strings = self.find_missing_strings()
        
        for module_path, strings in missing_strings.items():
            print(f"\nОбрабатываем модуль: {module_path}")
            self.add_missing_strings_to_module(module_path, strings)

def main():
    fixer = MissingStringsFixer(".")
    fixer.fix_all_modules()
    print("\nГотово! Все отсутствующие строки добавлены.")

if __name__ == "__main__":
    main() 