package rw.ac.ncdscreener.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import rw.ac.ncdscreener.api.ApiClient;
import rw.ac.ncdscreener.api.FhirApiService;
import rw.ac.ncdscreener.database.NCDScreenerDatabase;
import rw.ac.ncdscreener.database.dao.PatientDao;
import rw.ac.ncdscreener.database.entity.PatientEntity;
import rw.ac.ncdscreener.model.Patient;
import rw.ac.ncdscreener.utils.EntityConverter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class for managing patient data
 * Handles both local and remote data operations
 */
public class PatientRepository {
    private FhirApiService apiService;
    private PatientDao patientDao;
    private ExecutorService executorService;

    public PatientRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.patientDao = NCDScreenerDatabase.getDatabase(context).patientDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    // Local operations using Room
    public LiveData<List<PatientEntity>> getAllPatients() {
        return patientDao.getAllPatients();
    }

    public LiveData<PatientEntity> getPatientById(int patientId) {
        return patientDao.getPatientById(patientId);
    }

    public void savePatientLocally(Patient patient) {
        executorService.execute(() -> {
            PatientEntity entity = EntityConverter.toEntity(patient);
            long id = patientDao.insertPatient(entity);
            if (id > 0 && patient.getPatientId() == 0) {
                patient.setPatientId((int) id);
            }
            android.util.Log.d("PatientRepository", "Patient saved with ID: " + id);
        });
    }

    /**
     * Saves patient and returns the saved patient entity
     * This is a blocking call that waits for the save to complete
     */
    public Patient savePatientLocallySync(Patient patient) {
        PatientEntity entity = EntityConverter.toEntity(patient);
        long id = patientDao.insertPatient(entity);
        if (id > 0 && patient.getPatientId() == 0) {
            patient.setPatientId((int) id);
        }
        // Return the saved patient with updated ID
        PatientEntity savedEntity = patientDao.getPatientByIdSync((int) id);
        return savedEntity != null ? EntityConverter.fromEntity(savedEntity) : patient;
    }

    public Patient getPatientByNationalIdSync(int nationalId) {
        PatientEntity entity = patientDao.getPatientByNationalId(nationalId);
        return entity != null ? EntityConverter.fromEntity(entity) : null;
    }

    public Patient getPatientByIdSync(int patientId) {
        PatientEntity entity = patientDao.getPatientByIdSync(patientId);
        return entity != null ? EntityConverter.fromEntity(entity) : null;
    }

    public void updatePatient(Patient patient) {
        executorService.execute(() -> {
            PatientEntity entity = EntityConverter.toEntity(patient);
            patientDao.updatePatient(entity);
        });
    }

    public void deletePatient(int patientId) {
        executorService.execute(() -> {
            patientDao.deletePatient(patientId);
        });
    }

    // Remote operations (FHIR API)
    public void syncPatientToServer(Patient patient) {
        // Convert Patient to FHIR resource and send to server
        // This will be implemented with proper FHIR resource conversion
    }
}