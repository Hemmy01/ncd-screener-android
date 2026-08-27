# NCD Screener Android Application

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![FHIR R4](https://img.shields.io/badge/FHIR-R4-blue.svg)](https://www.hl7.org/fhir/)
[![Java](https://img.shields.io/badge/Language-Java%2011-orange.svg)](https://www.oracle.com/java/)

A comprehensive mobile application designed for Community Health Workers (CHWs) in Rwanda to conduct Non-Communicable Disease (NCD) screenings in the field. The app enables health workers to screen patients for conditions like hypertension and diabetes, with full offline support and FHIR R4 integration for healthcare data interoperability.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Database Schema](#database-schema)
- [Installation](#installation)
- [Usage Guide](#usage-guide)
- [API Integration](#api-integration)
- [Risk Scoring Algorithm](#risk-scoring-algorithm)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

The **NCD Screener** is a mobile health (mHealth) application that empowers Community Health Workers to conduct comprehensive health screenings for non-communicable diseases. The application follows healthcare interoperability standards (FHIR R4) and provides a user-friendly interface for capturing vital signs, assessing risk factors, and generating health reports.

### Target Users
- **Primary**: Community Health Workers (CHWs)
- **Secondary**: Healthcare administrators and supervisors

### Key Use Cases
- Patient registration with demographic data
- Vital signs measurement (blood pressure, glucose, BMI)
- Risk factor assessment through questionnaires
- Automated risk scoring and condition identification
- Screening history tracking
- Offline data collection with background synchronization

---

## ✨ Key Features

### 🔐 Authentication & Session Management
- Secure CHW login with username/password
- 1-hour session timeout for security
- Automatic session refresh on user activity
- Session expiration warnings

### 👥 Patient Management
- **Patient Registration**: Capture complete demographic information
  - National ID, name, date of birth, gender
  - Phone number and address
  - Geolocation capture for field registration
- **Patient Search**: Quick search and filtering capabilities
- **Patient Details**: Comprehensive patient profiles with screening history

### 🏥 NCD Screening
- **Vital Signs Measurement**:
  - Blood Pressure (Systolic/Diastolic) with real-time validation
  - Blood Glucose Level with automatic categorization
  - Weight and Height with automatic BMI calculation
  
- **Risk Factor Questionnaire**:
  - Family history of diabetes
  - Family history of hypertension
  - Smoking habits
  - Physical inactivity
  - Unhealthy diet patterns

- **Real-time Validation**: Color-coded indicators for:
  - Normal ranges (green)
  - Elevated readings (yellow)
  - High risk readings (orange)
  - Critical readings (red)

### 📊 Risk Assessment
- **Automated Risk Scoring**: 0-100 scale based on:
  - Vital signs measurements
  - Questionnaire responses
  - Clinical thresholds
- **Risk Level Classification**:
  - Minimal Risk (< 10)
  - Low Risk (10-24)
  - Moderate Risk (25-49)
  - High Risk (≥ 50)
- **Condition Identification**: Automatic detection of:
  - Hypertension (Stage 1, Stage 2, Crisis)
  - Diabetes
  - Pre-diabetes
  - Obesity/Overweight

### 📈 Screening History & Reports
- Complete screening history per patient
- Detailed screening reports with:
  - All measurements and observations
  - Identified conditions
  - Risk scores and recommendations
  - CHW information and timestamps
- Report generation for documentation

### 🌍 Offline Support
- **Full Offline Capability**: All features work without internet connection
- **Local Data Persistence**: Room database for reliable storage
- **Background Synchronization**: Automatic sync when connection is restored
- **Sync Queue Management**: Outbox pattern for pending uploads

### 🌐 FHIR R4 Integration
- Standard healthcare data format (HL7 FHIR R4)
- Resource mapping for:
  - Patient resources
  - Observation resources (vital signs)
  - Condition resources (diagnoses)
  - QuestionnaireResponse resources
  - ServiceRequest resources (referrals)
- FHIR resource converter utilities
- Sync with external FHIR servers

### 🎨 User Experience
- **Material Design 3**: Modern, intuitive interface
- **Dark Mode**: Light/dark theme switching
- **Multi-language Support**: English, French, Kinyarwanda
- **Accessibility**: Screen reader support and accessible UI components
- **Bottom Navigation**: Easy navigation between main sections
- **Dashboard**: Statistics and recent activity overview

### 📱 Additional Features
- Data export (CSV/JSON)
- Settings management
- CHW profile management
- Sync status monitoring
- Location services integration

---

## 🏗️ Architecture

The application follows a clean architecture pattern with clear separation of concerns:

### Architecture Pattern: MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────┐
│                      Presentation Layer                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Activities  │  │  Fragments   │  │   Adapters   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│                     ViewModel Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Patient    │  │  Screening   │  │     CHW      │  │
│  │  ViewModel   │  │  ViewModel   │  │  ViewModel   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Repository Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Patient    │  │  Screening   │  │     CHW      │  │
│  │ Repository   │  │ Repository   │  │ Repository   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌──────────────┐                    ┌──────────────┐  │
│  │  Room (Local │ ◄────────────────► │  FHIR API    │  │
│  │   Database)  │    Sync Service    │   (Remote)   │  │
│  └──────────────┘                    └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Components

#### 1. **Presentation Layer**
- **Activities**: Entry points and container for fragments
  - `LoginActivity`: Authentication screen
  - `MainActivity`: Main container with navigation
- **Fragments**: UI screens for specific features
  - `HomeFragment`: Dashboard with statistics
  - `PatientListFragment`: Patient browsing
  - `RegisterPatientFragment`: Patient registration
  - `ScreeningFormFragment`: Screening data capture
  - `ScreeningResultsFragment`: Results display
  - `ProfileFragment`: CHW profile and settings

#### 2. **ViewModel Layer**
- Manages UI-related data lifecycle-aware
- Handles business logic and data transformation
- Communicates with repositories
- Exposes LiveData for UI observation

#### 3. **Repository Layer**
- Single source of truth for data
- Abstracts data sources (local + remote)
- Manages data synchronization
- Handles network/database operations

#### 4. **Data Layer**
- **Local**: Room database for offline storage
- **Remote**: FHIR API for server synchronization
- **Converters**: Entity ↔ Model ↔ FHIR Resource

#### 5. **Services Layer**
- **FhirSyncService**: Background synchronization
- **SyncManager**: Coordinates sync operations
- **WorkManager**: Schedules periodic tasks

#### 6. **Utilities**
- `RiskScoringUtils`: Risk calculation algorithms
- `FhirResourceConverter`: FHIR resource transformation
- `HealthDataValidator`: Clinical validation rules
- `SessionManager`: Authentication state management
- `ThemeManager`: UI theme management
- `LocaleHelper`: Multi-language support

---

## 🛠️ Technology Stack

### Core Technologies
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 11 | Programming language |
| **Gradle** | - | Build system (Kotlin DSL) |
| **Android SDK** | 24-36 | Android framework |

### Major Libraries & Frameworks

#### Persistence & Database
- **Room** `2.6.1` - Local SQLite database ORM
  - Type converters for date/time
  - LiveData support for reactive updates
  - Foreign key relationships with cascade

#### Networking
- **Retrofit** `2.9.0` - REST API client
- **OkHttp** `4.12.0` - HTTP client with logging interceptor
- **Gson** `2.10.1` - JSON serialization/deserialization

#### Healthcare Standards
- **HAPI FHIR Structures R4** `7.4.0` - FHIR resource parsing
- **HAPI FHIR Base** `7.4.0` - FHIR core functionality

#### Android Components
- **Material Components** - Material Design 3 UI components
- **Navigation Component** - Fragment navigation framework
- **ConstraintLayout** - Flexible layout system
- **CardView** `1.0.0` - Card-based UI elements
- **WorkManager** `2.9.1` - Background task scheduling

#### Utilities
- **Guava** `30.1.1-android` - Google core libraries
- **Google Play Services** - Location and mapping

#### Testing
- **JUnit** `4.13.2` - Unit testing
- **Espresso** `3.5.1` - UI testing
- **AndroidX Test** `1.1.5` - Testing utilities

### Build Configuration
```gradle
compileSdk: 36
minSdk: 24 (Android 7.0 Nougat)
targetSdk: 36
Java: 11 (Source & Target compatibility)
```

---

## 🗄️ Database Schema

The application uses **Room** persistence library with the following schema:

### Main Database: `NCDScreenerDatabase` (Version 3)

#### Entity Relationship Diagram

```
┌─────────────────┐
│   chw_table     │
│─────────────────│
│ chwId (PK)      │
│ username        │
│ firstName       │
│ lastName        │
│ phoneNumber     │
│ password        │
└─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐        ┌─────────────────┐
│    patients     │        │   screenings    │
│─────────────────│        │─────────────────│
│ patientId (PK)  │◄───────│ screeningId(PK) │
│ nationalId      │   1:N  │ screeningDate   │
│ firstName       │        │ location        │
│ lastName        │        │ patientId (FK)  │
│ dateOfBirth     │        │ chwId           │
│ gender          │        │ chwName         │
│ phoneNumber     │        └─────────────────┘
│ address         │                 │
└─────────────────┘                 │ 1:N
                                    ▼
                    ┌───────────────────────────────────┐
                    │                                   │
         ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
         │  observations    │  │   conditions     │  │  questionnaires  │
         │──────────────────│  │──────────────────│  │──────────────────│
         │ observationId(PK)│  │ conditionId (PK) │  │questionnaireId(PK)│
         │ screeningId (FK) │  │ screeningId (FK) │  │ screeningId (FK) │
         │ observationType  │  │ conditionCode    │  │ questionCode     │
         │ value            │  │ conditionName    │  │ answer           │
         │ unit             │  └──────────────────┘  └──────────────────┘
         │ finalRiskScore   │
         └──────────────────┘
                    │
                    │ 1:N
                    ▼
         ┌──────────────────┐
         │ service_requests │
         │──────────────────│
         │ serviceRequestId │
         │ screeningId (FK) │
         │ referralCode     │
         │ reasonText       │
         │ status           │
         └──────────────────┘
```

### Table Descriptions

#### **patients**
Stores patient demographic information
- `patientId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `nationalId` (INTEGER) - National identification number
- `firstName` (TEXT) - Patient's first name
- `lastName` (TEXT) - Patient's last name
- `dateOfBirth` (LONG) - Stored as timestamp
- `gender` (TEXT) - Male/Female/Other
- `phoneNumber` (TEXT) - Contact number
- `address` (TEXT) - Residential address

#### **screenings**
Records screening sessions
- `screeningId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `screeningDate` (LONG) - Timestamp of screening
- `location` (TEXT) - Where screening was conducted
- `patientId` (INTEGER, FOREIGN KEY → patients)
- `chwId` (INTEGER) - Reference to CHW
- `chwName` (TEXT) - CHW name for display

#### **observations**
Stores vital signs and measurements
- `observationId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `screeningId` (INTEGER, FOREIGN KEY → screenings)
- `observationType` (TEXT) - Type of measurement
  - `blood_pressure_systolic`
  - `blood_pressure_diastolic`
  - `glucose`
  - `bmi`
  - `weight`
  - `height`
- `value` (REAL) - Numerical value
- `unit` (TEXT) - Unit of measurement (mmHg, mg/dL, kg, cm)
- `finalRiskScore` (INTEGER) - Calculated risk score

#### **conditions**
Identified medical conditions
- `conditionId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `screeningId` (INTEGER, FOREIGN KEY → screenings)
- `conditionCode` (TEXT) - SNOMED CT or internal code
- `conditionName` (TEXT) - Human-readable name
  - Hypertension Stage 1
  - Hypertension Stage 2
  - Diabetes
  - Pre-diabetes
  - Obesity

#### **questionnaires**
Questionnaire responses
- `questionnaireId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `screeningId` (INTEGER, FOREIGN KEY → screenings)
- `questionCode` (TEXT) - Question identifier
  - `family_history_diabetes`
  - `family_history_hypertension`
  - `smoking`
  - `physical_inactivity`
  - `unhealthy_diet`
- `answer` (TEXT) - Response (yes/no)

#### **service_requests**
Referral requests
- `serviceRequestId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `screeningId` (INTEGER, FOREIGN KEY → screenings)
- `referralCode` (TEXT) - Referral type code
- `reasonText` (TEXT) - Reason for referral
- `status` (TEXT) - Request status

#### **chw_table**
Community Health Worker accounts
- `chwId` (INTEGER, PRIMARY KEY, AUTO-INCREMENT)
- `username` (TEXT, UNIQUE) - Login username
- `firstName` (TEXT)
- `lastName` (TEXT)
- `phoneNumber` (TEXT)
- `password` (TEXT) - ⚠️ Note: Currently plain text (TODO: implement hashing)

### Additional Tables

#### **fhir_outbox**
Queue for pending FHIR resource uploads
- Stores resources that need to be synced to server
- Managed by background sync service

#### **fhir_resources**
Cached FHIR resources from server
- Local cache of fetched FHIR data
- Reduces network requests

### Database Features
- **Foreign Keys**: Enabled with CASCADE delete
- **Indexes**: Created on foreign key columns for performance
- **Migration**: Currently uses fallbackToDestructiveMigration (development mode)
- **Thread Safety**: Singleton pattern with synchronized access

---

## 📥 Installation

### Prerequisites
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **JDK**: Java Development Kit 11 or later
- **Android SDK**: API 24 or higher
- **Git**: Version control system

### Step-by-Step Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/ncd-screener-android.git
   cd ncd-screener-android
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure SDK**
   - Open File → Project Structure
   - Ensure SDK location is set correctly
   - Verify Java 11 is selected

4. **Sync Dependencies**
   ```bash
   ./gradlew build
   ```
   or click "Sync Project with Gradle Files" in Android Studio

5. **Configure FHIR Server (Optional)**
   - Open `api/FhirApiService.java`
   - Update `BASE_URL` if using a different FHIR server
   ```java
   String BASE_URL = "https://your-fhir-server.com/fhir/";
   ```

6. **Run the Application**
   - Connect an Android device or start an emulator
   - Click Run ▶️ or press `Shift+F10`
   - Select target device
   - Wait for installation and launch

### Build Variants
- **Debug**: Development build with logging enabled
- **Release**: Production build (requires signing configuration)

### Troubleshooting

**Gradle Sync Failed**
```bash
# Clear Gradle cache
./gradlew clean
./gradlew build --refresh-dependencies
```

**Dependency Conflicts**
- The project already handles common conflicts (Guava, Netty)
- Check `build.gradle.kts` for exclusion rules

**HAPI FHIR Issues**
- Ensure `useLibrary("org.apache.http.legacy")` is present
- Verify resource exclusions in packaging section

---

## 📖 Usage Guide

### First Time Setup

1. **Launch the Application**
   - App opens on LoginActivity

2. **Login with Default Credentials**
   - **Username**: `chw`
   - **Password**: `password`
   - Session valid for 1 hour

3. **Dashboard Overview**
   - View statistics (patients, screenings)
   - Access quick actions
   - See recent activity

### Patient Registration

1. **Navigate to Patient List**
   - Tap "Patient List" on home screen or bottom navigation

2. **Add New Patient**
   - Tap the floating action button (FAB) ➕
   - Fill in required information:
     - National ID (unique identifier)
     - First Name and Last Name
     - Date of Birth (date picker)
     - Gender (dropdown)
     - Phone Number
     - Address
   - Optional: Capture GPS location
   - Tap "Register Patient"

3. **Search Existing Patients**
   - Use search bar at top
   - Filter by name or National ID

### Conducting a Screening

1. **Start New Screening**
   - From home: Tap "New Screening"
   - From patient list: Select patient → "New Screening"

2. **Select Patient**
   - Choose from dropdown of registered patients
   - Or tap "Add New Patient" to register first

3. **Enter Vital Signs**
   
   **Blood Pressure:**
   - Enter Systolic BP (e.g., 120)
   - Enter Diastolic BP (e.g., 80)
   - Real-time color indicator:
     - 🟢 Green: Normal
     - 🟡 Yellow: Elevated
     - 🟠 Orange: High
     - 🔴 Red: Critical

   **Blood Glucose:**
   - Enter glucose level (mg/dL)
   - Auto-categorized:
     - Normal (< 100 fasting)
     - Prediabetic (100-125)
     - Diabetic (≥ 126)

   **Body Measurements:**
   - Enter Weight (kg)
   - Enter Height (cm)
   - BMI calculated automatically
   - Category displayed (Underweight/Normal/Overweight/Obese)

4. **Complete Questionnaire**
   - Toggle switches for risk factors:
     - ☑️ Family history of diabetes
     - ☑️ Family history of hypertension
     - ☑️ Current smoker
     - ☑️ Physically inactive
     - ☑️ Unhealthy diet

5. **Submit Screening**
   - Review entered data
   - Tap "Submit Screening"
   - View results screen

### Understanding Results

**Risk Score Display:**
- Number from 0-100
- Color-coded badge:
  - 🟢 Minimal (< 10)
  - 🔵 Low (10-24)
  - 🟡 Moderate (25-49)
  - 🔴 High (≥ 50)

**Identified Conditions:**
- List of detected conditions
- Based on clinical thresholds
- Examples:
  - Hypertension Stage 1 (140-159/90-99)
  - Hypertension Stage 2 (≥160/≥100)
  - Diabetes (glucose ≥126 fasting)
  - Obesity (BMI ≥30)

**Recommendations:**
- Automatically generated based on findings
- Referral suggestions
- Follow-up actions

### Viewing Screening History

1. **From Patient Details**
   - Navigate to patient profile
   - Scroll to "Screening History" section
   - Tap on any past screening

2. **Screening Details**
   - View all measurements
   - See risk score progression
   - Review conditions
   - Check CHW notes

### Data Synchronization

**Automatic Sync:**
- Runs every 15 minutes when online
- Background WorkManager job
- No user action required

**Manual Sync:**
- Settings → Sync Now
- View sync status in settings
- Check last sync time

**Offline Mode:**
- All features work offline
- Data saved locally
- Syncs when connection restored
- Queue indicator shows pending uploads

### Settings & Preferences

**Theme:**
- Settings → Theme
- Choose Light/Dark/System Default

**Language:**
- Settings → Language
- Options: English, French, Kinyarwanda
- App restarts to apply

**Profile:**
- View CHW information
- Change password (if implemented)
- Logout

---

## 🌐 API Integration

### FHIR R4 Integration

The application integrates with HL7 FHIR R4 (Fast Healthcare Interoperability Resources) standard for healthcare data exchange.

#### Base Configuration

**Default FHIR Server:**
```
https://fhirserver.hl7fundamentals.org/fhir/
```

**API Client Setup** (`api/ApiClient.java`):
```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(FhirApiService.BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

#### FHIR Resource Mapping

The app converts local models to FHIR R4 resources:

##### 1. **Patient Resource**
```json
{
  "resourceType": "Patient",
  "identifier": [{
    "system": "http://example.org/national-id",
    "value": "123456789"
  }],
  "name": [{
    "given": ["John"],
    "family": "Doe"
  }],
  "gender": "male",
  "birthDate": "1980-01-15",
  "telecom": [{
    "system": "phone",
    "value": "+250788123456"
  }],
  "address": [{
    "line": ["Kigali, Gasabo District"]
  }]
}
```

**Converter**: `FhirResourceConverter.convertPatientToFhir()`

##### 2. **Observation Resource** (Vital Signs)
```json
{
  "resourceType": "Observation",
  "status": "final",
  "category": [{
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/observation-category",
      "code": "vital-signs"
    }]
  }],
  "code": {
    "coding": [{
      "system": "http://loinc.org",
      "code": "85354-9",
      "display": "Blood pressure systolic"
    }]
  },
  "subject": {
    "reference": "Patient/123"
  },
  "effectiveDateTime": "2024-01-15T10:30:00Z",
  "valueQuantity": {
    "value": 120,
    "unit": "mmHg",
    "system": "http://unitsofmeasure.org",
    "code": "mm[Hg]"
  }
}
```

**LOINC Codes Used:**
- `85354-9` - Blood pressure systolic
- `8462-4` - Blood pressure diastolic
- `2339-0` - Glucose [Mass/volume] in Blood
- `39156-5` - Body mass index (BMI)

##### 3. **Condition Resource**
```json
{
  "resourceType": "Condition",
  "clinicalStatus": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/condition-clinical",
      "code": "active"
    }]
  },
  "verificationStatus": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/condition-ver-status",
      "code": "confirmed"
    }]
  },
  "code": {
    "coding": [{
      "system": "http://snomed.info/sct",
      "code": "38341003",
      "display": "Hypertensive disorder"
    }]
  },
  "subject": {
    "reference": "Patient/123"
  },
  "recordedDate": "2024-01-15"
}
```

**SNOMED CT Codes:**
- `38341003` - Hypertensive disorder
- `73211009` - Diabetes mellitus
- `15777000` - Pre-diabetes

##### 4. **QuestionnaireResponse Resource**
```json
{
  "resourceType": "QuestionnaireResponse",
  "status": "completed",
  "subject": {
    "reference": "Patient/123"
  },
  "authored": "2024-01-15T10:30:00Z",
  "item": [{
    "linkId": "family_history_diabetes",
    "text": "Family history of diabetes?",
    "answer": [{
      "valueBoolean": true
    }]
  }]
}
```

##### 5. **ServiceRequest Resource** (Referrals)
```json
{
  "resourceType": "ServiceRequest",
  "status": "active",
  "intent": "order",
  "code": {
    "coding": [{
      "system": "http://snomed.info/sct",
      "code": "3457005",
      "display": "Patient referral"
    }]
  },
  "subject": {
    "reference": "Patient/123"
  },
  "reasonCode": [{
    "text": "High blood pressure - needs specialist consultation"
  }]
}
```

#### Sync Strategy

**Fetch-Only Policy for Patients:**
- Only Patient resources are fetched from FHIR server
- All other resources (Observations, Conditions, etc.) stored locally
- No automatic POST to server (data sovereignty)

**Background Sync Service** (`services/FhirSyncService.java`):
```java
// Scheduled via WorkManager every 15 minutes
public class FhirSyncService extends Service {
    @Override
    public void syncData() {
        // Fetch updated patients if needed
        // Queue local changes in outbox
        // Process outbox when online
    }
}
```

**Outbox Pattern:**
1. Local changes saved to `fhir_outbox` table
2. Background service processes outbox
3. Successful uploads remove from outbox
4. Failed uploads remain for retry

#### API Endpoints

**Defined in** `api/FhirApiService.java`:

```java
public interface FhirApiService {
    // GET Patient by ID
    @GET("Patient/{patientId}")
    Call<Object> getPatient(@Path("patientId") String patientId);
    
