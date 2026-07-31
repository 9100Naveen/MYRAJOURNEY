import SwiftUI

struct PrescriptionDetailView: View {
    let medications: [Medication]
    var patientName: String? = nil
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Header with Clinic Logo
                headerSection
                
                // Patient Information
                patientSection
                
                // Prescription Content
                contentSection
                
                // Doctor's Signature & Validation
                footerSection
                
                // Verification QR (Decorative)
                qrSection
            }
            .background(Color.white)
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationTitle("Digital Prescription")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { /* Print/Export logic */ }) {
                    Image(systemName: "printer")
                }
            }
        }
    }
    
    private var headerSection: some View {
        VStack(spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("My RA Journey")
                        .font(.system(size: 24, weight: .black))
                        .foregroundColor(.blue)
                    Text("Specialized Rheumatology Clinic")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .kerning(1.2)
                }
                Spacer()
                ZStack {
                    Circle()
                        .fill(Color.blue.opacity(0.1))
                        .frame(width: 60, height: 60)
                    Image(systemName: "cross.case.fill")
                        .foregroundColor(.blue)
                        .font(.title)
                }
            }
            
            Rectangle()
                .fill(Color.blue)
                .frame(height: 4)
                .cornerRadius(2)
        }
        .padding(30)
    }
    
    private var patientSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("PATIENT NAME")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text(patientName ?? SessionManager.shared.userEmail?.components(separatedBy: "@").first?.capitalized ?? "Patient")
                        .font(.headline)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    Text("RX ID")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text("RX-\(String(format: "%06d", Int.random(in: 100000...999999)))")
                        .font(.system(.subheadline, design: .monospaced))
                }
            }
            
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("DATE")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text(Date().formatted(date: .long, time: .omitted))
                        .font(.subheadline)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    Text("AGE / GENDER")
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text("32Y / Male") // Placeholder
                        .font(.subheadline)
                }
            }
        }
        .padding(.horizontal, 30)
        .padding(.bottom, 30)
    }
    
    private var contentSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("℞ Prescribed Medications")
                .font(.title3)
                .fontWeight(.bold)
                .padding(.horizontal, 30)
            
            VStack(spacing: 0) {
                ForEach(medications) { med in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(med.name)
                                .font(.headline)
                            Spacer()
                            Text(med.dosage ?? "")
                                .font(.subheadline)
                                .foregroundColor(.blue)
                                .fontWeight(.bold)
                        }
                        
                        Text(med.frequency)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        if let ins = med.instructions {
                            Text(ins)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .padding(10)
                                .background(Color.gray.opacity(0.05))
                                .cornerRadius(8)
                        }
                    }
                    .padding(.vertical, 15)
                    .padding(.horizontal, 30)
                    
                    if med.id != medications.last?.id {
                        Divider().padding(.horizontal, 30)
                    }
                }
            }
        }
    }
    
    private var footerSection: some View {
        VStack(alignment: .trailing, spacing: 10) {
            Spacer(minLength: 40)
            
            if let doctorName = medications.first?.doctorName {
                VStack(alignment: .trailing, spacing: 4) {
                    Text("Digitally Signed by")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("Dr. \(doctorName)")
                        .font(.headline)
                        .italic()
                    Text(medications.first?.doctorSpecialization ?? "Rheumatologist")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                .padding(.trailing, 30)
            } else {
                VStack(alignment: .trailing, spacing: 4) {
                    Text("Authorized Signature")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Image(systemName: "signature")
                        .font(.largeTitle)
                        .foregroundColor(.blue)
                    Text("Medical Director")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                .padding(.trailing, 30)
            }
        }
        .frame(maxWidth: .infinity, alignment: .trailing)
    }
    
    private var qrSection: some View {
        VStack(spacing: 12) {
            Spacer(minLength: 40)
            Image(systemName: "qrcode")
                .font(.system(size: 80))
                .foregroundColor(.black.opacity(0.8))
            Text("Scan to verify authentic prescription")
                .font(.caption2)
                .foregroundColor(.secondary)
            Text("© 2026 My RA Journey. All Rights Reserved.")
                .font(.system(size: 8, weight: .light))
                .foregroundColor(.gray)
                .padding(.top, 20)
        }
        .padding(.bottom, 40)
    }
}
