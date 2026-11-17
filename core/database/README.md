# core:database

The source of truth (ADR 0007): the SQLDelight schema `SojournDatabase`, one `.sq` file per table group under `src/commonMain/sqldelight`.

| Tables | Holds |
|---|---|
| `city` | The catalogue's cities, stay counts and monthly "from" prices. |
| `stay` + `photo`, `stay_amenity`, `policy`, `blocked_range` | Listings and their children; a refresh clears a stay's children (`clearChildren`) and writes them again. Child rows cascade with their stay. |
| `quote` + `quote_line` | Issued quotes, so every surface shows the same total for a quote id (P-02); the lines sum to the total (P-05). |
| `booking` | The guest's bookings. |
| `saved` | Saved stays: local, kept across refreshes and offline. |
| `search_recent` | The newest few searches (`trimTo`). |

Columns are primitive (text, integer, real); `core:data` maps rows to the model. `DatabaseDriverFactory` opens the platform driver with foreign keys on; `databaseModule` provides it and the database to Koin (on Android it needs the application `Context`).

## Tests

`SojournDatabaseTest` runs on an in-memory SQLite on both platforms (JDBC on the JVM host, the native driver on iOS): round-trips, child replacement, city filtering, quote-line cascade, saved, recent-search trimming.

**May depend on:** SQLDelight, kotlinx-coroutines, Koin.
