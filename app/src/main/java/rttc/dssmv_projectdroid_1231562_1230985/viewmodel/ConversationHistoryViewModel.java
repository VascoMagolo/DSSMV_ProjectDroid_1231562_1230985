package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.repository.ConversationRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;

public class ConversationHistoryViewModel extends AndroidViewModel {

    private final ConversationRepository conversationRepository;

    private final MutableLiveData<List<Conversation>> _conversations = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _favoriteUpdateSuccess = new MutableLiveData<>();

    public ConversationHistoryViewModel(@NonNull Application application) {
        super(application);
        conversationRepository = new ConversationRepository();
    }

    public LiveData<List<Conversation>> getConversations() {
        return _conversations;
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


    public void loadConversations(Context context) {
        conversationRepository.loadConversations(context, new ConversationRepository.LoadCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                _conversations.postValue(conversations);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to load history");
            }
        });
    }

    public void saveConversation(Conversation conversation, Context context) {
        conversationRepository.saveConversation(conversation, context, new ConversationRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                _saveSuccess.postValue(true);
                loadConversations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to save conversation");
                _saveSuccess.postValue(false);
            }
        });
    }

    public void deleteConversation(Conversation conversation, Context context) {
        conversationRepository.deleteConversation(conversation, context, new ConversationRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                _deleteSuccess.postValue(true);
                loadConversations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to delete conversation");
                _deleteSuccess.postValue(false);
            }
        });
    }

    public void updateFavoriteStatus(String conversationId, boolean isFavorite, Context context) {
        conversationRepository.updateFavoriteStatus(conversationId, isFavorite, new ConversationRepository.FavoriteCallback() {
            @Override
            public void onSuccess() {
                _favoriteUpdateSuccess.postValue(true);
                loadConversations(context);
            }

            @Override
            public void onError(Exception e) {
                handleError(e, "Failed to update favorite");
                _favoriteUpdateSuccess.postValue(false);
            }
        });
    }

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