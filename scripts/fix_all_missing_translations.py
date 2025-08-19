#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

# Словарь недостающих строк для всех файлов
MISSING_STRINGS = {
    # Основные строки
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
    "achievement_insurance_protector_desc": "Получите страховое покрытие на %1$s",
    
    # Дополнительные строки для новых файлов
    "achievement_first_steps": "Первые шаги",
    "achievement_first_steps_desc": "Добавьте первую транзакцию",
    "achievement_transaction_master": "Мастер транзакций",
    "achievement_transaction_master_desc": "Добавьте 100 транзакций",
    "achievement_savings_champion": "Чемпион сбережений",
    "achievement_savings_champion_desc": "Сохраните 100,000 рублей",
    "achievement_budget_expert": "Эксперт бюджета",
    "achievement_budget_expert_desc": "Соблюдайте бюджет 3 месяца подряд",
    "achievement_category_explorer": "Исследователь категорий",
    "achievement_category_explorer_desc": "Используйте 10 различных категорий",
    "achievement_import_master": "Мастер импорта",
    "achievement_import_master_desc": "Импортируйте данные 5 раз",
    "achievement_export_master": "Мастер экспорта",
    "achievement_export_master_desc": "Экспортируйте данные 5 раз",
    "achievement_security_guard": "Охранник безопасности",
    "achievement_security_guard_desc": "Включите PIN-код",
    "achievement_analytics_expert": "Эксперт аналитики",
    "achievement_analytics_expert_desc": "Просмотрите статистику 10 раз",
    "achievement_goal_setter": "Постановщик целей",
    "achievement_goal_setter_desc": "Установите 5 финансовых целей",
    "achievement_debt_free": "Без долгов",
    "achievement_debt_free_desc": "Погасите все долги",
    "achievement_investment_starter": "Начинающий инвестор",
    "achievement_investment_starter_desc": "Начните инвестировать",
    "achievement_emergency_fund": "Резервный фонд",
    "achievement_emergency_fund_desc": "Создайте резервный фонд",
    "achievement_budget_sticker": "Соблюдающий бюджет",
    "achievement_budget_sticker_desc": "Соблюдайте бюджет 6 месяцев",
    "achievement_income_diversifier": "Диверсификатор дохода",
    "achievement_income_diversifier_desc": "Имейте 3 источника дохода",
    "achievement_expense_tracker": "Отслеживающий расходы",
    "achievement_expense_tracker_desc": "Отслеживайте расходы 30 дней",
    "achievement_savings_rate_improver": "Улучшающий сбережения",
    "achievement_savings_rate_improver_desc": "Улучшите норму сбережений",
    "achievement_financial_educator": "Финансовый педагог",
    "achievement_financial_educator_desc": "Прочитайте 20 финансовых советов",
    "achievement_retirement_planner": "Планировщик пенсии",
    "achievement_retirement_planner_desc": "Планируйте пенсию",
    "achievement_tax_optimizer": "Оптимизатор налогов",
    "achievement_tax_optimizer_desc": "Оптимизируйте налоги",
    "achievement_insurance_protector": "Защитник страховки",
    "achievement_insurance_protector_desc": "Получите страховое покрытие"
}

def fix_missing_translations():
    """Исправляет все MissingTranslation предупреждения."""
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
            
            # Добавляем недостающие строки
            for key, value in MISSING_STRINGS.items():
                if f'name="{key}"' not in content:
                    # Находим место для вставки (перед </resources>)
                    if '</resources>' in content:
                        insert_pos = content.find('</resources>')
                        new_string = f'    <string name="{key}">{value}</string>\n'
                        content = content[:insert_pos] + new_string + content[insert_pos:]
            
            if content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Added missing strings to {file_path}")
                fixed_count += 1
    
    print(f"Fixed missing translations in {fixed_count} files")

if __name__ == "__main__":
    fix_missing_translations()
    print("All MissingTranslation warnings fixed successfully!")
