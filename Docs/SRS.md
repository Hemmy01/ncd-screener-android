# Software Requirements Specification (SRS)

## NCD Screener – Non-Communicable Disease Screening App

# 1. Introduction

## 1.1 Purpose

This Software Requirements Specification (SRS) describes the requirements, system features, data flows, diagrams, and constraints for the NCD Screener, a mobile application designed to support community health workers in screening adults for common non-communicable diseases (NCDs) such as hypertension and diabetes.

## 1.2 Scope

The NCD Screener system will allow community health workers to:

- Register patients
- Record vital signs
- Capture questionnaire responses
- Calculate screening results
- Provide counseling advice
- Generate referrals
- Synchronize data to a FHIR server

## 1.3 Definitions and Acronyms

- NCD: Non-Communicable Disease
- FHIR: Fast Healthcare Interoperability Resources
- CHW: Community Health Worker
- BP: Blood Pressure
- MVVM: Model-View-ViewModel Architecture

## 1.4 References

- WHO NCD Screening Guidelines
- HAPI FHIR R4 Public Test Server
- Android Developer Documentation

---

# 2. Overall Description

## 2.1 System Perspective

The NCD Screener is a standalone Android application that integrates with an external FHIR R4 server via REST APIs. The application supports offline-first workflows using a local data cache.

## 2.2 System Users

Primary User:

- Community Health Worker (CHW)

External System:

- FHIR Server

## 2.3 High-Level System Functions

- Patient registration and lookup
- Conducting NCD screenings
- Data analysis and result generation
- Referral creation
- Data synchronization
- Reviewing screening history

## 2.4 Constraints

- Android OS, Java programming language
- Integration with FHIR R4 server
- Must support offline mode
- MVVM architecture enforced

---

# 3. Functional Requirements

## 3.1 Patient Registration

- The system shall allow CHWs to register new patients by entering demographic data.
- The system shall store patient data locally before syncing to the FHIR server.

## 3.2 Conduct NCD Screening

- The system shall allow CHWs to record vital signs including blood pressure, glucose, height, and weight.
- The system shall calculate BMI automatically.
- The system shall allow CHWs to complete a risk-factor questionnaire.
- The system shall compute screening outcomes based on collected data.

## 3.3 Counseling and Referral Generation

- The system shall provide automated counseling messages based on screening results.
- The system shall generate referrals using the FHIR ServiceRequest resource.

## 3.4 Data Synchronization

- The system shall synchronize local data with the FHIR server when internet is available.
- The system shall confirm synchronization success or failure.

## 3.5 Screening History

- The system shall allow CHWs to view past screening results for a selected patient.

---

# 4. Non-Functional Requirements

## 4.1 Performance

- Vital sign inputs must be processed in under 2 seconds.
- Sync operations must complete within 5 seconds on stable internet.

## 4.2 Usability

- The interface must be simple and intuitive for CHWs.
- All screens must remain readable under outdoor lighting conditions.

## 4.3 Reliability

- The system must cache all data offline before attempting sync.
- In the case of failed sync, no data loss should occur.

## 4.4 Security

- Only minimal patient identifiers will be stored.
- All network communication should use HTTPS when available.

---

# 5. Use Case Model

## 5.1 Use Case Diagram

![usecase diagram](assets/Usecase%20diagram.jpg)

This diagram includes:

- Actor: Community Health Worker
- Actor: FHIR Server
- Use Cases: Register New Patient, Conduct Screening, View Screening History, Generate Referral, Sync Data, Provide Counseling

## 5.2 Use Case Summaries

### UC01 – Register New Patient

- Actor: CHW
- Precondition: CHW logged in
- Postcondition: Patient stored locally

### UC02 – Search or Select Existing Patient

- Actor: CHW
- Precondition: Patient exists in local database
- Postcondition: Patient information displayed

### UC03 – Conduct NCD Screening

- Actor: CHW
- Includes: Record Vital Signs, Record Risk-Factor Questionnaire
- Postcondition: Screening results generated

### UC04 – View Screening Summary

- Actor: CHW
- Postcondition: Screening outcome displayed

### UC05 – Generate Referral

- Actor: CHW
- Postcondition: FHIR ServiceRequest created

### UC06 – Sync Data with FHIR Server

- Actor: CHW
- External System: FHIR Server

---

# 6. Data Flow Diagrams

## 6.1 DFD Level 0 (Context Diagram)

To be inserted as: `dfd_level0.png`

Main entities:

- Patient
- CHW
- FHIR Server
- NCD Screener System

## 6.2 DFD Level 1

Insert: `dfd_level1.png`

Processes included:
1.0 Patient Registration  
2.0 Conduct Screening  
3.0 Data Processing  
4.0 Generate Results and Referral  
5.0 Data Synchronization

Data Store:

- Local Data Cache

External Entities:

- Patient
- CHW
- FHIR Server

---

# 7. System Architecture

## 7.1 Architectural Pattern

The system uses MVVM (Model-View-ViewModel).

## 7.2 System Components

- Models: Patient, Observation, QuestionnaireResponse, Condition, ServiceRequest
- Repository: Handles local and remote data operations
- ViewModels: PatientViewModel, ScreeningViewModel, ReferralViewModel
- Views: Activities and Fragments

---

# 8. User Interface Prototypes

To be added later in folder `/ui_prototypes/`.

Recommended Screens:

- Login
- Home Dashboard
- Patient Registration
- Screening Form
- Questionnaire Screen
- Screening Summary
- Referral Creation

---

# 9. FHIR Integration Requirements

## 9.1 FHIR Resources Used

- Patient
- Observation
- QuestionnaireResponse
- Condition
- ServiceRequest

## 9.2 FHIR Endpoints

- POST /Patient
- POST /Observation
- POST /QuestionnaireResponse
- POST /Condition
- POST /ServiceRequest
- GET /Patient/{id}

## 9.3 FHIR Server

- HAPI FHIR Public R4 Test Server
- JSON-based REST implementation

---

# 10. Appendix

- Team roles
- Research notes
- Risk scoring documentation
