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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Translation;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.TranslationAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.TranslationHistoryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TranslationHistoryActivity extends AppCompatActivity {

    private TranslationHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private TranslationAdapter adapter;
    private TextView textView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_history);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
        recyclerView = findViewById(R.id.recyclerViewHistory);
        textView = findViewById(R.id.textEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TranslationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(TranslationHistoryViewModel.class);
        adapter.setOntranslationClickListener(this::showtranslationDetailsDialog);
        setupObservers();
        viewModel.loadtranslations(this);
    }

    private void setupObservers() {
        viewModel.gettranslations().observe(this, translations -> {
            adapter.updatetranslations(translations);
            if (translations.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(TranslationHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getDeleteSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "translation deleted", Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getFavoriteUpdateSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Favorite status updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showtranslationDetailsDialog(Translation translation) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_translation_details, null);
        TextView textOriginal = dialogView.findViewById(R.id.dialog_text_original);
        TextView textTranslated = dialogView.findViewById(R.id.dialog_text_translated);
        TextView textLanguages = dialogView.findViewById(R.id.dialog_text_languages);
        TextView textTimestamp = dialogView.findViewById(R.id.dialog_text_timestamp);
        MaterialButton btnDelete = dialogView.findViewById(R.id.dialog_btn_delete);
        MaterialButton btnFavorite = dialogView.findViewById(R.id.dialog_btn_favorite);
        MaterialButton btnShare = dialogView.findViewById(R.id.dialog_btn_share);
        textOriginal.setText(translation.getOriginalText() != null ? translation.getOriginalText() : "");
        textTranslated.setText(translation.getTranslatedText() != null ? translation.getTranslatedText() : "");
        String sourceLang = translation.getSourceLanguage() != null ?
                translation.getSourceLanguage().toUpperCase() : "Auto";
        String targetLang = translation.getTargetLanguage() != null ?
                translation.getTargetLanguage().toUpperCase() : "EN";
        textLanguages.setText(String.format(Locale.getDefault(), "%s → %s", sourceLang, targetLang));

        if (translation.getTimestamp() != null) {
            textTimestamp.setText(dateFormat.format(translation.getTimestamp()));
        } else {
            textTimestamp.setText("Data unavailable");
        }
        updateFavoriteIcon(btnFavorite, translation.getFavorite());


        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("translation details")
                .setPositiveButton("Close", null);
        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(translation);
        });
        btnFavorite.setOnClickListener(v -> {
            boolean newStatus = !translation.getFavorite();
            viewModel.updateFavoriteStatus(translation.getId(), newStatus, this);
            translation.setFavorite(newStatus);
            updateFavoriteIcon(btnFavorite, newStatus);
        });

        btnShare.setOnClickListener(v -> {
            String shareContent = "Original: " + (translation.getOriginalText() != null ? translation.getOriginalText() : "") +
                    "\n\nTranslated: " + (translation.getTranslatedText() != null ? translation.getTranslatedText() : "") +
                    "\n\nLanguages: " + sourceLang + " → " + targetLang;
            android.content.Intent shareIntent = new android.content.Intent();
            shareIntent.setAction(android.content.Intent.ACTION_SEND);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
            shareIntent.setType("text/plain");
            startActivity(android.content.Intent.createChooser(shareIntent, "Share translation via"));
        });
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Translation translation) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete translation?")
                .setMessage("Are you sure you want to delete this translation? This action cannot be undone")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletetranslation(translation, this);
                })
                .show();
    }
    private void updateFavoriteIcon(MaterialButton button, boolean isFavorite) {
        try {
            if (isFavorite) {
                button.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite, getTheme()));
                button.setIconTint(ContextCompat.getColorStateList(this, R.color.favorite_IconFav));
            } else {
                button.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite, getTheme()));
                button.setIconTint(ContextCompat.getColorStateList(this, R.color.favorite_IconNotFav));
            }
        } catch (Exception e) {
            button.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite, getTheme()));
        }
    }
}