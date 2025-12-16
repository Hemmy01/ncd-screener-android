package com.example.ncdscreener.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ncdscreener.database.NCDScreenerDatabase;
import com.example.ncdscreener.database.dao.CHWDao;
import com.example.ncdscreener.database.entity.CHWEntity;
import com.example.ncdscreener.model.CHW;
import com.example.ncdscreener.utils.EntityConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class for managing CHW data
 */
public class CHWRepository {
    private CHWDao chwDao;
    private ExecutorService executorService;
    private Handler mainHandler;

    public interface CHWCallback {
        void onResult(CHW chw);
    }

    public interface CHWCreateCallback {
        void onComplete(boolean success, String message);
    }

    public interface PasswordResetCallback {
        void onComplete(boolean success, String message);
    }

    public CHWRepository(Context context) {
        this.chwDao = NCDScreenerDatabase.getDatabase(context).chwDao();
        this.executorService = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Authenticates CHW with username and password
     */
    public void authenticate(String username, String password, CHWCallback callback) {
        executorService.execute(() -> {
            CHWEntity entity = chwDao.authenticate(username, password);
            CHW chw = entity != null ? EntityConverter.fromEntity(entity) : null;
            mainHandler.post(() -> callback.onResult(chw));
        });
    }

    /**
     * Gets CHW by username
     */
    public void getCHWByUsername(String username, CHWCallback callback) {
        executorService.execute(() -> {
            CHWEntity entity = chwDao.getCHWByUsername(username);
            CHW chw = entity != null ? EntityConverter.fromEntity(entity) : null;
            mainHandler.post(() -> callback.onResult(chw));
        });
    }

    /**
     * Creates a CHW account if username is not taken
     */
    public void createCHW(CHW chw, CHWCreateCallback callback) {
        executorService.execute(() -> {
            CHWEntity existing = chwDao.getCHWByUsernameSync(chw.getUsername());
            if (existing != null) {
                mainHandler.post(() -> callback.onComplete(false, "Username already exists"));
                return;
            }
            CHWEntity entity = EntityConverter.toEntity(chw);
            long id = chwDao.insertCHW(entity);
            if (id > 0 && chw.getChwId() == 0) {
                chw.setChwId((int) id);
            }
            mainHandler.post(() -> callback.onComplete(true, "Account created"));
        });
    }

    /**
     * Resets password for a username
     */
    public void resetPassword(String username, String newPassword, PasswordResetCallback callback) {
        executorService.execute(() -> {
            CHWEntity existing = chwDao.getCHWByUsernameSync(username);
            if (existing == null) {
                mainHandler.post(() -> callback.onComplete(false, "User not found"));
                return;
            }
            chwDao.updatePassword(username, newPassword);
            mainHandler.post(() -> callback.onComplete(true, "Password updated"));
        });
    }

    /**
     * Gets CHW by ID
     */
    public void getCHWById(int chwId, CHWCallback callback) {
        executorService.execute(() -> {
            CHWEntity entity = chwDao.getCHWById(chwId);
            CHW chw = entity != null ? EntityConverter.fromEntity(entity) : null;
            mainHandler.post(() -> callback.onResult(chw));
        });
    }

    /**
     * Saves CHW to database
     */
    public void saveCHW(CHW chw) {
        executorService.execute(() -> {
            CHWEntity entity = EntityConverter.toEntity(chw);
            long id = chwDao.insertCHW(entity);
            if (id > 0 && chw.getChwId() == 0) {
                chw.setChwId((int) id);
            }
        });
    }

    /**
     * Deletes CHW
     */
    public void deleteCHW(int chwId) {
        executorService.execute(() -> {
            chwDao.deleteCHW(chwId);
        });
    }
}

