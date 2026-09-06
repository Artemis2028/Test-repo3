import Foundation

/// Portable map descriptors — Swift stubs mirroring
/// `app/src/main/java/app/gridfix/android/map/MapPortable.kt`.
///
/// Slice 1 of the MapLibre rebuild: no MapLibre / MapKit imports here.
/// When Android `:core` is extracted (KMP), these names should stay aligned.

public struct BaseLayerDescriptor: Equatable, Sendable, Identifiable {
    public var id: String { key }
    public let key: String
    public let label: String
    public let attribution: String
    public let maxDownloadZoom: Int
    public let bulkDownload: Bool

    public init(
        key: String,
        label: String,
        attribution: String,
        maxDownloadZoom: Int,
        bulkDownload: Bool = false
    ) {
        self.key = key
        self.label = label
        self.attribution = attribution
        self.maxDownloadZoom = maxDownloadZoom
        self.bulkDownload = bulkDownload
    }
}

/// Calibrated image quad: photo-map / GeoTIFF share this.
public struct ImageQuad: Equatable, Sendable, Identifiable {
    public let id: String
    public let name: String
    /// 4 corners in order: TL, TR, BR, BL — (lat, lon).
    public let corners: [(lat: Double, lon: Double)]
    public let opacity: Float
    public let visible: Bool

    public init(
        id: String,
        name: String,
        corners: [(lat: Double, lon: Double)],
        opacity: Float = 1,
        visible: Bool = true
    ) {
        precondition(corners.count == 4, "ImageQuad needs exactly 4 corners")
        self.id = id
        self.name = name
        self.corners = corners
        self.opacity = opacity
        self.visible = visible
    }

    public static func == (lhs: ImageQuad, rhs: ImageQuad) -> Bool {
        lhs.id == rhs.id
            && lhs.name == rhs.name
            && lhs.opacity == rhs.opacity
            && lhs.visible == rhs.visible
            && zip(lhs.corners, rhs.corners).allSatisfy { $0.lat == $1.lat && $0.lon == $1.lon }
    }
}

public struct OfflinePack: Equatable, Sendable {
    public let layerKey: String
    public let latNorth: Double
    public let latSouth: Double
    public let lonWest: Double
    public let lonEast: Double
    public let minZoom: Int
    public let maxZoom: Int

    public init(
        layerKey: String,
        latNorth: Double,
        latSouth: Double,
        lonWest: Double,
        lonEast: Double,
        minZoom: Int,
        maxZoom: Int
    ) {
        self.layerKey = layerKey
        self.latNorth = latNorth
        self.latSouth = latSouth
        self.lonWest = lonWest
        self.lonEast = lonEast
        self.minZoom = minZoom
        self.maxZoom = maxZoom
    }
}

/// Minimal projection contract both map engines satisfy.
public protocol MapProjection {
    func toPixels(lat: Double, lon: Double) -> (x: Float, y: Float)
    func fromPixels(x: Float, y: Float) -> (lat: Double, lon: Double)
}

/// Placeholder catalog until MapSetup descriptors are shared via `:core`.
public enum MapPortableCatalog {
    public static let sampleLayers: [BaseLayerDescriptor] = [
        BaseLayerDescriptor(
            key: "osm",
            label: "OpenStreetMap",
            attribution: "© OpenStreetMap contributors",
            maxDownloadZoom: 17,
            bulkDownload: false
        ),
        BaseLayerDescriptor(
            key: "opentopo",
            label: "OpenTopoMap",
            attribution: "© OpenStreetMap, SRTM | OpenTopoMap",
            maxDownloadZoom: 15,
            bulkDownload: true
        ),
    ]

    public static func descriptor(for key: String) -> BaseLayerDescriptor {
        sampleLayers.first { $0.key == key } ?? sampleLayers[0]
    }
}
