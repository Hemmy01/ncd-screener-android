# NCD Screener – Non-Communicable Disease Screening App

## Project Proposal

**Project Name:** `final-project-group-af`  
**Course:** Mobile Programming  
**Group:** AF  
**Submission Date:** 9th November 2025

---

## Team Composition

| Name                             | Student ID | Primary Responsibility                  |
| -------------------------------- | ---------- | --------------------------------------- |
| Uwera Masereri Prisca            | 25570      | Patient Screening Data Management       |
| Hirwa Germain                    | 25571      | Questionnaire and Risk Factor Interface |
| Ikuzwe Nfuranzima O’neal Dauphin | 24714      | Observation Data Capture (BP, Glucose)  |
| Familoni Emmanuel Eniola         | 25951      | FHIR API Integration                    |
| Iradukunda Oscar                 | 26281      | Data Processing & Parsing               |
| Gahunde Simbi Gloria             | 25435      | Referral & Counseling Module            |
| Murenzi Munyaburanga Ivan        | 25868      | User Interface Design                   |
| Mbabazi Yvette                   | 25946      | Application Navigation                  |
| Ishimwe Alain Pacifique          | 26567      | Quality Assurance                       |

---

## 1. Project Concept

### Problem Statement

Non-communicable diseases (NCDs) such as hypertension and diabetes are leading causes of morbidity and mortality worldwide. In many communities, early detection is hindered by limited screening tools and poor follow-up systems. Community health workers often lack digital tools to efficiently collect and manage NCD screening data.

### Proposed Solution

The **NCD Screener** is an Android-based mobile application designed to assist community health workers in screening adults for common NCDs such as hypertension and diabetes. The app records vital signs, risk factors, and screening outcomes, and provides basic counseling or referral recommendations based on results.

---

## 2. FHIR Integration Strategy

### Core FHIR Resources Utilization

Our project leverages the **FHIR R4** standard to represent clinical and screening data.

**Primary Resources:**

- **`Patient`** – Basic demographic data of the screened individual
- **`Observation`** – Captured vital signs such as blood pressure, BMI, and glucose level
- **`QuestionnaireResponse`** – Answers from screening questionnaires (lifestyle, symptoms, etc.)
- **`Condition`** – Screening results indicating possible hypertension or diabetes
- **`ServiceRequest`** – Referrals for further testing or clinical follow-up

### Example FHIR Endpoints

```http
# Retrieve patient demographic info
GET /Patient/{patientId}

# Record a new blood pressure observation
POST /Observation

# Submit questionnaire response
POST /QuestionnaireResponse

# Record screening outcome (e.g., hypertension detected)
POST /Condition

# Create referral to health facility
POST /ServiceRequest
```
