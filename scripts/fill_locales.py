#!/usr/bin/env python3
import os
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

def is_backup_path(path: str) -> bool:
    base = os.path.relpath(path, ROOT).split(os.sep)[0]
    return base.startswith("backup_")

def find_default_strings():
    result = []
    for dirpath, dirnames, filenames in os.walk(ROOT):
        if is_backup_path(dirpath):
            continue
        if not dirpath.endswith(os.path.join("res", "values")):
            continue
        if "strings.xml" in filenames:
            result.append(os.path.join(dirpath, "strings.xml"))
    return sorted(result)

def parse_strings(file_path: str) -> dict:
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        out = {}
        for child in root:
            if child.tag == "string" and "name" in child.attrib:
                out[child.attrib["name"]] = (child.text or "")
        return out
    except Exception as e:
        print(f"[WARN] Failed to parse {file_path}: {e}")
        return {}

def write_strings(file_path: str, strings: dict):
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    resources = ET.Element("resources")
    for name, value in strings.items():
        el = ET.Element("string", {"name": name})
        el.text = value
        resources.append(el)
    tree = ET.ElementTree(resources)
    ET.indent(tree, space="    ", level=0)
    with open(file_path, "wb") as f:
        f.write(b"<?xml version='1.0' encoding='utf-8'?>\n")
        tree.write(f, encoding="utf-8")

def merge_locale(default_fp: str, locale_dir: str):
    default_map = parse_strings(default_fp)
    base_dir = os.path.dirname(default_fp)
    module_res_dir = os.path.dirname(base_dir)
    target_dir = os.path.join(module_res_dir, locale_dir)
    target_fp = os.path.join(target_dir, "strings.xml")
    existing = parse_strings(target_fp) if os.path.exists(target_fp) else {}

    merged = {}
    for k, v in default_map.items():
        merged[k] = existing.get(k, v)

    write_strings(target_fp, merged)
    rel = os.path.relpath(target_fp, ROOT)
    print(f"[OK] Updated {rel} ({len(merged)} strings)")

def main():
    targets = find_default_strings()
    if not targets:
        print("No strings.xml found")
        return 1
    for fp in targets:
        merge_locale(fp, "values-en")
        merge_locale(fp, "values-zh-rCN")
    return 0

if __name__ == "__main__":
    sys.exit(main())


