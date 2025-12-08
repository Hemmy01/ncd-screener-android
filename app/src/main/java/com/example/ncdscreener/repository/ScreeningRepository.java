package com.example.ncdscreener.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.ncdscreener.api.ApiClient;
import com.example.ncdscreener.api.FhirApiService;
import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.CHWDao;
import com.example.ncdscreener.database.dao.ConditionDao;
import com.example.ncdscreener.database.dao.ObservationDao;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.QuestionnaireDao;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.dao.ServiceRequestDao;
import com.example.ncdscreener.database.entity.CHWEntity;
import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.database.entity.ConditionEntity;
import com.example.ncdscreener.database.entity.ObservationEntity;
import com.example.ncdscreener.database.entity.QuestionnaireEntity;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.database.entity.ServiceRequestEntity;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.model.ServiceRequest;
import com.example.ncdscreener.utils.EntityConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Repository class for managing screening data
 * Handles both local and remote data operations
 */
public class ScreeningRepository {
    private FhirApiService apiService;
    private ScreeningDao screeningDao;
    private PatientDao patientDao;
    private CHWDao chwDao;
    private ObservationDao observationDao;
    private ConditionDao conditionDao;
    private QuestionnaireDao questionnaireDao;
    private ServiceRequestDao serviceRequestDao;
    private ExecutorService executorService;

    public ScreeningRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(context);
        this.screeningDao = database.screeningDao();
        this.patientDao = database.patientDao();
        this.chwDao = database.chwDao();
        this.observationDao = database.observationDao();
        this.conditionDao = database.conditionDao();
        this.questionnaireDao = database.questionnaireDao();
        this.serviceRequestDao = database.serviceRequestDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    // Local operations using Room
    public LiveData<List<ScreeningEntity>> getAllScreenings() {
        return screeningDao.getAllScreenings();
    }

    public LiveData<ScreeningEntity> getScreeningById(int screeningId) {
        return screeningDao.getScreeningById(screeningId);
    }

