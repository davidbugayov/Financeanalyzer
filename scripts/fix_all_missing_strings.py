#!/usr/bin/env python3
"""
Скрипт для автоматического добавления всех отсутствующих строк в соответствующие модули.
"""

import os
import xml.etree.ElementTree as ET
from pathlib import Path

class AllMissingStringsFixer:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        
    def add_missing_strings(self):
        """Добавляет все недостающие строки в соответствующие модули."""
        
        # Модуль profile
        profile_strings = {
            'permission_required_title': 'Требуется разрешение',
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
            'cd_done': 'Готово'
        }
        
        # Модуль statistics
        statistics_strings = {
            'statistics': 'Статистика',
            'tips': 'Советы',
            'income': 'Доходы',
            'expense': 'Расход',
            'savings_rate_title': 'Норма сбережений',
            'financial_cushion_title': 'Финансовая подушка',
            'financial_health_title': 'Финансовое здоровье',
            'financial_health_savings_rate_title': 'Норма сбережений',
            'financial_health_financial_cushion_title': 'Финансовая подушка',
            'chart_title_income': 'Доходы',
            'chart_title_expense': 'Расходы'
        }
        
        # Модуль transaction
        transaction_strings = {
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
            'import_transactions_title': 'Импорт транзакций',
            'add_button_text': 'Добавить',
            'category_other': 'Другое',
            'category_transfers': 'Переводы',
            'source_cash': 'Наличные',
            'source_card': 'Карта',
            'edit_transaction_title': 'Редактировать транзакцию',
            'save_button_text': 'Сохранить',
            'category_transfer': 'Перевод',
            'select_wallets': 'Выбрать кошельки',
            'error_title': 'Ошибка',
            'dialog_cancel': 'Отмена',
            'delete_category_title': 'Удалить категорию',
            'dialog_delete': 'Удалить',
            'delete_source_title': 'Удалить источник',
            'import_button': 'Импортировать',
            'import_transactions_content_description': 'Импорт транзакций',
            'import_unknown_error': 'Неизвестная ошибка импорта'
        }
        
        # Добавляем строки в модули
        self.add_strings_to_module('feature/profile', profile_strings)
        self.add_strings_to_module('feature/statistics', statistics_strings)
        self.add_strings_to_module('feature/transaction', transaction_strings)
        
        print("Все недостающие строки добавлены!")
    
    def add_strings_to_module(self, module_path: str, strings: dict):
        """Добавляет строки в указанный модуль."""
        strings_file = self.project_root / module_path / "src/main/res/values/strings.xml"
        
        if not strings_file.exists():
            print(f"Файл {strings_file} не найден!")
            return
        
        try:
            with open(strings_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            root = ET.fromstring(content)
            
            # Добавляем новые строки
            for name, value in strings.items():
                # Проверяем, не существует ли уже строка
                existing = root.find(f".//string[@name='{name}']")
                if existing is None:
                    new_string = ET.SubElement(root, 'string')
                    new_string.set('name', name)
                    new_string.text = value
            
            # Сохраняем файл
            tree = ET.ElementTree(root)
            ET.indent(tree, space="    ")
            
            with open(strings_file, 'w', encoding='utf-8') as f:
                f.write('<?xml version=\'1.0\' encoding=\'utf-8\'?>\n')
                tree.write(f, encoding='unicode', xml_declaration=False)
            
            print(f"Добавлено {len(strings)} строк в модуль {module_path}")
            
        except Exception as e:
            print(f"Ошибка при добавлении строк в {module_path}: {e}")

def main():
    fixer = AllMissingStringsFixer(".")
    fixer.add_missing_strings()

if __name__ == "__main__":
    main() 