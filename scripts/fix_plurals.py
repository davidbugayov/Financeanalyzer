#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path("/Users/davidbugayov/StudioProject/Financeanalyzer")

def fix_plurals(file_path):
    """Исправляет проблемы с множественными числами в XML файлах."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Исправляем проблемы с множественными числами
    content = re.sub(r'%d\s+important', r'%d important', content)
    content = re.sub(r'%d\s+transactions', r'%d transactions', content)
    content = re.sub(r'%d\s+large', r'%d large', content)
    content = re.sub(r'%d\s+small', r'%d small', content)
    content = re.sub(r'%d\s+from\s+%d', r'%1$d from %2$d', content)
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed plurals in {file_path}")
        return True
    return False

def main():
    strings_files = [
        ROOT / "ui/src/main/res/values/strings.xml",
        ROOT / "ui/src/main/res/values-en/strings.xml", 
        ROOT / "ui/src/main/res/values-zh-rCN/strings.xml"
    ]
    
    fixed_count = 0
    for file_path in strings_files:
        if file_path.exists():
            if fix_plurals(file_path):
                fixed_count += 1
    
    print(f"Fixed plurals in {fixed_count} files")

if __name__ == "__main__":
    main()
