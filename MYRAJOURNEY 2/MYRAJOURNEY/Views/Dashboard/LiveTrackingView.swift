import SwiftUI
import Combine

struct LiveTrackingView: View {
    @State private var timeRemaining = 300 // 5 minutes
    @State private var timerActive = false
    @State private var setsCompleted = 1
    @State private var cameraAuthorized = true
    
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    var body: some View {
        ZStack {
            // Camera Preview Placeholder (Light Gray for White Theme)
            Color.gray.opacity(0.1).ignoresSafeArea()
            
            VStack {
                Text("Camera Active - Tracking Movements")
                    .font(.caption)
                    .foregroundColor(.green)
                    .padding(8)
                    .background(Color.green.opacity(0.1))
                    .cornerRadius(5)
                    .padding(.top)
                
                Spacer()
                
                // Overlay View
                VStack(spacing: 20) {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("Current Exercise")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("Hand Stretching")
                                .font(.title2)
                                .bold()
                                .foregroundColor(.primary)
                        }
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("Time Remaining")
                                .font(.caption)
                            Text(formatTime(timeRemaining))
                                .font(.title2)
                                .bold()
                                .monospacedDigit()
                        }
                        .foregroundColor(.primary)
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(15)
                    .shadow(color: .black.opacity(0.1), radius: 10, x: 0, y: 5)
                    
                    HStack(spacing: 20) {
                        StatOverlay(title: "Sets", value: "\(setsCompleted)/3")
                        StatOverlay(title: "Accuracy", value: "94%")
                    }
                }
                .padding()
                
                // Controls
                HStack(spacing: 30) {
                    Button(action: { timerActive.toggle() }) {
                        Image(systemName: timerActive ? "pause.fill" : "play.fill")
                            .font(.title)
                            .padding(25)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .clipShape(Circle())
                            .shadow(radius: 5)
                    }
                    
                    Button(action: { /* Finish session */ }) {
                        Text("Finish")
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding(.horizontal, 40)
                            .padding(.vertical, 20)
                            .background(Color.blue)
                            .cornerRadius(30)
                    }
                }
                .padding(.bottom, 40)
            }
        }
        .navigationBarHidden(true)
        .onReceive(timer) { _ in
            if timerActive && timeRemaining > 0 {
                timeRemaining -= 1
            }
        }
    }
    
    private func formatTime(_ seconds: Int) -> String {
        let mins = seconds / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }
}

struct StatOverlay: View {
    let title: String
    let value: String
    
    var body: some View {
        VStack {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.headline)
                .foregroundColor(.primary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 10, x: 0, y: 5)
    }
}

#Preview {
    LiveTrackingView()
}
