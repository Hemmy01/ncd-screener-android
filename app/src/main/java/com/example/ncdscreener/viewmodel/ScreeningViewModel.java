package com.example.ncdscreener.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.repository.ScreeningRepository;

import java.util.List;

/**
 * ViewModel for Screening management
 * Manages screening data and operations
 */
public class ScreeningViewModel extends AndroidViewModel {
    private ScreeningRepository repository;
    private MutableLiveData<Screening> currentScreeningLiveData;

    public ScreeningViewModel(Application application) {
        super(application);
        repository = new ScreeningRepository(application);
        currentScreeningLiveData = new MutableLiveData<>();
    }

    public LiveData<List<com.example.ncdscreener.database.entity.ScreeningEntity>> getScreenings() {
        return repository.getAllScreenings();
    }

    public LiveData<Screening> getCurrentScreening() {
        return currentScreeningLiveData;
    }

    public void saveScreening(Screening screening) {
        repository.saveScreeningLocally(screening);
    }

    public void setCurrentScreening(Screening screening) {
        currentScreeningLiveData.setValue(screening);
    }

    public Screening getScreeningById(int screeningId) {
        // This would need patient and CHW to be passed
        // For now, return null - can be enhanced later
        return null;
    }
}

