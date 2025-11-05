package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.AccountFragment;
import rttc.dssmv_projectdroid_1231562_1230985.view.fragments.ImageFragment;

public class ConversationHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation_history);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