    // Additional endpoints can be added:
    // POST Patient
    // @POST("Patient")
    // Call<Object> createPatient(@Body JsonObject patient);
    
    // GET Observation
    // @GET("Observation")
    // Call<Bundle> getObservations(@Query("subject") String patientId);
}
```

#### Error Handling

**Network Errors:**
```java
try {
    Response<Object> response = apiCall.execute();
    if (response.isSuccessful()) {
        // Handle success
    } else {
        // HTTP error (4xx, 5xx)
        Log.e(TAG, "Error: " + response.code());
    }
} catch (IOException e) {
    // Network failure
    Log.e(TAG, "Network error", e);
}
```

**Offline Support:**
- All API calls wrapped in try-catch
- Fallback to local data on failure
- Queue for retry when online

#### Customization

**To Use Different FHIR Server:**

1. Update base URL in `FhirApiService.java`:
```java
String BASE_URL = "https://your-server.com/fhir/";
```

2. Add authentication if required:
```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();
        return chain.proceed(request);
    })
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

3. Implement additional FHIR operations as needed

---

## 📊 Risk Scoring Algorithm

The application uses a comprehensive risk scoring system to assess NCD risk based on clinical guidelines.

### Algorithm Overview

**Total Score Range**: 0-100 (capped at 100)

