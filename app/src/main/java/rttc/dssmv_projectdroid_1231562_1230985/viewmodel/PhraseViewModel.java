package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;
import rttc.dssmv_projectdroid_1231562_1230985.model.PhraseRepository;

import java.util.List;

public class PhraseViewModel extends AndroidViewModel {
    private final PhraseRepository phraseRepository;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public PhraseViewModel(@NonNull Application application) {
        super(application);
        phraseRepository =  new PhraseRepository();
    }

    public void loadPhrases(String language) {
        _isLoading.setValue(true);
        phraseRepository.loadPhrasesByLanguage(language);
    }

    public LiveData<List<GenericPhrase>> getPhrases() {
        return phraseRepository.getPhrases();
    }

    public LiveData<String> getErrorMessage(){
        return phraseRepository.getErrorMessage();
    }
}

