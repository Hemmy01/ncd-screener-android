package com.example.ncdscreener.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.repository.PatientRepository;
import com.example.ncdscreener.utils.EntityConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * ViewModel for Patient management
 * Manages patient data and operations
 */
public class PatientViewModel extends AndroidViewModel {
    private PatientRepository repository;
    private LiveData<List<PatientEntity>> patientsEntityLiveData;
    private MutableLiveData<Patient> selectedPatientLiveData;
    private ExecutorService executorService;

    public PatientViewModel(Application application) {
        super(application);
        repository = new PatientRepository(application);
        patientsEntityLiveData = repository.getAllPatients();
        selectedPatientLiveData = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Patient>> getPatients() {
        return Transformations.map(patientsEntityLiveData, entities -> {
            if (entities == null) return new ArrayList<>();
            return entities.stream()
                .map(EntityConverter::fromEntity)
                .collect(Collectors.toList());
        });
    }

    public LiveData<Patient> getSelectedPatient() {
        return selectedPatientLiveData;
    }

    public void savePatient(Patient patient) {
        repository.savePatientLocally(patient);
    }
    
    /**
     * Saves patient synchronously and returns the saved patient
     * Use this when you need to wait for the save to complete
     */
    public Patient savePatientSync(Patient patient) {
        return repository.savePatientLocallySync(patient);
    }

    public void selectPatient(int patientId) {
        if (patientId > 0) {
            // Use synchronous method to avoid LiveData observer issues
            // Execute on background thread to avoid blocking UI
            new Thread(() -> {
                Patient patient = repository.getPatientByIdSync(patientId);
                if (patient != null) {
                    selectedPatientLiveData.postValue(patient);
                } else {
                    android.util.Log.e("PatientViewModel", "Patient not found with ID: " + patientId);
                }
            }).start();
        } else {
            // Clear selection (patientId = 0 or negative means clear)
            selectedPatientLiveData.postValue(null);
        }
    }

    /**
     * Gets patient by national ID synchronously (for background threads only)
     * @deprecated Use getPatientByNationalIdAsync instead for UI thread
     */
    @Deprecated
    public Patient getPatientByNationalId(int nationalId) {
        return repository.getPatientByNationalIdSync(nationalId);
    }
    
    /**
     * Gets patient by national ID asynchronously with callback
     * Safe to call from UI thread
     */
    public void getPatientByNationalIdAsync(int nationalId, PatientCallback callback) {
        executorService.execute(() -> {
            Patient patient = repository.getPatientByNationalIdSync(nationalId);
            callback.onResult(patient);
        });
    }
    
    /**
     * Callback interface for async patient lookup
     */
    public interface PatientCallback {
        void onResult(Patient patient);
    }

    public void updatePatient(Patient patient) {
        repository.updatePatient(patient);
    }
}

