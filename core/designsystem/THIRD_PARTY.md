# Third-party assets in core:designsystem

| Asset | Source | Version | Licence | How it got here |
|---|---|---|---|---|
| Alexandria (400, 500, 600) | [google/fonts `ofl/alexandria`](https://github.com/google/fonts/tree/main/ofl/alexandria) | variable `Alexandria[wght].ttf`, fetched 2026-11-16 | SIL OFL 1.1 — `licenses/OFL-Alexandria.txt` | Static instances cut from the variable font with fontTools `instancer` at `wght` 400/500/600 |
| Readex Pro (400, 500, 600) | [google/fonts `ofl/readexpro`](https://github.com/google/fonts/tree/main/ofl/readexpro) | variable `ReadexPro[HEXP,wght].ttf`, fetched 2026-11-16 | SIL OFL 1.1 — `licenses/OFL-ReadexPro.txt` | Same, with `HEXP` pinned to 0 |
| 59 icons + 2 filled variants | [lucide-static](https://www.npmjs.com/package/lucide-static) | 1.73.0 | ISC — `licenses/LICENSE-Lucide.txt` | `tooling/scripts/lucide_to_vector.py` → Android vector XML, stroke 1.75 |
| 12 sample photos (`sample_*.jpg`) | Wikimedia Commons, mostly Unsplash photographers' work mirrored there: [living room](https://commons.wikimedia.org/wiki/File:Living_room_(Unsplash).jpg) (Jarosław Ceborski), [apartment](https://commons.wikimedia.org/wiki/File:Artchapiz1_artchapiz.es_apartment_rental_granada_01.jpg) (Artchapiz), [kitchen](https://commons.wikimedia.org/wiki/File:Blue_white_kitchen_interior_(Unsplash).jpg) (NeONBRAND), [bedroom](https://commons.wikimedia.org/wiki/File:Hotel_bedroom_windows_(Unsplash).jpg) (Markus Spiske), [loft bedroom](https://commons.wikimedia.org/wiki/File:NY_loft_bedroom_(Unsplash).jpg) (Gabriel Beaudry), [window nook](https://commons.wikimedia.org/wiki/File:Breather_Montreal_interior_(Unsplash).jpg) and [reading corner](https://commons.wikimedia.org/wiki/File:15A_Hanover_St_interior_(Unsplash).jpg) (Breather), [breakfast table](https://commons.wikimedia.org/wiki/File:Round_table_in_a_bright_room_(Unsplash).jpg) (Nirzar Pangarkar), [dining with a view](https://commons.wikimedia.org/wiki/File:Dining_with_a_view_(Unsplash).jpg) (Rik van der Kroon), [dining room](https://commons.wikimedia.org/wiki/File:New_Home_(Unsplash).jpg) (Erick Lee Hodge), [London](https://commons.wikimedia.org/wiki/File:Cloudy_London_skyline_(Unsplash).jpg) (Rob Bye), [Paris](https://commons.wikimedia.org/wiki/File:City_view_of_Paris_(Unsplash).jpg) (Paul Dufour) | fetched 2026-11-16 | CC0 1.0 (public domain); credited anyway | 800 px renditions, resized to 640 px, JPEG quality 72 with `sips`. Used only by the component samples (previews, screenshots, showcase); real listing photos come from the data layer |

Static instances are bundled instead of the variable files because Compose Multiplatform's `Font(resource, weight)` selects a file per weight; three weights cover every type token.

To update the icons: bump the `lucide-static` version here, download its `icons/` folder, and run

```bash
python3 -I tooling/scripts/lucide_to_vector.py <lucide-static>/icons core/designsystem/src/commonMain/composeResources/drawable
```