**Risk Categories:**
- **Minimal Risk**: < 10 points (Green)
- **Low Risk**: 10-24 points (Blue)
- **Moderate Risk**: 25-49 points (Yellow)
- **High Risk**: ≥ 50 points (Red)

### Scoring Components

#### 1. **Blood Pressure (Systolic)**

| Systolic BP (mmHg) | Points | Category |
|-------------------|--------|----------|
| ≥ 180 | 25 | Crisis |
| 140-179 | 15 | Stage 2 Hypertension |
| 130-139 | 10 | Stage 1 Hypertension |
| 120-129 | 5 | Elevated |
| < 120 | 0 | Normal |

#### 2. **Blood Pressure (Diastolic)**

| Diastolic BP (mmHg) | Points | Category |
|--------------------|--------|----------|
| ≥ 120 | 25 | Crisis |
| 90-119 | 15 | Stage 2 Hypertension |
| 80-89 | 5 | Elevated |
| < 80 | 0 | Normal |

#### 3. **Blood Glucose (Fasting)**

| Glucose (mg/dL) | Points | Category |
|----------------|--------|----------|
| ≥ 200 | 25 | Diabetic Range |
| 140-199 | 15 | High |
| 100-139 | 5 | Prediabetic |
| < 100 | 0 | Normal |

