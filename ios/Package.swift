// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "GridFix",
    platforms: [
        .iOS(.v16),
        .macOS(.v13),
    ],
    products: [
        .library(name: "GridFixCore", targets: ["GridFixCore"]),
    ],
    targets: [
        .target(
            name: "GridFixCore",
            path: "GridFixCore"
        ),
        .testTarget(
            name: "GridFixCoreTests",
            dependencies: ["GridFixCore"],
            path: "GridFixCoreTests"
        ),
    ]
)
