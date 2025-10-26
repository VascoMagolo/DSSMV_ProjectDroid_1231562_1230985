package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import rttc.dssmv_projectdroid_1231562_1230985.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnGuest = findViewById(R.id.btnGuest);

        btnLogin.setOnClickListener(v -> {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {

            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnGuest.setOnClickListener(v -> {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}