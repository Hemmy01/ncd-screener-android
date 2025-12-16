package rw.ac.ncdscreener.repository;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

import rw.ac.ncdscreener.api.ApiClient;
import rw.ac.ncdscreener.api.FhirApiService;
import rw.ac.ncdscreener.database.NCDScreenerDatabase;
import rw.ac.ncdscreener.database.dao.CHWDao;
import rw.ac.ncdscreener.database.dao.ConditionDao;
import rw.ac.ncdscreener.database.dao.ObservationDao;
import rw.ac.ncdscreener.database.dao.PatientDao;
import rw.ac.ncdscreener.database.dao.QuestionnaireDao;
import rw.ac.ncdscreener.database.dao.ScreeningDao;
import rw.ac.ncdscreener.database.dao.ServiceRequestDao;
import rw.ac.ncdscreener.database.entity.CHWEntity;
import rw.ac.ncdscreener.database.entity.PatientEntity;
import rw.ac.ncdscreener.database.entity.ConditionEntity;
import rw.ac.ncdscreener.database.entity.ObservationEntity;
import rw.ac.ncdscreener.database.entity.QuestionnaireEntity;
import rw.ac.ncdscreener.database.entity.ScreeningEntity;
import rw.ac.ncdscreener.database.entity.ServiceRequestEntity;
import rw.ac.ncdscreener.model.CHW;
import rw.ac.ncdscreener.model.Condition;
import rw.ac.ncdscreener.model.Observation;
import rw.ac.ncdscreener.model.Patient;
import rw.ac.ncdscreener.model.Questionnaire;
import rw.ac.ncdscreener.model.Screening;
import rw.ac.ncdscreener.model.ServiceRequest;
import rw.ac.ncdscreener.utils.EntityConverter;

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

    @RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    public Screening getScreeningByIdSync(int screeningId, Patient patient, CHW chw) {
        ScreeningEntity screeningEntity = screeningDao.getScreeningByIdSync(screeningId);
        if (screeningEntity == null) {
            return null;
        }

        List<ObservationEntity> observationEntities = observationDao.getObservationsByScreeningId(screeningId);
        List<ConditionEntity> conditionEntities = conditionDao.getConditionsByScreeningId(screeningId);
        List<QuestionnaireEntity> questionnaireEntities = questionnaireDao.getQuestionnairesByScreeningId(screeningId);
        List<ServiceRequestEntity> serviceRequestEntities = serviceRequestDao.getServiceRequestsByScreeningId(screeningId);

        List<Object> observations = observationEntities.stream()
                .map(EntityConverter::fromEntity)
                .collect(Collectors.toList()).reversed();

        List<Object> conditions = conditionEntities.stream()
                .map(EntityConverter::fromEntity)
                .collect(Collectors.toList()).reversed();

        List<Object> questionnaires = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            questionnaires = questionnaireEntities.stream()
                    .map(EntityConverter::fromEntity)
                    .collect(Collectors.toList()).reversed();
        }

        List<ServiceRequest> serviceRequests = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            serviceRequests = serviceRequestEntities.stream()
                    .map(EntityConverter::fromEntity)
                    .collect(Collectors.toList()).reversed();
        }

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