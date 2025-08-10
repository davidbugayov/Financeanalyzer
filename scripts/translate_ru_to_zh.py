#!/usr/bin/env python3
import os
import re
import subprocess
import xml.etree.ElementTree as ET
from typing import Dict, Tuple

FILE = os.path.join(os.path.dirname(__file__), "..", "ui", "src", "main", "res", "values-zh-rCN", "strings.xml")

CYRILLIC_RE = re.compile(r"[\u0400-\u04FF]")

def mask(text: str) -> Tuple[str, Dict[str, str]]:
    placeholders: Dict[str, str] = {}
    def repl(m):
        key = f"__PH_{len(placeholders)}__"
        placeholders[key] = m.group(0)
        return key
    safe = re.sub(r"%\d+\$[sdf]", repl, text)
    safe = safe.replace("\n", " __NL__ ")
    return safe, placeholders

def unmask(text: str, placeholders: Dict[str, str]) -> str:
    for k, v in placeholders.items():
        text = text.replace(k, v)
    return text.replace(" __NL__ ", "\n")

def gtrans_ru_to_zh(text: str) -> str:
    try:
        out = subprocess.check_output([
            "trans", "-b", "ru:zh-CN", "-no-ansi", text
        ], stderr=subprocess.DEVNULL)
        return out.decode("utf-8").strip()
    except Exception:
        return text

def main():
    tree = ET.parse(FILE)
    root = tree.getroot()
    changed = 0
    for el in root:
        if el.tag != "string":
            continue
        val = el.text or ""
        if not CYRILLIC_RE.search(val):
            continue
        safe, ph = mask(val)
        translated = gtrans_ru_to_zh(safe)
        el.text = unmask(translated, ph)
        changed += 1
    if changed:
        ET.indent(tree, space="    ", level=0)
        with open(FILE, "wb") as f:
            f.write(b"<?xml version='1.0' encoding='utf-8'?>\n")
            tree.write(f, encoding="utf-8")
        print(f"[OK] Translated {changed} entries -> {os.path.relpath(FILE)}")
    else:
        print("[SKIP] No Cyrillic entries found")

if __name__ == "__main__":
    main()


