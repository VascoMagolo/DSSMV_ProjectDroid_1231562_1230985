package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import rttc.dssmv_projectdroid_1231562_1230985.model.Translation;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationsRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;

/**
 * View Model for {@link rttc.dssmv_projectdroid_1231562_1230985.view.TranslationHistoryActivity}
 * Manages translation history operations: load, save, delete, update favorite status
 */
public class TranslationHistoryViewModel extends AndroidViewModel {

    private final TranslationsRepository translationsRepository;

    // LiveData for translations and operation results
    private final MutableLiveData<List<Translation>> _translations = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _favoriteUpdateSuccess = new MutableLiveData<>();

    public TranslationHistoryViewModel(@NonNull Application application) {
        super(application);
        translationsRepository = new TranslationsRepository();
    }

    public LiveData<List<Translation>> gettranslations() {
        return _translations;
    }
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }
    public LiveData<Boolean> getSaveSuccess() {
        return _saveSuccess;
    }
    public LiveData<Boolean> getDeleteSuccess() {
        return _deleteSuccess;
    }
    public LiveData<Boolean> getFavoriteUpdateSuccess() {
        return _favoriteUpdateSuccess;
    }

    /**
     * Loads all translations from the current user's history
     * Updates {@code _translations} LiveData on success
     * @param context Context for repository operations
     */
    public void loadTranslations(Context context) {
        translationsRepository.loadTranslations(context, new TranslationsRepository.LoadCallback() {
            @Override
            public void onSuccess(List<Translation> translations) {
                _translations.postValue(translations);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to load history");
            }
        });
    }

    /**
     * Saves a new translation to the user's history
     * Updates {@code _saveSuccess} LiveData on operation result
     * @param translation Translation to save
     * @param context Context for repository operations
     */
    public void saveTranslation(Translation translation, Context context) {
        translationsRepository.saveTranslation(translation, context, new TranslationsRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                _saveSuccess.postValue(true);
                loadTranslations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to save translation");
                _saveSuccess.postValue(false);
            }
        });
    }

    /**
     * Deletes a translation from the user's history
     * Updates {@code _deleteSuccess} LiveData on operation result
     * @param translation Translation to delete
     * @param context Context for repository operations
     */
    public void deletetranslation(Translation translation, Context context) {
        translationsRepository.deleteTranslation(translation, context, new TranslationsRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                _deleteSuccess.postValue(true);
                loadTranslations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to delete translation");
                _deleteSuccess.postValue(false);
            }
        });
    }

    /**
     * Updates the favorite status of a translation
     * Updates {@code _favoriteUpdateSuccess} LiveData on operation result
     * @param translationId ID of the translation to update
     * @param isFavorite New favorite status
     * @param context Context for repository operations
     */
    public void updateFavoriteStatus(String translationId, boolean isFavorite, Context context) {
        translationsRepository.updateFavoriteStatus(translationId, isFavorite, new TranslationsRepository.FavoriteCallback() {
            @Override
            public void onSuccess() {
                _favoriteUpdateSuccess.postValue(true);
                loadTranslations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to update favorite");
                _favoriteUpdateSuccess.postValue(false);
            }
        });
    }

    /**
     * Handles errors from repository operations
     * Updates {@code _errorMessage} LiveData with appropriate message
     * @param e Exception encountered
     * @param defaultMessage Default message to use for unexpected errors
     */
    private void handleError(Exception e, String defaultMessage) {
        if (e instanceof AuthException) {
            _errorMessage.postValue(e.getMessage());
        } else if (e instanceof NetworkException) {
            _errorMessage.postValue(e.getMessage());
        } else {
            _errorMessage.postValue(defaultMessage + ": " + e.getMessage());
        }
    }
}