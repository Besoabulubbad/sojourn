# Local setup

What you need on a Mac to build, test and run Sojourn. Linux works for everything except iOS.

## Requirements

| Tool | Version | Why | Install |
|---|---|---|---|
| JDK | 21 (provisioned automatically) | Gradle daemon JVM via `gradle/gradle-daemon-jvm.properties` (Foojay toolchain resolver downloads Azul 21 on first run) | nothing to do; needs network once |
| Android Studio | current stable with AGP 9.5 support | IDE, emulator, SDK manager | [developer.android.com/studio](https://developer.android.com/studio) |
| Android SDK | platform 37, build-tools, emulator image (API 36/37, arm64) | compile and run | SDK Manager |
| Xcode | current stable, **full install** (not just Command Line Tools) | compile the iOS framework, run the iOS app | App Store, then `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer` and `sudo xcodebuild -license accept` |
| Homebrew | any | tool installs below | [brew.sh](https://brew.sh) |
| GitHub CLI | 2.x | PRs, checks, secrets from the terminal | `brew install gh` then `gh auth login` |
| Maestro | 2.x | run end-to-end flows locally | `curl -fsSL "https://get.maestro.mobile.dev" \| bash` |
| Python + pipx | 3.11+ | preview the docs site | `brew install pipx && pipx install mkdocs-material` |

`local.properties` (git-ignored) must contain `sdk.dir=/Users/<you>/Library/Android/sdk`.

## First build

```bash
git clone <repo-url> sojourn && cd sojourn
./gradlew help                      # resolves the JDK toolchain, warms caches
./gradlew :app:android:assembleDebug
./gradlew check                     # what CI runs on a PR
```

The first build downloads Kotlin/Native toolchains for iOS (~1 GB) when any iOS target is configured; subsequent builds are incremental and use the configuration cache.

## Run

- **Android:** open in Android Studio and run the `app.android` configuration, or `./gradlew :app:android:installDebug`.
- **iOS:** `open iosApp/iosApp.xcodeproj`, pick a simulator, Run. Xcode invokes Gradle to build `Shared.framework` via the build phase script.
- **Switch language / theme:** Profile tab inside the app; or set the device language and theme for the OS-level pairs.

## Tests

```bash
./gradlew allTests                          # unit tests, all modules, all targets available locally
./gradlew :tooling:konsist:test             # architecture rules
./gradlew verifyRoborazziDebug              # screenshot compare against committed baselines
./gradlew recordRoborazziDebug              # re-record (only after a reviewed visual change)
./gradlew :app:shared:iosSimulatorArm64Test # iOS unit tests (needs Xcode)
maestro test .maestro/                      # end-to-end on a running emulator/simulator
```

Screenshot diffs are written to `<module>/build/outputs/roborazzi/` as side-by-side images.

## Docs site

```bash
mkdocs serve   # http://127.0.0.1:8000, live reload
mkdocs build --strict
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `xcodebuild: error: tool 'xcodebuild' requires Xcode` | Full Xcode is missing or not selected; see Requirements |
| Kotlin/Native toolchain download fails | check network/proxy; retry — it resumes |
| Configuration cache problems after changing `build-logic` | `./gradlew --no-configuration-cache <task>` once, then report it |
| Screenshot test fails only on CI | fonts or Robolectric SDK mismatch; both are pinned — compare `@Config(sdk)` and the baseline's recorded SDK |
| Koin `NoDefinitionFoundException` | run `:app:shared:test` — `checkModules()` names the missing binding |

## AI tooling

This repository is built with coding agents in the loop (see [ADR 0013](../adr/0013-ai-agents-dev-loop.md)). Two things make that work locally:

**The agent contract.** [`CLAUDE.md`](https://github.com/Besoabulubbad/sojourn/blob/main/CLAUDE.md) at the root is read automatically by Claude Code and can be pointed at from any other agent. It is the rules; the guides in this folder are the playbooks.

**MCP servers** — `.mcp.json` at the root is project-scoped and contains no secrets:

| Server | Purpose | Auth |
|---|---|---|
| `context7` — `https://mcp.context7.com/mcp` | version-exact library documentation. Ask the agent to "use context7" for the versions in `gradle/libs.versions.toml` before using an unfamiliar API. | none required; an optional key raises rate limits (`CONTEXT7_API_KEY`) |
| `github` — `https://api.githubcopilot.com/mcp/` | issues, pull requests, checks | per developer: run `/mcp` in Claude Code and complete the GitHub OAuth flow |

Claude Code picks up `.mcp.json` on start and asks once to trust the project's servers. Other agents accept the same two endpoints in their own MCP configuration.

**Rule of thumb:** an agent that cannot cite the pinned version's documentation for an API it is about to use should fetch it first. "I remember this API" is not a source.