#### 4. **Body Mass Index (BMI)**

| BMI (kg/m²) | Points | Category |
|-------------|--------|----------|
| ≥ 30 | 10 | Obese |
| 25-29.9 | 5 | Overweight |
| 18.5-24.9 | 0 | Normal |
| < 18.5 | 0 | Underweight |

#### 5. **Questionnaire Risk Factors**

| Risk Factor | Points |
|-------------|--------|
| Family history of diabetes | 10 |
| Family history of hypertension | 10 |
| Current smoker | 8 |
| Physical inactivity | 5 |
| Unhealthy diet | 5 |

### Implementation

**Code Location**: `utils/RiskScoringUtils.java`

```java
public static int calculateOverallRiskScore(
    List<Observation> observations, 
    List<Questionnaire> questionnaires
) {
    int riskScore = 0;
    
    // Process vital signs
    for (Observation obs : observations) {
        String type = obs.getObservationType();
        double value = obs.getValue();
        
        if ("blood_pressure_systolic".equals(type)) {
            if (value >= 180) riskScore += 25;
            else if (value >= 140) riskScore += 15;
            else if (value >= 130) riskScore += 10;
            else if (value >= 120) riskScore += 5;
        }
        // ... other observations
    }
    
    // Process questionnaire
    for (Questionnaire q : questionnaires) {
        if ("yes".equalsIgnoreCase(q.getAnswer())) {
            riskScore += getQuestionPoints(q.getQuestionCode());
        }
    }
    
    // Cap at 100
    return Math.min(riskScore, 100);
}
```