    public void saveScreeningLocally(Screening screening) {
        executorService.execute(() -> {
            // Ensure Patient exists in database before saving Screening
            if (screening.getPatient() != null) {
                int patientId = screening.getPatient().getPatientId();
                if (patientId == 0) {
                    // New patient, save it first
                    PatientEntity newPatientEntity = EntityConverter.toEntity(screening.getPatient());
                    long insertedId = patientDao.insertPatient(newPatientEntity);
                    if (insertedId > 0) {
                        screening.getPatient().setPatientId((int) insertedId);
                    }
                } else {
                    // Check if patient exists
                    PatientEntity patientEntity = patientDao.getPatientByIdSync(patientId);
                    if (patientEntity == null) {
                        // Patient doesn't exist, save it first
                        PatientEntity newPatientEntity = EntityConverter.toEntity(screening.getPatient());
                        long insertedId = patientDao.insertPatient(newPatientEntity);
                        if (insertedId > 0) {
                            screening.getPatient().setPatientId((int) insertedId);
                        }
                    }
                }
            }
            
            // Ensure CHW exists in database before saving Screening
            if (screening.getChw() != null) {
                int chwId = screening.getChw().getChwId();
                if (chwId == 0) {
                    // New CHW, save it first
                    CHWEntity newChwEntity = EntityConverter.toEntity(screening.getChw());
                    long insertedId = chwDao.insertCHW(newChwEntity);
                    if (insertedId > 0) {
                        screening.getChw().setChwId((int) insertedId);
                    }
                } else {
                    // Check if CHW exists
                    CHWEntity chwEntity = chwDao.getCHWById(chwId);
                    if (chwEntity == null) {
                        // CHW doesn't exist, save it first
                        CHWEntity newChwEntity = EntityConverter.toEntity(screening.getChw());
                        long insertedId = chwDao.insertCHW(newChwEntity);
                        if (insertedId > 0) {
                            screening.getChw().setChwId((int) insertedId);
                        }
                    }
                }
            }
            
            // Now save screening entity (Patient and CHW are guaranteed to exist)
            ScreeningEntity screeningEntity = EntityConverter.toEntity(screening);
            long screeningId = screeningDao.insertScreening(screeningEntity);
            
            if (screeningId > 0 && screening.getScreeningId() == 0) {
                screening.setScreeningId((int) screeningId);
                screeningEntity.setScreeningId((int) screeningId);
            }
            
            int finalScreeningId = screeningEntity.getScreeningId();
            
            // Save observations
            if (screening.getObservations() != null && !screening.getObservations().isEmpty()) {
                List<ObservationEntity> observationEntities = screening.getObservations().stream()
                    .map(obs -> EntityConverter.toEntity(obs, finalScreeningId))
                    .collect(Collectors.toList());
                observationDao.insertObservations(observationEntities);
            }
            
            // Save conditions
            if (screening.getConditions() != null && !screening.getConditions().isEmpty()) {
                List<ConditionEntity> conditionEntities = screening.getConditions().stream()
                    .map(cond -> EntityConverter.toEntity(cond, finalScreeningId))
                    .collect(Collectors.toList());
                conditionDao.insertConditions(conditionEntities);
            }
            
            // Save questionnaires
            if (screening.getQuestionnaires() != null && !screening.getQuestionnaires().isEmpty()) {
                List<QuestionnaireEntity> questionnaireEntities = screening.getQuestionnaires().stream()
                    .map(q -> EntityConverter.toEntity(q, finalScreeningId))
                    .collect(Collectors.toList());
                questionnaireDao.insertQuestionnaires(questionnaireEntities);
            }
            
            // Save service requests
            if (screening.getServiceRequests() != null && !screening.getServiceRequests().isEmpty()) {
                List<ServiceRequestEntity> serviceRequestEntities = screening.getServiceRequests().stream()
                    .map(sr -> EntityConverter.toEntity(sr, finalScreeningId))
                    .collect(Collectors.toList());
                serviceRequestDao.insertServiceRequests(serviceRequestEntities);
            }
        });
    }

    public Screening getScreeningByIdSync(int screeningId, Patient patient, CHW chw) {
        ScreeningEntity screeningEntity = screeningDao.getScreeningByIdSync(screeningId);
        if (screeningEntity == null) {
            return null;
        }
        
        List<ObservationEntity> observationEntities = observationDao.getObservationsByScreeningId(screeningId);
        List<ConditionEntity> conditionEntities = conditionDao.getConditionsByScreeningId(screeningId);
        List<QuestionnaireEntity> questionnaireEntities = questionnaireDao.getQuestionnairesByScreeningId(screeningId);
        List<ServiceRequestEntity> serviceRequestEntities = serviceRequestDao.getServiceRequestsByScreeningId(screeningId);
        
        List<Observation> observations = observationEntities.stream()
            .map(EntityConverter::fromEntity)
            .collect(Collectors.toList());
        
        List<Condition> conditions = conditionEntities.stream()
            .map(EntityConverter::fromEntity)
            .collect(Collectors.toList());
        
        List<Questionnaire> questionnaires = questionnaireEntities.stream()
            .map(EntityConverter::fromEntity)
            .collect(Collectors.toList());
        
        List<ServiceRequest> serviceRequests = serviceRequestEntities.stream()
            .map(EntityConverter::fromEntity)
            .collect(Collectors.toList());
        
        return EntityConverter.fromEntity(screeningEntity, patient, chw, 
            observations, conditions, questionnaires, serviceRequests);
    }

    public void deleteScreening(int screeningId) {
        executorService.execute(() -> {
            screeningDao.deleteScreening(screeningId);
        });
    }

    // Remote operations (FHIR API)
    public void syncScreeningToServer(Screening screening) {
        // Convert Screening to FHIR resources and send to server
        // This will be implemented with proper FHIR resource conversion
    }
}

