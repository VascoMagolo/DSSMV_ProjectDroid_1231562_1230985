package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.utils.AuthUiHelper;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private LoginViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        etEmail = findViewById(R.id.edtEmail);
        etPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnGuest = findViewById(R.id.btnGuest);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            viewModel.LoginUser(this, email, password);
        });
        AuthUiHelper.setupLoginObservers(this, viewModel, btnLogin, this::navigateToHome);

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btnGuest.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("IS_GUEST", true);
            startActivity(intent);
            finish();
        });
    }
    private void navigateToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
