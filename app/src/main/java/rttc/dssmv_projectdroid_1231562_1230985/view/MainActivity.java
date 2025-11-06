package rttc.dssmv_projectdroid_1231562_1230985.view;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.AccountFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ConversationFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ImageFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.PhrasesFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        boolean isGuestUser = getIntent().getBooleanExtra("IS_GUEST", false);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (isGuestUser) {
            bottomNav.getMenu().removeItem(R.id.nav_account);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_voice) {
                selected = new ConversationFragment();
            } else if (item.getItemId() == R.id.nav_image) {
                selected = new ImageFragment();
            } else if (item.getItemId() == R.id.nav_phrases) {
                selected = new PhrasesFragment();
            } else if (item.getItemId() == R.id.nav_account) {
                selected = new AccountFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ConversationFragment())
                    .commit();
        }
    }
}