#!/usr/bin/env python3
import os
import re
import sys
import xml.etree.ElementTree as ET
from typing import Dict, List, Tuple

try:
    from googletrans import Translator
except Exception:
    Translator = None

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

PLACEHOLDER_PATTERN = r"%\d+\$[sd]"
PLACEHOLDER_RE = re.compile(PLACEHOLDER_PATTERN)

def is_backup_path(path: str) -> bool:
    base = os.path.relpath(path, ROOT).split(os.sep)[0]
    return base.startswith("backup_")

def parse_strings(file_path: str) -> Dict[str, str]:
    if not os.path.exists(file_path):
        return {}
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        out = {}
        for el in root:
            if el.tag == "string" and "name" in el.attrib:
                out[el.attrib["name"]] = el.text or ""
        return out
    except Exception as e:
        print(f"[WARN] parse failed {file_path}: {e}")
        return {}

def write_strings(target_fp: str, mapping: Dict[str, str]):
    os.makedirs(os.path.dirname(target_fp), exist_ok=True)
    resources = ET.Element("resources")
    for k, v in mapping.items():
        el = ET.Element("string", {"name": k})
        el.text = v
        resources.append(el)
    tree = ET.ElementTree(resources)
    ET.indent(tree, space="    ", level=0)
    with open(target_fp, "wb") as f:
        f.write(b"<?xml version='1.0' encoding='utf-8'?>\n")
        tree.write(f, encoding="utf-8")

def mask_placeholders(text: str) -> Tuple[str, Dict[str, str]]:
    placeholders: Dict[str, str] = {}
    def repl(m):
        key = f"__PH_{len(placeholders)}__"
        placeholders[key] = m.group(0)
        return key
    safe = re.sub(PLACEHOLDER_PATTERN, repl, text)
    return safe, placeholders

def unmask_placeholders(text: str, placeholders: Dict[str, str]) -> str:
    for key, val in placeholders.items():
        text = text.replace(key, val)
    return text

def translate_text(text: str, dest: str, translator) -> str:
    # keep placeholders and emojis intact
    if not text:
        return text
    # quick skip for non-cyrillic
    if not re.search(r"[\u0400-\u04FF]", text):
        return text
    # mark placeholders
    safe, placeholders = mask_placeholders(text)
    # translate
    try:
        translated = translator.translate(safe, src="ru", dest=dest).text
    except Exception:
        return text
    # restore placeholders
    return unmask_placeholders(translated, placeholders)

def translate_batch(texts: List[str], dest: str, translator) -> List[str]:
    if not texts:
        return []
    masked: List[Tuple[str, Dict[str, str]]] = [mask_placeholders(t) for t in texts]
    masked_texts = [m[0] for m in masked]
    try:
        results = translator.translate(masked_texts, src="ru", dest=dest)
        if not isinstance(results, list):
            results = [results]
        outs = [r.text for r in results]
    except Exception:
        # fallback: return originals
        outs = masked_texts
    # unmask
    return [unmask_placeholders(t, placeholders) for t, (_, placeholders) in zip(outs, masked)]

def process_locale(default_fp: str, locale_dir: str, dest_lang: str):
    base_dir = os.path.dirname(default_fp)
    module_res_dir = os.path.dirname(base_dir)
    target_dir = os.path.join(module_res_dir, locale_dir)
    target_fp = os.path.join(target_dir, "strings.xml")
    source = parse_strings(default_fp)
    target = parse_strings(target_fp)

    translator = Translator() if Translator else None
    changed = False
    if translator:
        # collect batch
        todo_keys: List[str] = []
        todo_texts: List[str] = []
        for k, v in source.items():
            current = target.get(k, "")
            if not current or re.search(r"[\u0400-\u04FF]", current):
                todo_keys.append(k)
                todo_texts.append(current or v)
            elif k not in target:
                target[k] = v
                changed = True
        # batch translate in chunks
        BATCH = 80
        out_idx = 0
        while out_idx < len(todo_keys):
            chunk_keys = todo_keys[out_idx: out_idx + BATCH]
            chunk_texts = todo_texts[out_idx: out_idx + BATCH]
            translated = translate_batch(chunk_texts, dest_lang, translator)
            for ck, tv in zip(chunk_keys, translated):
                if target.get(ck, "") != tv:
                    target[ck] = tv
                    changed = True
            out_idx += BATCH
    else:
        # no translator available: at least ensure all keys exist
        for k, v in source.items():
            if k not in target:
                target[k] = v
                changed = True

    if changed:
        write_strings(target_fp, target)
        print(f"[OK] Translated {os.path.relpath(target_fp, ROOT)}")
    else:
        print(f"[SKIP] {os.path.relpath(target_fp, ROOT)} (no changes)")

def main():
    defaults = []
    for dirpath, _, filenames in os.walk(ROOT):
        if is_backup_path(dirpath):
            continue
        if dirpath.endswith(os.path.join("res", "values")) and "strings.xml" in filenames:
            defaults.append(os.path.join(dirpath, "strings.xml"))
    if not defaults:
        print("No defaults found")
        return 1
    for fp in sorted(defaults):
        process_locale(fp, "values-en", "en")
        process_locale(fp, "values-zh-rCN", "zh-cn")
    return 0

if __name__ == "__main__":
    sys.exit(main())


