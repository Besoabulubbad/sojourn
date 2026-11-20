#!/usr/bin/env python3
"""
Composes the repository's social preview (plan, Phase 6): 1280 x 640, the Sojourn lockup and one line on the left,
three screens of the app on the right (Explore in English and light, a stay in Arabic and dark, the booking
confirmed in Arabic and dark), all from the showcase screenshots that CI verifies.

Output: docs/showcase/social-preview.png. GitHub takes a social preview only by hand: Settings > General > Social
preview > Edit > Upload.

Usage (needs Pillow and rsvg-convert):
    python3 -I tooling/scripts/social_preview.py
"""
from __future__ import annotations

import json
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
SCREENS = ROOT / "docs/showcase/screens"
OUT = ROOT / "docs/showcase/social-preview.png"
FONT = ROOT / "core/designsystem/src/commonMain/composeResources/font/alexandria_medium.ttf"
WIDTH, HEIGHT = 1280, 640
SHOTS = ["explore-en-light.png", "details-ar-dark.png", "booking-confirmed-ar-dark.png"]


def token(name: str, theme: str) -> str:
    tokens = json.loads((ROOT / "design/tokens.json").read_text(encoding="utf-8"))
    for entry in tokens["color"]["tokens"]:
        if entry["name"] == name:
            return entry["value"][theme]
    raise SystemExit(f"Unknown colour token {name}")


def lockup(width: int) -> Image.Image:
    with tempfile.TemporaryDirectory() as tmp:
        png = Path(tmp) / "lockup.png"
        subprocess.run(
            ["rsvg-convert", "-w", str(width), "-o", str(png), str(ROOT / "design/brand/sojourn-lockup.svg")],
            check=True,
        )
        return Image.open(png).convert("RGBA")


def phone(path: Path, height: int, radius: int) -> Image.Image:
    shot = Image.open(path).convert("RGB")
    shot = shot.crop((0, 0, shot.width, int(shot.width * 2.05)))
    shot = shot.resize((int(height * shot.width / shot.height), height), Image.LANCZOS)
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, *shot.size), radius=radius, fill=255)
    framed = Image.new("RGBA", shot.size)
    framed.paste(shot, (0, 0), mask)
    return framed


def main() -> None:
    bg, ink, muted = token("bg", "light"), token("ink", "light"), token("ink-muted", "light")
    canvas = Image.new("RGBA", (WIDTH, HEIGHT), bg)
    canvas.alpha_composite(lockup(360), (64, 200))
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.truetype(str(FONT), 25)
    small = ImageFont.truetype(str(FONT), 18)
    draw.text((68, 340), "Furnished stays, a few nights to a year", font=font, fill=ink)
    draw.text((68, 384), "Kotlin Multiplatform · Compose · English and Arabic", font=small, fill=muted)

    height, y, overlap, margin = 500, 60, 40, 36
    cards = [phone(SCREENS / name, height, radius=28) for name in SHOTS]
    # Laid out from the right margin, so the three screens always fit beside the text.
    x = WIDTH - margin - (sum(card.width for card in cards) - overlap * (len(cards) - 1))
    for index, card in enumerate(cards):
        shadow = Image.new("RGBA", (card.width + 40, card.height + 40), (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle((20, 24, card.width + 20, card.height + 24), 30, fill=(0, 0, 0, 60))
        shadow = shadow.filter(ImageFilter.GaussianBlur(10))
        offset = (x + index * (card.width - overlap), y + (0 if index == 1 else 24))
        canvas.alpha_composite(shadow, (offset[0] - 20, offset[1] - 20))
        canvas.alpha_composite(card, offset)
    canvas.convert("RGB").save(OUT, optimize=True)
    print(OUT.relative_to(ROOT), canvas.size)


if __name__ == "__main__":
    main()
