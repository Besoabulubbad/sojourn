#!/usr/bin/env python3
"""
Audits one screen of the running Android app for the two accessibility rules a machine can check (docs/guides/
accessibility.md): every tappable element has a name a screen reader can say, and every tap target is at least
48 dp (C-03). Reads a uiautomator dump of whatever the device shows.

Usage:
    adb shell uiautomator dump /sdcard/u.xml && adb pull /sdcard/u.xml
    python3 -I tooling/scripts/a11y_audit.py u.xml <density-dpi> <screen-name> [screen-height-px]

<density-dpi> is from `adb shell wm density`. A target cut off by the screen's bottom edge or a sheet's pinned
footer measures short; the report says where each one is, so those can be told apart by eye.
"""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET

PACKAGE = "com.abulubad.sojourn"
TOUCH_MIN_DP = 48


def bounds(node: ET.Element) -> list[int]:
    return list(map(int, re.findall(r"\d+", node.get("bounds", "[0,0][0,0]"))))


def spoken(node: ET.Element) -> str:
    """The node's own name, else the first name among its descendants (Compose merges them for a button)."""
    for n in node.iter():
        name = n.get("content-desc") or n.get("text")
        if name:
            return name
    return ""


def main() -> None:
    path, dpi, screen = sys.argv[1], float(sys.argv[2]), sys.argv[3]
    bottom = int(sys.argv[4]) if len(sys.argv) > 4 else None
    scale = dpi / 160
    issues = []
    for node in ET.parse(path).getroot().iter("node"):
        if node.get("package") != PACKAGE:
            continue
        if "true" not in (node.get("clickable"), node.get("long-clickable"), node.get("checkable")):
            continue
        x1, y1, x2, y2 = bounds(node)
        name = spoken(node)
        if not name:
            issues.append(f"UNNAMED {node.get('class')} at {node.get('bounds')}")
        width, height = (x2 - x1) / scale, (y2 - y1) / scale
        clipped = x1 <= 0 or y1 <= 0 or (bottom is not None and y2 >= bottom)
        if (width < TOUCH_MIN_DP - 0.5 or height < TOUCH_MIN_DP - 0.5) and not clipped:
            issues.append(f"SMALL {width:.0f}x{height:.0f} dp '{name[:40]}' at {node.get('bounds')}")
    for issue in issues:
        print(f"{screen}: {issue}")
    print(f"{screen}: {len(issues)} issue(s)")


if __name__ == "__main__":
    main()