### Clinical Validation

**Data Validation** (`utils/HealthDataValidator.java`):

```java
public static String validateBloodPressure(int systolic, int diastolic) {
    if (systolic >= 180 || diastolic >= 120) {
        return "CRISIS - Seek immediate medical attention";
    } else if (systolic >= 140 || diastolic >= 90) {
        return "HIGH - Stage 2 Hypertension";
    } else if (systolic >= 130 || diastolic >= 80) {
        return "ELEVATED - Stage 1 Hypertension";
    } else if (systolic >= 120) {
        return "ELEVATED - Monitor regularly";
    } else {
        return "NORMAL";
    }
}

public static String validateGlucose(double glucose) {
    if (glucose >= 126) {
        return "DIABETIC RANGE - Consult physician";
    } else if (glucose >= 100) {
        return "PREDIABETIC - Lifestyle changes recommended";
    } else {
        return "NORMAL";
    }
}

public static double calculateBMI(double weightKg, double heightCm) {
    double heightM = heightCm / 100.0;
    return weightKg / (heightM * heightM);
}
```

### Condition Identification

Conditions are automatically identified when thresholds are exceeded:

```java
public void identifyConditions(Screening screening) {
    List<Condition> conditions = new ArrayList<>();
    
    // Check blood pressure
    Observation systolic = getObservation(screening, "blood_pressure_systolic");
    Observation diastolic = getObservation(screening, "blood_pressure_diastolic");
    
    if (systolic.getValue() >= 140 || diastolic.getValue() >= 90) {
        Condition hypertension = new Condition();
        hypertension.setConditionCode("38341003"); // SNOMED CT
        hypertension.setConditionName("Hypertension");
        conditions.add(hypertension);
    }
    
    // Check glucose
    Observation glucose = getObservation(screening, "glucose");
    if (glucose.getValue() >= 126) {
        Condition diabetes = new Condition();
        diabetes.setConditionCode("73211009");
        diabetes.setConditionName("Diabetes Mellitus");
        conditions.add(diabetes);
    }
    
    screening.setConditions(conditions);
}
```

### Risk Level Descriptions

**Generated Recommendations:**

```java
public static String getRiskRecommendation(int score) {
    if (score >= 50) {
        return "HIGH RISK: Immediate medical consultation recommended. " +
               "Multiple risk factors identified requiring professional assessment.";
    } else if (score >= 25) {
        return "MODERATE RISK: Schedule check-up with healthcare provider. " +
               "Lifestyle modifications and monitoring advised.";
    } else if (score >= 10) {
        return "LOW RISK: Continue healthy lifestyle. " +
               "Regular screening recommended.";
    } else {
        return "MINIMAL RISK: Maintain current healthy practices. " +
               "Annual screening suggested.";
    }
}
```

---

## 📁 Project Structure

