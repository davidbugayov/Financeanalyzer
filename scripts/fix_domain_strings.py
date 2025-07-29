#!/usr/bin/env python3
"""
Скрипт для добавления всех недостающих строк в domain модуль.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_missing_strings_to_domain():
    """Добавляет недостающие строки в domain модуль"""
    file_path = Path("domain/src/main/res/values/strings.xml")
    
    missing_strings = {
        'achievement_first_steps': 'Первые шаги',
        'achievement_first_steps_desc': 'Создана первая транзакция',
        'achievement_transaction_master': 'Мастер транзакций',
        'achievement_data_analyst': 'Аналитик данных',
        'achievement_first_budget': 'Первый бюджет',
        'achievement_category_organizer': 'Организатор категорий',
        'achievement_early_bird': 'Ранняя пташка',
        'achievement_night_owl': 'Ночная сова',
        'achievement_export_master': 'Мастер экспорта',
        'achievement_backup_enthusiast': 'Энтузиаст резервных копий',
        'category_products': 'Продукты',
        'category_services': 'Услуги',
        'category_gifts': 'Подарки',
        'category_rental': 'Аренда',
        'category_transport': 'Транспорт',
        'category_utilities': 'Коммунальные услуги',
        'category_clothing': 'Одежда',
        'category_entertainment': 'Развлечения',
        'category_other': 'Другое',
        'recommendation_increase_savings_rate': 'Увеличьте норму сбережений',
        'recommendation_create_emergency_fund': 'Создайте резервный фонд',
        'export_transaction_type_expense': 'Расход',
        'export_transaction_type_income': 'Доход',
        'wallet_type_cash': 'Наличные',
        'wallet_type_card': 'Карта',
        'wallet_type_savings': 'Сбережения',
        'wallet_type_investment': 'Инвестиции',
        'wallet_type_other': 'Другое'
    }
    
    add_strings_to_file(file_path, missing_strings, "domain")

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

if __name__ == "__main__":
    add_missing_strings_to_domain()
    print("Недостающие строки в domain модуле добавлены!") 