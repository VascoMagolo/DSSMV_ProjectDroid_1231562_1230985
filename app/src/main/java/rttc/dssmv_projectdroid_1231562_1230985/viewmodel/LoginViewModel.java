package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.repository.AuthRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;


public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToHome = _navigateToHome;

    public LoginViewModel() {
        authRepository = new AuthRepository();
    }

    public void login(Context context, String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }
        email = email.trim().toLowerCase();
        password = password.trim();
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        authRepository.login(context, email, password, new AuthRepository.LoginCallback() {

            @Override
            public void onSuccess(User user) {
                _isLoading.postValue(false);
                _navigateToHome.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                _isLoading.postValue(false);
                if (e instanceof AuthException) {
                    _errorMessage.postValue(e.getMessage());
                } else if (e instanceof NetworkException) {
                    _errorMessage.postValue(e.getMessage());
                } else if (e instanceof ApiException) {
                    _errorMessage.postValue(e.getMessage());
                } else {
                    _errorMessage.postValue("An unknown login error occurred.");
                }
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.setValue("Please fill all the fields");
            return false;
        }

        if (password.length() < 6) {
            _errorMessage.setValue("Password must be at least 6 characters");
            return false;
        }

        _errorMessage.setValue(null);
        return true;
    }

    public void onNavigationComplete() {
        _navigateToHome.setValue(false);
    }
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}