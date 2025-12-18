package com.example.ncdscreener.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.ConditionDao;
import com.example.ncdscreener.database.dao.ObservationDao;
import com.example.ncdscreener.database.dao.PatientDao;
import com.example.ncdscreener.database.dao.QuestionnaireDao;
import com.example.ncdscreener.database.dao.ScreeningDao;
import com.example.ncdscreener.database.dao.ServiceRequestDao;
import com.example.ncdscreener.database.entity.ConditionEntity;
import com.example.ncdscreener.database.entity.ObservationEntity;
import com.example.ncdscreener.database.entity.PatientEntity;
import com.example.ncdscreener.database.entity.QuestionnaireEntity;
import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.database.entity.ServiceRequestEntity;
import com.example.ncdscreener.model.Condition;
import com.example.ncdscreener.model.Observation;
import com.example.ncdscreener.model.Patient;
import com.example.ncdscreener.model.Questionnaire;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.model.ServiceRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting patient and screening data to various formats
 * Supports CSV and JSON export formats
 */
public class DataExporter {
    
    private static final String TAG = "DataExporter";
    private Context context;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateTimeFormat;
    
    public DataExporter(Context context) {
        this.context = context;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }
    
    /**
     * Export patients to file
     * @param patients List of patients to export
     * @param format Export format: "csv" or "json"
     * @return File path if successful, null otherwise
     */
    public String exportPatients(List<Patient> patients, String format) {
        if (patients == null || patients.isEmpty()) {
            Log.w(TAG, "No patients to export");
            return null;
        }
        
        try {
            File file = createExportFile("patients", format);
            if (file == null) {
                return null;
            }
            
            FileWriter writer = new FileWriter(file);
            
            if ("csv".equalsIgnoreCase(format)) {
                exportPatientsToCSV(patients, writer);
            } else if ("json".equalsIgnoreCase(format)) {
                exportPatientsToJSON(patients, writer);
            } else {
                Log.e(TAG, "Unsupported format: " + format);
                writer.close();
                return null;
            }
            
            writer.close();
            Log.d(TAG, "Patients exported successfully to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
            
        } catch (IOException e) {
            Log.e(TAG, "Error exporting patients", e);
            return null;
        }
    }
    
    /**
     * Export all data (patients, screenings, observations, etc.)
     * @param format Export format: "csv" or "json"
     * @return File path if successful, null otherwise
     */
    public String exportAllData(String format) {
        try {
            NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(context);
            PatientDao patientDao = database.patientDao();
            ScreeningDao screeningDao = database.screeningDao();
            ObservationDao observationDao = database.observationDao();
            ConditionDao conditionDao = database.conditionDao();
            QuestionnaireDao questionnaireDao = database.questionnaireDao();
            ServiceRequestDao serviceRequestDao = database.serviceRequestDao();
            
            List<PatientEntity> patientEntities = patientDao.getAllPatientsSync();
            List<ScreeningEntity> screeningEntities = screeningDao.getAllScreeningsSync();
            
            File file = createExportFile("all_data", format);
            if (file == null) {
                return null;
            }
            
            FileWriter writer = new FileWriter(file);
            
            if ("csv".equalsIgnoreCase(format)) {
                exportAllDataToCSV(patientEntities, screeningEntities, 
                    observationDao, conditionDao, questionnaireDao, serviceRequestDao, writer);
            } else if ("json".equalsIgnoreCase(format)) {
                exportAllDataToJSON(patientEntities, screeningEntities,
                    observationDao, conditionDao, questionnaireDao, serviceRequestDao, writer);
            } else {
                Log.e(TAG, "Unsupported format: " + format);
                writer.close();
                return null;
            }
            
            writer.close();
            Log.d(TAG, "All data exported successfully to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting all data", e);
            return null;
        }
    }
    
    /**
     * Export screenings for a specific patient
     * @param patientId Patient ID
     * @param format Export format: "csv" or "json"
     * @return File path if successful, null otherwise
     */
    public String exportPatientScreenings(int patientId, String format) {
        try {
            NCDScreenerDatabase database = NCDScreenerDatabase.getDatabase(context);
            ScreeningDao screeningDao = database.screeningDao();
            ObservationDao observationDao = database.observationDao();
            ConditionDao conditionDao = database.conditionDao();
            QuestionnaireDao questionnaireDao = database.questionnaireDao();
            ServiceRequestDao serviceRequestDao = database.serviceRequestDao();
            
            List<ScreeningEntity> screenings = screeningDao.getAllScreeningsSync();
            // Filter by patient ID
            screenings.removeIf(s -> s.getPatientId() != patientId);
            
            if (screenings.isEmpty()) {
                Log.w(TAG, "No screenings found for patient: " + patientId);
                return null;
            }
            
            File file = createExportFile("patient_" + patientId + "_screenings", format);
            if (file == null) {
                return null;
            }
            
            FileWriter writer = new FileWriter(file);
            
            if ("csv".equalsIgnoreCase(format)) {
                exportScreeningsToCSV(screenings, observationDao, conditionDao, 
                    questionnaireDao, serviceRequestDao, writer);
            } else if ("json".equalsIgnoreCase(format)) {
                exportScreeningsToJSON(screenings, observationDao, conditionDao,
                    questionnaireDao, serviceRequestDao, writer);
            } else {
                Log.e(TAG, "Unsupported format: " + format);
                writer.close();
                return null;
            }
            
            writer.close();
            Log.d(TAG, "Patient screenings exported successfully to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
            
        } catch (Exception e) {
            Log.e(TAG, "Error exporting patient screenings", e);
            return null;
        }
    }
    
    /**
     * Create export file in Downloads directory
     */
    private File createExportFile(String prefix, String format) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "NCDScreener_" + prefix + "_" + timestamp + "." + format.toLowerCase();
            File file = new File(downloadsDir, filename);
            
            if (file.createNewFile()) {
                return file;
            } else {
                Log.e(TAG, "Failed to create file: " + file.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating export file", e);
            return null;
        }
    }
    
    /**
     * Export patients to CSV format
     */
    private void exportPatientsToCSV(List<Patient> patients, FileWriter writer) throws IOException {
        // CSV Header
        writer.append("Patient ID,National ID,First Name,Last Name,Date of Birth,Gender,Phone,Address\n");
        
        // CSV Data
        for (Patient patient : patients) {
            writer.append(String.valueOf(patient.getPatientId())).append(",");
            writer.append(String.valueOf(patient.getNationalId())).append(",");
            writer.append(escapeCSV(patient.getFirstName())).append(",");
            writer.append(escapeCSV(patient.getLastName())).append(",");
            writer.append(patient.getDateOfBirth() != null ? dateFormat.format(patient.getDateOfBirth()) : "").append(",");
            writer.append(escapeCSV(patient.getGender())).append(",");
            writer.append(escapeCSV(patient.getPhoneNumber())).append(",");
            writer.append(escapeCSV(patient.getAddress())).append("\n");
        }
    }
    
    /**
     * Export patients to JSON format
     */
    private void exportPatientsToJSON(List<Patient> patients, FileWriter writer) throws IOException {
        JSONArray jsonArray = new JSONArray();
        
        for (Patient patient : patients) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("patientId", patient.getPatientId());
                jsonObject.put("nationalId", patient.getNationalId());
                jsonObject.put("firstName", patient.getFirstName());
                jsonObject.put("lastName", patient.getLastName());
                jsonObject.put("dateOfBirth", patient.getDateOfBirth() != null ? 
                    dateFormat.format(patient.getDateOfBirth()) : null);
                jsonObject.put("gender", patient.getGender());
                jsonObject.put("phoneNumber", patient.getPhoneNumber());
                jsonObject.put("address", patient.getAddress());
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                Log.e(TAG, "Error creating JSON object for patient", e);
            }
        }
        
        try {
            writer.write(jsonArray.toString(2)); // Pretty print with 2-space indent
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON array to string", e);
            writer.write("[]"); // Write empty array as fallback
        }
    }
    
