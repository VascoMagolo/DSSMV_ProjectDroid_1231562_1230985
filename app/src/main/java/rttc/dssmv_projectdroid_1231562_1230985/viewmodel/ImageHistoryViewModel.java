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

public class ImageHistoryViewModel extends AndroidViewModel {
    private final ImageHistoryRepository imageHistoryRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public ImageHistoryViewModel(@NonNull Application application) {
        super(application);
        imageHistoryRepository = new ImageHistoryRepository();
    }

    public void loadImageHistory(Context context) {
        isLoading.setValue(true);
        imageHistoryRepository.loadImageHistory(context);
        isLoading.setValue(false);
    }

    public void saveImageHistory(ImageHistory imageHistory, Context context) {
        imageHistoryRepository.saveImageHistory(imageHistory, context);
    }

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