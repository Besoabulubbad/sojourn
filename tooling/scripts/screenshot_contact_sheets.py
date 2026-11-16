#!/usr/bin/env python3
"""
Builds one contact sheet per screenshot case: every recorded variant (language × theme × font scale × width) of
the case side by side, labelled, so a reviewer can check them all at once for clipping, overlap, broken words,
mirroring and contrast.

Why: sampling a few variants missed real bugs (a card clipping its text at normal size in English only). Every
visual change is reviewed on these sheets before its baselines are committed.

Usage (needs Pillow):
    python3 -I tooling/scripts/screenshot_contact_sheets.py core/designsystem/screenshots/androidHostTest <out dir>
"""
from __future__ import annotations

import glob
import os
import re
import sys
from collections import defaultdict

from PIL import Image, ImageDraw

VARIANT = re.compile(r"(.+)_(en|ar)_(light|dark)_f(\d+)_w(\d+)\.png")
COLUMNS = 4
CELL_WIDTH = 600
LABEL_HEIGHT = 20


def main(source: str, out: str) -> None:
    os.makedirs(out, exist_ok=True)
    cases: dict[str, list[tuple[str, str, str, str, str]]] = defaultdict(list)
    for path in sorted(glob.glob(os.path.join(source, "*.png"))):
        match = VARIANT.match(os.path.basename(path))
        if match:
            cases[match.group(1)].append((*match.groups()[1:], path))
    for case, variants in cases.items():
        # Rows by width then font scale; columns by language then theme.
        variants.sort(key=lambda v: (v[3], v[2], v[0], v[1]))
        images = [Image.open(v[4]).convert("RGB") for v in variants]
        scale = CELL_WIDTH / max(i.width for i in images)
        images = [i.resize((int(i.width * scale), int(i.height * scale))) for i in images]
        cell_w = max(i.width for i in images)
        cell_h = max(i.height for i in images) + LABEL_HEIGHT
        rows = (len(images) + COLUMNS - 1) // COLUMNS
        # Magenta shows any area a variant does not fill, so a narrow or short render stands out.
        sheet = Image.new("RGB", (cell_w * COLUMNS, cell_h * rows), (255, 0, 255))
        draw = ImageDraw.Draw(sheet)
        for k, (image, variant) in enumerate(zip(images, variants)):
            x, y = (k % COLUMNS) * cell_w, (k // COLUMNS) * cell_h
            draw.rectangle([x, y, x + cell_w, y + LABEL_HEIGHT], fill="black")
            draw.text((x + 4, y + 4), f"{variant[0]} {variant[1]} f{variant[2]} w{variant[3]}", fill="white")
            sheet.paste(image, (x, y + LABEL_HEIGHT))
        sheet.save(os.path.join(out, f"{case}.jpg"), quality=80)
    print(f"{len(cases)} sheets in {out}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    main(sys.argv[1], sys.argv[2])
