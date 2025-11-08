package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.repository.AccountRepository;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;


public class AccountViewModel extends AndroidViewModel {

    private final AccountRepository accountRepository;
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AccountViewModel(@NonNull Application application) {
        super(application);
        accountRepository = new AccountRepository();
    }

    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void deleteUserAccount(Context context) {
        SessionManager session = new SessionManager(context);
        User user = session.getUser();
        String userId = null;
        if (user != null) {
            userId = user.getId();
        }

        if (userId == null || userId.isEmpty()) {
            errorMessage.postValue("User not logged in.");
            return;
        }

        accountRepository.deleteAccount(context, userId, new AccountRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                deleteSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                if (e instanceof AuthException) {
                    errorMessage.postValue("Authentication Error. Please log in again.");
                } else if (e instanceof NetworkException) {
                    errorMessage.postValue("Network Error: " + e.getMessage());
                } else {
                    errorMessage.postValue("An unexpected error occurred.");
                }
                deleteSuccess.postValue(false);
            }
        });
    }
}