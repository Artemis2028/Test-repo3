import Foundation

/// MGRS formatting entry points — **stub for first iOS pass**.
///
/// Android uses NGA's MGRS library via `Coordinates.mgrs` / `parseMgrs` in
/// `coords/Coordinates.kt` (~400 lines + UTM Snyder series). Full port (or
/// KMP `:core` extraction) is the next milestone; until then call sites should
/// show a clear "MGRS pending" state rather than inventing a grid.
public enum Mgrs {
    public struct Parts: Equatable, Sendable {
        public let gzd: String
        public let square: String
        public let easting: String
        public let northing: String
        public let full: String

        public init(gzd: String, square: String, easting: String, northing: String, full: String) {
            self.gzd = gzd
            self.square = square
            self.easting = easting
            self.northing = northing
            self.full = full
        }
    }

    /// TODO: Port `Coordinates.mgrs` (NGA / Snyder) or share via KMP `:core`.
    public static func format(lat: Double, lon: Double, digits: Int = 8) -> Parts? {
        _ = (lat, lon, digits)
        return nil
    }

    /// TODO: Port `Coordinates.parseMgrs` / `parseMgrsCorner`.
    public static func parse(_ text: String) -> (lat: Double, lon: Double)? {
        _ = text
        return nil
    }

    public static let pendingMessage = "MGRS formatting not yet ported — see Coordinates.kt / KMP :core plan"
}
