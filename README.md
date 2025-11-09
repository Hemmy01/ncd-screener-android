# MediTrack - Patient Medication Tracker

## Project Proposal
**Project Name:** `final-project-group-af`  
**Course:** Mobile Programming  
**Group:** AF  
**Submission Date:** [Current Date]

## Team Composition
| Name | Student ID | Primary Responsibility |
|------|------------|---------------------|
| Uwera Masereri Prisca | 25570 | Patient Data Management |
| Hirwa Germain | 25571 | Medication List Interface |
| Ikuzwe Nfuranzima O neal Dauphin | 24714 | Medication Details Display |
| Familoni Emmanuel Eniola | 25951 | FHIR API Integration |
| Iradukunda Oscar | 26281 | Data Processing & Parsing |
| Gahunde Simbi Gloria | 25435 | Notification System |
| Murenzi Munyaburanga Ivan | 25868 | User Interface Design |
| Mbabazi Yvette | 25946 | Application Navigation |
| Ishimwe Alain Pacifique | 26567 | Quality Assurance |

## 1. Project Concept

### Problem Statement
Patients often struggle with medication adherence due to complex prescription regimens, lack of clear dosage instructions, and forgetfulness. Current solutions lack seamless integration with standardized healthcare data systems, leading to medication errors and poor health outcomes.

### Proposed Solution
MediTrack is an Android application that leverages FHIR (Fast Healthcare Interoperability Resources) standards to provide patients with secure, real-time access to their prescribed medications. The app transforms complex medical data into an intuitive interface that promotes medication adherence through clear information display and timely reminders.

## 2. FHIR Integration Strategy

### Core FHIR Resources Utilization
Our solution will primarily utilize the **`MedicationRequest`** resource as the foundation for medication management, complemented by supporting resources:

**Primary Resources:**
- **`MedicationRequest`**: Core prescription data including:
  - Medication identification and coding
  - Dosage instructions (timing, frequency, route, quantity)
  - Prescription status and validity periods
  - Patient and prescriber references

**Supporting Resources:**
- **`Patient`**: Demographic context and identification
- **`Medication`**: Detailed pharmaceutical information

### FHIR API Implementation
We will implement the following FHIR RESTful API endpoints:

```http
# Retrieve active patient medications
GET /MedicationRequest?patient={patientId}&status=active

# Access patient demographic information
GET /Patient/{patientId}

# Fetch detailed medication information
GET /Medication/{medicationId}
```

### FHIR Server Configuration
- **Development Server**: HAPI FHIR Public Test Server (`http://hapi.fhir.org/baseR4`)
- **Data Standards**: FHIR R4 specification
- **Authentication**: Open access for prototype development

## 3. Technical Approach

### Development Framework
- **Platform**: Android Native
- **Programming Language**: Java
- **IDE**: Android Studio
- **Architecture Pattern**: MVVM (Model-View-ViewModel)

### Key Technical Components
1. **Data Layer**
   - Retrofit for FHIR API communication
   - Gson for JSON parsing of FHIR resources
   - Repository pattern for data abstraction

2. **Presentation Layer**
   - RecyclerView for medication lists
   - Fragment-based navigation
   - LiveData for reactive UI updates

3. **Business Logic Layer**
   - ViewModel for UI data management
   - WorkManager for notification scheduling
   - Custom utilities for FHIR data processing

## 4. Feature Specifications

### Core Features
1. **Medication Dashboard**
   - Display active prescriptions in organized list
   - Show medication status and next dose timing
   - Quick access to detailed information

2. **Dosage Management**
   - Clear presentation of dosage instructions
   - Administration route and frequency display
   - Timing and scheduling information

3. **Reminder System**
   - Configurable medication intake alerts
   - Push notification capabilities
   - Customizable scheduling options

4. **Patient Information**
   - Secure display of patient demographics
   - Medication history overview
   - Prescriber information access

## 5. Implementation Plan

### Phase 1: Foundation (Week 1-2)
- Project setup and repository configuration
- FHIR API integration and testing
- Basic UI framework establishment
- Data models and parsing implementation

### Phase 2: Core Features (Week 3-4)
- Medication list and detail interfaces
- Notification system development
- Navigation and user flow optimization
- Initial integration testing

### Phase 3: Refinement (Week 5-6)
- UI/UX polishing and styling
- Comprehensive testing and bug fixes
- Performance optimization
- Documentation completion

## 6. Expected Outcomes

### Technical Deliverables
- Fully functional Android application
- Complete FHIR API integration
- Comprehensive documentation
- Source code with version control history

### User Benefits
- Improved medication adherence through reminders
- Better understanding of prescription regimens
- Secure access to personal health information
- Enhanced patient engagement in healthcare

## 7. Innovation Value

This project demonstrates the practical application of FHIR standards in mobile health solutions, showcasing how standardized healthcare APIs can be leveraged to create patient-centric applications that bridge the gap between complex medical data and everyday healthcare management.

## 8. Compliance & Standards

- **FHIR R4** compliance for healthcare data interoperability
- **Android development** best practices
- **Data privacy** principles through read-only FHIR access
- **Healthcare standards** adherence through proper resource utilization

---

**Approval Requested:** This proposal outlines our approach to developing a FHIR-compliant medication tracking application that addresses real-world healthcare challenges through modern mobile technology and standardized data exchange.