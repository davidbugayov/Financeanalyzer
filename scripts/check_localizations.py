#!/usr/bin/env python3
"""
Скрипт для проверки синхронизации строковых ресурсов между локализациями.

Проверяет, что все строки из основной локализации (values/) присутствуют
в английской (values-en/) и китайской (values-zh-rCN/) версиях.
"""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path


def extract_strings_from_xml(file_path):
    """Извлекает все строки из XML файла ресурсов."""
    strings = {}
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()

        for string_elem in root.findall('.//string'):
            name = string_elem.get('name')
            if name:
                strings[name] = string_elem.text or ""
    except Exception as e:
        print(f"Ошибка при чтении {file_path}: {e}")
        return {}

    return strings


def check_localization_sync():
    """Проверяет синхронизацию локализаций."""
    base_path = Path(__file__).parent.parent / "ui" / "src" / "main" / "res"

    # Основные папки локализаций
    locales = {
        'ru': 'values',
        'en': 'values-en',
        'zh': 'values-zh-rCN'
    }

    # Файлы для проверки
    string_files = [
        'strings.xml',
        'strings_achievements.xml',
        'strings_dialogs.xml',
        'strings_errors.xml',
        'strings_home_common.xml',
        'strings_libraries.xml',
        'strings_profile.xml',
        'strings_recommendations.xml',
        'strings_security.xml',
        'strings_statistics.xml',
        'strings_transaction.xml'
    ]

    all_missing = []

    print("🔍 Проверка синхронизации локализаций...")
    print("=" * 60)

    for string_file in string_files:
        print(f"\n📄 Проверка файла: {string_file}")
        print("-" * 40)

        # Читаем строки из основной локализации
        base_file = base_path / locales['ru'] / string_file
        if not base_file.exists():
            print(f"⚠️  Пропускаем {string_file} - файл не найден в основной локализации")
            continue

        base_strings = extract_strings_from_xml(base_file)

        if not base_strings:
            print(f"⚠️  Пропускаем {string_file} - не удалось прочитать строки")
            continue

        print(f"📊 Основная локализация содержит {len(base_strings)} строк")

        # Проверяем каждую локализацию
        for locale_name, locale_path in locales.items():
            if locale_name == 'ru':
                continue  # Пропускаем основную локализацию

            locale_file = base_path / locale_path / string_file
            if not locale_file.exists():
                print(f"❌ Файл {string_file} отсутствует в {locale_name}")
                all_missing.append(f"{locale_name}: {string_file} (файл отсутствует)")
                continue

            locale_strings = extract_strings_from_xml(locale_file)
            missing_strings = []

            for string_name in base_strings:
                if string_name not in locale_strings:
                    missing_strings.append(string_name)

            if missing_strings:
                print(f"❌ В {locale_name} отсутствуют {len(missing_strings)} строк:")
                for missing in missing_strings[:5]:  # Показываем первые 5
                    print(f"   - {missing}")
                if len(missing_strings) > 5:
                    print(f"   ... и еще {len(missing_strings) - 5} строк")

                for missing in missing_strings:
                    all_missing.append(f"{locale_name}: {string_file} -> {missing}")
            else:
                print(f"✅ {locale_name}: все строки переведены")

    print("\n" + "=" * 60)
    print("📋 ИТОГИ:")

    if all_missing:
        print(f"❌ Найдено {len(all_missing)} проблем с локализациями:")
        for i, missing in enumerate(all_missing[:10], 1):
            print(f"   {i}. {missing}")

        if len(all_missing) > 10:
            print(f"   ... и еще {len(all_missing) - 10} проблем")

        print("\n💡 Рекомендация: Добавьте недостающие строки в соответствующие файлы локализации")
        return False
    else:
        print("✅ Все локализации синхронизированы!")
        return True


if __name__ == "__main__":
    success = check_localization_sync()
    exit(0 if success else 1)
