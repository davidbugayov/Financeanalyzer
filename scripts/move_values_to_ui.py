#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Consolidate all values resources into ui module and delete source files if merged.

Rules:
- Merge types: strings.xml, colors.xml, dimens.xml, integers.xml, arrays.xml, styles.xml, themes.xml
- Destination dir: ui/src/main/res/values
- If destination file is missing, create it with <resources> root.
- When merging, if a name already exists with the same value, skip. If exists with different value, keep source and report conflict.
- Delete source file only if every node was merged/skipped without conflicts.
- Creates backups under scripts/backups/resources_<timestamp>/
"""

from __future__ import annotations

import shutil
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
import xml.etree.ElementTree as ET

PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEST_DIR = PROJECT_ROOT / "ui/src/main/res/values"
BACKUP_DIR = PROJECT_ROOT / "scripts/backups" / f"resources_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

MERGE_FILES = {
    "strings.xml",
    "colors.xml",
    "dimens.xml",
    "integers.xml",
    "arrays.xml",
    "styles.xml",
    "themes.xml",
}


def ensure_resources_file(path: Path) -> ET.ElementTree:
    if not path.exists():
        path.parent.mkdir(parents=True, exist_ok=True)
        root = ET.Element("resources")
        tree = ET.ElementTree(root)
        tree.write(path, encoding="utf-8", xml_declaration=True)
        return tree
    return ET.parse(path)


def index_by_name(root: ET.Element) -> dict[str, ET.Element]:
    result: dict[str, ET.Element] = {}
    for child in list(root):
        name = child.get("name")
        if name:
            result[name] = child
    return result


@dataclass
class MergeResult:
    merged: list[str]
    skipped_same: list[str]
    conflicts: list[str]


def merge_files(src: Path, dst: Path) -> MergeResult:
    src_tree = ET.parse(src)
    src_root = src_tree.getroot()
    dst_tree = ensure_resources_file(dst)
    dst_root = dst_tree.getroot()

    dst_index = index_by_name(dst_root)

    merged: list[str] = []
    skipped_same: list[str] = []
    conflicts: list[str] = []

    for node in list(src_root):
        name = node.get("name")
        if not name:
            # skip comments/unknown nodes
            continue
        dst_node = dst_index.get(name)
        if dst_node is None:
            dst_root.append(node)
            merged.append(name)
            dst_index[name] = node
        else:
            # Compare serialized text + attributes excluding translatable flag for strings
            src_text = (node.text or "").strip()
            dst_text = (dst_node.text or "").strip()
            # Normalize whitespace
            if src_text == dst_text and {k: v for k, v in node.attrib.items() if k != "translatable"} == {
                k: v for k, v in dst_node.attrib.items() if k != "translatable"
            }:
                skipped_same.append(name)
            else:
                conflicts.append(name)

    if merged:
        dst_tree.write(dst, encoding="utf-8", xml_declaration=True)

    return MergeResult(merged=merged, skipped_same=skipped_same, conflicts=conflicts)


def main() -> int:
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)

    # Gather candidate source files (exclude ui and build/ and backups)
    candidates: list[Path] = []
    for p in PROJECT_ROOT.rglob("res/values/*.xml"):
        if "build/" in str(p) or "/ui/" in str(p) or "/backup" in str(p) or "/backups/" in str(p):
            continue
        if p.name in MERGE_FILES:
            candidates.append(p)

    report_lines: list[str] = []
    report_lines.append("Values consolidation report\n")

    for src in sorted(candidates):
        dst = DEST_DIR / src.name
        backup_path = BACKUP_DIR / src.relative_to(PROJECT_ROOT)
        backup_path.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, backup_path)

        try:
            res = merge_files(src, dst)
        except ET.ParseError:
            report_lines.append(f"PARSE ERROR: {src}\n")
            continue

        if res.conflicts:
            report_lines.append(f"CONFLICTS in {src}: {res.conflicts}\n")
            # keep file for manual resolution
            continue

        # Safe to delete if no conflicts
        src.unlink()
        # Clean up empty dir
        try:
            if not any(src.parent.iterdir()):
                src.parent.rmdir()
        except Exception:
            pass

        report_lines.append(
            f"Merged {src} -> {dst.name}; added {len(res.merged)}, same {len(res.skipped_same)}; deleted source.\n"
        )

    report_path = BACKUP_DIR / "values_consolidation_report.txt"
    report_path.write_text("".join(report_lines), encoding="utf-8")
    print(f"Done. Report: {report_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())


