# AI Health Assistant

## Overview

AI Health Assistant is a multi-layered system designed to predict diseases based on user-provided symptoms. The system integrates an Android application, a Java-based backend, a Python machine learning service, and a native C++ preprocessing layer.

The architecture demonstrates end-to-end communication between mobile, backend, and machine learning components.

---

## Objectives

* Predict diseases using machine learning
* Provide real-time responses through a mobile interface
* Demonstrate integration of multiple technologies
* Implement a modular and scalable architecture

---

## System Architecture

```
Android Application (Java + XML)
        │
        ▼
C++ Native Layer (NDK - JNI)
        │
        ▼
Java Backend (Jakarta EE / JAX-RS)
        │
        ▼
Python Flask API
        │
        ▼
Machine Learning Model (Scikit-learn)
        │
        ▼
Prediction Response → Back to Android
```

---

## Project Structure

```
AI-Health-Assistant/
├── android-app/        # Android application (UI + API calls)
├── backend/            # Java backend (Jakarta EE)
├── ml-service/         # Python ML + Flask API
└── README.md
```

---

## Technology Stack

### Frontend (Mobile)

* Java (Android)
* XML (UI Layout)

### Backend

* Java (Jakarta EE / JAX-RS)
* Apache Tomcat

### Machine Learning

* Python
* Pandas
* NumPy
* Scikit-learn

### API Layer

* Flask (REST API)

### Native Layer

* C++ (Android NDK)
* JNI (Java Native Interface)

---

## Workflow

### Step 1: User Input

User enters symptoms in binary format:

```
1,1,1,1
```

---

### Step 2: Native Processing (C++)

* Input is passed to C++ via JNI
* Validates values (only 0 or 1 allowed)
* Returns sanitized data

---

### Step 3: Android Request

Android sends a JSON request to backend:

```
POST /health/predict
{
  "symptoms": [1,1,1,1]
}
```

---

### Step 4: Java Backend

* Receives request via REST API
* Forwards data to Flask API

---

### Step 5: Python ML Service

* Loads trained model (`model.pkl`)
* Predicts disease using input features

---

### Step 6: Response Flow

```
Python → Java → Android
```

---

### Step 7: Output

The application displays the predicted disease and a basic explanation.

---

## Machine Learning Model

* Algorithm: Decision Tree Classifier
* Dataset: Custom dataset (200 entries)
* Features:

  * Fever
  * Cough
  * Headache
  * Fatigue

### Training Pipeline

```
Dataset (CSV)
    │
    ▼
Data Processing (Pandas)
    │
    ▼
Model Training (Scikit-learn)
    │
    ▼
Model Export (Joblib → model.pkl)
```

---

## Key Components

### Android Application

* User interface for input
* Calls native C++ function
* Sends API requests
* Displays results

---

### C++ Native Module

* Function: `processSymptoms()`
* Role:

  * Input validation
  * Data preprocessing
* Integrated using JNI

---

### Java Backend

* REST API using Jakarta EE
* Endpoint:

  ```
  /health/predict
  ```
* Acts as middleware between mobile app and ML service

---

### Flask API (Python)

* Endpoint:

  ```
  /predict
  ```
* Handles ML inference

---

### Machine Learning Model

* Stored as:

  ```
  model.pkl
  ```
* Loaded dynamically during prediction

---

## Features

* Real-time disease prediction
* Multi-language integration (Java, Python, C++)
* REST API communication
* Native-level preprocessing
* Modular architecture

---

## Advantages

* Scalable and modular design
* Efficient input validation using C++
* Clear separation of concerns
* Easy integration of additional services

---

## Limitations

* Limited symptom set
* Simplified dataset
* No database integration
* Basic explanation system

---

## Future Enhancements

* Integration with database for history storage
* Use of generative AI for advanced explanations
* Expansion of dataset and symptoms
* User authentication and tracking

---

## How to Run

### 1. Run ML Service

```
cd ml-service
python app.py
```

---

### 2. Run Java Backend

* Open backend project in NetBeans
* Deploy on Apache Tomcat

---

### 3. Run Android Application

* Open project in Android Studio
* Build and run on emulator/device

---

## Conclusion

This project demonstrates a complete multi-tier system integrating mobile development, backend services, machine learning, and native programming. It provides a strong foundation for building advanced AI-driven healthcare applications.

---
