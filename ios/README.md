# MGRS GPS / GridFix — iOS

SwiftUI + SPM scaffold for the iOS port of **MGRS GPS** (Android package
`app.gridfix.android`, tip ~0.9.32 in this repo). The Android Gradle app is
untouched; this tree only adds `ios/`.

| Product | Bundle ID |
|---------|-----------|
| MGRS GPS (display) | `app.gridfix.ios` |

## Layout

```
ios/
  Package.swift          # SPM library GridFixCore (+ unit tests)
  GridFixCore/           # Geodesy, golden vectors, MapPortable stubs, Mgrs stub
  GridFixCoreTests/      # Vincenty vs Android GoldenVectors DISTANCE_CSV
  GridFixApp/            # SwiftUI shell (Position / Navigate / Map tabs)
  project.yml            # XcodeGen → GridFixApp.xcodeproj
  README.md              # this file
```

## Requirements (Mac)

- macOS with Xcode 15+ (Swift 5.9, iOS 16 SDK)
- Optional: [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)

Linux CI cannot run `xcodebuild`; use a `macos-latest` workflow later
(see `docs/ios-ci-workflow.example.yml`).

## Open / build in Xcode

### Option A — XcodeGen (recommended)

```bash
cd ios
brew install xcodegen   # once
xcodegen generate
open GridFixApp.xcodeproj
```

Select the **GridFixApp** scheme → iPhone simulator → Run.

### Option B — Open the Swift package, then add an app target

```bash
cd ios
open Package.swift
```

Xcode opens `GridFixCore`. To run the UI:

1. File → New → Project → iOS App (SwiftUI), product name **MGRS GPS**,
   bundle id `app.gridfix.ios`.
2. File → Add Package Dependencies → Add Local… → select this `ios/` folder.
3. Link **GridFixCore** to the app target.
4. Replace the template sources with the files under `GridFixApp/` (or add that
   folder to the target).

### Run core tests (Mac)

```bash
cd ios
swift test
# or in Xcode: Product → Test on the GridFix package / GridFixCoreTests
```

## What is ported vs stubbed

| Area | Status |
|------|--------|
| `Geodesy.distanceAndBearing` (Vincenty WGS84) | **Fully ported** from `Geodesy.kt` |
| Golden distance/bearing vectors | **Copied exactly** from `GoldenVectors.kt` DISTANCE_CSV |
| Unit tests vs those vectors | **In** `GridFixCoreTests` |
| `MapPortable` models | **Stubbed** (`BaseLayerDescriptor`, `ImageQuad`, `OfflinePack`, `MapProjection`) |
| MGRS format / parse | **Stubbed** with TODO → `Coordinates.kt` / KMP `:core` |
| Navigate / Map UI | Placeholders |
| MapLibre Native | Not wired yet |

## Relation to Android / `:core`

Portable Kotlin already called out for a future shared module:

- `app/.../coords/Geodesy.kt` → `GridFixCore/Geodesy.swift` (done)
- `app/.../map/MapPortable.kt` → `GridFixCore/MapPortable.swift` (names aligned)
- `GoldenVectors.kt` / `GoldenTest` → `GoldenVectors.swift` + `GeodesyGoldenTests`

Next: extract a KMP `:core` (or keep dual ports in lockstep via the golden
generator), port MGRS/UTM, then MapLibre on both platforms. Staging notes:
[`docs/ios-port-staging.md`](../docs/ios-port-staging.md).

## Do not break Android

Do not move or rename Android sources from this pass. Keep building with
`./gradlew assembleDebug` / `testDebugUnitTest` at the repo root.
