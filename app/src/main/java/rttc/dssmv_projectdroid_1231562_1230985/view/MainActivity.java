package rttc.dssmv_projectdroid_1231562_1230985.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // checks if user is guest, passed from login activity
        boolean isGuestUser = getIntent().getBooleanExtra("IS_GUEST", false);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // if user is guest removes account option
        if (isGuestUser) {
            bottomNav.getMenu().removeItem(R.id.nav_account);
        }

        // Main navigation logic
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_voice) {
                selected = new TranslationFragment();
            } else if (item.getItemId() == R.id.nav_image) {
                selected = new ImageFragment();
            } else if (item.getItemId() == R.id.nav_phrases) {
                selected = new PhrasesFragment();
            } else if (item.getItemId() == R.id.nav_account) {
                selected = new AccountFragment();
            } else if (item.getItemId() == R.id.nav_bilingual) {
                selected = new BilingualFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }
            return true;
        });

        // On app start, loads default fragment
        // Prevents blank screen on first load
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BilingualFragment())
                    .commit();
        }
    }
}