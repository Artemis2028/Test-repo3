import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            PositionView()
                .tabItem { Label("Position", systemImage: "location.circle") }
            NavigatePlaceholderView()
                .tabItem { Label("Navigate", systemImage: "safari") }
            MapPlaceholderView()
                .tabItem { Label("Map", systemImage: "map") }
        }
    }
}

#Preview {
    ContentView()
}
