#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import json
import os
import re
import shutil
import sys
from collections import defaultdict
from dataclasses import dataclass, asdict
from datetime import datetime
from pathlib import Path
import xml.etree.ElementTree as ET


BACKUP_PREFIX = "backup_dimens_"


@dataclass
class DimenOccurrence:
    module: str
    file: str
    value: str


def _to_jsonable(obj):
    if isinstance(obj, Path):
        return str(obj)
    if isinstance(obj, set):
        return sorted(list(obj))
    if hasattr(obj, "__dict__"):
        return obj.__dict__
    return obj


def is_backup_dir(path: Path) -> bool:
    return path.name.startswith(BACKUP_PREFIX)


def find_dimen_files(project_root: Path) -> list[Path]:
    files: list[Path] = []
    for p in project_root.rglob("**/res/values/dimens*.xml"):
        # skip backup directories anywhere in path
        if any(is_backup_dir(parent) for parent in p.parents):
            continue
        files.append(p)
    return files


def parse_dimens(file: Path) -> dict[str, str]:
    try:
        tree = ET.parse(file)
    except ET.ParseError:
        return {}
    root = tree.getroot()
    result: dict[str, str] = {}
    for node in root.findall("dimen"):
        name = node.get("name")
        if not name:
            continue
        text = (node.text or "").strip()
        # normalize whitespace
        text = re.sub(r"\s+", " ", text)
        result[name] = text
    return result


def ensure_ui_dimens_file(project_root: Path) -> Path:
    ui_file = project_root / "ui/src/main/res/values/dimens.xml"
    ui_file.parent.mkdir(parents=True, exist_ok=True)
    if not ui_file.exists():
        ui_file.write_text("""<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n</resources>\n""", encoding="utf-8")
    return ui_file


def read_xml_root(path: Path) -> ET.Element:
    tree = ET.parse(path)
    return tree.getroot()


def write_xml_root(path: Path, root: ET.Element) -> None:
    # pretty basic writer (ElementTree won’t pretty print by default)
    xml = ET.tostring(root, encoding="unicode")
    # ensure header
    if not xml.lstrip().startswith("<?xml"):
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + xml
    # add newlines between tags for readability
    xml = xml.replace("><", ">\n    <")
    # ensure single root resources line
    xml = xml.replace("<resources>\n    \n", "<resources>\n    ")
    path.write_text(xml, encoding="utf-8")


def add_or_update_dimen(path: Path, name: str, value: str) -> None:
    root = read_xml_root(path)
    # find existing
    for node in root.findall("dimen"):
        if node.get("name") == name:
            node.text = value
            write_xml_root(path, root)
            return
    # append new node
    new_node = ET.Element("dimen")
    new_node.set("name", name)
    new_node.text = value
    root.append(new_node)
    write_xml_root(path, root)


def remove_dimen_from_file(path: Path, name: str) -> bool:
    changed = False
    tree = ET.parse(path)
    root = tree.getroot()
    for node in list(root.findall("dimen")):
        if node.get("name") == name:
            root.remove(node)
            changed = True
    if changed:
        write_xml_root(path, root)
    return changed


def module_name_from_path(project_root: Path, file: Path) -> str:
    try:
        rel = file.relative_to(project_root)
    except Exception:
        return file.parent.parent.parent.name
    parts = rel.parts
    # Heuristic: module is top-level dir (e.g., ui, core, feature, app, domain, etc.)
    return parts[0] if parts else "unknown"


def build_name_index(project_root: Path, files: list[Path]) -> dict[str, list[DimenOccurrence]]:
    name_index: dict[str, list[DimenOccurrence]] = defaultdict(list)
    for f in files:
        module = module_name_from_path(project_root, f)
        for name, value in parse_dimens(f).items():
            name_index[name].append(
                DimenOccurrence(module=module, file=str(f), value=value)
            )
    return name_index


def grep_usages(project_root: Path, key: str) -> int:
    # very quick and dirty search in text files
    patterns = [
        rf"R\\.dimen\\.{re.escape(key)}\b",
        rf"@dimen/{re.escape(key)}\b",
    ]
    count = 0
    for path in project_root.rglob("**/*"):
        if path.is_dir():
            # skip backups and build outputs
            if is_backup_dir(path) or path.name in {"build", ".gradle", ".git"}:
                continue
            continue
        # limit to likely text files
        if path.suffix.lower() not in {".kt", ".java", ".xml"}:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        for pat in patterns:
            for _ in re.finditer(pat, text):
                count += 1
    return count


