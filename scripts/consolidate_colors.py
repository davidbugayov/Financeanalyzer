#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict
from datetime import datetime
import shutil


def find_color_files(root: Path) -> list[Path]:
    results: list[Path] = []
    for p in root.rglob("**/res/values/colors.xml"):
        if "build/" in str(p) or "backup" in str(p):
            continue
        results.append(p)
    return results


def parse_colors(file: Path) -> dict[str, str]:
    try:
        tree = ET.parse(file)
    except ET.ParseError:
        return {}
    root = tree.getroot()
    result: dict[str, str] = {}
    for node in root.findall("color"):
        name = node.get("name")
        if not name:
            continue
        text = (node.text or "").strip()
        result[name] = text
    return result


def read_xml_root(path: Path) -> ET.Element:
    tree = ET.parse(path)
    return tree.getroot()


def write_xml_root(path: Path, root: ET.Element) -> None:
    xml = ET.tostring(root, encoding="unicode")
    if not xml.lstrip().startswith("<?xml"):
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + xml
    xml = xml.replace("><", ">\n    <")
    path.write_text(xml, encoding="utf-8")


def ensure_ui_colors_file(project_root: Path) -> Path:
    path = project_root / "ui/src/main/res/values/colors.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    if not path.exists():
        path.write_text("""<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n</resources>\n""", encoding="utf-8")
    return path


def add_or_update_color(path: Path, name: str, value: str) -> None:
    root = read_xml_root(path)
    for node in root.findall("color"):
        if node.get("name") == name:
            node.text = value
            write_xml_root(path, root)
            return
    new_node = ET.Element("color")
    new_node.set("name", name)
    new_node.text = value
    root.append(new_node)
    write_xml_root(path, root)


def remove_color_from_file(path: Path, name: str, expect_value: str) -> bool:
    changed = False
    try:
        tree = ET.parse(path)
    except ET.ParseError:
        return False
    root = tree.getroot()
    for node in list(root.findall("color")):
        if node.get("name") == name and (node.text or "").strip() == expect_value:
            root.remove(node)
            changed = True
    if changed:
        write_xml_root(path, root)
    return changed


def backup(files: set[Path], project_root: Path) -> Path:
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_dir = project_root / f"backup_colors_{ts}"
    for f in files:
        dest = backup_dir / f.relative_to(project_root)
        dest.parent.mkdir(parents=True, exist_ok=True)
        try:
            shutil.copy2(f, dest)
        except Exception:
            pass
    return backup_dir


def main():
    project_root = Path(__file__).resolve().parents[1]
    ui_colors = ensure_ui_colors_file(project_root)
    files = find_color_files(project_root)

    index: dict[str, list[tuple[Path, str]]] = defaultdict(list)
    for f in files:
        for name, value in parse_colors(f).items():
            index[name].append((f, value))

    changed: set[Path] = set()
    moved = 0
    removed = 0

    for name, occs in sorted(index.items()):
        values = {v for _, v in occs}
        if len(values) == 1 and len(occs) > 1:
            value = values.pop()
            add_or_update_color(ui_colors, name, value)
            changed.add(ui_colors)
            moved += 1
            for f, v in occs:
                if f == ui_colors:
                    continue
                if remove_color_from_file(f, name, value):
                    changed.add(f)
                    removed += 1

    if changed:
        bdir = backup(changed, project_root)
        print(f"Backup saved to: {bdir}")
    print(f"Consolidated colors: {moved}, removed duplicates: {removed}")


if __name__ == "__main__":
    main()


