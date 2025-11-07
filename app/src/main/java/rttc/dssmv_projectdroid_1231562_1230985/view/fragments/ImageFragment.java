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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.ImageViewModel;

public class ImageFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imgPreview;
    private TextView txtOriginal;
    private TextView txtTranslated;
    private Spinner spinnerTargetLang;
    private MaterialButton btnTakePhoto;

    private String targetLang = "en";
    private Uri photoUri;

    private ImageViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imgPreview = view.findViewById(R.id.img_preview);
        txtOriginal = view.findViewById(R.id.text_original);
        txtTranslated = view.findViewById(R.id.text_translated_from_image);
        spinnerTargetLang = view.findViewById(R.id.spinner_target_lang);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);

        viewModel = new ViewModelProvider(this).get(ImageViewModel.class);

        setupSpinner();
        setupObservers();

        btnTakePhoto.setOnClickListener(v -> openCamera());

        return view;
    }

    private void setupSpinner() {
        String[] languages = {"English (en)", "Português (pt)", "Español (es)", "Français (fr)", "Deutsch (de)", "日本語 (ja)", "中文 (zh)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetLang.setAdapter(adapter);

        spinnerTargetLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.contains("(en)")) targetLang = "en";
                else if (selected.contains("(pt)")) targetLang = "pt";
                else if (selected.contains("(es)")) targetLang = "es";
                else if (selected.contains("(fr)")) targetLang = "fr";
                else if (selected.contains("(de)")) targetLang = "de";
                else if (selected.contains("(ja)")) targetLang = "ja";
                else if (selected.contains("(zh)")) targetLang = "zh";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
