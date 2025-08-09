#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def file_needs_alias(text: str) -> bool:
    return "UiR." in text and "import com.davidbugayov.financeanalyzer.ui.R as UiR" not in text

def fix_file(path: Path) -> bool:
    if path.suffix != ".kt":
        return False
    text = path.read_text(encoding="utf-8", errors="ignore")
    original = text
    # Replace any fully-qualified UiR usage to alias
    text = text.replace("com.davidbugayov.financeanalyzer.ui.UiR", "UiR")
    if file_needs_alias(text):
        lines = text.splitlines()
        last_import = -1
        for i, line in enumerate(lines):
            if line.startswith("import "):
                last_import = i
        insert_at = last_import + 1 if last_import >= 0 else 0
        lines.insert(insert_at, "import com.davidbugayov.financeanalyzer.ui.R as UiR")
        text = "\n".join(lines)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False

def main():
    changed = 0
    for d in [ROOT / "feature", ROOT / "presentation", ROOT / "navigation", ROOT / "app/src/main/java"]:
        if not d.exists():
            continue
        for kt in d.rglob("*.kt"):
            if "/ui/" in str(kt):
                continue
            if fix_file(kt):
                changed += 1
    print(f"Fixed files: {changed}")

if __name__ == "__main__":
    main()


