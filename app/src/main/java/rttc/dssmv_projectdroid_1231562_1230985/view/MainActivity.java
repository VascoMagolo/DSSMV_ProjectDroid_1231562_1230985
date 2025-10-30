package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.controller.ConversationController;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.AccountFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ConversationFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ImageFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 1001;
    private ConversationController conversationController;
    private ConversationFragment conversationFragment;
    private boolean isGuestUser = false; // true if the user is a guest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        isGuestUser = getIntent().getBooleanExtra("IS_GUEST", false);
        conversationFragment = new ConversationFragment();
        conversationController = new ConversationController(this, conversationFragment);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // if guest user, remove account tab
        if (isGuestUser) {
            bottomNav.getMenu().removeItem(R.id.nav_account);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            if (item.getItemId() == R.id.nav_voice) selected = conversationFragment;
            else if (item.getItemId() == R.id.nav_image) selected = new ImageFragment();
            else if (item.getItemId() == R.id.nav_account) selected = new AccountFragment();

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }

            return true;
        });

        checkAudioPermission();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, conversationFragment)
                    .commit();
        }
    }

    public void startSpeechListening(String targetLanguageCode) {
        if (checkAudioPermission()) {
            conversationController.startListening(targetLanguageCode); // default source language
        }
    }

    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                conversationController.startListening("en"); // exemplo default
            } else {
                Toast.makeText(this, "Microphone permissions negated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to set guest user status
    public void setGuestUser(boolean guest) {
        isGuestUser = guest;
    }
}
