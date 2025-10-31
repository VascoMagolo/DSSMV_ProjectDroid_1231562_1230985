package rttc.dssmv_projectdroid_1231562_1230985.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthRepository {

    private final FirebaseAuth mAuth;
    private final MutableLiveData<Task<AuthResult>> registrationTaskLiveData;

    public AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.registrationTaskLiveData = new MutableLiveData<>();
    }

    public LiveData<Task<AuthResult>> getRegistrationTaskLiveData() {
        return registrationTaskLiveData;
    }

    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(registrationTaskLiveData::postValue);
    }
}