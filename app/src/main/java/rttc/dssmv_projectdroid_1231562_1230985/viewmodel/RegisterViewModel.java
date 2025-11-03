package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.model.AuthRepository;


public class RegisterViewModel extends ViewModel {

    private AuthRepository authRepository;

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToHome = _navigateToHome;

    private MutableLiveData<Boolean> _registrationSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> registrationSuccess = _registrationSuccess;
    public RegisterViewModel() {
        authRepository = new AuthRepository();

        authRepository.getRegistrationResult().observeForever(result -> {
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

    public void registerUser(String name, String email, String password) {
        if (!validateInput(name, email, password)) {
            return;
        }

        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        authRepository.RegisterUser(email, name, password);
    }


    private boolean validateInput(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
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