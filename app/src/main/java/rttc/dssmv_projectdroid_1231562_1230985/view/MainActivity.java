// java
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        conversationFragment = new ConversationFragment();
        conversationController = new ConversationController(this, conversationFragment);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_voice) {
                selected = conversationFragment;
            } else if (id == R.id.nav_image) {
                selected = new ImageFragment();
            } else if (id == R.id.nav_account) {
                selected = new AccountFragment();
            }

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
            conversationController.startListening(targetLanguageCode);
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
                conversationController.startListening("en");
            } else {
                Toast.makeText(this, "Microphone permissions negated.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