def backup_files(project_root: Path, files: set[Path]) -> Path:
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = project_root / f"{BACKUP_PREFIX}{ts}"
    for f in files:
        dest = backup_dir / f.relative_to(project_root)
        dest.parent.mkdir(parents=True, exist_ok=True)
        try:
            shutil.copy2(f, dest)
        except Exception:
            pass
    return backup_dir


def generate_report(report_path: Path, payload: dict) -> None:
    payload = json.loads(json.dumps(payload, default=_to_jsonable))
    report_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def consolidate_dimens(project_root: Path, mode: str, do_backup: bool, unsafe_remove: bool) -> dict:
    ui_dimens_path = ensure_ui_dimens_file(project_root)
    all_files = find_dimen_files(project_root)

    name_index = build_name_index(project_root, all_files)

    changed_files: set[Path] = set()
    consolidated: dict[str, dict] = {}
    conflicts: dict[str, dict] = {}

    # Step 1: unify same-name, same-value across modules into ui
    for name, occs in sorted(name_index.items()):
        values = {o.value for o in occs}
        if len(values) == 1 and len(occs) > 1:
            value = values.pop()
            # ensure in ui
            add_or_update_dimen(ui_dimens_path, name, value)
            changed_files.add(ui_dimens_path)
            # remove from other files
            removed_from = []
            for o in occs:
                fpath = Path(o.file)
                if fpath == ui_dimens_path:
                    continue
                if remove_dimen_from_file(fpath, name):
                    changed_files.add(fpath)
                    removed_from.append(o.file)
            consolidated[name] = {
                "value": value,
                "moved_to": str(ui_dimens_path),
                "removed_from": removed_from,
            }
        elif len(values) > 1 and len(occs) > 1:
            # conflicting definitions — report only
            conflicts[name] = {
                "definitions": [asdict(o) for o in occs],
            }

    # Step 2: find potentially unused dimens
    unused: dict[str, list[DimenOccurrence]] = {}
    for name, occs in sorted(name_index.items()):
        usage = grep_usages(project_root, name)
        if usage == 0:
            unused[name] = occs

    # Optional removal of unused (unsafe)
    removed_unused: dict[str, list[str]] = {}
    if unsafe_remove and unused:
        for name, occs in unused.items():
            removed_from = []
            for o in occs:
                fpath = Path(o.file)
                if remove_dimen_from_file(fpath, name):
                    removed_from.append(o.file)
                    changed_files.add(fpath)
            if removed_from:
                removed_unused[name] = removed_from

    backup_dir = None
    if do_backup and changed_files:
        backup_dir = backup_files(project_root, changed_files)

    return {
        "changed_files": [str(p) for p in sorted(changed_files)],
        "backup_dir": str(backup_dir) if backup_dir else None,
        "consolidated": consolidated,
        "conflicts": conflicts,
        "unused_candidates": {
            k: [asdict(o) for o in v] for k, v in unused.items()
        },
        "removed_unused": removed_unused,
    }


def main():
    parser = argparse.ArgumentParser(description="Consolidate duplicate dimens across modules into ui module, with report & backups.")
    parser.add_argument("--project-root", default=".", help="Project root (default: .)")
    parser.add_argument("--mode", choices=["auto", "interactive", "report"], default="auto")
    parser.add_argument("--backup", action="store_true", help="Backup changed files into backup_dimens_* directory")
    parser.add_argument("--unsafe-remove", action="store_true", help="Also remove potentially unused dimens (dangerous)")
    parser.add_argument("--report", default="scripts/dimens_report.json", help="Path to JSON report")
    args = parser.parse_args()

    project_root = Path(args.project_root).resolve()

    if args.mode == "report":
        # just compute and report, no changes
        files = find_dimen_files(project_root)
        index = build_name_index(project_root, files)
        payload = {
            "files": [str(f) for f in files],
            "by_name": {k: [asdict(o) for o in v] for k, v in index.items()},
        }
        generate_report(Path(args.report), payload)
        print(f"Report written to {args.report}")
        return

    # auto / interactive (interactive == auto for now)
    result = consolidate_dimens(project_root, args.mode, args.backup, args.unsafe_remove)
    generate_report(Path(args.report), result)
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    sys.exit(main())


