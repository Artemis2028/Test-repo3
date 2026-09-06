import SwiftUI
import GridFixCore

struct MapPlaceholderView: View {
    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("MapLibre Native is the planned engine. Portable models (`BaseLayerDescriptor`, `ImageQuad`, `OfflinePack`) already live in GridFixCore.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                Section("Sample layers (stubs)") {
                    ForEach(MapPortableCatalog.sampleLayers) { layer in
                        VStack(alignment: .leading, spacing: 2) {
                            Text(layer.label).font(.headline)
                            Text(layer.key).font(.caption.monospaced())
                            Text(layer.attribution).font(.caption2).foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 2)
                    }
                }
            }
            .navigationTitle("Map")
        }
    }
}
