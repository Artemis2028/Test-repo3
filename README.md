# Meridian

Offline-first MGRS land-navigation for Android. Independent reimplementation —
map, compass, pace count, and a grid that agrees with the sheet in your hand.

**Product:** Meridian  
**Package:** `com.artemis.mgrsnav`  
**License:** MIT

No accounts. No telemetry. No cloud sync. Everything stays on the device.

## Features (v1)

1. **Position** — MGRS 4–10 digits; Glance readout or compass Dial; fix quality
   in plain words (EXCELLENT → DEGRADED, plus STALE / NETWORK); precision capped
   by fix accuracy.
2. **Navigate** — azimuth, back azimuth, distance, ETA; haptic guide with stubs
   when no vibrator; arrival feedback.
3. **Map** — osmdroid map with MGRS grid overlay concept, waypoint markers,
   offline tile cache, and a simple ruler.
4. **Waypoints** — Room/SQLite storage, folders, GPX import/export.
5. **Field tools** — pace count, declination diagram (toy model), sun/moon times;
   resection/intersection as a stretch demo.

## Architecture

```
com.artemis.mgrsnav
├── domain/          Pure JVM field math (MGRS, geo, sun/moon, pace, declination)
├── data/            Room DB, GPX codec, repositories
├── ui/              Jetpack Compose screens (Position, Navigate, Map, Waypoints, Tools)
└── MeridianApp      Manual DI wiring
```

MGRS conversion uses [mil.nga:mgrs](https://github.com/ngageoint/mgrs-java) (MIT).

## Requirements

- **JDK 17+** (project targets Java 17 bytecode; JDK 21 works)
- **Android SDK** with `compileSdk 35`, platform tools, and a device/emulator for APK installs
- Android Gradle Plugin 8.7.x / Gradle 8.9+

Set `ANDROID_HOME` (or create `local.properties` with `sdk.dir=...`).

## Build

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest   # or: ./gradlew test
```

Unit tests cover MGRS round-trips, zone samples, angle wrap, haptic cues,
GPX import/export, pace math, twilight ordering, and declination conversion.

If the Android SDK is not installed, Gradle will still sync the JVM domain
sources; install command-line tools first:

```bash
# Example (Linux)
mkdir -p "$HOME/Android/Sdk/cmdline-tools"
# download commandlinetools-linux-*.zip from Google, unzip into cmdline-tools/latest
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

## Not a primary means of navigation

A phone GPS is an aid. Carry a map and a compass, know your pace count, and
confirm every grid against the ground.

## Third-party

| Component | License |
|-----------|---------|
| mil.nga MGRS / Grid | MIT |
| osmdroid | Apache 2.0 |
| Jetpack / Room / Compose | Apache 2.0 |
| OpenStreetMap tiles | ODbL © OSM contributors |

## License

MIT — see [LICENSE](LICENSE).