```
ncd-screener-android-main/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/ncdscreener/
│   │   │   │   ├── activities/
│   │   │   │   │   ├── LoginActivity.java           # Authentication screen
│   │   │   │   │   └── MainActivity.java             # Main container activity
│   │   │   │   │
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── PatientAdapter.java           # Patient list RecyclerView
│   │   │   │   │   ├── ScreeningHistoryAdapter.java  # Screening history list
│   │   │   │   │   ├── ObservationAdapter.java       # Observations display
│   │   │   │   │   ├── ConditionAdapter.java         # Conditions display
│   │   │   │   │   ├── QuestionnaireAdapter.java     # Questionnaire list
│   │   │   │   │   └── ServiceRequestAdapter.java    # Referrals list
│   │   │   │   │
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiClient.java                # Retrofit client setup
│   │   │   │   │   └── FhirApiService.java           # FHIR API endpoints
│   │   │   │   │
│   │   │   │   ├── database/
│   │   │   │   │   ├── NCDScreenerDatabase.java      # Main Room database
│   │   │   │   │   ├── AppDatabase.java              # FHIR outbox database
│   │   │   │   │   │
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── PatientDao.java           # Patient data access
│   │   │   │   │   │   ├── ScreeningDao.java         # Screening data access
│   │   │   │   │   │   ├── ObservationDao.java       # Observation data access
│   │   │   │   │   │   ├── ConditionDao.java         # Condition data access
│   │   │   │   │   │   ├── QuestionnaireDao.java     # Questionnaire data access
│   │   │   │   │   │   ├── ServiceRequestDao.java    # Service request data access
│   │   │   │   │   │   ├── CHWDao.java               # CHW data access
│   │   │   │   │   │   ├── FhirOutboxDao.java        # Sync queue data access
│   │   │   │   │   │   └── FhirResourceDao.java      # FHIR cache data access
│   │   │   │   │   │
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── PatientEntity.java        # Patient table entity
│   │   │   │   │       ├── ScreeningEntity.java      # Screening table entity
│   │   │   │   │       ├── ObservationEntity.java    # Observation table entity
│   │   │   │   │       ├── ConditionEntity.java      # Condition table entity
│   │   │   │   │       ├── QuestionnaireEntity.java  # Questionnaire table entity
│   │   │   │   │       ├── ServiceRequestEntity.java # Service request table entity
│   │   │   │   │       ├── CHWEntity.java            # CHW table entity
│   │   │   │   │       ├── FhirOutboxEntity.java     # Sync queue table entity
│   │   │   │   │       └── FhirResourceEntity.java   # FHIR cache table entity
│   │   │   │   │
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── HomeFragment.java             # Dashboard screen
│   │   │   │   │   ├── PatientListFragment.java      # Patient list screen
│   │   │   │   │   ├── RegisterPatientFragment.java  # Patient registration form
│   │   │   │   │   ├── PatientDetailFragment.java    # Patient details screen
│   │   │   │   │   ├── ScreeningFormFragment.java    # Screening data entry
│   │   │   │   │   ├── ScreeningResultsFragment.java # Screening results display
│   │   │   │   │   ├── ScreeningDetailFragment.java  # Screening detail view
│   │   │   │   │   ├── ProfileFragment.java          # CHW profile screen
│   │   │   │   │   └── SettingsFragment.java         # App settings screen
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── Patient.java                  # Patient domain model
│   │   │   │   │   ├── Screening.java                # Screening domain model
│   │   │   │   │   ├── Observation.java              # Observation domain model
│   │   │   │   │   ├── Condition.java                # Condition domain model
│   │   │   │   │   ├── Questionnaire.java            # Questionnaire domain model
│   │   │   │   │   ├── ServiceRequest.java           # Service request domain model
│   │   │   │   │   └── CHW.java                      # CHW domain model
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── PatientRepository.java        # Patient data repository
│   │   │   │   │   ├── ScreeningRepository.java      # Screening data repository
│   │   │   │   │   ├── ObservationRepository.java    # Observation data repository
│   │   │   │   │   ├── ConditionRepository.java      # Condition data repository
│   │   │   │   │   ├── QuestionnaireRepository.java  # Questionnaire data repository
│   │   │   │   │   ├── ServiceRequestRepository.java # Service request data repository
│   │   │   │   │   └── CHWRepository.java            # CHW data repository
│   │   │   │   │
│   │   │   │   ├── services/
│   │   │   │   │   ├── FhirSyncService.java          # Background sync service
│   │   │   │   │   └── SyncManager.java              # Sync coordination
│   │   │   │   │
│   │   │   │   ├── utils/
│   │   │   │   │   ├── RiskScoringUtils.java         # Risk calculation logic
│   │   │   │   │   ├── FhirResourceConverter.java    # FHIR conversion utilities
│   │   │   │   │   ├── HealthDataValidator.java      # Clinical validation
│   │   │   │   │   ├── SessionManager.java           # Authentication session
│   │   │   │   │   ├── ThemeManager.java             # Theme switching
│   │   │   │   │   ├── LocaleHelper.java             # Multi-language support
│   │   │   │   │   ├── EntityConverter.java          # Entity ↔ Model conversion
│   │   │   │   │   ├── AccessibilityHelper.java      # Accessibility support
│   │   │   │   │   └── DataExporter.java             # Data export utilities
│   │   │   │   │
│   │   │   │   └── viewmodel/
│   │   │   │       ├── PatientViewModel.java         # Patient UI logic
│   │   │   │       ├── ScreeningViewModel.java       # Screening UI logic
│   │   │   │       └── CHWViewModel.java             # CHW UI logic
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/                           # XML layouts
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── fragment_home.xml
│   │   │   │   │   ├── fragment_patient_list.xml
│   │   │   │   │   ├── fragment_register_patient.xml
│   │   │   │   │   ├── fragment_screening_form.xml
│   │   │   │   │   ├── fragment_screening_results.xml
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   ├── navigation/
│   │   │   │   │   └── nav_graph.xml                 # Navigation graph
│   │   │   │   │
│   │   │   │   ├── menu/
│   │   │   │   │   ├── bottom_nav_menu.xml           # Bottom navigation
│   │   │   │   │   └── main_menu.xml                 # Toolbar menu
│   │   │   │   │
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml                   # English strings
│   │   │   │   │   ├── colors.xml                    # Color palette
│   │   │   │   │   ├── themes.xml                    # App themes
│   │   │   │   │   └── dimens.xml                    # Dimensions
│   │   │   │   │
│   │   │   │   ├── values-fr/                        # French translations
│   │   │   │   │   └── strings.xml
│   │   │   │   │
│   │   │   │   ├── values-rw/                        # Kinyarwanda translations
│   │   │   │   │   └── strings.xml
│   │   │   │   │
│   │   │   │   ├── values-night/                     # Dark theme overrides
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   │
│   │   │   │   ├── drawable/                         # Images and icons
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   └── mipmap/                           # App icons
│   │   │   │       └── ...
│   │   │   │
│   │   │   └── AndroidManifest.xml                   # App manifest
│   │   │
│   │   ├── androidTest/                              # Instrumented tests
│   │   │   └── java/com/example/ncdscreener/
│   │   │       └── ExampleInstrumentedTest.java
│   │   │
│   │   └── test/                                     # Unit tests
│   │       └── java/com/example/ncdscreener/
│   │           └── ExampleUnitTest.java
│   │
│   ├── build.gradle.kts                              # App-level Gradle config
│   └── proguard-rules.pro                            # ProGuard rules
│
├── gradle/
│   └── libs.versions.toml                            # Version catalog
│
├── build.gradle.kts                                  # Project-level Gradle config
├── settings.gradle.kts                               # Gradle settings
├── gradle.properties                                 # Gradle properties
├── gradlew                                           # Gradle wrapper (Unix)
├── gradlew.bat                                       # Gradle wrapper (Windows)
├── .gitignore                                        # Git ignore rules
└── README.md                                         # This file
```

