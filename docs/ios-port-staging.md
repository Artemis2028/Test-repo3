# iOS port staging

This repository (`Artemis2028/Test-repo3`) holds a copy of `Artemis2028/gridfix`
(Android tip ~0.9.32) as the base for an iOS port.

- Leave the Android Gradle app alone for product/testing (`./gradlew assembleDebug`).
- Keep `gridfix` as the Android line; evolve portable code + iOS here.

## What’s in `ios/` (this pass)

| Path | Role |
|------|------|
| `ios/Package.swift` | SPM package **GridFix** → library `GridFixCore` |
| `ios/GridFixCore/Geodesy.swift` | Full Vincenty port of `coords/Geodesy.kt` |
| `ios/GridFixCore/GoldenVectors.swift` | Distance/bearing legs copied from `GoldenVectors.kt` |
| `ios/GridFixCore/MapPortable.swift` | Stubs aligned with `map/MapPortable.kt` |
| `ios/GridFixCore/Mgrs.swift` | Stub (+ TODO) for `Coordinates.mgrs` / `parseMgrs` |
| `ios/GridFixCoreTests/` | Golden geodesy tests (same tolerances as Android) |
| `ios/GridFixApp/` | SwiftUI shell: Position (live Geodesy), Navigate/Map placeholders |
| `ios/project.yml` | XcodeGen → `GridFixApp.xcodeproj`, bundle id `app.gridfix.ios` |
| `ios/README.md` | How to open/build on a Mac |

Example CI (not installed under `.github/workflows` — token lacks `workflow`
scope): [`ios-ci-workflow.example.yml`](ios-ci-workflow.example.yml).

## Ported vs stubbed

- **Fully ported:** `Geodesy.distanceAndBearing` / great-circle helpers; golden
  distance vectors + XCTest suite.
- **Stubbed:** MGRS format/parse; MapLibre UI; Navigate tab behaviour; full UTM /
  convergence / MGRS golden CSV (remain in Kotlin until MGRS lands).

## Next steps

1. **Mac verify** — `cd ios && swift test`; `xcodegen generate && open GridFixApp.xcodeproj`.
2. **MGRS / UTM** — port `Coordinates.kt` or share via a future KMP `:core`
   (preferred long-term so Android NGA + iOS Snyder stay on one generator).
3. **MapLibre Native** — wire Map tab using `BaseLayerDescriptor` / `OfflinePack`
   already stubbed in `GridFixCore`.
4. **KMP `:core` extraction** — move pure Kotlin (`Geodesy`, `MapPortable`,
   goldens) to `commonMain`; Swift becomes a thin consumer or stays a dual port
   locked by `kmp/gen_golden.py`.
5. **CI** — add `macos-latest` from `docs/ios-ci-workflow.example.yml` when a PAT
   with `workflow` scope can push `.github/workflows`.

## Portable Android starting points

- `app/src/main/java/app/gridfix/android/coords/Geodesy.kt`
- `app/src/main/java/app/gridfix/android/map/MapPortable.kt`
- `app/src/test/java/app/gridfix/android/GoldenVectors.kt` (+ `GoldenTest.kt`)
- `app/src/main/java/app/gridfix/android/coords/Coordinates.kt` (MGRS entry points)
