//
//  MYRAJOURNEYApp.swift
//  MYRAJOURNEY
//
//  Created by M.PAVANKALYAN on 08/03/26.
//

import SwiftUI

@main
struct MYRAJOURNEYApp: App {
    @StateObject private var appState = AppState.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(appState.isDarkMode ? .dark : .light)
        }
    }
}
