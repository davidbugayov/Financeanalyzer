#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

# Словарь недостающих строк для основного файла
MISSING_STRINGS = {
    "achievement_budget_saver_desc": "Сохраняйте %1$s%% вашего дохода в течение %2$d месяцев",
    "achievement_category_master_desc": "Используйте %1$s различных категорий",
    "achievement_transaction_counter_desc": "Добавьте %1$d транзакций",
    "achievement_savings_milestone_desc": "Сохраните %1$s в общей сложности",
    "achievement_export_master_desc": "Экспортируйте данные %1$d раз",
    "achievement_import_master_desc": "Импортируйте данные %1$d раз",
    "achievement_security_guard_desc": "Включите функции безопасности",
    "achievement_analytics_expert_desc": "Просматривайте статистику %1$d раз",
    "achievement_goal_setter_desc": "Установите и достигните %1$d финансовых целей",
    "achievement_debt_free_desc": "Погасите %1$s долга",
    "achievement_investment_starter_desc": "Начните инвестировать %1$s",
    "achievement_emergency_fund_desc": "Создайте резервный фонд в размере %1$s",
    "achievement_budget_sticker_desc": "Соблюдайте бюджет в течение %1$d месяцев",
    "achievement_income_diversifier_desc": "Имейте %1$d источников дохода",
    "achievement_expense_tracker_desc": "Отслеживайте расходы в течение %1$d дней",
    "achievement_savings_rate_improver_desc": "Улучшите норму сбережений на %1$s%%",
    "achievement_financial_educator_desc": "Прочитайте %1$d финансовых советов",
    "achievement_retirement_planner_desc": "Планируйте пенсию с %1$s",
    "achievement_tax_optimizer_desc": "Оптимизируйте налоги на %1$s",
    "achievement_insurance_protector_desc": "Получите страховое покрытие на %1$s"
}

def fix_extra_translations():
    """Исправляет ExtraTranslation предупреждения, добавляя недостающие строки в основной файл."""
    main_file = ROOT / "ui/src/main/res/values/strings.xml"
    
    if main_file.exists():
        with open(main_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Добавляем недостающие строки
        for key, value in MISSING_STRINGS.items():
            if f'name="{key}"' not in content:
                # Находим место для вставки (перед </resources>)
                if '</resources>' in content:
                    insert_pos = content.find('</resources>')
                    new_string = f'    <string name="{key}">{value}</string>\n'
                    content = content[:insert_pos] + new_string + content[insert_pos:]
        
        with open(main_file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Added missing strings to {main_file}")

if __name__ == "__main__":
    fix_extra_translations()
    print("ExtraTranslation warnings fixed successfully!")
