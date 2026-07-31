// Mock Data & LocalStorage State Helper for MyRA Journey Portal

const INITIAL_PATIENTS = [
  { id: 1, name: "Grishma Patel", email: "grishma@example.com", age: 34, gender: "Female", role: "PATIENT" },
  { id: 2, name: "Test User", email: "testuser@example.com", age: 45, gender: "Male", role: "PATIENT" },
  { id: 3, name: "Sarah Connor", email: "sarah@example.com", age: 29, gender: "Female", role: "PATIENT" },
  { id: 4, name: "David Miller", email: "david@example.com", age: 52, gender: "Male", role: "PATIENT" }
];

const INITIAL_DOCTORS = [
  { id: 101, name: "Dr. Alok Sharma", email: "alok@example.com", role: "DOCTOR" },
  { id: 102, name: "Dr. Amanda Ross", email: "amanda@example.com", role: "DOCTOR" }
];

const INITIAL_ASSIGNMENTS = [
  { patientId: 1, doctorId: 101 },
  { patientId: 2, doctorId: 101 },
  { patientId: 3, doctorId: 102 },
  { patientId: 4, doctorId: 102 }
];

const INITIAL_MEDICATIONS = [
  { id: 1, patientId: 1, name: "Methotrexate", dosage: "15mg", frequency: "Once weekly", instructions: "Take with food on Saturday mornings", adherence: ["2026-05-23"] },
  { id: 2, patientId: 1, name: "Folic Acid", dosage: "5mg", frequency: "Once daily", instructions: "Take every day except Saturday", adherence: ["2026-05-24", "2026-05-25", "2026-05-26"] },
  { id: 3, patientId: 2, name: "Adalimumab (Humira)", dosage: "40mg", frequency: "Every 2 weeks", instructions: "Subcutaneous injection, alternate thighs", adherence: ["2026-05-15"] },
  { id: 4, patientId: 3, name: "Prednisone", dosage: "5mg", frequency: "Once daily", instructions: "Take in the morning to prevent insomnia", adherence: ["2026-05-25", "2026-05-26"] }
];

