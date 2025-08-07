#!/usr/bin/env python3
"""
Скрипт автоматической замены вызовов StringProvider на ResourceProvider.getString
"""
import re
from pathlib import Path

def camel_to_snake(name):
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

pattern = re.compile(r'StringProvider\.([A-Za-z0-9_]+)\((.*?)\)')

root = Path('.')

for file_path in root.rglob('*.kt'):
    text = file_path.read_text(encoding='utf-8')
    if 'StringProvider.' not in text:
        continue

    def repl(m):
        key = m.group(1)
        args = m.group(2).strip()
        snake = camel_to_snake(key)
        if args:
            return f"resourceProvider.getString(R.string.{snake}, {args})"
        else:
            return f"resourceProvider.getString(R.string.{snake})"

    new_text = pattern.sub(repl, text)

    # вставить импорт и ресурс провайдер, если его нет
    if 'ResourceProvider' not in new_text:
        lines = new_text.splitlines()
        import_idxs = [i for i,l in enumerate(lines) if l.startswith('import ')]
        if import_idxs:
            last_import = max(import_idxs)
            injection = [
                'import com.davidbugayov.financeanalyzer.utils.ResourceProvider',
                'import org.koin.core.component.inject',
                '',
                'private val resourceProvider: ResourceProvider by inject()',
                ''
            ]
            lines = lines[:last_import+1] + injection + lines[last_import+1:]
            new_text = '\n'.join(lines)

    file_path.write_text(new_text, encoding='utf-8')
    print(f"Processed {file_path}")
