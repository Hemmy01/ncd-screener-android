package com.example.ncdscreener.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "fhir_resources",
        indices = {@Index(value = {"resourceType"}), @Index(value = {"localId"}), @Index(value = {"serverId"})})
public class FhirResource {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String localId;
    public String serverId;
    public String resourceType;
    public String resourceJson;
    public String serverJson;
    public String serverUrl;
    public String etag;
    public long lastModified;
    public long lastSynced;
    public boolean isDeleted;
}