    /**
     * Export all data to CSV
     */
    private void exportAllDataToCSV(List<PatientEntity> patients, List<ScreeningEntity> screenings,
                                    ObservationDao observationDao, ConditionDao conditionDao,
                                    QuestionnaireDao questionnaireDao, ServiceRequestDao serviceRequestDao,
                                    FileWriter writer) throws IOException {
        // Export patients
        writer.append("=== PATIENTS ===\n");
        writer.append("Patient ID,National ID,First Name,Last Name,Date of Birth,Gender,Phone,Address\n");
        for (PatientEntity entity : patients) {
            Patient patient = EntityConverter.fromEntity(entity);
            writer.append(String.valueOf(patient.getPatientId())).append(",");
            writer.append(String.valueOf(patient.getNationalId())).append(",");
            writer.append(escapeCSV(patient.getFirstName())).append(",");
            writer.append(escapeCSV(patient.getLastName())).append(",");
            writer.append(patient.getDateOfBirth() != null ? dateFormat.format(patient.getDateOfBirth()) : "").append(",");
            writer.append(escapeCSV(patient.getGender())).append(",");
            writer.append(escapeCSV(patient.getPhoneNumber())).append(",");
            writer.append(escapeCSV(patient.getAddress())).append("\n");
        }
        
        // Export screenings
        writer.append("\n=== SCREENINGS ===\n");
        writer.append("Screening ID,Patient ID,Screening Date,Location,CHW ID\n");
        for (ScreeningEntity entity : screenings) {
            writer.append(String.valueOf(entity.getScreeningId())).append(",");
            writer.append(String.valueOf(entity.getPatientId())).append(",");
            long screeningDate = entity.getScreeningDate();
            writer.append(screeningDate > 0 ? dateTimeFormat.format(new Date(screeningDate)) : "").append(",");
            writer.append(escapeCSV(entity.getLocation())).append(",");
            writer.append(String.valueOf(entity.getChwId())).append("\n");
        }
        
        // Add more sections for observations, conditions, etc. as needed
        writer.append("\n=== EXPORT COMPLETE ===\n");
    }
    
