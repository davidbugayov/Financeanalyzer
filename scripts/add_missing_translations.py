#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

# Словарь переводов для разных файлов
TRANSLATIONS = {
    "strings_home_common.xml": {
        "en": {
            "profile": "Profile",
            "total_income": "Total income",
            "total_expense": "Total expense", 
            "no_expenses_period": "No expenses for the period",
            "no_incomes_period": "No income for the period"
        },
        "zh": {
            "profile": "个人资料",
            "total_income": "总收入",
            "total_expense": "总支出",
            "no_expenses_period": "该期间无支出",
            "no_incomes_period": "该期间无收入"
        }
    },
    "strings_profile.xml": {
        "en": {
            "settings_language_current_value": "English",
            "settings_language_ru": "Russian",
            "settings_language_en": "English", 
            "settings_language_zh": "Chinese",
            "notification_disabled_description": "Notifications are disabled",
            "permission_required_title": "Permission required",
            "unknown": "Unknown"
        },
        "zh": {
            "settings_language_current_value": "中文",
            "settings_language_ru": "俄语",
            "settings_language_en": "英语",
            "settings_language_zh": "中文", 
            "notification_disabled_description": "通知已禁用",
            "permission_required_title": "需要权限",
            "unknown": "未知"
        }
    },
    "strings_security.xml": {
        "en": {
            "auth_enter_pin": "Enter PIN",
            "auth_wrong_pin": "Wrong PIN"
        },
        "zh": {
            "auth_enter_pin": "输入PIN",
            "auth_wrong_pin": "PIN错误"
        }
    },
    "strings_transaction.xml": {
        "en": {
            "edit_transaction_title": "Edit transaction",
            "save_button_text": "Save",
            "add_button_text": "Add",
            "import_transactions_content_description": "Import transactions",
            "dialog_delete": "Delete",
            "delete_source_title": "Delete source"
        },
        "zh": {
            "edit_transaction_title": "编辑交易",
            "save_button_text": "保存",
            "add_button_text": "添加",
            "import_transactions_content_description": "导入交易",
            "dialog_delete": "删除",
            "delete_source_title": "删除来源"
        }
    }
}

def add_missing_translations():
    """Добавляет недостающие переводы во все файлы."""
    for filename, translations in TRANSLATIONS.items():
        # Английский перевод
        en_file = ROOT / "ui/src/main/res/values-en" / filename
        if en_file.exists():
            with open(en_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Добавляем недостающие строки
            for key, value in translations["en"].items():
                if f'name="{key}"' not in content:
                    # Находим место для вставки (перед </resources>)
                    if '</resources>' in content:
                        insert_pos = content.find('</resources>')
                        new_string = f'    <string name="{key}">{value}</string>\n'
                        content = content[:insert_pos] + new_string + content[insert_pos:]
            
            with open(en_file, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {en_file}")
        
        # Китайский перевод
        zh_file = ROOT / "ui/src/main/res/values-zh-rCN" / filename
        if zh_file.exists():
            with open(zh_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Добавляем недостающие строки
            for key, value in translations["zh"].items():
                if f'name="{key}"' not in content:
                    # Находим место для вставки (перед </resources>)
                    if '</resources>' in content:
                        insert_pos = content.find('</resources>')
                        new_string = f'    <string name="{key}">{value}</string>\n'
                        content = content[:insert_pos] + new_string + content[insert_pos:]
            
            with open(zh_file, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {zh_file}")

if __name__ == "__main__":
    add_missing_translations()
    print("Missing translations added successfully!")

