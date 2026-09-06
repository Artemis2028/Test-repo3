import SwiftUI
import GridFixCore

/// Lat/lon inputs → Vincenty distance & bearing via `Geodesy` (ported from Android).
struct PositionView: View {
    @State private var fromLat = "24.4539"
    @State private var fromLon = "54.3773"
    @State private var toLat = "24.46293"
    @State private var toLon = "54.3773"

    private var result: Geodesy.Result? {
        guard let fla = Double(fromLat), let flo = Double(fromLon),
              let tla = Double(toLat), let tlo = Double(toLon) else { return nil }
        return Geodesy.distanceAndBearing(fromLat: fla, fromLon: flo, toLat: tla, toLon: tlo)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text("iOS port of MGRS GPS / GridFix. Geodesy is live; MGRS formatting is stubbed.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }

                Section("From (lat, lon)") {
                    TextField("Latitude", text: $fromLat)
                        .keyboardType(.numbersAndPunctuation)
                    TextField("Longitude", text: $fromLon)
                        .keyboardType(.numbersAndPunctuation)
                }

                Section("To (lat, lon)") {
                    TextField("Latitude", text: $toLat)
                        .keyboardType(.numbersAndPunctuation)
                    TextField("Longitude", text: $toLon)
                        .keyboardType(.numbersAndPunctuation)
                }

                Section("Geodesic (WGS84 Vincenty)") {
                    if let r = result {
                        LabeledContent("Distance") {
                            Text(String(format: "%.4f m", r.distanceMeters))
                                .font(.body.monospacedDigit())
                        }
                        LabeledContent("Bearing (true)") {
                            Text(String(format: "%.6f°", Geodesy.normalizeBearing(r.bearingDegrees)))
                                .font(.body.monospacedDigit())
                        }
                    } else {
                        Text("Enter valid numbers")
                            .foregroundStyle(.secondary)
                    }
                }

                Section("MGRS") {
                    Text(Mgrs.pendingMessage)
                        .font(.footnote)
                        .foregroundStyle(.orange)
                }
            }
            .navigationTitle("Position")
        }
    }
}

#Preview {
    PositionView()
}
