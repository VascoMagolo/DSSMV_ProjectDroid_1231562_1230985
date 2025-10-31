package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import rttc.dssmv_projectdroid_1231562_1230985.model.AuthRepository;

public class RegisterViewModel extends ViewModel {

    private AuthRepository authRepository;

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    private MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToHome = _navigateToHome;

    public LiveData<Task<AuthResult>> registrationTask;

    public RegisterViewModel() {
        authRepository = new AuthRepository();
        registrationTask = authRepository.getRegistrationTaskLiveData();
    }

    public void registerUser(String name, String email, String password) {
        if (!validateInput(name, email, password)) {
            return;
        }

        _isLoading.setValue(true);
        authRepository.registerUser(email, password);
    }

    public void processRegistrationResponse(Task<AuthResult> task) {
        _isLoading.setValue(false);
        if (task.isSuccessful()) {
            _navigateToHome.setValue(true);
        } else {
            _errorMessage.setValue(task.getException().getMessage());
        }
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