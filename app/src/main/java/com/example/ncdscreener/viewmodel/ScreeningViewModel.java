package com.example.ncdscreener.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.ncdscreener.database.entity.ScreeningEntity;
import com.example.ncdscreener.model.Screening;
import com.example.ncdscreener.repository.ScreeningRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for Screening management
 * Manages screening data and operations
 */
public class ScreeningViewModel extends AndroidViewModel {
    private ScreeningRepository repository;
    private MutableLiveData<Screening> currentScreeningLiveData;
    private MutableLiveData<Screening> selectedScreeningLiveData;
    private ExecutorService executorService;

    public ScreeningViewModel(Application application) {
        super(application);
        repository = new ScreeningRepository(application);
        currentScreeningLiveData = new MutableLiveData<>();
        selectedScreeningLiveData = new MutableLiveData<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ScreeningEntity>> getScreenings() {
        return repository.getAllScreenings();
    }

    public LiveData<Screening> getCurrentScreening() {
        return currentScreeningLiveData;
    }

    public LiveData<Screening> getSelectedScreening() {
        return selectedScreeningLiveData;
    }

    public LiveData<ScreeningEntity> getScreeningById(int screeningId) {
        return repository.getScreeningById(screeningId);
    }

    public void loadScreeningDetail(int screeningId) {
        executorService.execute(() -> {
            Screening screening = repository.getScreeningDetailById(screeningId);
            selectedScreeningLiveData.postValue(screening);
        });
    }

    public void saveScreening(Screening screening) {
        repository.saveScreeningLocally(screening);
    }

    public void setCurrentScreening(Screening screening) {
        currentScreeningLiveData.setValue(screening);
    }

    public void deleteScreening(int screeningId) {
        repository.deleteScreening(screeningId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}

