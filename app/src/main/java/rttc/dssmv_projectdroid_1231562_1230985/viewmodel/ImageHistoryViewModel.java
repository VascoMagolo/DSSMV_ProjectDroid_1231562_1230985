package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ImageHistoryRepository;

import java.util.List;

/**
 * ViewModel for {@link rttc.dssmv_projectdroid_1231562_1230985.view.ImageHistoryActivity}
 * Interacts with {@link ImageHistoryRepository} to fetch data from supabase
 * Manages retrieval and deletion of image translation records
 */
public class ImageHistoryViewModel extends AndroidViewModel {
    private final ImageHistoryRepository imageHistoryRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public ImageHistoryViewModel(@NonNull Application application) {
        super(application);
        imageHistoryRepository = new ImageHistoryRepository();
    }

    /**
     * Triggers the loading of the user's image translation history.
     * @param context context for session manager
     */
    public void loadImageHistory(Context context) {
        isLoading.setValue(true);
        imageHistoryRepository.loadImageHistory(context);
        isLoading.setValue(false);
    }

    public void saveImageHistory(ImageHistory imageHistory, Context context) {
        imageHistoryRepository.saveImageHistory(imageHistory, context);
    }

    /**
     * Deletes a specific record of image history
     * @param imageHistory image history record to delete
     * @param context context needed for session management
     */
    public void deleteImageHistory(ImageHistory imageHistory, Context context) {
        imageHistoryRepository.deleteImageHistory(imageHistory, context);
    }

    public LiveData<List<ImageHistory>> getImageHistory() {
        return imageHistoryRepository.getImageHistory();
    }

    public LiveData<String> getErrorMessage() {
        return imageHistoryRepository.getErrorMessage();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}