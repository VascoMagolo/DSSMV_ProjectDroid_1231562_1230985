package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ImageViewModel;

public class ImageFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imgPreview;
    private TextView txtOriginal;
    private TextView txtTranslated;
    private AutoCompleteTextView autoCompleteTargetLanguage;

    private String targetLang = "en";
    private Uri photoUri;
    private ImageViewModel viewModel;
    private SessionManager sessionManager;
    private final String[] languages = {"Português", "English", "Español", "Français", "日本語", "中文", "Deutsch"};
    private final String[] languageCodes = {"pt", "en", "es", "fr", "ja", "zh", "de"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imgPreview = view.findViewById(R.id.img_preview);
        txtOriginal = view.findViewById(R.id.text_original);
        txtTranslated = view.findViewById(R.id.text_translated_from_image);
        autoCompleteTargetLanguage = view.findViewById(R.id.autoCompleteTargetLanguage);
        MaterialButton btnTakePhoto = view.findViewById(R.id.btn_take_photo);

        viewModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionManager = new SessionManager(requireContext());

        setupTargetLanguageMenu();
        setupObservers();

        btnTakePhoto.setOnClickListener(v -> openCamera());

        return view;
    }

    private void setupTargetLanguageMenu() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        autoCompleteTargetLanguage.setAdapter(adapter);

        User user = sessionManager.getUser();
        String preferredLangCode = "en";
        if (user != null && user.getPreferredLanguage() != null) {
            preferredLangCode = user.getPreferredLanguage();
        }

        String defaultLangName = getLanguageNameFromCode(preferredLangCode);
        if (defaultLangName != null) {
            autoCompleteTargetLanguage.setText(defaultLangName, false);
            targetLang = preferredLangCode;
        }

        autoCompleteTargetLanguage.setOnItemClickListener((parent, view, position, id) -> {
            targetLang = languageCodes[position];
        });
    }

    private String getLanguageNameFromCode(String langCode) {
        if (langCode == null) {
            return languages[0];
        }
        String trimmedLangCode = langCode.trim();
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equalsIgnoreCase(trimmedLangCode)) {
                return languages[i];
            }
        }
        return languages[0];
    }

    private void setupObservers() {
        viewModel.getExtractedText().observe(getViewLifecycleOwner(), text -> {
            txtOriginal.setText("Extracted Text:\n" + text);
        });

        viewModel.getTranslatedText().observe(getViewLifecycleOwner(), text -> {
            txtTranslated.setText("Translation:\n" + text);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                txtTranslated.setText("Processing...");
            }
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } catch (IOException e) {
                Toast.makeText(getContext(), "Error creating the image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && photoUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), photoUri);
                imgPreview.setImageBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageBytes = stream.toByteArray();

                viewModel.processImage(imageBytes, targetLang);

            } catch (IOException e) {
                Toast.makeText(requireContext(), "Error processing the image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
