package com.example.myrajourney.core.network;

import com.example.myrajourney.data.model.ActiveRequest;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.AppointmentRequest;
import com.example.myrajourney.data.model.AuthRequest;
import com.example.myrajourney.data.model.AuthResponse;
import com.example.myrajourney.data.model.CreateUserRequest;
import com.example.myrajourney.data.model.Doctor;
import com.example.myrajourney.data.model.DoctorOverview;
import com.example.myrajourney.data.model.EducationArticle;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
import com.example.myrajourney.data.model.MedicationRequest;
import com.example.myrajourney.data.model.Notification;
import com.example.myrajourney.data.model.PatientOverview;
import com.example.myrajourney.data.model.RehabPlan;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.data.model.ReportNote;
import com.example.myrajourney.data.model.CrpData;
import com.example.myrajourney.data.model.Settings;
import com.example.myrajourney.data.model.SettingsRequest;
import com.example.myrajourney.data.model.Symptom;
import com.example.myrajourney.data.model.SymptomRequest;
import com.example.myrajourney.data.model.User;
import com.example.myrajourney.data.model.RemoveMedicationRequest;
import com.example.myrajourney.data.model.GenericResponse;
import com.example.myrajourney.data.model.UserResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

        // --- Auth ---
        @POST("auth/register")
        Call<ApiResponse<AuthResponse>> register(@Body AuthRequest request);

        @POST("auth/login")
        Call<ApiResponse<AuthResponse>> login(@Body AuthRequest request);

        // ✔ UPDATED — now accepts { "email": "..." }
        @POST("auth/forgot-password")
        Call<ApiResponse<com.example.myrajourney.auth.ForgotPasswordActivity.ForgotPasswordResponse>> forgotPassword(
                        @Body com.example.myrajourney.auth.ForgotPasswordActivity.ForgotPasswordRequest request);

        // ✔ UPDATED — minimal reset: { "email": "...", "password": "..." }
        @POST("auth/reset-password")
        Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> request);

        @POST("auth/change-password")
        Call<ApiResponse<Void>> changePassword(@Body Map<String, String> request);

        @GET("auth/me")
        Call<ApiResponse<UserResponse>> getCurrentUser();

        @PUT("users/me")
        Call<ApiResponse<User>> updateProfile(@Body Map<String, String> fields);

        // --- Education ---
        @GET("education/articles")
        Call<ApiResponse<List<EducationArticle>>> getEducationArticles();

        @GET("education/articles/{slug}")
        Call<ApiResponse<EducationArticle>> getEducationArticle(@Path("slug") String slug);

        // --- Patient ---
        @GET("patients/me/overview")
        Call<ApiResponse<PatientOverview>> getPatientOverview();

        @GET("patients")
        Call<ApiResponse<List<User>>> getAllPatients();

        // --- Doctor ---
        @GET("doctor/overview")
        Call<ApiResponse<DoctorOverview>> getDoctorOverview();

        @GET("admin/doctors")
        Call<ApiResponse<List<Doctor>>> getAllDoctors();

        @GET("users/doctors")
        Call<ApiResponse<List<User>>> getDoctors();

        // --- Appointments ---
        @GET("appointments")
        Call<ApiResponse<List<Appointment>>> getAppointments();

        @GET("appointments")
        Call<ApiResponse<List<Appointment>>> getAppointments(
                        @Query("patient_id") Integer patientId,
                        @Query("doctor_id") Integer doctorId);

        @GET("appointments")
        Call<ApiResponse<List<Appointment>>> getPatientAppointments(
                        @Query("patient_id") int patientId);

        @GET("appointments")
        Call<ApiResponse<List<Appointment>>> getDoctorAppointments(
                        @Query("doctor_id") int doctorId);

        @POST("appointments")
        Call<ApiResponse<Appointment>> createAppointment(@Body AppointmentRequest request);

        @GET("appointments/{id}")
        Call<ApiResponse<Appointment>> getAppointment(@Path("id") String id);

        // --- Medication ---
        @GET("patient-medications")
        Call<ApiResponse<List<Medication>>> getPatientMedications();

        @GET("patient-medications")
        Call<ApiResponse<List<Medication>>> getPatientMedications(
                        @Query("patient_id") int patientId);

        @GET("patient-medications")
        Call<ApiResponse<List<Map<String, Object>>>> getPatientMedicationsRaw(
                        @Query("patient_id") int patientId);

        @POST("patient-medications")
        Call<ApiResponse<Map<String, Object>>> doctorAssignMedication(@Body Map<String, Object> request);

        @PATCH("patient-medications/{id}")
        Call<ApiResponse<Void>> setMedicationActive(@Path("id") String id, @Body ActiveRequest request);

        @GET("medications")
        Call<ApiResponse<List<Medication>>> searchMedications(@Query("q") String query);

        @POST("api/remove_medication.php")
        Call<GenericResponse> removeMedication(@Body RemoveMedicationRequest request);

        @POST("medications/log")
        Call<ApiResponse<MedicationLog>> logMedicationIntake(@Body MedicationLogRequest request);

        @DELETE("patient-medications/{id}")
        Call<ApiResponse<Void>> deleteMedication(@Path("id") String id);

        // --- Admin Medication Management ---
        @DELETE("admin/patient-medications/clear-all")
        Call<ApiResponse<Void>> clearAllPatientMedications();

        @GET("admin/patient-medications/all")
        Call<ApiResponse<List<Map<String, Object>>>> getAllPatientMedications();

        // --- Reports ---
        @GET("reports")
        Call<ApiResponse<List<Report>>> getReports();

        @POST("reports")
        @Multipart
        Call<ApiResponse<Report>> createReport(
                        @Part("patient_id") RequestBody patientId,
                        @Part("title") RequestBody title,
                        @Part("description") RequestBody description,
                        @Part MultipartBody.Part file);

        @POST("reports/notes")
        Call<ApiResponse<ReportNote>> createReportNote(@Body Map<String, Object> request);

        @GET("reports/{id}/notes")
        Call<ApiResponse<List<ReportNote>>> getReportNotes(@Path("id") String reportId);

        @GET("reports/{id}")
        Call<ApiResponse<Report>> getReport(@Path("id") String id);

        // status
        @POST("reports/status")
        Call<ApiResponse<Object>> updateReportStatus(@Body Map<String, Object> request);

        // --- CRP ---
        @GET("crp/history/{patient_id}")
        Call<ApiResponse<List<CrpData>>> getCrpHistory(@Path("patient_id") int patientId);

        // --- Notifications ---
        @GET("notifications")
        Call<ApiResponse<List<Notification>>> getNotifications(
                        @Query("page") Integer page,
                        @Query("limit") Integer limit,
                        @Query("unread") Boolean unread);

        @POST("notifications/{id}/read")
        Call<ApiResponse<Void>> markNotificationRead(@Path("id") String id);

        // --- Symptoms ---
        @GET("symptoms")
        Call<ApiResponse<List<Symptom>>> getSymptoms();

        @GET("symptoms")
        Call<ApiResponse<List<com.example.myrajourney.data.model.SymptomHistory>>> getSymptomHistory(@Query("patient_id") int patientId);

        @POST("symptoms")
        Call<ApiResponse<Symptom>> createSymptom(@Body SymptomRequest request);

        // --- Settings ---
        @GET("settings")
        Call<ApiResponse<Settings>> getSettings();

        @PUT("settings")
        Call<ApiResponse<Settings>> updateSettings(@Body SettingsRequest request);

        // --- Rehab ---
        @POST("rehab-plans")
        Call<ApiResponse<Map<String, Object>>> createRehabPlan(@Body Map<String, Object> request);

        @GET("rehab-plans")
        Call<ApiResponse<List<RehabPlan>>> getRehabPlans(@Query("patient_id") Integer patientId);

        @DELETE("rehab-exercises/{id}")
        Call<ApiResponse<Void>> deleteRehabExercise(@Path("id") int exerciseId);

        // --- Exercise Assignments ---
        @POST("exercise-assignments")
        Call<ApiResponse<Map<String, Object>>> createExerciseAssignment(@Body Map<String, Object> request);

        @GET("exercise-assignments/patient")
        Call<ApiResponse<List<Map<String, Object>>>> getPatientExerciseAssignments(
                        @Query("patient_id") String patientId);

        @GET("exercise-assignments/doctor")
        Call<ApiResponse<List<Map<String, Object>>>> getDoctorExerciseAssignments(@Query("doctor_id") String doctorId);

        @PUT("exercise-assignments/{id}")
        Call<ApiResponse<Map<String, Object>>> updateExerciseAssignment(@Path("id") String assignmentId,
                        @Body Map<String, Object> request);

        @DELETE("exercise-assignments/{id}")
        Call<ApiResponse<Void>> deleteExerciseAssignment(@Path("id") String assignmentId);

        // --- Exercise Reports ---
        @GET("exercise-reports/patient/{patientId}")
        Call<ApiResponse<List<Map<String, Object>>>> getPatientExerciseReports(@Path("patientId") String patientId,
                        @Query("start_date") String startDate, @Query("end_date") String endDate,
                        @Query("doctor_id") String doctorId, @Query("exercise_id") String exerciseId);

        @POST("exercise-sessions/{sessionId}/report")
        Call<ApiResponse<Map<String, Object>>> saveExerciseSessionReport(@Path("sessionId") String sessionId,
                        @Body Map<String, Object> reportData);

        @GET("exercise-sessions/{sessionId}/report")
        Call<ApiResponse<Map<String, Object>>> getExerciseSessionReport(@Path("sessionId") String sessionId);

        // --- Admin ---
        @POST("admin/users")
        Call<ApiResponse<User>> createUser(@Body CreateUserRequest request);

        @POST("admin/assign-patient")
        Call<ApiResponse<Void>> assignPatientToDoctor(@Body Map<String, Integer> request);

        @GET("users")
        Call<ApiResponse<List<User>>> getAllUsers();

        // --- Chatbot ---
        @POST("chatbot/chat")
        Call<ApiResponse<Map<String, Object>>> sendChat(@Body Map<String, String> request);

        @GET("chatbot/history")
        Call<ApiResponse<List<Map<String, String>>>> getChatHistory(@Query("limit") Integer limit);

        // --- User Management (Admin) ---
        @POST("admin/users/{id}/delete")
        Call<ApiResponse<Void>> deleteUser(@Path("id") int userId);

        @PUT("admin/users/{id}/status")
        Call<ApiResponse<Void>> updateUserStatus(@Path("id") int userId,
                        @Body com.example.myrajourney.admin.management.AdminManagementActivity.UpdateUserStatusRequest request);

}
