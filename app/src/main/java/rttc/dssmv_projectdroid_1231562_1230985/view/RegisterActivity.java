package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private Button btnRegister;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnCreate);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            viewModel.registerUser(name, email, password);
        });

        setupObservers();
    }

    private void setupObservers() {

        viewModel.isLoading.observe(this, isLoading -> {
            btnRegister.setEnabled(!isLoading);
        });

        viewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showErrorMessage(message);
            }
        });

        viewModel.navigateToHome.observe(this, navigate -> {
            if (navigate) {
                showSuccessMessage();
                navigateToHome();
                viewModel.onNavigationComplete();
            }
        });
    }


    private void showSuccessMessage() {
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}