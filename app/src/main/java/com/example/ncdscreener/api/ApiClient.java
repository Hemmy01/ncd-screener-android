package com.example.ncdscreener.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit API Client for FHIR server communication
 */
public class ApiClient {
    private static Retrofit retrofit;
    private static FhirApiService apiService;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(FhirApiService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static FhirApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(FhirApiService.class);
        }
        return apiService;
    }
}

