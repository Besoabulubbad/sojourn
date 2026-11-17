#!/usr/bin/env python3
"""
Builds the catalogue's photo pool (ADR 0016) from Openverse and Wikimedia Commons.

Why: the catalogue's photos are hosted on the docs site (docs/catalog/photos/) and must be reproducible and
credited. Every photo was reviewed by hand for a luxury standard. The sources file lists each chosen image by its
Openverse id or Commons file title and its room category; this script checks the licence is still CC0 or public
domain, downloads it, resizes it to 1024 px, and writes the pool manifest and the credits page.

Inputs:  tooling/catalog/photo-pool-sources.json   [{"openverse": "<id>" | "commons": "File:…", "category": …}, …]
Outputs: docs/catalog/photos/<category>-NN.jpg       the photos the docs site serves
         tooling/catalog/photo-pool.json             the manifest make_catalog.py reads
         docs/catalog/CREDITS.md                     author and source of every photo

Usage (network required; the APIs rate-limit, so the script paces itself; resizing uses macOS `sips`):
    python3 -I tooling/scripts/fetch_photo_pool.py
"""
from __future__ import annotations

import html
import json
import re
import subprocess
import tempfile
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCES = ROOT / "tooling/catalog/photo-pool-sources.json"
PHOTOS = ROOT / "docs/catalog/photos"
MANIFEST = ROOT / "tooling/catalog/photo-pool.json"
CREDITS = ROOT / "docs/catalog/CREDITS.md"
WIDTH = 1024
USER_AGENT = "sojourn-photo-pool/1.0 (generic client)"


def get(url: str) -> bytes:
    for attempt in range(4):
        try:
            request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            return urllib.request.urlopen(request, timeout=60).read()
        except Exception:
            time.sleep(20 * (attempt + 1))
    raise SystemExit(f"Could not fetch {url}")


def info(title: str) -> dict:
    url = ("https://commons.wikimedia.org/w/api.php?action=query&format=json&prop=imageinfo&iiprop=url|extmetadata"
           f"&iiurlwidth={WIDTH}&iiextmetadatafilter=LicenseShortName|Artist&titles=" + urllib.parse.quote(title))
    page = next(iter(json.loads(get(url))["query"]["pages"].values()))
    return page["imageinfo"][0]


def openverse(image_id: str) -> dict:
    meta = json.loads(get(f"https://api.openverse.org/v1/images/{image_id}/"))
    return {"url": meta["url"], "licence": meta["license"].upper(), "author": meta.get("creator") or "Unknown",
            "title": meta.get("title") or image_id, "page": meta.get("foreign_landing_url") or meta["url"],
            "source": meta.get("provider") or "openverse"}


def commons(title: str) -> dict:
    meta = info(title)
    return {"url": meta["thumburl"], "licence": meta["extmetadata"]["LicenseShortName"]["value"],
            "author": plain(meta["extmetadata"].get("Artist", {}).get("value", "")) or "Unknown",
            "title": title[len("File:"):], "page": meta["descriptionurl"], "source": "wikimedia"}


def save_resized(data: bytes, target: Path) -> None:
    """Writes [data] as a JPEG at most WIDTH px wide (sips, macOS) so the docs site serves small files."""
    with tempfile.NamedTemporaryFile(suffix=".jpg") as raw:
        raw.write(data)
        raw.flush()
        subprocess.run(["sips", "-Z", str(WIDTH), "-s", "format", "jpeg", "-s", "formatOptions", "75", raw.name,
                        "--out", str(target)], check=True, capture_output=True)


def plain(text: str) -> str:
    return re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", "", text))).strip()


def main() -> None:
    sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    PHOTOS.mkdir(parents=True, exist_ok=True)
    counts: dict[str, int] = {}
    manifest = []
    for source in sources:
        category = source["category"]
        counts[category] = counts.get(category, 0) + 1
        file = f"{category.replace('_', '-')}-{counts[category]:02d}.jpg"
        meta = openverse(source["openverse"]) if "openverse" in source else commons(source["commons"])
        if not (meta["licence"].startswith("CC0") or meta["licence"] in ("PDM", "PUBLIC DOMAIN")):
            raise SystemExit(f"{meta['title']} is not CC0 or public domain ({meta['licence']})")
        target = PHOTOS / file
        if not target.exists():
            save_resized(get(meta["url"]), target)
            time.sleep(3)
        manifest.append({"file": file, "category": category, **meta})
    MANIFEST.write_text(json.dumps({"photos": manifest}, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    lines = [
        "# Catalogue photo credits", "",
        "Every photo the seed catalogue uses, hosted at `/catalog/photos/` (ADR 0016). Each was reviewed by hand for "
        "the brand's standard. All are CC0 1.0 or public domain, found through Openverse (StockSnap, rawpixel, "
        "Flickr) and Wikimedia Commons; credited anyway.", "",
        "| File | Category | Author | Source | Licence |", "|---|---|---|---|---|",
    ]
    lines += [f"| `{p['file']}` | {p['category']} | {p['author']} | [{p['title']}]({p['page']}) ({p['source']}) "
              f"| {p['licence']} |" for p in manifest]
    CREDITS.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(manifest)} photos -> {PHOTOS.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
