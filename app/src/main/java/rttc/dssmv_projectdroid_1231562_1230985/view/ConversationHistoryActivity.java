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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.ConversationAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ConversationHistoryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ConversationHistoryActivity extends AppCompatActivity {

    private ConversationHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private TextView textView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_history);

        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        textView = findViewById(R.id.textEmpty);
        btnBack.setOnClickListener(v -> onBackPressed());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ConversationHistoryViewModel.class);

        adapter.setOnConversationClickListener(this::showConversationDetailsDialog);

        setupObservers();
        viewModel.loadConversation(this);
    }

    private void setupObservers() {
        viewModel.getConversations().observe(this, conversations -> {
            adapter.updateConversations(conversations);
            if (conversations.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(ConversationHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showConversationDetailsDialog(Conversation conversation) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_conversation_details, null);

        // Configurar as views do dialog
        TextView textOriginal = dialogView.findViewById(R.id.dialog_text_original);
        TextView textTranslated = dialogView.findViewById(R.id.dialog_text_translated);
        TextView textLanguages = dialogView.findViewById(R.id.dialog_text_languages);
        TextView textTimestamp = dialogView.findViewById(R.id.dialog_text_timestamp);
        MaterialButton btnDelete = dialogView.findViewById(R.id.dialog_btn_delete);
        MaterialButton btnFavorite = dialogView.findViewById(R.id.dialog_btn_favorite);
        MaterialButton btnShare = dialogView.findViewById(R.id.dialog_btn_share);
        textOriginal.setText(conversation.getOriginalText() != null ? conversation.getOriginalText() : "");
        textTranslated.setText(conversation.getTranslatedText() != null ? conversation.getTranslatedText() : "");

        String sourceLang = conversation.getSourceLanguage() != null ?
                conversation.getSourceLanguage().toUpperCase() : "Auto";
        String targetLang = conversation.getTargetLanguage() != null ?
                conversation.getTargetLanguage().toUpperCase() : "EN";
        textLanguages.setText(String.format(Locale.getDefault(), "%s → %s", sourceLang, targetLang));

        if (conversation.getTimestamp() != null) {
            textTimestamp.setText(dateFormat.format(conversation.getTimestamp()));
        } else {
            textTimestamp.setText("Data unavailable");
        }

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Conversation details")
                .setPositiveButton("Close", null);

        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(conversation);
        });
        btnShare.setOnClickListener(v -> {
            String shareContent = "Original: " + (conversation.getOriginalText() != null ? conversation.getOriginalText() : "") +
                    "\n\nTranslated: " + (conversation.getTranslatedText() != null ? conversation.getTranslatedText() : "") +
                    "\n\nLanguages: " + sourceLang + " → " + targetLang;
            android.content.Intent shareIntent = new android.content.Intent();
            shareIntent.setAction(android.content.Intent.ACTION_SEND);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
            shareIntent.setType("text/plain");
            startActivity(android.content.Intent.createChooser(shareIntent, "Share conversation via"));
        });
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Conversation conversation) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete conversation?")
                .setMessage("Are you sure you want to delete this conversation? This action cannot be undone")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteConversation(conversation, this);
                    Toast.makeText(this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}