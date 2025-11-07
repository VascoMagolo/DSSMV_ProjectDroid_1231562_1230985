package rttc.dssmv_projectdroid_1231562_1230985.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.ConversationAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationHistoryViewModel;

import java.util.ArrayList;

public class ConversationHistoryActivity extends AppCompatActivity {

    private ConversationHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation_history);

        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewHistory);
        textView = findViewById(R.id.textEmpty);
        btnBack.setOnClickListener(v -> onBackPressed());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ConversationHistoryViewModel.class);

        setupObservers();
        viewModel.loadConversation(this);
    }

    private void setupObservers() {
        viewModel.getConversations().observe(this, conversations -> {
            adapter.updateConversations(conversations);
            if (conversations.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }else{
                textView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && errorMessage.isEmpty()) {
                Toast.makeText(ConversationHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
        });
    }
}
