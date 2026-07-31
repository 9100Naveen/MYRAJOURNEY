import SwiftUI

struct ChatMessage: Identifiable {
    let id = UUID()
    let text: String
    let isUser: Bool
}

struct ChatView: View {
    @State private var messages: [ChatMessage] = [
        ChatMessage(text: "Hello! I'm your AI-powered RA health assistant. I use advanced medical knowledge to help you manage your rheumatoid arthritis. How can I help you today?", isUser: false)
    ]
    @State private var inputText: String = ""
    @State private var isTyping: Bool = false
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 12) {
                            ForEach(messages) { message in
                                ChatBubble(message: message)
                                    .id(message.id)
                            }
                            
                            if isTyping {
                                HStack {
                                    Text("AI is typing...")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                        .padding(.leading, 8)
                                    Spacer()
                                }
                                .padding(.horizontal)
                                .id("typing")
                            }
                        }
                        .padding(.vertical)
                    }
                    .onChange(of: messages.count) { _, _ in
                        withAnimation {
                            proxy.scrollTo(messages.last?.id, anchor: .bottom)
                        }
                    }
                    .onChange(of: isTyping) { _, newValue in
                        if newValue {
                            withAnimation {
                                proxy.scrollTo("typing", anchor: .bottom)
                            }
                        }
                    }
                }
                
                HStack {
                    TextField("Ask me about RA...", text: $inputText)
                        .padding(12)
                        .background(Color(.systemGray6))
                        .cornerRadius(20)
                    
                    Button(action: sendMessage) {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(.white)
                            .padding(12)
                            .background(inputText.isEmpty ? Color.gray : Color.purple)
                            .clipShape(Circle())
                    }
                    .disabled(inputText.isEmpty || isTyping)
                }
                .padding()
            }
            .navigationTitle("Health Assistant")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarItems(trailing: Button("Close") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
    
    private func sendMessage() {
        let userMessage = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !userMessage.isEmpty else { return }
        
        messages.append(ChatMessage(text: userMessage, isUser: true))
        inputText = ""
        isTyping = true
        
        // Emulate chatbot api call fallback
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            let response = generateFallbackResponse(for: userMessage.lowercased())
            messages.append(ChatMessage(text: response, isUser: false))
            isTyping = false
        }
    }
    
    private func generateFallbackResponse(for message: String) -> String {
        if message.contains("pain") || message.contains("hurt") {
            return "I understand you're experiencing pain. Here are some immediate steps:\n• Apply heat or cold therapy\n• Take prescribed pain medication\n• Rest the affected joints."
        } else if message.contains("medication") || message.contains("pill") {
            return "RA medications work best when taken consistently. If you missed a dose, take it when you remember if it's the same day. Do not double dose."
        } else if message.contains("fatigue") || message.contains("tired") {
            return "RA fatigue is real. Take short naps, pace your activities, and stay hydrated. Gentle exercise can also boost energy."
        } else if message.contains("flare") {
            return "RA flares need attention. Rest joints, apply ice to hot/swollen areas, and contact your rheumatologist if it lasts."
        } else if message.contains("hello") || message.contains("hi") {
            return "Hello! I'm here to help with your RA management. Ask me about pain relief, medications, flares, exercise, or diet."
        }
        return "I'm here to help with your RA management. Ask me specific questions about pain, medications, symptoms, or exercise."
    }
}

struct ChatBubble: View {
    let message: ChatMessage
    var body: some View {
        HStack {
            if message.isUser { Spacer() }
            
            Text(message.text)
                .padding(12)
                .background(message.isUser ? Color.purple : Color(.systemGray5))
                .foregroundColor(message.isUser ? .white : .primary)
                .cornerRadius(16)
                .frame(maxWidth: 280, alignment: message.isUser ? .trailing : .leading)
            
            if !message.isUser { Spacer() }
        }
        .padding(.horizontal)
    }
}
