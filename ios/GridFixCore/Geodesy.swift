import Foundation

/// Distance and bearing on the WGS84 ellipsoid — Vincenty's inverse formula.
///
/// Port of `app/src/main/java/app/gridfix/android/coords/Geodesy.kt`.
/// Same algorithm as `CLLocation.distance(from:)` / Android `Location.distanceBetween`.
/// Pure Swift, no UIKit/MapKit — moves to a shared KMP `:core` later as the contract.
public enum Geodesy {

    private static let a = 6_378_137.0
    private static let f = 1.0 / 298.257223563
    private static let b = a * (1.0 - f)
    private static let meanRadius = 6_371_008.8

    public struct Result: Equatable, Sendable {
        /// Geodesic distance in metres.
        public let distanceMeters: Double
        /// Initial bearing in degrees from true north — **not** normalised to 0..<360
        /// (mirrors Kotlin `Geodesy.distanceAndBearing`; wrap in the readout).
        public let bearingDegrees: Double

        public init(distanceMeters: Double, bearingDegrees: Double) {
            self.distanceMeters = distanceMeters
            self.bearingDegrees = bearingDegrees
        }
    }

    /// Geodesic distance in metres and initial bearing in degrees from true north.
    public static func distanceAndBearing(
        fromLat: Double, fromLon: Double, toLat: Double, toLon: Double
    ) -> Result {
        if fromLat == toLat && fromLon == toLon {
            return Result(distanceMeters: 0.0, bearingDegrees: 0.0)
        }

        let l = (toLon - fromLon) * .pi / 180.0
        let u1 = atan((1.0 - f) * tan(fromLat * .pi / 180.0))
        let u2 = atan((1.0 - f) * tan(toLat * .pi / 180.0))
        let sinU1 = sin(u1), cosU1 = cos(u1)
        let sinU2 = sin(u2), cosU2 = cos(u2)

        var lambda = l
        var sinSigma = 0.0
        var cosSigma = 0.0
        var sigma = 0.0
        var cosSqAlpha = 0.0
        var cos2SigmaM = 0.0
        var converged = false

        for _ in 0..<200 {
            let sinLambda = sin(lambda)
            let cosLambda = cos(lambda)
            let p = cosU2 * sinLambda
            let q = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            sinSigma = sqrt(p * p + q * q)
            if sinSigma == 0.0 {
                return Result(distanceMeters: 0.0, bearingDegrees: 0.0)
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            let sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SigmaM =
                cosSqAlpha == 0.0 ? 0.0 : cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha
            let c = f / 16.0 * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha))
            let previous = lambda
            lambda = l + (1.0 - c) * f * sinAlpha *
                (sigma + c * sinSigma *
                    (cos2SigmaM + c * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)))
            if abs(lambda - previous) < 1e-12 {
                converged = true
                break
            }
        }

        if !converged {
            return Result(
                distanceMeters: greatCircleMeters(lat1: fromLat, lon1: fromLon, lat2: toLat, lon2: toLon),
                bearingDegrees: greatCircleBearing(lat1: fromLat, lon1: fromLon, lat2: toLat, lon2: toLon)
            )
        }

        let uSq = cosSqAlpha * (a * a - b * b) / (b * b)
        let bigA = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)))
        let bigB = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)))
        let deltaSigma = bigB * sinSigma * (
            cos2SigmaM + bigB / 4.0 * (
                cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM) -
                    bigB / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma) *
                    (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)
            )
        )
        let s = b * bigA * (sigma - deltaSigma)

        let sinLambda = sin(lambda)
        let cosLambda = cos(lambda)
        let alpha1 = atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
        return Result(distanceMeters: s, bearingDegrees: alpha1 * 180.0 / .pi)
    }

    /// Great-circle distance on a sphere of the WGS84 mean radius.
    public static func greatCircleMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double) -> Double {
        let p1 = lat1 * .pi / 180.0
        let p2 = lat2 * .pi / 180.0
        let dp = p2 - p1
        let dl = (lon2 - lon1) * .pi / 180.0
        let h = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
        return 2.0 * meanRadius * atan2(sqrt(h), sqrt(max(1.0 - h, 0.0)))
    }

    public static func greatCircleBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double) -> Double {
        let p1 = lat1 * .pi / 180.0
        let p2 = lat2 * .pi / 180.0
        let dl = (lon2 - lon1) * .pi / 180.0
        let y = sin(dl) * cos(p2)
        let x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (atan2(y, x) * 180.0 / .pi + 360.0).truncatingRemainder(dividingBy: 360.0)
    }

    /// Wrap a bearing into `[0, 360)`.
    public static func normalizeBearing(_ degrees: Double) -> Double {
        var b = degrees.truncatingRemainder(dividingBy: 360.0)
        if b < 0 { b += 360.0 }
        return b
    }
}
