#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

TARGET_DIRS = [
    ROOT / "feature",
    ROOT / "presentation",
    ROOT / "navigation",
    ROOT / "app/src/main/java",
]

REPLACEMENTS = [
    (re.compile(r"\bR\.string\."), "UiR.string."),
    (re.compile(r"\bR\.dimen\."), "UiR.dimen."),
    (re.compile(r"\bR\.color\."), "UiR.color."),
    (re.compile(r"\bR\.integer\."), "UiR.integer."),
    (re.compile(r"\bR\.array\."), "UiR.array."),
    (re.compile(r"\bR\.drawable\."), "UiR.drawable."),
]


def ensure_import_alias(text: str) -> str:
    if "UiR." not in text:
        return text
    if "import com.davidbugayov.financeanalyzer.ui.R as UiR" in text:
        return text
    # insert after last import line
    lines = text.splitlines()
    last_import_idx = -1
    for i, line in enumerate(lines):
        if line.startswith("import "):
            last_import_idx = i
    insert_idx = last_import_idx + 1 if last_import_idx >= 0 else 0
    lines.insert(insert_idx, "import com.davidbugayov.financeanalyzer.ui.R as UiR")
    return "\n".join(lines)


def process_file(path: Path) -> bool:
    if path.suffix != ".kt":
        return False
    text = path.read_text(encoding="utf-8", errors="ignore")
    original = text
    for pattern, repl in REPLACEMENTS:
        text = pattern.sub(repl, text)
    text = ensure_import_alias(text)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main():
    changed = 0
    for d in TARGET_DIRS:
        if not d.exists():
            continue
        for kt in d.rglob("*.kt"):
            if "/ui/" in str(kt):
                continue
            if process_file(kt):
                changed += 1
    print(f"Rewritten files: {changed}")


if __name__ == "__main__":
    main()


