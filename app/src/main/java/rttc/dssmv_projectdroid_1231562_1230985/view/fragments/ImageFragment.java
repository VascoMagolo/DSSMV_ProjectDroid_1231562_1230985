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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

import static rttc.dssmv_projectdroid_1231562_1230985.BuildConfig.TranslateAPI_KEY;

public class ImageFragment extends Fragment {

    private ImageView imgPreview;
    private TextView txtTranslated;
    private MaterialButton btnTakePhoto;

    private String targetLang = "en";

    private final TranslationRepository translationRepository = new TranslationRepository();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private Uri photoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        imgPreview = view.findViewById(R.id.img_preview);
        txtTranslated = view.findViewById(R.id.text_translated_from_image);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        Spinner spinnerLang = view.findViewById(R.id.spinner_target_lang);

        targetLang = "en";

        spinnerLang.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view1, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (selected.contains("(en)")) targetLang = "en";
                else if (selected.contains("(es)")) targetLang = "es";
                else if (selected.contains("(fr)")) targetLang = "fr";
                else if (selected.contains("(de)")) targetLang = "de";
                else if (selected.contains("(it)")) targetLang = "it";
                else if (selected.contains("(pt)")) targetLang = "pt";
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnTakePhoto.setOnClickListener(v -> openCamera());

        return view;
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

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoUri != null) {
                processImageForOCR(photoUri);
            } else {
                Toast.makeText(getContext(), "Error: image not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void processImageForOCR(Uri photoUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), photoUri);

            ImageView imgPreview = requireView().findViewById(R.id.img_preview);
            imgPreview.setImageBitmap(bitmap);

            extractTextFromImage(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing the image", Toast.LENGTH_SHORT).show();
        }
    }
    private void extractTextFromImage(Bitmap bitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageBytes = stream.toByteArray();

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "image",
                            "photo.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/jpeg"))
                    )
                    .build();

            Request request = new Request.Builder()
                    .url("https://ocr43.p.rapidapi.com/v1/results")
                    .post(body)
                    .addHeader("x-rapidapi-key", TranslateAPI_KEY)
                    .addHeader("x-rapidapi-host", "ocr43.p.rapidapi.com")
                    .build();

            OkHttpClient client = new OkHttpClient();

            new Thread(() -> {
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        String extractedText = jsonObject
                                .getJSONArray("results")
                                .getJSONObject(0)
                                .getJSONArray("entities")
                                .getJSONObject(0)
                                .getJSONArray("objects")
                                .getJSONObject(0)
                                .getJSONArray("entities")
                                .getJSONObject(0)
                                .getString("text");

                        requireActivity().runOnUiThread(() -> {
                            TextView resultView = requireView().findViewById(R.id.text_original);
                            TextView translatedView = requireView().findViewById(R.id.text_translated_from_image);

                            resultView.setText("Extracted Text:\n" + extractedText);
                            translatedView.setText("Translation...");

                            translationRepository.detectAndTranslate(extractedText, targetLang, new TranslationRepository.TranslationCallback() {
                                @Override
                                public void onSuccess(String translatedText, String detectedLang) {
                                    requireActivity().runOnUiThread(() -> {
                                        translatedView.setText("Translation(" + detectedLang + " → " + targetLang + "):\n" + translatedText);
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    requireActivity().runOnUiThread(() ->
                                            translatedView.setText("Error in translation: " + e.getMessage())
                                    );
                                }
                            });
                        });


                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error in OCR", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
