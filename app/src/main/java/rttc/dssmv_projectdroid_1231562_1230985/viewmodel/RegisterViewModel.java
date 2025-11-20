package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.repository.AuthRepository;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.ApiException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.AuthException;
import rttc.dssmv_projectdroid_1231562_1230985.exceptions.NetworkException;

/**
 * ViewModel for RegisterActivity
 * Handles user registration logic, including validating inputs and communicates with {@link AuthRepository} to create
 * a new user in supabase
 */
public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepository;

    // LiveData that indicates if registration request is in progress
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // LiveData for validation or API error
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    public LiveData<String> errorMessage = _errorMessage;

    // LiveData that signals the view to navigate to login screen upon success
    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>(false);
    public LiveData<Boolean> navigateToLogin = _navigateToLogin;

    public RegisterViewModel() {
        authRepository = new AuthRepository();
    }

    /**
     * Validates input data and tries to register a new user
     * @param preferredLanguage user selected default language code (e.g., "pt")
     */
    public void register(String name, String email, String password, String confirmPassword, String preferredLanguage) {
        if (!validateInput(name, email, password, confirmPassword, preferredLanguage)) {
            return;
        }
        email = email.trim().toLowerCase();
        password = password.trim();
        name = name.trim();

        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        authRepository.RegisterUser(name, email, password, preferredLanguage, new AuthRepository.RegisterCallback() {

            @Override
            public void onSuccess() {
                _isLoading.postValue(false);
                _navigateToLogin.postValue(true);
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
                    _errorMessage.postValue("An unknown registration error occurred.");
                }
            }
        });
    }

    /**
     * Validates all registration fields
     * Updates _errorMessage if it fails
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInput(String name, String email, String password, String confirmPassword, String preferredLanguage) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _errorMessage.setValue("Please fill all the fields");
            return false;
        }

        if (password.length() < 6) {
            _errorMessage.setValue("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            _errorMessage.setValue("Passwords do not match");
            return false;
        }
        if (preferredLanguage == null || preferredLanguage.isEmpty()) {
            _errorMessage.setValue("Please select a preferred language");
            return false;
        }
        _errorMessage.setValue(null);
        return true;
    }

    /**
     * resets navigation because it has been handled by the view
     */
    public void onNavigationComplete() {
        _navigateToLogin.setValue(false);
    }

    /**
     * clears current error message
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}