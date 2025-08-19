#!/usr/bin/env python3
import os
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

def fix_modifier_parameter(file_path):
    """Исправляет проблемы с ModifierParameter в Kotlin файлах."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Паттерн для поиска функций с Modifier параметром не на первом месте
    # Ищем @Composable функции с modifier параметром
    pattern = r'@Composable\s+fun\s+(\w+)\s*\(\s*([^)]*modifier\s*:\s*Modifier\s*=\s*Modifier[^)]*)\s*\)'
    
    def fix_function_params(match):
        func_name = match.group(1)
        params_str = match.group(2)
        
        # Разбиваем параметры
        params = []
        current_param = ""
        paren_count = 0
        
        for char in params_str:
            if char == '(':
                paren_count += 1
            elif char == ')':
                paren_count -= 1
            elif char == ',' and paren_count == 0:
                if current_param.strip():
                    params.append(current_param.strip())
                current_param = ""
                continue
            current_param += char
        
        if current_param.strip():
            params.append(current_param.strip())
        
        # Находим modifier параметр
        modifier_param = None
        other_params = []
        
        for param in params:
            if 'modifier' in param and 'Modifier' in param:
                modifier_param = param
            else:
                other_params.append(param)
        
        if modifier_param:
            # Перемещаем modifier в начало
            new_params = [modifier_param] + other_params
            new_params_str = ',\n    '.join(new_params)
            return f'@Composable\nfun {func_name}(\n    {new_params_str}\n)'
        
        return match.group(0)
    
    # Применяем исправления
    content = re.sub(pattern, fix_function_params, content, flags=re.MULTILINE | re.DOTALL)
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed ModifierParameter in {file_path}")
        return True
    return False

def main():
    """Основная функция для исправления ModifierParameter во всех файлах."""
    kotlin_files = [
        ROOT / "ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/card/AdviceCard.kt",
        ROOT / "ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/AnimatedBottomNavigationBar.kt",
        ROOT / "ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/AppTopBar.kt",
        ROOT / "ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/FeedbackComponents.kt",
        ROOT / "ui/src/main/java/com/davidbugayov/financeanalyzer/ui/components/card/PremiumStatisticsCard.kt"
    ]
    
    fixed_count = 0
    for file_path in kotlin_files:
        if file_path.exists():
            if fix_modifier_parameter(file_path):
                fixed_count += 1
    
    print(f"Fixed ModifierParameter in {fixed_count} files")

if __name__ == "__main__":
    main()

