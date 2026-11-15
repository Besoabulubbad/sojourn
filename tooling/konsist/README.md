# tooling:konsist

Architecture tests over the whole repository (ADR 0014): the module dependency contract, forbidden imports (Material outside `core:designsystem`, platform locale APIs outside `core:localization`, `NavKey` outside `core:navigation`, `KoinComponent` outside `app:shared`), and naming conventions. Runs in `check`; every rule written down in `docs/` gets a test here.

```bash
./gradlew :tooling:konsist:test
```
