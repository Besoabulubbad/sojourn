import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Koin first: App() resolves the locale controller and accessibility settings from it.
        MainViewControllerKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
