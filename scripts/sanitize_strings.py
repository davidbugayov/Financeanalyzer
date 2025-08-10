#!/usr/bin/env python3
import os
import re
import sys
import xml.etree.ElementTree as ET
from typing import Dict, List, Tuple

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

PLACEHOLDER_RE = re.compile(r"%(\d+\$)?[dsfb]", re.IGNORECASE)
NUMERIC_PH_RE = re.compile(r"__ph_(\d+)__", re.IGNORECASE)
NUMERIC_PH_SPACED_RE = re.compile(r"__\s*pH_(\d+)\s*__", re.IGNORECASE)
BACKSLASHED_PH_RE = re.compile(r"\\\s*'__\s*pH_(\d+)\s*__\s*\\'", re.IGNORECASE)
BACKSLASHED_PERCENT_PH_RE = re.compile(r"\\\s*'%\s*(\d+)\$([dsfbDSFB])\s*\\'", re.IGNORECASE)

def sanitize_text(text: str) -> str:
    if text is None:
        return text
    # Normalize backslash-newline artifacts
    text = text.replace("\\ N", "\\n").replace("\\ n", "\\n").replace("\\\n", "\\n")
    # Remove stray backslashes before quotes
    text = re.sub(r"\\\s*'", "'", text)
    text = re.sub(r"'\s*\\", "'", text)
    # Lowercase placeholder types (e.g., %D -> %d)
    def lower_types(m: re.Match) -> str:
        g = m.group(0)
        if g.endswith(('D','S','F','B')):
            return g[:-1] + g[-1].lower()
        return g
    text = re.sub(r"%(\d+\$)?[DSFB]", lower_types, text)
    # Convert __ph_0__ -> %1$s style
    def repl_numph(m: re.Match) -> str:
        idx = int(m.group(1)) + 1
        return f"%{idx}$s"
    text = NUMERIC_PH_RE.sub(repl_numph, text)
    text = NUMERIC_PH_SPACED_RE.sub(repl_numph, text)
    text = BACKSLASHED_PH_RE.sub(repl_numph, text)
    # Unquote/backslash quoted positional placeholders like \ '%2$s '\n+    def repl_percent(m: re.Match) -> str:
        idx = m.group(1)
        t = m.group(2).lower()
        return f"%{idx}${t}"
    text = BACKSLASHED_PERCENT_PH_RE.sub(repl_percent, text)
    # Escape literal single % that are not placeholders
    masked: Dict[str, str] = {}
    def mask_placeholders(m: re.Match) -> str:
        key = f"__PLH_{len(masked)}__"
        masked[key] = m.group(0)
        return key
    tmp = PLACEHOLDER_RE.sub(mask_placeholders, text)
    # double stray %
    tmp = tmp.replace('%', '%%')
    # unmask placeholders
    for k, v in masked.items():
        tmp = tmp.replace(k, v)
    return tmp

def fix_multiples(el: ET.Element) -> None:
    # If multiple placeholders exist without positions, ensure positional
    val = el.text or ""
    matches = list(PLACEHOLDER_RE.finditer(val))
    if len(matches) > 1 and not any('$' in m.group(0) for m in matches):
        # convert in-order to positional keeping type
        i = 1
        def repl(m: re.Match):
            nonlocal i
            spec = m.group(0)
            t = spec[-1].lower()
            out = f"%{i}${t}"
            i += 1
            return out
        el.text = PLACEHOLDER_RE.sub(repl, val)

def process_file(path: str) -> bool:
    tree = ET.parse(path)
    root = tree.getroot()
    changed = False
    for el in root:
        if el.tag != 'string':
            continue
        orig = el.text
        new = sanitize_text(orig)
        if new != orig:
            el.text = new
            changed = True
        fix_multiples(el)
    if changed:
        ET.indent(tree, space="    ", level=0)
        with open(path, 'wb') as f:
            f.write(b"<?xml version='1.0' encoding='utf-8'?>\n")
            tree.write(f, encoding='utf-8')
    return changed

def main():
    targets = [
        os.path.join(ROOT, 'ui', 'src', 'main', 'res', 'values-en', 'strings.xml'),
        os.path.join(ROOT, 'ui', 'src', 'main', 'res', 'values-zh-rCN', 'strings.xml'),
    ]
    total = 0
    for p in targets:
        if os.path.exists(p):
            if process_file(p):
                print(f"[FIXED] {os.path.relpath(p, ROOT)}")
                total += 1
            else:
                print(f"[OK] {os.path.relpath(p, ROOT)} unchanged")
        else:
            print(f"[SKIP] {p} not found")
    if total == 0:
        sys.exit(0)

if __name__ == '__main__':
    main()


