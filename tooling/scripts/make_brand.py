#!/usr/bin/env python3
"""
Draws the Sojourn brand assets from the design tokens (plan: showcase workstream, Phase 2).

The mark is a doorway arch with a lamp inside: a home base. Colours come from design/tokens.json only:
brand (ground), bg (arch), accent (lamp); the dark variants use the dark theme's values. The wordmark is
"Sojourn" outlined from the bundled Alexandria SemiBold, so the SVGs need no font to render.

Outputs
  design/brand/sojourn-mark.svg, -mark-dark.svg      app-icon artwork, 108-unit grid (Android adaptive icon)
  design/brand/sojourn-wordmark.svg, -dark.svg       wordmark in brand-ink (light / dark)
  design/brand/sojourn-lockup.svg, -dark.svg         mark + wordmark, for the README header
  app/android/src/main/res/...                       adaptive icon (foreground, background, monochrome) + PNG fallbacks,
                                                     and the window/splash colours (values/ and values-night/)
  iosApp/iosApp/Assets.xcassets/AppIcon.appiconset   1024 px light and dark icons
  docs/showcase/brand/                               copies for the docs site and README

Usage (needs fontTools and rsvg-convert):
    python3 -I tooling/scripts/make_brand.py
"""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parents[2]
TOKENS = json.loads((ROOT / "design/tokens.json").read_text(encoding="utf-8"))
FONT = ROOT / "core/designsystem/src/commonMain/composeResources/font/alexandria_semibold.ttf"
BRAND = ROOT / "design/brand"
RES = ROOT / "app/android/src/main/res"
IOS_ICONS = ROOT / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"


def color(name: str, theme: str) -> str:
    for token in TOKENS["color"]["tokens"]:
        if token["name"] == name:
            return token["value"][theme]
    raise SystemExit(f"Unknown colour token {name}")


# --- the mark, on Android's 108 x 108 adaptive grid (safe zone: circle of radius 33 at the centre) --------------
ARCH = "M39,76 V50 A15,15 0 0 1 69,50 V76 Z"
LAMP = (54.0, 51.0, 5.5)


def mark_svg(theme: str, size: int = 1024, rounded: bool = True) -> str:
    ground, arch, lamp = color("brand", theme) if theme == "light" else color("bg", theme), \
        color("bg", "light") if theme == "light" else color("brand", "dark"), color("accent", theme)
    radius = 24 if rounded else 0
    cx, cy, r = LAMP
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 108 108">'
        f'<rect width="108" height="108" rx="{radius}" fill="{ground}"/>'
        f'<path d="{ARCH}" fill="{arch}"/>'
        f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{lamp}"/>'
        "</svg>\n"
    )


# --- the wordmark -----------------------------------------------------------------------------------------------
def wordmark_paths(text: str, cap_px: float) -> tuple[str, float, float]:
    font = TTFont(FONT)
    glyph_set = font.getGlyphSet()
    cmap = font.getBestCmap()
    upm = font["head"].unitsPerEm
    ascent = font["hhea"].ascent
    descent = -font["hhea"].descent
    scale = cap_px / upm
    x = 0.0
    paths = []
    for ch in text:
        name = cmap[ord(ch)]
        pen = SVGPathPen(glyph_set)
        glyph_set[name].draw(TransformPen(pen, (scale, 0, 0, -scale, x, ascent * scale)))
        if pen.getCommands():
            paths.append(pen.getCommands())
        x += glyph_set[name].width * scale
    return " ".join(paths), x, (ascent + descent) * scale


def wordmark_svg(theme: str) -> str:
    d, width, height = wordmark_paths("Sojourn", 96)
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width:.0f}" height="{height:.0f}" '
        f'viewBox="0 0 {width:.1f} {height:.1f}"><path d="{d}" fill="{color("brand-ink", theme)}"/></svg>\n'
    )


def lockup_svg(theme: str) -> str:
    d, width, height = wordmark_paths("Sojourn", 96)
    mark = 132.0
    gap = 28.0
    total = mark + gap + width
    ground = color("brand", "light") if theme == "light" else color("bg", "dark")
    arch = color("bg", "light") if theme == "light" else color("brand", "dark")
    cx, cy, r = LAMP
    s = mark / 108
    y = (mark - height) / 2
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{total:.0f}" height="{mark:.0f}" viewBox="0 0 {total:.1f} {mark:.1f}">'
        f'<g transform="scale({s:.4f})"><rect width="108" height="108" rx="24" fill="{ground}"/>'
        f'<path d="{ARCH}" fill="{arch}"/><circle cx="{cx}" cy="{cy}" r="{r}" fill="{color("accent", theme)}"/></g>'
        f'<g transform="translate({mark + gap:.1f},{y:.1f})"><path d="{d}" fill="{color("brand-ink", theme)}"/></g>'
        "</svg>\n"
    )


# --- Android ----------------------------------------------------------------------------------------------------
def vector(paths: list[tuple[str, str]]) -> str:
    body = "\n".join(f'    <path android:fillColor="{fill}" android:pathData="{data}" />' for data, fill in paths)
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Generated by tooling/scripts/make_brand.py from design/tokens.json. Do not edit. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n'
        f"{body}\n</vector>\n"
    )


