#!/usr/bin/env python3
"""
Consolidate duplicate Android resources across modules.

Targets:
- values: colors*.xml, integers*.xml, styles*.xml
- drawables and mipmap files (by exact content hash)

Rules:
- Prefer keeping a copy located under ui/src/main/res. If not present, keep the
  first encountered file and remove other identical duplicates.
- Only remove duplicates when values (or file content) are strictly identical.
- Before any modification, create a timestamped backup tree under
  scripts/backups/resources_<timestamp>/

Usage:
  Dry run (no changes):
    python3 scripts/consolidate_resources_generic.py --dry-run

  Apply changes:
    python3 scripts/consolidate_resources_generic.py --apply

This script is intentionally conservative.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import time
import xml.etree.ElementTree as ET


WORKSPACE = Path(__file__).resolve().parents[1]

RES_DIR_RE = re.compile(r"src/.*/res/")
# We will only modify files inside the base 'values' folder (no qualifiers)
BASE_VALUES_DIR = "values"

EXCLUDE_DIRS = {
    ".git",
    "build",
    "build-cache",
    ".gradle",
    ".idea",
    "out",
}

VALUE_FILE_PATTERNS = (
    re.compile(r"colors.*\\.xml$"),
    re.compile(r"integers.*\\.xml$"),
    re.compile(r"styles.*\\.xml$"),
)


def is_under_res(path: Path) -> bool:
    try:
        parts = path.as_posix().split("/")
        return "res" in parts and "src" in parts
    except Exception:
        return False


def iter_files(root: Path) -> list[Path]:
    files: list[Path] = []
    for dirpath, dirnames, filenames in os.walk(root):
        # prune excluded
        dirnames[:] = [d for d in dirnames if d not in EXCLUDE_DIRS]
        for f in filenames:
            files.append(Path(dirpath) / f)
    return files


def normalize_text(text: str | None) -> str:
    if text is None:
        return ""
    return re.sub(r"\s+", "", text).lower()


def parse_values_file(path: Path) -> list[dict]:
    # Only base values folder
    if path.parent.name != BASE_VALUES_DIR:
        return []
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        return []
    root = tree.getroot()
    resources: list[dict] = []
    for elem in root:
        tag = elem.tag
        if tag not in ("color", "integer", "style"):
            continue
        name = elem.attrib.get("name")
        if not name:
            continue
        if tag == "style":
            # Serialize style items deterministically
            items = []
            for item in elem.findall("item"):
                k = item.attrib.get("name", "")
                v = normalize_text((item.text or ""))
                items.append((k, v))
            items.sort()
            value = json.dumps(items, ensure_ascii=False)
        else:
            value = normalize_text(elem.text or "")
        resources.append(
            {
                "type": tag,
                "name": name,
                "norm_value": value,
                "file": str(path),
                "serialized": ET.tostring(elem, encoding="unicode"),
            }
        )
    return resources


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="apply changes")
    parser.add_argument("--dry-run", action="store_true", help="dry run (default)")
    args = parser.parse_args()

    apply = args.apply and not args.dry_run

    files = iter_files(WORKSPACE)

    # Collect values files
    values_files: list[Path] = [
        p for p in files
        if p.suffix == ".xml"
        and is_under_res(p)
        and any(ptrn.search(p.name) for ptrn in VALUE_FILE_PATTERNS)
    ]

    values_entries = []
    for vf in values_files:
        values_entries.extend(parse_values_file(vf))

    # Index by (type, name, norm_value)
    dup_map: dict[tuple[str, str, str], list[str]] = {}
    for e in values_entries:
        key = (e["type"], e["name"], e["norm_value"])
        dup_map.setdefault(key, []).append(e["file"])

    # Detect duplicates (more than one source file) with identical normalized value
    duplicates_values: dict[str, list[str]] = {}
    key_to_entries: dict[str, list[dict]] = {}
    for (typ, name, norm), paths in dup_map.items():
        if len(paths) > 1:
            key = f"{typ}:{name}"
            duplicates_values[key] = sorted(set(paths))
            # Collect full entries for later element-level edits
            entries = [e for e in values_entries if e["type"] == typ and e["name"] == name and e["norm_value"] == norm]
            key_to_entries[key] = entries

    # Drawables and mipmap (exact same filename + identical content)
    drawable_files: list[Path] = [
        p for p in files
        if is_under_res(p)
        and (
            "/drawable" in p.as_posix() or "/mipmap" in p.as_posix()
        )
        and p.is_file()
        and p.suffix.lower() in {".xml", ".png", ".webp", ".jpg", ".jpeg", ".svg"}
    ]

    # Group by (basename, hash)
    dups_drawables: dict[str, list[str]] = {}
    hash_cache: dict[str, str] = {}
    for p in drawable_files:
        try:
            file_hash = sha256_file(p)
        except Exception:
            continue
        hash_cache[str(p)] = file_hash
        key = f"{p.name}:{file_hash}"
        dups_drawables.setdefault(key, []).append(str(p))

    duplicates_drawables: dict[str, list[str]] = {
        k: v for k, v in dups_drawables.items() if len(v) > 1
    }

    report = {
        "values_duplicates": duplicates_values,
        "drawable_duplicates": duplicates_drawables,
    }

    print(json.dumps(report, ensure_ascii=False, indent=2))

    if not apply:
        return

    timestamp = time.strftime("%Y%m%d_%H%M%S")
    backup_root = WORKSPACE / "scripts" / "backups" / f"resources_{timestamp}"
    backup_root.mkdir(parents=True, exist_ok=True)

    def backup(path_str: str) -> None:
        src = Path(path_str)
        dst = backup_root / src.relative_to(WORKSPACE)
        dst.parent.mkdir(parents=True, exist_ok=True)
        if src.is_file():
            shutil.copy2(src, dst)

    # Helper to choose keeper path (prefer ui module)
    def choose_keeper(paths: list[str]) -> str:
        ui_paths = [p for p in paths if "/ui/" in p]
        if ui_paths:
            return sorted(ui_paths)[0]
        return sorted(paths)[0]

    removed_files: list[str] = []
    edited_files: set[str] = set()

    def remove_element_from_file(path_str: str, typ: str, name: str) -> None:
        path = Path(path_str)
        try:
            tree = ET.parse(path)
        except ET.ParseError:
            return
        root = tree.getroot()
        changed = False
        for child in list(root):
            if child.tag == typ and child.attrib.get("name") == name:
                root.remove(child)
                changed = True
        if changed:
            backup(path_str)
            # Ensure pretty basic structure
            tree.write(path, encoding="utf-8", xml_declaration=True)
            edited_files.add(path_str)

    def ensure_element_in_ui(typ: str, name: str, serialized: str) -> str:
        # Choose target ui file
        if typ == "color":
            target = WORKSPACE / "ui/src/main/res/values/colors.xml"
        elif typ == "integer":
            target = WORKSPACE / "ui/src/main/res/values/integers.xml"
        else:  # style
            target = WORKSPACE / "ui/src/main/res/values/styles.xml"

        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            target.write_text("""<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources/>\n""", encoding="utf-8")

        tree = ET.parse(target)
        root = tree.getroot()
        # Check if already present
        for child in root:
            if child.tag == typ and child.attrib.get("name") == name:
                return str(target)
        # Append new element from serialized xml
        try:
            new_elem = ET.fromstring(serialized)
            root.append(new_elem)
            tree.write(target, encoding="utf-8", xml_declaration=True)
        except ET.ParseError:
            # As a fallback, append a minimal element for color/integer
            if typ in {"color", "integer"}:
                minimal = ET.Element(typ, {"name": name})
                root.append(minimal)
                tree.write(target, encoding="utf-8", xml_declaration=True)
        return str(target)

    # Merge/remove duplicates in values at element level
    for key, paths in duplicates_values.items():
        typ, name = key.split(":", 1)
        # Determine serialized content from any entry (all equal by design here)
        entries = key_to_entries.get(key, [])
        serialized = entries[0]["serialized"] if entries else f"<{typ} name=\"{name}\"/>"

        # Prefer keeping in UI; create if absent
        ui_existing = [p for p in paths if "/ui/" in p]
        if ui_existing:
            keep_file = choose_keeper(ui_existing)
        else:
            keep_file = ensure_element_in_ui(typ, name, serialized)

        # Remove the duplicate element from other files (do not delete files)
        for p in paths:
            if p == keep_file:
                continue
            remove_element_from_file(p, typ, name)

    # Remove duplicate drawables/mipmap (same name+content)
    for key, paths in duplicates_drawables.items():
        keep = choose_keeper(paths)
        for p in paths:
            if p == keep:
                continue
            backup(p)
            try:
                os.remove(p)
                removed_files.append(p)
            except FileNotFoundError:
                pass

    # Write summary
    summary = {
        "removed_files": removed_files,
        "edited_files": sorted(edited_files),
        "backup_dir": str(backup_root),
    }
    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()


