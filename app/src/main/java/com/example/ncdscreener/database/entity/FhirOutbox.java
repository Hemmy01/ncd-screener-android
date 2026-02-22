package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "fhir_outbox", 
        indices = {@Index(value = {"status"}), @Index(value = {"createdAt"})})
public class FhirOutbox {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String localId;
    public String resourceType;
    public String operation;
    public String resourceJson;
    public String status;
    public String serverId;
    public String serverUrl;
    public String idempotencyKey;
    public int retryCount;
    public long createdAt;
    public long lastRetryAt;
    public String errorMessage;
    public int httpStatusCode;
}
