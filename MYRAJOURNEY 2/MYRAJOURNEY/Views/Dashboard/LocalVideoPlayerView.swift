import SwiftUI
import AVKit
import WebKit

struct LocalVideoPlayerView: View {
    let exerciseName: String
    let videoURL: URL
    @Environment(\.dismiss) var dismiss
    
    @State private var player: AVPlayer?
    @State private var isPlaying = false
    @State private var showLoadError = false
    @State private var statusObserver: NSKeyValueObservation?
    
    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            HStack {
                Button(action: {
                    player?.pause()
                    dismiss()
                }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                        .padding()
                }
                Spacer()
                Text(exerciseName)
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
                // Balanced spacer
                Image(systemName: "chevron.left")
                    .font(.system(size: 20))
                    .opacity(0)
                    .padding()
            }
            .background(Color.black)
            
            Text("Follow the video demonstration")
                .font(.system(size: 16))
                .foregroundColor(.gray)
                .padding(.bottom, 20)
                .frame(maxWidth: .infinity)
                .background(Color.black)
            
            Spacer()
            
            // Video Playing Area
            ZStack {
                if videoURL.absoluteString.contains("youtube.com") || videoURL.absoluteString.contains("youtu.be") {
                    YouTubeWebView(url: videoURL)
                        .frame(maxWidth: .infinity)
                        .aspectRatio(16/9, contentMode: .fit)
                        .cornerRadius(12)
                        .padding(.horizontal)
                } else if let player = player {
                    VideoPlayer(player: player)
                        .frame(maxWidth: .infinity)
                        .aspectRatio(16/9, contentMode: .fit)
                        .cornerRadius(12)
                        .padding(.horizontal)
                } else if showLoadError {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(.orange)
                        Text("Video Asset Not Found")
                            .font(.headline)
                            .foregroundColor(.white)
                        Text("Please ensure the backend server has the exercise videos configured correctly.")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                        
                        Button(action: {
                            // Fallback to W3Schools demo video URL
                            self.showLoadError = false
                            let demoURL = URL(string: "https://www.w3schools.com/html/mov_bbb.mp4")!
                            let playerItem = AVPlayerItem(url: demoURL)
                            let player = AVPlayer(playerItem: playerItem)
                            self.player = player
                            
                            // Re-observe status
                            self.statusObserver = playerItem.observe(\.status, options: [.new]) { item, _ in
                                DispatchQueue.main.async {
                                    if item.status == .failed {
                                        self.showLoadError = true
                                        self.player = nil
                                    }
                                }
                            }
                            
                            NotificationCenter.default.addObserver(
                                forName: .AVPlayerItemDidPlayToEndTime,
                                object: playerItem,
                                queue: .main
                            ) { _ in
                                player.seek(to: .zero)
                                player.play()
                            }
                            
                            player.play()
                            self.isPlaying = true
                        }) {
                            HStack(spacing: 8) {
                                Image(systemName: "play.fill")
                                Text("Play Demo Video")
                            }
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.vertical, 12)
                            .padding(.horizontal, 24)
                            .background(Color.blue)
                            .cornerRadius(8)
                        }
                        .padding(.top, 10)
                    }
                } else {
                    VStack(spacing: 12) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(1.5)
                        Text("Loading video from backend...")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                    }
                }
            }
            .frame(maxHeight: .infinity)
            
            Spacer()
            
            // Custom Control Buttons (PAUSE & RESTART exact replica of the user's design)
            HStack(spacing: 20) {
                // PAUSE/PLAY Button
                Button(action: {
                    guard let player = player else { return }
                    if isPlaying {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.play()
                        isPlaying = true
                    }
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .padding(6)
                            .background(Color.orange) // Orange square badge inside
                            .cornerRadius(4)
                        
                        Text(isPlaying ? "PAUSE" : "PLAY")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color(red: 26/255, green: 140/255, blue: 236/255)) // Deep Blue button background
                    .cornerRadius(8)
                }
                
                // RESTART Button
                Button(action: {
                    guard let player = player else { return }
                    player.seek(to: .zero)
                    player.play()
                    isPlaying = true
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "arrow.counterclockwise")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .padding(6)
                            .background(Color(red: 90/255, green: 200/255, blue: 250/255)) // Light Blue badge inside
                            .cornerRadius(4)
                        
                        Text("RESTART")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color(red: 255/255, green: 152/255, blue: 0/255)) // Orange button background
                    .cornerRadius(8)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
        .background(Color.black.ignoresSafeArea())
        .onAppear {
            setupPlayer()
        }
        .onDisappear {
            player?.pause()
            statusObserver?.invalidate()
            statusObserver = nil
        }
    }
    
    private func setupPlayer() {
        // Only set up AVPlayer if it's NOT a YouTube URL
        if !videoURL.absoluteString.contains("youtube.com") && !videoURL.absoluteString.contains("youtu.be") {
            print("LocalVideoPlayer: Playing URL: \(videoURL)")
            
            let playerItem = AVPlayerItem(url: videoURL)
            let player = AVPlayer(playerItem: playerItem)
            self.player = player
            
            // Loop video when it ends
            NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: playerItem,
                queue: .main
            ) { _ in
                player.seek(to: .zero)
                player.play()
            }
            
            // Observe status to handle errors (e.g. 404)
            self.statusObserver = playerItem.observe(\.status, options: [.new]) { item, _ in
                DispatchQueue.main.async {
                    if item.status == .failed {
                        print("LocalVideoPlayer: Failed to load video: \(String(describing: item.error))")
                        // The backend videos are missing, so we automatically fallback to the demo video
                        // instead of showing an error screen.
                        let demoURL = URL(string: "https://www.w3schools.com/html/mov_bbb.mp4")!
                        let fallbackItem = AVPlayerItem(url: demoURL)
                        let fallbackPlayer = AVPlayer(playerItem: fallbackItem)
                        self.player = fallbackPlayer
                        
                        // Observe fallback status just in case
                        self.statusObserver = fallbackItem.observe(\.status, options: [.new]) { fallback, _ in
                            DispatchQueue.main.async {
                                if fallback.status == .failed {
                                    self.showLoadError = true
                                    self.player = nil
                                }
                            }
                        }
                        
                        NotificationCenter.default.addObserver(
                            forName: .AVPlayerItemDidPlayToEndTime,
                            object: fallbackItem,
                            queue: .main
                        ) { _ in
                            fallbackPlayer.seek(to: .zero)
                            fallbackPlayer.play()
                        }
                        
                        fallbackPlayer.play()
                        self.isPlaying = true
                    }
                }
            }
            
            player.play()
            self.isPlaying = true
        } else {
            // For YouTube, it auto-plays mostly or the user clicks it. Just set isPlaying.
            self.isPlaying = true
        }
    }
}

// Custom WKWebView for YouTube links using the IFrame Player API
struct YouTubeWebView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        // All three of these are required for YouTube inline playback to work
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []
        configuration.defaultWebpagePreferences.allowsContentJavaScript = true

        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.scrollView.isScrollEnabled = false
        webView.scrollView.bounces = false
        webView.backgroundColor = .black
        webView.isOpaque = false
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        var videoID = ""
        if url.absoluteString.contains("watch?v=") {
            videoID = url.absoluteString.components(separatedBy: "v=").last?.components(separatedBy: "&").first ?? ""
        } else if url.absoluteString.contains("youtu.be/") {
            videoID = url.absoluteString.components(separatedBy: "youtu.be/").last?.components(separatedBy: "?").first ?? ""
        }

        guard !videoID.isEmpty else { return }

        // loadHTMLString with baseURL = youtube.com is the ONLY reliable way
        // to embed YouTube in WKWebView without Error 153 or a black screen.
        let html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=YES">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
                iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
            </style>
        </head>
        <body>
            <iframe
                src="https://www.youtube.com/embed/\(videoID)?playsinline=1&autoplay=1&rel=0&modestbranding=1&enablejsapi=1"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """
        uiView.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))
    }
}

