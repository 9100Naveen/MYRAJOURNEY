package com.example.myrajourney.patient.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myrajourney.data.model.Appointment;
import com.example.myrajourney.data.model.PatientOverview;
import com.example.myrajourney.data.repository.PatientRepository;
import com.example.myrajourney.doctor.dashboard.HealthMetric;

import java.util.ArrayList;
import java.util.List;

public class PatientDashboardViewModel extends AndroidViewModel {

    private final PatientRepository repository;

    private final MutableLiveData<List<HealthMetric>> healthMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> upcomingAppointments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> taskCompleted = new MutableLiveData<>();
    private final MutableLiveData<String> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PatientDashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientRepository(application);
        refreshData();
    }

    public LiveData<List<HealthMetric>> getHealthMetrics() { return healthMetrics; }
    public LiveData<List<Appointment>> getUpcomingAppointments() { return upcomingAppointments; }
    public LiveData<Boolean> getTaskCompleted() { return taskCompleted; }
    public LiveData<String> getCurrentUser() { return currentUser; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Called by Activity onResume
    public void refreshAppointments() {
        refreshData(); // Just refresh everything to be safe
    }

    public void refreshData() {
        repository.getPatientOverview(new PatientRepository.DataCallback<PatientOverview>() {
            @Override
            public void onSuccess(PatientOverview data) {
                if (data != null) {
                    // 1. Set User Name
                    currentUser.setValue(data.getPatientName());

                    // 2. Set Metrics
                    mapOverviewToMetrics(data);

                    // 3. Set Appointment (Directly from data, no second API call needed)
                    List<Appointment> list = new ArrayList<>();
                    if (data.getNextAppointment() != null) {
                        list.add(data.getNextAppointment());
                    }
                    upcomingAppointments.setValue(list);
                }
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Overview failed: " + error);
                // Clear appointment list on error so old data doesn't persist
                upcomingAppointments.setValue(new ArrayList<>());
            }
        });
    }

    private void mapOverviewToMetrics(PatientOverview overview) {
        List<HealthMetric> metrics = new ArrayList<>();

        // DAS28
        double das = overview.getDas28Score();
        String dasColor = das > 5.1 ? "#F44336" : (das < 2.6 ? "#4CAF50" : "#FF9800");
        metrics.add(new HealthMetric("DAS28 Score", String.valueOf(das), "📊", dasColor));

        // Pain
        int pain = overview.getPainLevel();
        String painColor = pain >= 7 ? "#F44336" : (pain >= 4 ? "#FF9800" : "#4CAF50");
        metrics.add(new HealthMetric("Pain Level", pain + "/10", "😣", painColor));

        // Notifications
        metrics.add(new HealthMetric("Notifications", String.valueOf(overview.getUnreadNotifications()), "🔔", "#2196F3"));

        healthMetrics.setValue(metrics);
    }

    public void setTaskCompleted(boolean completed) {
        taskCompleted.setValue(completed);
    }
}