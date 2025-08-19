#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

# Словарь дополнительных переводов
ADDITIONAL_TRANSLATIONS = {
    "strings.xml": {
        "en": {
            "achievement_budget_saver_desc": "Save %1$s%% of your income for %2$d months",
            "achievement_category_master_desc": "Use %1$s different categories",
            "achievement_transaction_counter_desc": "Add %1$d transactions",
            "achievement_savings_milestone_desc": "Save %1$s in total",
            "achievement_export_master_desc": "Export data %1$d times",
            "achievement_import_master_desc": "Import data %1$d times",
            "achievement_security_guard_desc": "Enable security features",
            "achievement_analytics_expert_desc": "View statistics %1$d times",
            "achievement_goal_setter_desc": "Set and achieve %1$d financial goals",
            "achievement_debt_free_desc": "Pay off %1$s in debt",
            "achievement_investment_starter_desc": "Start investing %1$s",
            "achievement_emergency_fund_desc": "Build emergency fund of %1$s",
            "achievement_budget_sticker_desc": "Stay within budget for %1$d months",
            "achievement_income_diversifier_desc": "Have %1$d income sources",
            "achievement_expense_tracker_desc": "Track expenses for %1$d days",
            "achievement_savings_rate_improver_desc": "Improve savings rate by %1$s%%",
            "achievement_financial_educator_desc": "Read %1$d financial tips",
            "achievement_retirement_planner_desc": "Plan retirement with %1$s",
            "achievement_tax_optimizer_desc": "Optimize taxes by %1$s",
            "achievement_insurance_protector_desc": "Get insurance coverage of %1$s"
        },
        "zh": {
            "achievement_budget_saver_desc": "节省收入的%1$s%%，持续%2$d个月",
            "achievement_category_master_desc": "使用%1$s个不同类别",
            "achievement_transaction_counter_desc": "添加%1$d笔交易",
            "achievement_savings_milestone_desc": "总共节省%1$s",
            "achievement_export_master_desc": "导出数据%1$d次",
            "achievement_import_master_desc": "导入数据%1$d次",
            "achievement_security_guard_desc": "启用安全功能",
            "achievement_analytics_expert_desc": "查看统计%1$d次",
            "achievement_goal_setter_desc": "设定并实现%1$d个财务目标",
            "achievement_debt_free_desc": "还清%1$s债务",
            "achievement_investment_starter_desc": "开始投资%1$s",
            "achievement_emergency_fund_desc": "建立%1$s应急基金",
            "achievement_budget_sticker_desc": "连续%1$d个月遵守预算",
            "achievement_income_diversifier_desc": "拥有%1$d个收入来源",
            "achievement_expense_tracker_desc": "连续%1$d天跟踪支出",
            "achievement_savings_rate_improver_desc": "提高储蓄率%1$s%%",
            "achievement_financial_educator_desc": "阅读%1$d个财务提示",
            "achievement_retirement_planner_desc": "规划退休，目标%1$s",
            "achievement_tax_optimizer_desc": "优化税收%1$s",
            "achievement_insurance_protector_desc": "获得%1$s保险保障"
        }
    }
}

def add_remaining_translations():
    """Добавляет оставшиеся недостающие переводы."""
    for filename, translations in ADDITIONAL_TRANSLATIONS.items():
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
    add_remaining_translations()
    print("Remaining translations added successfully!")