const INITIAL_EXERCISES = [
  { id: 1, patientId: 1, name: "Gentle Wrist Extensions", description: "Hold your arm straight out, fingers down. Use the other hand to pull back gently.", duration: 300, videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4", assignedAt: "2026-05-20", completedSessions: 5 },
  { id: 2, patientId: 1, name: "Ankle Pumps & Rotations", description: "Point toes away, then pull toes back. Rotate ankle in circles in both directions.", duration: 420, videoUrl: "https://www.w3schools.com/html/movie.mp4", assignedAt: "2026-05-21", completedSessions: 3 },
  { id: 3, patientId: 2, name: "Finger Flexion & Extensions", description: "Make a tight fist, then open hands wide and stretch fingers outward.", duration: 240, videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4", assignedAt: "2026-05-22", completedSessions: 1 },
  { id: 4, patientId: 3, name: "Knee Extensions (Seated)", description: "Sit straight, slowly extend knee to raise leg. Hold for 3 seconds, lower slowly.", duration: 360, videoUrl: "https://www.w3schools.com/html/movie.mp4", assignedAt: "2026-05-22", completedSessions: 2 }
];

const INITIAL_SYMPTOMS = [
  { id: 1, patientId: 1, painLevel: 6, stiffnessLevel: 7, fatigueLevel: 5, notes: "Morning stiffness lasted about 45 minutes in hands.", createdAt: "2026-05-24" },
  { id: 2, patientId: 1, painLevel: 5, stiffnessLevel: 5, fatigueLevel: 6, notes: "Felt better in the afternoon after warm compress.", createdAt: "2026-05-25" },
  { id: 3, patientId: 1, painLevel: 3, stiffnessLevel: 4, fatigueLevel: 4, notes: "Exercised today. Fingers feel more mobile.", createdAt: "2026-05-26" },
  { id: 4, patientId: 2, painLevel: 7, stiffnessLevel: 8, fatigueLevel: 8, notes: "Severe fatigue today, struggled with holding objects.", createdAt: "2026-05-25" },
  { id: 5, patientId: 2, painLevel: 5, stiffnessLevel: 6, fatigueLevel: 5, notes: "Stiffness reduced slightly after taking Humira.", createdAt: "2026-05-26" }
];

const INITIAL_APPOINTMENTS = [
  { id: 1, patientId: 1, doctorName: "Dr. Alok Sharma", displayTitle: "Routine Rheumatology Review", displayDate: "June 05, 2026", displayTimeSlot: "10:30 AM - 11:00 AM", status: "SCHEDULED" },
  { id: 2, patientId: 2, doctorName: "Dr. Alok Sharma", displayTitle: "Biologics Adherence Discussion", displayDate: "June 08, 2026", displayTimeSlot: "02:00 PM - 02:30 PM", status: "SCHEDULED" },
  { id: 3, patientId: 3, doctorName: "Dr. Amanda Ross", displayTitle: "Medication Adjustment Follow-up", displayDate: "June 12, 2026", displayTimeSlot: "09:00 AM - 09:30 AM", status: "SCHEDULED" }
];

const INITIAL_REPORTS = [
  { id: 1, patientId: 1, title: "May Complete Blood Count (CBC)", fileName: "cbc_may_2026.pdf", fileUrl: "#", status: "Reviewed", createdAt: "2026-05-18" },
  { id: 2, patientId: 1, title: "C-Reactive Protein (CRP) Assay", fileName: "crp_may_2026.pdf", fileUrl: "#", status: "Reviewed", createdAt: "2026-05-20" },
  { id: 3, patientId: 2, title: "Rheumatoid Factor & Anti-CCP Test", fileName: "rf_anticcp_report.pdf", fileUrl: "#", status: "Pending Review", createdAt: "2026-05-25" }
];

// Initialize storage helper
export const initStorage = () => {
  if (!localStorage.getItem("myra_patients")) {
    localStorage.setItem("myra_patients", JSON.stringify(INITIAL_PATIENTS));
    localStorage.setItem("myra_doctors", JSON.stringify(INITIAL_DOCTORS));
    localStorage.setItem("myra_assignments", JSON.stringify(INITIAL_ASSIGNMENTS));
    localStorage.setItem("myra_medications", JSON.stringify(INITIAL_MEDICATIONS));
    localStorage.setItem("myra_exercises", JSON.stringify(INITIAL_EXERCISES));
    localStorage.setItem("myra_symptoms", JSON.stringify(INITIAL_SYMPTOMS));
    localStorage.setItem("myra_appointments", JSON.stringify(INITIAL_APPOINTMENTS));
    localStorage.setItem("myra_reports", JSON.stringify(INITIAL_REPORTS));
  }
};

// Data retrieval wrappers
export const getPatients = () => JSON.parse(localStorage.getItem("myra_patients") || "[]");
export const savePatients = (data) => localStorage.setItem("myra_patients", JSON.stringify(data));

export const getDoctors = () => JSON.parse(localStorage.getItem("myra_doctors") || "[]");
export const saveDoctors = (data) => localStorage.setItem("myra_doctors", JSON.stringify(data));

export const getAssignments = () => JSON.parse(localStorage.getItem("myra_assignments") || "[]");
export const saveAssignments = (data) => localStorage.setItem("myra_assignments", JSON.stringify(data));

export const getMedications = () => JSON.parse(localStorage.getItem("myra_medications") || "[]");
export const saveMedications = (data) => localStorage.setItem("myra_medications", JSON.stringify(data));

export const getExercises = () => JSON.parse(localStorage.getItem("myra_exercises") || "[]");
export const saveExercises = (data) => localStorage.setItem("myra_exercises", JSON.stringify(data));

export const getSymptoms = () => JSON.parse(localStorage.getItem("myra_symptoms") || "[]");
export const saveSymptoms = (data) => localStorage.setItem("myra_symptoms", JSON.stringify(data));

export const getAppointments = () => JSON.parse(localStorage.getItem("myra_appointments") || "[]");
export const saveAppointments = (data) => localStorage.setItem("myra_appointments", JSON.stringify(data));

export const getReports = () => JSON.parse(localStorage.getItem("myra_reports") || "[]");
export const saveReports = (data) => localStorage.setItem("myra_reports", JSON.stringify(data));
