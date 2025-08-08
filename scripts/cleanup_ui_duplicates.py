#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path


def parse_strings(file: Path):
    tree = ET.parse(file)
    root = tree.getroot()
    entries = {}
    for s in root.findall('string'):
        name = s.get('name')
        if name:
            entries[name] = s
    return tree, root, entries


def main():
    project_root = Path(__file__).resolve().parents[1]
    ui_values = project_root / 'ui/src/main/res/values'
    main_file = ui_values / 'strings.xml'
    if not main_file.exists():
        print('strings.xml not found')
        return

    # collect keys from specialized files
    specialized_files = list(ui_values.glob('strings_*.xml'))
    specialized_keys = set()
    for f in specialized_files:
        try:
            _, _, entries = parse_strings(f)
            specialized_keys.update(entries.keys())
        except Exception as e:
            print(f'Failed to parse {f}: {e}')

    # load main
    tree, root, entries = parse_strings(main_file)

    removed = 0
    for key in list(entries.keys()):
        if key in specialized_keys:
            elem = entries[key]
            try:
                root.remove(elem)
                removed += 1
            except Exception:
                pass

    # pretty print indent if available (Python 3.9+)
    try:
        ET.indent(tree, space='    ')
    except Exception:
        pass

    # write back
    main_file.write_text("<?xml version='1.0' encoding='utf-8'?>\n" + ET.tostring(root, encoding='unicode'))
    print(f'Removed {removed} duplicate keys from {main_file}')


if __name__ == '__main__':
    main()


