package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.model.AuthRepository;

public class LoginViewModel extends ViewModel {

    private AuthRepository authRepository;

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToHome = _navigateToHome;

    private MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> LoginSuccess = _loginSuccess;

    public LoginViewModel() {
        authRepository = new AuthRepository();

        authRepository.getLoginResult().observeForever(result -> {
            _isLoading.setValue(false);
            if (result) {
                _navigateToHome.setValue(true);
            }
        });
        authRepository.getErrorMessage().observeForever(errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()){
                _errorMessage.setValue(errorMessage);
            }

        });
    }

    public void LoginUser(Context context, String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        authRepository.loginInSupabase(context, email, password);
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
}
