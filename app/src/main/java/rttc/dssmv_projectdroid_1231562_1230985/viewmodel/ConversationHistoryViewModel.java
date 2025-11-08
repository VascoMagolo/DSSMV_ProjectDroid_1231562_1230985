package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ConversationRepository;

import java.util.List;

public class ConversationHistoryViewModel extends AndroidViewModel {
    private final ConversationRepository conversationRepository;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();

    public ConversationHistoryViewModel(@NonNull Application application) {
        super(application);
        conversationRepository = new ConversationRepository();
    }

    public void loadConversation(Context context) {
        _isLoading.setValue(true);
        conversationRepository.loadConversations(context);
    }

    public LiveData<List<Conversation>> getConversations() {
        return conversationRepository.getConversations();
    }

    public LiveData<String> getErrorMessage() {
        return conversationRepository.getErrorMessage();
    }
    public void saveConversation(Conversation conversation, Context context) {
        conversationRepository.saveConversation(conversation, context);
    }

    public void deleteConversation(Conversation conversation, Context context) {
        conversationRepository.deleteConversation(conversation, context);
    }
}
