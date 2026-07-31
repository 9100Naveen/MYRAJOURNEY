import SwiftUI
import UniformTypeIdentifiers

struct AddReportView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var description = ""
    @State private var isSaving = false
    @State private var errorMessage: String?
    var patientId: String? = nil
    
    @State private var selectedFileData: Data?
    @State private var selectedFileName: String?
    @State private var selectedMimeType: String?
    @State private var showingFileImporter = false
    
    // Date & Time selection state
    @State private var selectedDate = Date()
    @State private var showDatePicker = false
    
    var onSaveComplete: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            // Header Space (matching the top purple bar in screenshot if needed)
            Color(red: 0.3, green: 0, blue: 0.7)
                .frame(height: 1)
                .ignoresSafeArea(edges: .top)
            
            ScrollView {
                VStack(spacing: 25) {
                    Spacer(minLength: 40)
                    
                    // Report Name Input
                    VStack(alignment: .leading, spacing: 8) {
                        TextField("Report Name", text: $title)
                            .padding()
                            .background(Color.gray.opacity(0.15))
                            .cornerRadius(4)
                            .overlay(
                                Rectangle()
                                    .frame(height: 2)
                                    .foregroundColor(.gray.opacity(0.3)),
                                alignment: .bottom
                            )
                    }
                    
                    // Date & Time Input
                    Button(action: { showDatePicker = true }) {
                        HStack {
                            Text(formattedDate)
                                .foregroundColor(.primary)
                            Spacer()
                            Image(systemName: "calendar")
                                .foregroundColor(.gray)
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.4), lineWidth: 1)
                        )
                    }
                    
                    // Upload Area
                    Button(action: { showingFileImporter = true }) {
                        VStack(spacing: 20) {
                            if let fileName = selectedFileName {
                                Image(systemName: "doc.fill")
                                    .font(.system(size: 80))
                                    .foregroundColor(.blue)
                                
                                Text(fileName)
                                    .font(.headline)
                                    .foregroundColor(.blue)
                                
                                Text("Tap to change file")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            } else {
                                Image(systemName: "cloud.upload")
                                    .font(.system(size: 80))
                                    .foregroundColor(.gray.opacity(0.5))
                                
                                Text("Drag & Drop file here or tap to select")
                                    .font(.system(size: 18))
                                    .foregroundColor(.black.opacity(0.8))
                                    .multilineTextAlignment(.center)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 50)
                        .background(Color.white)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(selectedFileName != nil ? Color.blue : Color.gray.opacity(0.4), 
                                        lineWidth: selectedFileName != nil ? 2 : 1)
                        )
                    }
                    .onDrop(of: [.fileURL, .item], isTargeted: nil) { providers in
                        if let provider = providers.first {
                            provider.loadItem(forTypeIdentifier: UTType.fileURL.identifier, options: nil) { item, error in
                                if let data = item as? Data, let url = URL(dataRepresentation: data, relativeTo: nil) {
                                    DispatchQueue.main.async {
                                        handleFileSelection(url: url)
                                    }
                                }
                            }
                            return true
                        }
                        return false
                    }
                    
                    // Submit Button
                    Button(action: saveReport) {
                        HStack {
                            if isSaving {
                                ProgressView().tint(.white)
                            } else {
                                Text("SUBMIT REPORT")
                                    .font(.system(size: 18, weight: .bold))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color(red: 0.38, green: 0, blue: 1)) // Purple/Blue from screenshot
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .shadow(color: .black.opacity(0.15), radius: 5, x: 0, y: 3)
                    }
                    .disabled(isSaving || title.isEmpty || selectedFileData == nil)
                    
                    if let error = errorMessage {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                            .padding(.top, 10)
                    }
                    
                    Spacer(minLength: 100)
                }
                .padding(25)
            }
        }
        .navigationBarHidden(true) // Matching the screenshot which seems to have a custom header or none
        .sheet(isPresented: $showDatePicker) {
            VStack(spacing: 20) {
                Text("Select Upload Date & Time")
                    .font(.headline)
                    .padding(.top)
                
                DatePicker("", selection: $selectedDate)
                    .datePickerStyle(WheelDatePickerStyle())
                    .labelsHidden()
                
                Button("Done") {
                    showDatePicker = false
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                .padding(.horizontal)
            }
            .presentationDetents([.medium])
        }
        .fileImporter(
            isPresented: $showingFileImporter,
            allowedContentTypes: [.pdf, .image, .item],
            allowsMultipleSelection: false
        ) { result in
            switch result {
            case .success(let urls):
                if let url = urls.first {
                    handleFileSelection(url: url)
                }
            case .failure(let error):
                errorMessage = "Error selecting file: \(error.localizedDescription)"
            }
        }
    }
    
    private var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: selectedDate)
    }
    
    private func handleFileSelection(url: URL) {
        // Start accessing the security-scoped resource.
        guard url.startAccessingSecurityScopedResource() else {
            errorMessage = "Permission denied to access file."
            return
        }
        defer { url.stopAccessingSecurityScopedResource() }
        
        do {
            selectedFileData = try Data(contentsOf: url)
            selectedFileName = url.lastPathComponent
            
            // Mime type detection
            if let type = UTType(filenameExtension: url.pathExtension) {
                selectedMimeType = type.preferredMIMEType ?? "application/octet-stream"
            } else {
                selectedMimeType = "application/octet-stream"
            }
        } catch {
            errorMessage = "Failed to read file: \(error.localizedDescription)"
        }
    }
    
    private func saveReport() {
        isSaving = true
        errorMessage = nil
        
        PatientService.shared.createReport(
            title: title,
            description: description,
            patientId: patientId,
            fileData: selectedFileData,
            fileName: selectedFileName,
            mimeType: selectedMimeType
        ) { result in
            DispatchQueue.main.async {
                isSaving = false
                switch result {
                case .success(let response):
                    if response.success {
                        onSaveComplete()
                        dismiss()
                    } else {
                        errorMessage = response.message ?? "Failed to save report"
                    }
                case .failure(let error):
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}

#Preview {
    AddReportView(onSaveComplete: {})
}
