package com.example.myrajourney.data.repository;

import android.content.Context;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.AppointmentRequest;
import com.example.myrajourney.data.model.EducationArticle;
import com.example.myrajourney.data.model.Medication;
import com.example.myrajourney.data.model.MedicationLog;
import com.example.myrajourney.data.model.MedicationLogRequest;
import com.example.myrajourney.data.model.PatientOverview;
import com.example.myrajourney.data.model.RehabPlan;
import com.example.myrajourney.data.model.Report;
import com.example.myrajourney.data.model.Symptom;
import com.example.myrajourney.data.model.SymptomRequest;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRepository {

    private final ApiService apiService;

    public PatientRepository(Context context) {
        this.apiService = ApiClient.getApiService(context);
    }

    // ========================================================================
    // 1. DASHBOARD OVERVIEW
    // ========================================================================

    public void getPatientOverview(final DataCallback<PatientOverview> callback) {
        apiService.getPatientOverview().enqueue(new Callback<ApiResponse<PatientOverview>>() {
            @Override
            public void onResponse(Call<ApiResponse<PatientOverview>> call,
                                   Response<ApiResponse<PatientOverview>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load overview: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PatientOverview>> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    // ========================================================================
    // 2. APPOINTMENTS
    // ========================================================================

    // ⭐ NEW — used by dashboard to show the single upcoming appointment
    public void getNextAppointment(final DataCallback<Appointment> callback) {

        apiService.getPatientOverview().enqueue(new Callback<ApiResponse<PatientOverview>>() {
            @Override
            public void onResponse(Call<ApiResponse<PatientOverview>> call,
                                   Response<ApiResponse<PatientOverview>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    PatientOverview overview = response.body().getData();

                    if (overview != null) {
                        callback.onSuccess(overview.getNextAppointment());
                    } else {
                        callback.onSuccess(null);
                    }
                } else {
                    callback.onError("Failed to fetch next appointment");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PatientOverview>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Used for listing full appointment history
    public void getAppointments(final DataCallback<List<Appointment>> callback) {
        apiService.getAppointments().enqueue(new Callback<ApiResponse<List<Appointment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Appointment>>> call,
                                   Response<ApiResponse<List<Appointment>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load appointments");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Appointment>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void requestAppointment(AppointmentRequest request,
                                   final DataCallback<Appointment> callback) {

        apiService.createAppointment(request).enqueue(new Callback<ApiResponse<Appointment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Appointment>> call,
                                   Response<ApiResponse<Appointment>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to book appointment: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Appointment>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ========================================================================
    // 3. MEDICATIONS
    // ========================================================================

    public void getMedications(final DataCallback<List<Medication>> callback) {
        apiService.getPatientMedications().enqueue(new Callback<ApiResponse<List<Medication>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Medication>>> call,
                                   Response<ApiResponse<List<Medication>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load medications");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Medication>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void logMedicationIntake(String medicationId, String timestamp,
                                    final DataCallback<MedicationLog> callback) {

        MedicationLogRequest request = new MedicationLogRequest(medicationId, timestamp);

        apiService.logMedicationIntake(request).enqueue(new Callback<ApiResponse<MedicationLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<MedicationLog>> call,
                                   Response<ApiResponse<MedicationLog>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to log medication intake");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MedicationLog>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ========================================================================
    // 4. SYMPTOMS
    // ========================================================================

    public void getSymptomHistory(final DataCallback<List<Symptom>> callback) {
        apiService.getSymptoms().enqueue(new Callback<ApiResponse<List<Symptom>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Symptom>>> call,
                                   Response<ApiResponse<List<Symptom>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load symptom history");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Symptom>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void logSymptoms(SymptomRequest request, final DataCallback<Symptom> callback) {
        apiService.createSymptom(request).enqueue(new Callback<ApiResponse<Symptom>>() {
            @Override
            public void onResponse(Call<ApiResponse<Symptom>> call,
                                   Response<ApiResponse<Symptom>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to submit symptoms");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Symptom>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ========================================================================
    // 5. REHAB PLANS
    // ========================================================================

    public void getRehabPlans(int patientId, final DataCallback<List<RehabPlan>> callback) {
        apiService.getRehabPlans(patientId).enqueue(new Callback<ApiResponse<List<RehabPlan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RehabPlan>>> call,
                                   Response<ApiResponse<List<RehabPlan>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load rehab plans");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RehabPlan>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ========================================================================
    // 6. REPORTS
    // ========================================================================

    public void getReports(final DataCallback<List<Report>> callback) {
        apiService.getReports().enqueue(new Callback<ApiResponse<List<Report>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Report>>> call,
                                   Response<ApiResponse<List<Report>>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to load reports");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Report>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void uploadReport(int patientId, String title, String description, File file,
                             final DataCallback<Report> callback) {

        RequestBody pIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(patientId));
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        apiService.createReport(pIdBody, titleBody, descBody, body)
                .enqueue(new Callback<ApiResponse<Report>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Report>> call,
                                           Response<ApiResponse<Report>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            callback.onSuccess(response.body().getData());
                        } else {
                            callback.onError("Upload failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Report>> call, Throwable t) {
                        callback.onError("Upload Error: " + t.getMessage());
                    }
                });
    }

    // ========================================================================
    // CALLBACK
    // ========================================================================

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}
