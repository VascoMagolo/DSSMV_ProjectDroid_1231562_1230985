package rttc.dssmv_projectdroid_1231562_1230985.controller;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import org.jetbrains.annotations.NotNull;
import rttc.dssmv_projectdroid_1231562_1230985.view.RegisterActivity;

public class RegisterController {

    private final FirebaseAuth mAuth;
    private final RegisterActivity mActivity;

    public RegisterController(RegisterActivity mActivity) {
        this.mActivity = mActivity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String name, String email, String password) {
        if (!validateInput(name, email, password)) {
            return;
        }

        mActivity.disableRegisterButton();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mActivity.showSuccessMessage();
                    mActivity.navigateToHome();
                }
                else{
                    mActivity.enableRegisterButton();
                    mActivity.showErrorMessage(task.getException().getMessage());
                }
            }
        });
    }

    private boolean validateInput(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mActivity.showErrorMessage("Please fill all the fields");
            return false;
        }

        if (password.length() < 6) {
            mActivity.showErrorMessage("Password must be at least 6 characters");
            return false;
        }
        return true;
    }
}





