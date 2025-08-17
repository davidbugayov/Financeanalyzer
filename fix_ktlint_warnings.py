#!/usr/bin/env python3
"""
Скрипт для автоматического исправления ktlint предупреждений
"""

import os
import re
import glob

def fix_long_lines(file_path):
    """Исправляет длинные строки (более 120 символов)"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    lines = content.split('\n')
    fixed_lines = []
    
    for line in lines:
        if len(line) > 120:
            # Исправляем длинные строки с if-else
            if 'if (' in line and 'else' in line and ':' in line:
                # Разбиваем if-else конструкции
                if 'if (isSelected)' in line and 'MaterialTheme.colorScheme' in line:
                    if 'onBackground' in line and 'onSurfaceVariant' in line:
                        # Специальный случай для цветов
                        fixed_line = line.replace(
                            'if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,',
                            '''if (isSelected) 
                                MaterialTheme.colorScheme.onBackground 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,'''
                        )
                        fixed_lines.append(fixed_line)
                        continue
                
                # Общий случай для if-else
                if 'if (' in line and 'else' in line:
                    # Находим позицию else
                    else_pos = line.find('else')
                    if else_pos > 0:
                        before_else = line[:else_pos].strip()
                        after_else = line[else_pos:].strip()
                        
                        # Разбиваем на несколько строк
                        fixed_line = f"{before_else}\n                            {after_else}"
                        fixed_lines.append(fixed_line)
                        continue
            
            # Исправляем длинные вызовы функций
            if '(' in line and ')' in line and len(line) > 120:
                # Разбиваем длинные вызовы функций
                if 'LineChartPoint(' in line:
                    fixed_line = line.replace(
                        'com.davidbugayov.financeanalyzer.presentation.chart.statistic.model.LineChartPoint(',
                        '''com.davidbugayov.financeanalyzer.presentation.chart.statistic.model
                    .LineChartPoint('''
                    )
                    fixed_lines.append(fixed_line)
                    continue
            
            # Если не удалось исправить, оставляем как есть
            fixed_lines.append(line)
        else:
            fixed_lines.append(line)
    
    # Записываем исправленный контент
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(fixed_lines))

def fix_function_names(file_path):
    """Исправляет имена функций, начинающиеся с заглавной буквы"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Паттерны для поиска функций с неправильными именами
    patterns = [
        # @Composable fun FunctionName -> @Composable fun functionName
        (r'@Composable\s+fun\s+([A-Z][a-zA-Z0-9]*)\s*\(',
         lambda m: f'@Composable fun {m.group(1)[0].lower() + m.group(1)[1:]}('),
        
        # fun FunctionName -> fun functionName
        (r'fun\s+([A-Z][a-zA-Z0-9]*)\s*\(',
         lambda m: f'fun {m.group(1)[0].lower() + m.group(1)[1:]}('),
    ]
    
    for pattern, replacement in patterns:
        content = re.sub(pattern, replacement, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_wildcard_imports(file_path):
    """Исправляет wildcard импорты"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Заменяем wildcard импорты на конкретные
    wildcard_replacements = {
        'import androidx.compose.foundation.*': '''import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape''',
        
        'import androidx.compose.material3.*': '''import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface''',
        
        'import androidx.compose.runtime.*': '''import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue''',
        
        'import androidx.compose.ui.*': '''import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp''',
    }
    
    for wildcard, replacement in wildcard_replacements.items():
        if wildcard in content:
            content = content.replace(wildcard, replacement)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    """Основная функция"""
    # Находим все Kotlin файлы
    kotlin_files = []
    for root, dirs, files in os.walk('.'):
        for file in files:
            if file.endswith('.kt'):
                kotlin_files.append(os.path.join(root, file))
    
    print(f"Найдено {len(kotlin_files)} Kotlin файлов")
    
    for file_path in kotlin_files:
        print(f"Обрабатываю: {file_path}")
        try:
            fix_long_lines(file_path)
            fix_function_names(file_path)
            fix_wildcard_imports(file_path)
        except Exception as e:
            print(f"Ошибка при обработке {file_path}: {e}")

if __name__ == "__main__":
    main()