def circle_path(cx: float, cy: float, r: float) -> str:
    return f"M{cx - r},{cy} a{r},{r} 0 1,0 {2 * r},0 a{r},{r} 0 1,0 {-2 * r},0"


def android() -> None:
    lamp = circle_path(*LAMP)
    (RES / "drawable").mkdir(parents=True, exist_ok=True)
    (RES / "drawable/ic_launcher_foreground.xml").write_text(
        vector([(ARCH, color("bg", "light")), (lamp, color("accent", "light"))]), encoding="utf-8")
    (RES / "drawable/ic_launcher_monochrome.xml").write_text(
        vector([(ARCH + " " + lamp, "#FFFFFFFF")]), encoding="utf-8")
    (RES / "values").mkdir(exist_ok=True)
    (RES / "values/ic_launcher_background.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated by tooling/scripts/make_brand.py. -->\n'
        f'<resources>\n    <color name="ic_launcher_background">{color("brand", "light")}</color>\n</resources>\n',
        encoding="utf-8")
    adaptive = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        "</adaptive-icon>\n"
    )
    for name in ("ic_launcher", "ic_launcher_round"):
        (RES / f"mipmap-anydpi-v26/{name}.xml").write_text(adaptive, encoding="utf-8")
    # PNG fallbacks for API 24-25 (no adaptive icons there).
    square = BRAND / "sojourn-mark.svg"
    round_svg = BRAND / ".round.svg"
    round_svg.write_text(mark_svg("light").replace('rx="24"', 'rx="54"'), encoding="utf-8")
    for density, px in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        out = RES / f"mipmap-{density}"
        out.mkdir(exist_ok=True)
        render(square, out / "ic_launcher.png", px)
        render(round_svg, out / "ic_launcher_round.png", px)
    round_svg.unlink()


# --- iOS --------------------------------------------------------------------------------------------------------
def android_window() -> None:
    """The window and splash colours: the screen colour (bg) behind the splash and before Compose draws, so a cold
    start never flashes the wrong theme, and the launcher icon's ground (brand) behind the splash icon in both."""
    for folder, theme in (("values", "light"), ("values-night", "dark")):
        (RES / folder).mkdir(parents=True, exist_ok=True)
        icon_ground = color("brand", "light")
        (RES / f"{folder}/window_colors.xml").write_text(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!-- Generated by tooling/scripts/make_brand.py. -->\n"
            f'<resources>\n    <color name="window_background">{color("bg", theme)}</color>\n'
            f'    <color name="splash_icon_background">{icon_ground}</color>\n</resources>\n',
            encoding="utf-8",
        )


def ios() -> None:
    light = BRAND / ".ios-light.svg"
    dark = BRAND / ".ios-dark.svg"
    light.write_text(mark_svg("light", rounded=False), encoding="utf-8")
    dark.write_text(mark_svg("dark", rounded=False), encoding="utf-8")
    render(light, IOS_ICONS / "app-icon-1024.png", 1024)
    render(dark, IOS_ICONS / "app-icon-1024-dark.png", 1024)
    light.unlink()
    dark.unlink()
    contents = json.loads((IOS_ICONS / "Contents.json").read_text(encoding="utf-8"))
    for image in contents["images"]:
        appearances = image.get("appearances", [])
        if any(a.get("value") == "dark" for a in appearances):
            image["filename"] = "app-icon-1024-dark.png"
    (IOS_ICONS / "Contents.json").write_text(json.dumps(contents, indent=2) + "\n", encoding="utf-8")


def render(svg: Path, png: Path, px: int) -> None:
    subprocess.run(["rsvg-convert", "-w", str(px), "-h", str(px), "-o", str(png), str(svg)], check=True)


def main() -> None:
    android_window()
    BRAND.mkdir(parents=True, exist_ok=True)
    (BRAND / "sojourn-mark.svg").write_text(mark_svg("light"), encoding="utf-8")
    (BRAND / "sojourn-mark-dark.svg").write_text(mark_svg("dark"), encoding="utf-8")
    (BRAND / "sojourn-wordmark.svg").write_text(wordmark_svg("light"), encoding="utf-8")
    (BRAND / "sojourn-wordmark-dark.svg").write_text(wordmark_svg("dark"), encoding="utf-8")
    (BRAND / "sojourn-lockup.svg").write_text(lockup_svg("light"), encoding="utf-8")
    (BRAND / "sojourn-lockup-dark.svg").write_text(lockup_svg("dark"), encoding="utf-8")
    showcase = ROOT / "docs/showcase/brand"
    showcase.mkdir(parents=True, exist_ok=True)
    for name in ("sojourn-lockup.svg", "sojourn-lockup-dark.svg", "sojourn-mark.svg", "sojourn-mark-dark.svg"):
        (showcase / name).write_text((BRAND / name).read_text(encoding="utf-8"), encoding="utf-8")
    android()
    ios()
    print("Brand assets written to design/brand, app/android/src/main/res and iosApp AppIcon.appiconset")


if __name__ == "__main__":
    main()
