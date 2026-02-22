package com.example.ncdscreener.service;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import com.example.ncdscreener.util.RetrofitClient;

public class FhirServerHealthChecker {
    private static final String TAG = "FhirServerHealthChecker";
    private static final int HEALTH_CHECK_TIMEOUT_SECONDS = 5;
    
    private static final String[] SERVER_URLS = {
        "https://fhirserver.hl7fundamentals.org/fhir/",
        "https://r4.smarthealthit.org/",
        "https://hapi.fhir.org/baseR4/"
    };
    
    public static class ServerHealth {
        public String url;
        public boolean isHealthy;
        public int responseCode;
        public String errorMessage;
        public long responseTimeMs;
        
        public ServerHealth(String url) {
            this.url = url;
            this.isHealthy = false;
        }
    }
    
    public static ServerHealth checkServerHealth(String baseUrl) {
        ServerHealth health = new ServerHealth(baseUrl);
        long startTime = System.currentTimeMillis();
        
        try {
            retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(new okhttp3.OkHttpClient.Builder()
                            .connectTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .readTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .build())
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build();
            
            FhirApiService apiService = retrofit.create(FhirApiService.class);
            Call<JsonObject> call = apiService.getMetadata();
            Response<JsonObject> response = call.execute();
            
            health.responseTimeMs = System.currentTimeMillis() - startTime;
            health.responseCode = response.code();
            health.isHealthy = response.isSuccessful();
            
            if (!health.isHealthy) {
                health.errorMessage = "HTTP " + response.code() + ": " + response.message();
            }
            
            Log.d(TAG, "Server " + baseUrl + " health: " + (health.isHealthy ? "HEALTHY" : "UNHEALTHY") + 
                    " (Code: " + health.responseCode + ", Time: " + health.responseTimeMs + "ms)");
            
        } catch (IOException e) {
            health.responseTimeMs = System.currentTimeMillis() - startTime;
            health.isHealthy = false;
            health.errorMessage = "Network error: " + e.getMessage();
            Log.w(TAG, "Server " + baseUrl + " health check failed: " + e.getMessage());
        } catch (Exception e) {
            health.responseTimeMs = System.currentTimeMillis() - startTime;
            health.isHealthy = false;
            health.errorMessage = "Error: " + e.getMessage();
            Log.e(TAG, "Server " + baseUrl + " health check error", e);
        }
        
        return health;
    }
    
    public static ServerHealth findHealthyServer() {
        for (String serverUrl : SERVER_URLS) {
            ServerHealth health = checkServerHealth(serverUrl);
            if (health.isHealthy) {
                Log.d(TAG, "Found healthy server: " + serverUrl);
                return health;
            }
        }
        
        Log.w(TAG, "No healthy servers found");
        return null;
    }
    
    public static List<ServerHealth> checkAllServers() {
        List<ServerHealth> healthyServers = new ArrayList<>();
        
        for (String serverUrl : SERVER_URLS) {
            ServerHealth health = checkServerHealth(serverUrl);
            if (health.isHealthy) {
                healthyServers.add(health);
            }
        }
        
        return healthyServers;
    }
}
