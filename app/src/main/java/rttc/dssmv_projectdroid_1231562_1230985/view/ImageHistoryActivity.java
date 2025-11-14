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
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;
import rttc.dssmv_projectdroid_1231562_1230985.view.adapters.ImageHistoryAdapter;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ImageHistoryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageHistoryActivity extends AppCompatActivity {

    private ImageHistoryViewModel viewModel;
    private RecyclerView recyclerView;
    private ImageHistoryAdapter adapter;
    private TextView textEmpty;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_history);

        setupViews();
        setupRecyclerView();
        setupViewModel();
        setupObservers();

        viewModel.loadImageHistory(this);
    }

    private void setupViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewImageHistory);
        textEmpty = findViewById(R.id.textEmptyImageHistory);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        adapter.setOnImageHistoryClickListener(imageHistory -> {
            showImageHistoryDetailsDialog(imageHistory);
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ImageHistoryViewModel.class);
    }

    private void setupObservers() {
        viewModel.getImageHistory().observe(this, imageHistory -> {
            if (imageHistory != null) {
                adapter.updateImageHistory(imageHistory);
                updateEmptyState(imageHistory.isEmpty());
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(ImageHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showImageHistoryDetailsDialog(ImageHistory imageHistory) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_history_details, null);

        TextView textOriginal = dialogView.findViewById(R.id.dialog_image_text_original);
        TextView textTranslated = dialogView.findViewById(R.id.dialog_image_text_translated);
        TextView textLanguage = dialogView.findViewById(R.id.dialog_image_language);
        TextView textTimestamp = dialogView.findViewById(R.id.dialog_image_timestamp);
        MaterialButton btnDelete = dialogView.findViewById(R.id.dialog_btn_delete_image);
        ImageView imagePreview = dialogView.findViewById(R.id.dialog_image_preview);

        if (imageHistory.getImageUrl() != null && !imageHistory.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(imageHistory.getImageUrl())
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_delete)
                    .resize(400, 400)
                    .centerCrop()
                    .into(imagePreview);
        } else {
            imagePreview.setImageResource(R.drawable.ic_photo_placeholder);
        }

        textOriginal.setText(imageHistory.getExtractedText() != null ? imageHistory.getExtractedText() : "");
        textTranslated.setText(imageHistory.getTranslatedText() != null ? imageHistory.getTranslatedText() : "");

        String targetLang = imageHistory.getTargetLanguage() != null ?
                imageHistory.getTargetLanguage().toUpperCase() : "EN";
        textLanguage.setText(targetLang);

        if (imageHistory.getTimestamp() != null) {
            textTimestamp.setText(dateFormat.format(imageHistory.getTimestamp()));
        } else {
            textTimestamp.setText("Date unavailable");
        }

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setTitle("Image Translation Details")
                .setPositiveButton("Close", null);

        final androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(imageHistory);
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(ImageHistory imageHistory) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Image Translation?")
                .setMessage("Are you sure you want to delete this image translation? This action cannot be undone")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteImageHistory(imageHistory, this);
                    Toast.makeText(this, "Image translation deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}