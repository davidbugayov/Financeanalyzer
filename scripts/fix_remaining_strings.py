#!/usr/bin/env python3
"""
Скрипт для добавления оставшихся недостающих строк.
"""
import xml.etree.ElementTree as ET
from pathlib import Path

def add_remaining_strings():
    """Добавляет оставшиеся недостающие строки"""
    
    # Добавляем в transaction
    transaction_file = Path("feature/transaction/src/main/res/values/strings.xml")
    if transaction_file.exists():
        tree = ET.parse(transaction_file)
        root = tree.getroot()
        
        remaining_strings = {
            'bank_tinkoff': 'Тинькофф',
            'bank_alfabank': 'Альфа-Банк',
            'error_unknown': 'Неизвестная ошибка'
        }
        
        existing_strings = set()
        for string_elem in root.findall("string"):
            name = string_elem.get("name")
            existing_strings.add(name)
        
        for name, value in remaining_strings.items():
            if name not in existing_strings:
                string_elem = ET.SubElement(root, "string")
                string_elem.set("name", name)
                string_elem.text = value
                print(f"Добавлена строка {name} в transaction")
        
        tree.write(transaction_file, encoding="utf-8", xml_declaration=True)
    
    print("Оставшиеся строки добавлены!")

if __name__ == "__main__":
    add_remaining_strings() 