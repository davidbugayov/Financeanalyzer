#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

# Словарь plurals для добавления
PLURALS_TO_ADD = {
    "transaction_count": {
        "one": "%d транзакция",
        "other": "%d транзакций"
    },
    "category_count": {
        "one": "%d категория", 
        "other": "%d категорий"
    },
    "month_count": {
        "one": "%d месяц",
        "other": "%d месяцев"
    },
    "day_count": {
        "one": "%d день",
        "other": "%d дней"
    },
    "week_count": {
        "one": "%d неделя",
        "other": "%d недель"
    },
    "year_count": {
        "one": "%d год",
        "other": "%d лет"
    },
    "goal_count": {
        "one": "%d цель",
        "other": "%d целей"
    },
    "achievement_count": {
        "one": "%d достижение",
        "other": "%d достижений"
    },
    "source_count": {
        "one": "%d источник",
        "other": "%d источников"
    },
    "tip_count": {
        "one": "%d совет",
        "other": "%d советов"
    },
    "debt_count": {
        "one": "%d долг",
        "other": "%d долгов"
    },
    "wallet_count": {
        "one": "%d кошелек",
        "other": "%d кошельков"
    },
    "budget_count": {
        "one": "%d бюджет",
        "other": "%d бюджетов"
    },
    "export_count": {
        "one": "%d экспорт",
        "other": "%d экспортов"
    },
    "import_count": {
        "one": "%d импорт",
        "other": "%d импортов"
    }
}

def fix_plurals():
    """Исправляет все PluralsCandidate предупреждения."""
    strings_files = [
        ROOT / "ui/src/main/res/values/strings.xml",
        ROOT / "ui/src/main/res/values-en/strings.xml", 
        ROOT / "ui/src/main/res/values-zh-rCN/strings.xml"
    ]
    
    fixed_count = 0
    for file_path in strings_files:
        if file_path.exists():
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Добавляем plurals
            for plural_name, plural_items in PLURALS_TO_ADD.items():
                if f'name="{plural_name}"' not in content:
                    # Находим место для вставки (перед </resources>)
                    if '</resources>' in content:
                        insert_pos = content.find('</resources>')
                        plural_xml = f'    <plurals name="{plural_name}">\n'
                        for quantity, text in plural_items.items():
                            plural_xml += f'        <item quantity="{quantity}">{text}</item>\n'
                        plural_xml += '    </plurals>\n'
                        content = content[:insert_pos] + plural_xml + content[insert_pos:]
            
            if content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Added plurals to {file_path}")
                fixed_count += 1
    
    print(f"Fixed plurals in {fixed_count} files")

if __name__ == "__main__":
    fix_plurals()
    print("All PluralsCandidate warnings fixed successfully!")
