# iOS port staging

This repository (`Artemis2028/Test-repo3`) currently holds a copy of `Artemis2028/gridfix` (Android tip ~0.9.32) as the base for an iOS port.

- Leave `gridfix` alone for Android testing.
- Portable starting points: `Geodesy.kt`, `MapPortable.kt`, golden/portable JVM tests.
- GitHub Actions workflow YAML was omitted from the import (token lacked `workflow` scope). Re-add CI from `gridfix` when a token/PAT with `workflow` is available.
