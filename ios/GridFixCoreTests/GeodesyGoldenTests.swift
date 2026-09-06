import XCTest
@testable import GridFixCore

/// Mirrors Android `GoldenTest` geodesy cases against the same DISTANCE_CSV numbers.
final class GeodesyGoldenTests: XCTestCase {

    func testFixtureLoaded() {
        XCTAssertGreaterThan(GoldenVectors.distance.count, 10, "no distance vectors")
    }

    func testDistanceMatchesSharedVectors() {
        for v in GoldenVectors.distance {
            let d = Geodesy.distanceAndBearing(
                fromLat: v.fromLat, fromLon: v.fromLon,
                toLat: v.toLat, toLon: v.toLon
            )
            XCTAssertEqual(
                d.distanceMeters, v.meters,
                accuracy: GoldenVectors.tolDistanceMeters,
                "distance for \(v.name)"
            )
        }
    }

    func testBearingMatchesSharedVectors() {
        for v in GoldenVectors.distance {
            if v.meters <= 1.0 { continue }
            let d = Geodesy.distanceAndBearing(
                fromLat: v.fromLat, fromLon: v.fromLon,
                toLat: v.toLat, toLon: v.toLon
            )
            let got = Geodesy.normalizeBearing(d.bearingDegrees)
            var delta = abs(got - v.bearing).truncatingRemainder(dividingBy: 360.0)
            if delta > 180.0 { delta = 360.0 - delta }
            XCTAssertLessThan(
                delta, GoldenVectors.tolBearingDegrees,
                "bearing for \(v.name): got \(got), want \(v.bearing)"
            )
        }
    }

    func testDistanceIsSymmetric() {
        for v in GoldenVectors.distance {
            let there = Geodesy.distanceAndBearing(
                fromLat: v.fromLat, fromLon: v.fromLon,
                toLat: v.toLat, toLon: v.toLon
            ).distanceMeters
            let back = Geodesy.distanceAndBearing(
                fromLat: v.toLat, fromLon: v.toLon,
                toLat: v.fromLat, toLon: v.fromLon
            ).distanceMeters
            XCTAssertEqual(there, back, accuracy: 0.001, "distance for \(v.name) is not symmetric")
        }
    }

    func testZeroLengthLeg() {
        let d = Geodesy.distanceAndBearing(
            fromLat: 24.4539, fromLon: 54.3773,
            toLat: 24.4539, toLon: 54.3773
        )
        XCTAssertEqual(d.distanceMeters, 0.0, accuracy: 1e-9)
        XCTAssertFalse(d.bearingDegrees.isNaN)
    }

    func testGreatCircleShortcutIsCloseButNotExact() {
        var worst = 0.0
        for v in GoldenVectors.distance {
            if v.meters <= 1000.0 { continue }
            let gc = Geodesy.greatCircleMeters(
                lat1: v.fromLat, lon1: v.fromLon,
                lat2: v.toLat, lon2: v.toLon
            )
            worst = max(worst, abs(gc - v.meters) / v.meters)
        }
        XCTAssertGreaterThan(worst, 1e-4, "great circle should differ from the ellipsoid at all")
        XCTAssertLessThan(worst, 0.008, "great circle drifted further than expected: \(worst)")
    }

    func testNormalizeBearing() {
        XCTAssertEqual(Geodesy.normalizeBearing(0), 0, accuracy: 1e-12)
        XCTAssertEqual(Geodesy.normalizeBearing(360), 0, accuracy: 1e-12)
        XCTAssertEqual(Geodesy.normalizeBearing(-90), 270, accuracy: 1e-12)
        XCTAssertEqual(Geodesy.normalizeBearing(450), 90, accuracy: 1e-12)
    }
}
