#!/usr/bin/env python3
"""
Скрипт для добавления недостающих строк в feature/transaction модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_transaction():
    transaction_file = Path("feature/transaction/src/main/res/values/strings.xml")
    if not transaction_file.exists():
        print(f"Файл {transaction_file} не найден!")
        return
    tree = ET.parse(transaction_file)
    root = tree.getroot()
    existing_strings = set(s.get("name") for s in root.findall("string"))
    missing_strings = {
        'add_button': 'Добавить',
        'select_category': 'Выберите категорию',
        'add_category': 'Добавить категорию',
        'category_name': 'Название категории',
        'select_icon': 'Выберите иконку',
        'category': 'Категория',
        'add_custom_category': 'Добавить свою категорию',
        'select_color': 'Выберите цвет',
        'add_custom_source': 'Добавить свой источник',
        'source_name': 'Название источника',
        'date': 'Дата',
        'select_date_button': 'Выберите дату',
        'note_optional': 'Заметка (необязательно)',
        'select_source': 'Выберите источник',
        'delete_source': 'Удалить источник',
        'source': 'Источник',
        'income_type': 'Доход',
        'expense_type': 'Расход',
        'deduct_from_wallets': 'Списать с кошельков',
        'add_to_wallets': 'Добавить в кошельки',
        'header_date': 'Дата',
        'header_amount': 'Сумма',
        'transaction_source_alfa': 'Альфа-Банк',
        'csv_expense_value': 'Расход',
        'transaction_source_ozon': 'Ozon',
        'transaction_source_tinkoff': 'Тинькофф',
        'back': 'Назад',
        'import_transactions_title': 'Импорт транзакций',
        'got_it': 'Понятно',
        'bank_sberbank': 'Сбербанк',
        'bank_tinkoff': 'Тинькофф',
        'bank_alfabank': 'Альфа-Банк',
        'bank_ozon': 'Ozon',
        'add_button_text': 'Добавить',
        'log_error_loading_wallets': 'Ошибка загрузки кошельков',
        'error_unknown': 'Неизвестная ошибка',
        'log_error_loading_wallets_base': 'Ошибка загрузки кошельков',
        'category_other': 'Другое',
        'category_transfers': 'Переводы',
        'source_cash': 'Наличные',
        'source_card': 'Карта',
        'sberbank_name': 'Сбербанк',
        'edit_transaction_title': 'Редактировать транзакцию',
        'save_button_text': 'Сохранить',
        'transaction_transfer': 'Перевод',
        'select_wallets': 'Выберите кошельки',
        'error_title': 'Ошибка',
        'dialog_cancel': 'Отмена',
        'delete_category_title': 'Удалить категорию',
        'dialog_delete': 'Удалить',
        'delete_source_title': 'Удалить источник',
        'import_button': 'Импорт',
        'import_unknown_error': 'Неизвестная ошибка импорта',
    }
    added_count = 0
    for name, value in missing_strings.items():
        if name not in existing_strings:
            string_elem = ET.SubElement(root, "string")
            string_elem.set("name", name)
            string_elem.text = value
            added_count += 1
            print(f"Добавлена строка {name} в transaction")
    if added_count > 0:
        tree.write(transaction_file, encoding="utf-8", xml_declaration=True)
        print(f"Добавлено {added_count} строк в transaction")
    else:
        print("Недостающих строк в transaction не найдено.")

if __name__ == "__main__":
    add_missing_strings_to_transaction() 