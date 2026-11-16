# Third-party assets in core:designsystem

| Asset | Source | Version | Licence | How it got here |
|---|---|---|---|---|
| Alexandria (400, 500, 600) | [google/fonts `ofl/alexandria`](https://github.com/google/fonts/tree/main/ofl/alexandria) | variable `Alexandria[wght].ttf`, fetched 2026-11-16 | SIL OFL 1.1 — `licenses/OFL-Alexandria.txt` | Static instances cut from the variable font with fontTools `instancer` at `wght` 400/500/600 |
| Readex Pro (400, 500, 600) | [google/fonts `ofl/readexpro`](https://github.com/google/fonts/tree/main/ofl/readexpro) | variable `ReadexPro[HEXP,wght].ttf`, fetched 2026-11-16 | SIL OFL 1.1 — `licenses/OFL-ReadexPro.txt` | Same, with `HEXP` pinned to 0 |
| 58 icons | [lucide-static](https://www.npmjs.com/package/lucide-static) | 1.73.0 | ISC — `licenses/LICENSE-Lucide.txt` | `tooling/scripts/lucide_to_vector.py` → Android vector XML, stroke 1.75 |

Static instances are bundled instead of the variable files because Compose Multiplatform's `Font(resource, weight)` selects a file per weight; three weights cover every type token.

To update the icons: bump the `lucide-static` version here, download its `icons/` folder, and run

```bash
python3 -I tooling/scripts/lucide_to_vector.py <lucide-static>/icons core/designsystem/src/commonMain/composeResources/drawable
```