### Key Directory Descriptions

- **activities/**: Contains Activity classes (app entry points)
- **adapters/**: RecyclerView adapters for list displays
- **api/**: Retrofit API client and service definitions
- **database/**: Room database, DAOs, and entities
- **fragments/**: Fragment classes (UI screens)
- **model/**: Domain models (business logic layer)
- **repository/**: Data access layer (single source of truth)
- **services/**: Background services and managers
- **utils/**: Utility classes and helpers
- **viewmodel/**: ViewModel classes (MVVM pattern)
- **res/**: Android resources (layouts, strings, drawables)

---

## ⚙️ Configuration

### Application Configuration

**AndroidManifest.xml:**
```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.NCDScreener">
    
    <!-- Login is the launcher activity -->
    <activity android:name=".activities.LoginActivity"
              android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    
    <activity android:name=".activities.MainActivity"
              android:exported="false" />
    
    <!-- Background sync service -->
    <service android:name=".services.FhirSyncService"
             android:exported="false" />
</application>
```

### Gradle Configuration

**app/build.gradle.kts:**
```kotlin
android {
    namespace = "rw.ac.ncdscreener"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "rw.ac.ncdscreener"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

### Database Configuration

**NCDScreenerDatabase.java:**
```java
@Database(
    entities = {
        CHWEntity.class,
        PatientEntity.class,
        ScreeningEntity.class,
        ObservationEntity.class,
        ConditionEntity.class,
        QuestionnaireEntity.class,
        ServiceRequestEntity.class
    },
    version = 3,
    exportSchema = false
)
public abstract class NCDScreenerDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ncd_screener_database";
    
    // Use fallbackToDestructiveMigration for development
    // Implement proper migrations for production
    public static NCDScreenerDatabase getDatabase(Context context) {
        return Room.databaseBuilder(
            context.getApplicationContext(),
            NCDScreenerDatabase.class,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build();
    }
}
```

### Session Configuration

**SessionManager.java:**
```java
// Session duration: 1 hour
private static final long SESSION_DURATION = 60 * 60 * 1000;

// Preferences name
private static final String PREF_NAME = "NCDScreenerPrefs";
```

### Sync Configuration

**SyncManager.java:**
```java
// Sync interval: 15 minutes
private static final long SYNC_INTERVAL = 15;
private static final TimeUnit SYNC_INTERVAL_UNIT = TimeUnit.MINUTES;

public void schedulePeriodicSync() {
    PeriodicWorkRequest syncRequest = 
        new PeriodicWorkRequest.Builder(
            SyncWorker.class,
            SYNC_INTERVAL,
            SYNC_INTERVAL_UNIT
        )
        .setConstraints(
            new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build();
    
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "fhir_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        );
}
```

### Theme Configuration

**values/themes.xml:**
```xml
<style name="Theme.NCDScreener" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/primary</item>
    <item name="colorPrimaryVariant">@color/primary_dark</item>
    <item name="colorOnPrimary">@color/white</item>
    <item name="colorSecondary">@color/secondary</item>
    <!-- Material 3 theme attributes -->
</style>
```

### String Resources

**values/strings.xml** (English)
**values-fr/strings.xml** (French)
**values-rw/strings.xml** (Kinyarwanda)

---

## 🔧 Development

### Prerequisites for Development
- Android Studio Flamingo or later
- JDK 11 or later
- Android SDK API 24+
- Git

### Setting Up Development Environment

1. **Clone and Configure**
   ```bash
   git clone <repository-url>
   cd ncd-screener-android
   ```

2. **Configure Signing (for release builds)**
   Create `keystore.properties`:
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=your_key_alias
   storeFile=path/to/keystore.jks
   ```

3. **Run on Emulator/Device**
   ```bash
   ./gradlew installDebug
   ```

### Code Style Guidelines

**Java Code Style:**
- Follow Android Code Style Guide
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable names
- Add Javadoc comments for public methods

**Example:**
```java
/**
 * Calculates BMI from weight and height
 * @param weightKg Weight in kilograms
 * @param heightCm Height in centimeters
 * @return BMI value
 */
