#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
DOMAIN = ROOT / "domain"
UI_VALUES = ROOT / "ui/src/main/res/values"
DOMAIN_STR = DOMAIN / "src/main/res/values/strings.xml"


def collect_domain_string_keys() -> set[str]:
    pattern = re.compile(r"R\.string\.([A-Za-z0-9_]+)")
    keys: set[str] = set()
    for kt in DOMAIN.rglob("*.kt"):
        text = kt.read_text(encoding="utf-8", errors="ignore")
        for m in pattern.finditer(text):
            keys.add(m.group(1))
    return keys


def load_ui_strings() -> dict[str, str]:
    values: dict[str, str] = {}
    for xml_file in sorted(UI_VALUES.glob("strings*.xml")):
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()
        except ET.ParseError:
            continue
        for node in root.findall("string"):
            name = node.get("name")
            if not name:
                continue
            text = (node.text or "").strip()
            values[name] = text
    return values


def write_domain_strings(keys: set[str], mapping: dict[str, str]):
    DOMAIN_STR.parent.mkdir(parents=True, exist_ok=True)
    root = ET.Element("resources")
    for key in sorted(keys):
        value = mapping.get(key, key)
        el = ET.SubElement(root, "string", {"name": key})
        el.text = value
    ET.indent(root)  # type: ignore[attr-defined]
    tree = ET.ElementTree(root)
    tree.write(DOMAIN_STR, encoding="utf-8", xml_declaration=True)


def main():
    keys = collect_domain_string_keys()
    mapping = load_ui_strings()
    write_domain_strings(keys, mapping)
    print(f"Synced {len(keys)} strings to {DOMAIN_STR}")


if __name__ == "__main__":
    main()