    /**
     * Export all data to JSON
     */
    private void exportAllDataToJSON(List<PatientEntity> patients, List<ScreeningEntity> screenings,
                                     ObservationDao observationDao, ConditionDao conditionDao,
                                     QuestionnaireDao questionnaireDao, ServiceRequestDao serviceRequestDao,
                                     FileWriter writer) throws IOException {
        JSONObject root = new JSONObject();
        
        try {
            // Patients
            JSONArray patientsArray = new JSONArray();
            for (PatientEntity entity : patients) {
                Patient patient = EntityConverter.fromEntity(entity);
                JSONObject patientObj = new JSONObject();
                patientObj.put("patientId", patient.getPatientId());
                patientObj.put("nationalId", patient.getNationalId());
                patientObj.put("firstName", patient.getFirstName());
                patientObj.put("lastName", patient.getLastName());
                patientObj.put("dateOfBirth", patient.getDateOfBirth() != null ? 
                    dateFormat.format(patient.getDateOfBirth()) : null);
                patientObj.put("gender", patient.getGender());
                patientObj.put("phoneNumber", patient.getPhoneNumber());
                patientObj.put("address", patient.getAddress());
                patientsArray.put(patientObj);
            }
            root.put("patients", patientsArray);
            
            // Screenings
            JSONArray screeningsArray = new JSONArray();
            for (ScreeningEntity entity : screenings) {
                JSONObject screeningObj = new JSONObject();
                screeningObj.put("screeningId", entity.getScreeningId());
                screeningObj.put("patientId", entity.getPatientId());
                long screeningDate = entity.getScreeningDate();
                screeningObj.put("screeningDate", screeningDate > 0 ? 
                    dateTimeFormat.format(new Date(screeningDate)) : null);
                screeningObj.put("location", entity.getLocation());
                screeningObj.put("chwId", entity.getChwId());
                screeningsArray.put(screeningObj);
            }
            root.put("screenings", screeningsArray);
            
            try {
                writer.write(root.toString(2)); // Pretty print
            } catch (JSONException e) {
                Log.e(TAG, "Error converting JSON object to string", e);
                writer.write("{}"); // Write empty object as fallback
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for all data", e);
        }
    }
    
    /**
     * Export screenings to CSV
     */
    private void exportScreeningsToCSV(List<ScreeningEntity> screenings,
                                       ObservationDao observationDao, ConditionDao conditionDao,
                                       QuestionnaireDao questionnaireDao, ServiceRequestDao serviceRequestDao,
                                       FileWriter writer) throws IOException {
        writer.append("Screening ID,Patient ID,Screening Date,Location,CHW ID\n");
        for (ScreeningEntity entity : screenings) {
            writer.append(String.valueOf(entity.getScreeningId())).append(",");
            writer.append(String.valueOf(entity.getPatientId())).append(",");
            long screeningDate = entity.getScreeningDate();
            writer.append(screeningDate > 0 ? dateTimeFormat.format(new Date(screeningDate)) : "").append(",");
            writer.append(escapeCSV(entity.getLocation())).append(",");
            writer.append(String.valueOf(entity.getChwId())).append("\n");
        }
    }
    
    /**
     * Export screenings to JSON
     */
    private void exportScreeningsToJSON(List<ScreeningEntity> screenings,
                                        ObservationDao observationDao, ConditionDao conditionDao,
                                        QuestionnaireDao questionnaireDao, ServiceRequestDao serviceRequestDao,
                                        FileWriter writer) throws IOException {
        JSONArray jsonArray = new JSONArray();
        
        for (ScreeningEntity entity : screenings) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("screeningId", entity.getScreeningId());
                jsonObject.put("patientId", entity.getPatientId());
                long screeningDate = entity.getScreeningDate();
                jsonObject.put("screeningDate", screeningDate > 0 ? 
                    dateTimeFormat.format(new Date(screeningDate)) : null);
                jsonObject.put("location", entity.getLocation());
                jsonObject.put("chwId", entity.getChwId());
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                Log.e(TAG, "Error creating JSON object for screening", e);
            }
        }
        
        try {
            writer.write(jsonArray.toString(2));
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON array to string", e);
            writer.write("[]"); // Write empty array as fallback
        }
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