public static double calculateBMI(double weightKg, double heightCm) {
    double heightM = heightCm / 100.0;
    return weightKg / (heightM * heightM);
}
```

### Testing

**Run Unit Tests:**
```bash
./gradlew test
```

**Run Instrumented Tests:**
```bash
./gradlew connectedAndroidTest
```

**Test Coverage:**
```bash
./gradlew jacocoTestReport
```

### Debugging

**Enable Logging:**
```java
private static final String TAG = "NCDScreener";
Log.d(TAG, "Debug message");
Log.e(TAG, "Error message", exception);
```

**Database Inspector:**
- Android Studio → View → Tool Windows → App Inspection
- Select running device → Database Inspector
- Browse tables and execute queries

**Network Inspector:**
- Add logging interceptor (already configured)
- View API calls in Logcat

### Building Release APK

```bash
# Clean build
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Version Management

**Update Version:**
In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1"  // Semantic versioning
}
```

### Database Migrations

**Add Migration:**
```java
static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // SQL migration statements
        database.execSQL("ALTER TABLE patients ADD COLUMN email TEXT");
    }
};

// In database builder:
.addMigrations(MIGRATION_3_4)
```

### Common Tasks

**Add New Dependency:**
```kotlin
// In app/build.gradle.kts
dependencies {
    implementation("com.library:name:version")
}
```

**Add New Fragment:**
1. Create fragment class in `fragments/`
2. Create layout XML in `res/layout/`
3. Add to navigation graph `res/navigation/nav_graph.xml`
4. Add navigation action if needed

**Add New Database Table:**
1. Create entity class in `database/entity/`
2. Create DAO interface in `database/dao/`
3. Add entity to database class `@Database` annotation
4. Increment database version
5. Add migration if needed

---

## 🤝 Contributing

We welcome contributions to improve the NCD Screener application!

### How to Contribute

1. **Fork the Repository**
   - Click "Fork" on GitHub
   - Clone your fork locally

2. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Changes**
   - Follow code style guidelines
   - Add tests if applicable
   - Update documentation

4. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```
   
   **Commit Message Format:**
   - `feat:` new feature
   - `fix:` bug fix
   - `docs:` documentation changes
   - `style:` formatting changes
   - `refactor:` code refactoring
   - `test:` adding tests
   - `chore:` maintenance tasks

5. **Push and Create Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```
   - Go to GitHub and create Pull Request
   - Describe your changes clearly
   - Reference any related issues

### Areas for Contribution

**High Priority:**
- [ ] Implement password hashing for CHW accounts
- [ ] Add proper database migrations
- [ ] Implement automated tests (unit + integration)
- [ ] Add FHIR POST operations for complete sync
- [ ] Improve error handling and user feedback

**Medium Priority:**
- [ ] Add data export functionality
- [ ] Implement offline data conflict resolution
- [ ] Add more questionnaire types
- [ ] Enhance accessibility features
- [ ] Add charting for screening history

**Enhancement Ideas:**
- [ ] Add photo capture for patient identification
- [ ] Implement barcode scanning for patient ID
- [ ] Add report generation (PDF export)
- [ ] Implement multi-CHW collaboration
- [ ] Add analytics dashboard

### Code Review Process
1. All PRs require review before merging
2. Automated checks must pass
3. Code should follow style guidelines
4. Tests should be added for new features

### Reporting Issues

**Bug Reports:**
- Use GitHub Issues
- Include device/OS version
- Provide steps to reproduce
- Include screenshots if applicable

**Feature Requests:**
- Describe the feature clearly
- Explain the use case
- Suggest implementation approach if possible

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 NCD Screener Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 Contact & Support

### Project Team
- **Project Lead**: [Name] - [email@example.com]
- **Technical Lead**: [Name] - [email@example.com]

### Getting Help
- **Documentation**: This README and inline code comments
- **Issues**: GitHub Issues for bug reports and questions
- **Discussions**: GitHub Discussions for general questions

### Acknowledgments
- **FHIR Community**: For healthcare interoperability standards
- **HAPI FHIR**: For excellent FHIR implementation libraries
- **Android Team**: For comprehensive development tools
- **Contributors**: All developers who contribute to this project

---

## 🔮 Roadmap

### Version 1.1 (Next Release)
- [ ] Enhanced security (password hashing)
- [ ] Improved sync reliability
- [ ] Additional questionnaire templates
- [ ] Performance optimizations

### Version 1.2
- [ ] Advanced analytics and reporting
- [ ] Multi-clinic support
- [ ] Supervisor dashboard
- [ ] Data visualization charts

### Version 2.0
- [ ] Machine learning risk prediction
- [ ] Integration with national health systems
- [ ] Real-time collaboration features
- [ ] Advanced FHIR operations

---

## 📊 Statistics

- **Total Lines of Code**: ~15,000
- **Total Files**: ~80 Java files
- **Database Tables**: 9
- **Supported Languages**: 3 (English, French, Kinyarwanda)
- **Minimum Android Version**: 7.0 (API 24)
- **Latest Android Version**: 14+ (API 36)

---

## ⚠️ Important Notes

### Security Considerations
- **Passwords**: Currently stored in plain text - implement hashing before production use
- **HTTPS**: Always use HTTPS for FHIR API connections
- **Data Privacy**: Ensure compliance with local healthcare data regulations
- **Authentication**: Consider implementing OAuth2 or similar for production

### Production Deployment Checklist
- [ ] Implement password hashing
- [ ] Configure ProGuard/R8 for code obfuscation
- [ ] Set up proper database migrations
- [ ] Configure SSL certificate pinning
- [ ] Remove debug logging
- [ ] Test on multiple devices/screen sizes
- [ ] Perform security audit
- [ ] Set up crash reporting (e.g., Firebase Crashlytics)
- [ ] Configure analytics
- [ ] Prepare app store assets

### Known Limitations
- Session timeout is fixed at 1 hour (not configurable)
- Database uses destructive migration (data loss on schema changes)
- Limited offline conflict resolution
- No automated backup mechanism
- Single CHW session (no multi-user support on same device)

---

## 🙏 Thank You

Thank you for using the NCD Screener application. This tool is designed to empower Community Health Workers and improve healthcare delivery in underserved areas. Your feedback and contributions help make this application better for everyone.

**Together, we can improve health outcomes through technology.**

---

*Last Updated: January 2024*  
*Version: 1.0*  
*Maintained by: NCD Screener Development Team*
