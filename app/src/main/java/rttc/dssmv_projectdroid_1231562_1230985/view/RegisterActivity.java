package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.utils.AuthUiHelper;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private RegisterViewModel viewModel;
    private AutoCompleteTextView edtPreferredLanguage;
    private final String[] languages = {"Select...", "Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private final String[] languageCodes = {"", "pt", "en", "es", "fr", "ja", "zh", "de"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPreferredLanguage = findViewById(R.id.autoCompletePreferredLanguage);
        Button btnRegister = findViewById(R.id.btnCreate);
        setupLanguageSpinner();
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            String confirmpassword = edtConfirmPassword.getText().toString();
            String selectedLanguageText = edtPreferredLanguage.getText().toString();
            String langCode = getLanguageCodeFromSelection(selectedLanguageText);

            viewModel.register(name, email, password, confirmpassword, langCode);
        });

        AuthUiHelper.setupRegisterObservers(this, viewModel, btnRegister, () -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
    private void setupLanguageSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        edtPreferredLanguage.setAdapter(spinnerAdapter);
        edtPreferredLanguage.setText(languages[0], false);
    }
    private String getLanguageCodeFromSelection(String selectedText) {
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(selectedText)) {
                return languageCodes[i];
            }
        }
        return "";
    }

}