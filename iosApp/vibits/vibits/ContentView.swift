//
//  ContentView.swift
//  vibits
//
//  Created by Dmitrii Belskii on 13.01.26.
//

import SwiftUI
import shared

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
