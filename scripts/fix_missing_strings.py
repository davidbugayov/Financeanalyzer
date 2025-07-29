#!/usr/bin/env python3
"""
Скрипт для автоматического добавления недостающих строк в соответствующие модули.
"""
import xml.etree.ElementTree as ET
from pathlib import Path
import time

class MissingStringsFixer:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        
    def add_missing_strings_to_profile(self):
        """Добавляет недостающие строки в profile модуль"""
        file_path = self.project_root / "feature/profile/src/main/res/values/strings.xml"
        
        missing_strings = {
            'total_transactions': 'Всего транзакций',
            'permission_required_title': 'Требуется разрешение',
            'profile_theme_light': 'Светлая',
            'profile_theme_dark': 'Тёмная',
            'profile_theme_system': 'Системная',
            'done': 'Готово',
            'ok': 'ОК',
            'profile_title': 'Профиль',
            'budget': 'Бюджет',
            'settings_theme_light': 'Светлая',
            'settings_theme_dark': 'Тёмная',
            'settings_theme_system': 'Системная',
            'analytics_title': 'Аналитика',
            'income': 'Доходы',
            'expenses': 'Расходы',
            'balance': 'Баланс',
            'savings_rate': 'Норма сбережений',
            'average_expense': 'Средний расход',
            'sources_used': 'Используемые источники',
            'cd_done': 'Готово',
            'achievement_first_transaction_desc': 'Создана первая транзакция'
        }
        
        self._add_strings_to_file(file_path, missing_strings)
        print(f"Добавлено {len(missing_strings)} строк в profile модуль")
    
    def add_missing_strings_to_statistics(self):
        """Добавляет недостающие строки в statistics модуль"""
        file_path = self.project_root / "feature/statistics/src/main/res/values/strings.xml"
        
        missing_strings = {
            'select_period': 'Выбрать период',
            'all_time': 'Все время',
            'day': 'День',
            'week': 'Неделя',
            'month': 'Месяц',
            'period_quarter': 'Квартал',
            'year': 'Год',
            'start_date': 'Начальная дата',
            'end_date': 'Конечная дата',
            'apply': 'Применить',
            'statistics': 'Статистика',
            'tips': 'Советы',
            'income': 'Доходы',
            'expense': 'Расход',
            'average_expenses_title': 'Средние расходы',
            'savings_rate_title': 'Норма сбережений',
            'financial_cushion_title': 'Финансовая подушка',
            'financial_health_title': 'Финансовое здоровье',
            'financial_health_savings_rate_title': 'Норма сбережений',
            'financial_health_average_expenses_title': 'Средние расходы',
            'financial_health_financial_cushion_title': 'Финансовая подушка',
            'financial_health_ok_button': 'Понятно',
            'chart_title_income': 'Доходы',
            'chart_title_expense': 'Расходы'
        }
        
        self._add_strings_to_file(file_path, missing_strings)
        print(f"Добавлено {len(missing_strings)} строк в statistics модуль")
    
    def add_missing_strings_to_transaction(self):
        """Добавляет недостающие строки в transaction модуль"""
        file_path = self.project_root / "feature/transaction/src/main/res/values/strings.xml"
        
        missing_strings = {
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
            'select_wallets': 'Выбрать кошельки',
            'error_title': 'Ошибка',
            'dialog_cancel': 'Отмена',
            'delete_category_title': 'Удалить категорию',
            'dialog_delete': 'Удалить',
            'delete_source_title': 'Удалить источник',
            'import_transactions_title': 'Импорт транзакций',
            'import_button': 'Импортировать',
            'import_transactions_content_description': 'Импорт транзакций',
            'import_unknown_error': 'Неизвестная ошибка импорта',
            'header_date': 'Дата',
            'header_amount': 'Сумма',
            'transaction_source_alfa': 'Альфа-Банк',
            'csv_expense_value': 'Расход',
            'transaction_source_ozon': 'Озон',
            'transaction_source_tinkoff': 'Тинькофф',
            'back': 'Назад',
            'got_it': 'Понятно',
            'bank_sberbank': 'Сбербанк',
            'log_error_loading_wallets': 'Ошибка при загрузке кошельков',
            'log_error_loading_wallets_base': 'Ошибка при загрузке кошельков',
            'category_other': 'Другое',
            'category_transfers': 'Переводы',
            'source_cash': 'Наличные',
            'source_card': 'Карта',
            'bank_ozon': 'Озон',
            'sberbank_name': 'Сбербанк',
            'edit_transaction_title': 'Редактировать транзакцию',
            'save_button_text': 'Сохранить',
            'add_button_text': 'Добавить',
            'category_transfer': 'Перевод'
        }
        
        self._add_strings_to_file(file_path, missing_strings)
        print(f"Добавлено {len(missing_strings)} строк в transaction модуль")
    
    def _add_strings_to_file(self, file_path: Path, strings_dict: dict):
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
            print(f"Добавлено {added_count} новых строк в {file_path}")
    
    def fix_all_missing_strings(self):
        """Исправляет все недостающие строки"""
        print("Начинаем исправление недостающих строк...")
        
        self.add_missing_strings_to_profile()
        self.add_missing_strings_to_statistics()
        self.add_missing_strings_to_transaction()
        
        print("\nИсправление недостающих строк завершено!")

def main():
    import sys
    
    if len(sys.argv) != 2:
        print("Использование: python3 scripts/fix_missing_strings.py <project_root>")
        sys.exit(1)
    
    project_root = sys.argv[1]
    fixer = MissingStringsFixer(project_root)
    
    try:
        fixer.fix_all_missing_strings()
        print("\nВсе недостающие строки добавлены!")
    except Exception as e:
        print(f"Ошибка: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 