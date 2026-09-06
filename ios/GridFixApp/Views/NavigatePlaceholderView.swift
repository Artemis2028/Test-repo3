import SwiftUI

struct NavigatePlaceholderView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                Image(systemName: "safari")
                    .font(.system(size: 48))
                    .foregroundStyle(.secondary)
                Text("Navigate")
                    .font(.title2.bold())
                Text("Waypoint azimuth, distance, haptic guide — ported after Geodesy + MGRS land in GridFixCore.")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle("Navigate")
        }
    }
}
