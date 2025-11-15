package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;

public class ImageHistoryAdapter extends RecyclerView.Adapter<ImageHistoryAdapter.ImageHistoryViewHolder> {

    private List<ImageHistory> imageHistoryList;
    private OnImageHistoryClickListener listener;

    public interface OnImageHistoryClickListener {
        void onImageHistoryClick(ImageHistory imageHistory);
    }

    public ImageHistoryAdapter(List<ImageHistory> imageHistoryList) {
        this.imageHistoryList = imageHistoryList;
    }

    public void setOnImageHistoryClickListener(OnImageHistoryClickListener listener) {
        this.listener = listener;
    }

    public void updateImageHistory(List<ImageHistory> imageHistoryList) {
        this.imageHistoryList = imageHistoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_history, parent, false);
        return new ImageHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHistoryViewHolder holder, int position) {
        ImageHistory imageHistory = imageHistoryList.get(position);
        holder.bind(imageHistory);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageHistoryClick(imageHistory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageHistoryList != null ? imageHistoryList.size() : 0;
    }

    static class ImageHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textPreview;
        private final TextView textTimestamp;
        private final TextView textLanguage;
        private final ImageView imgThumbnail;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

        public ImageHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textPreview = itemView.findViewById(R.id.text_image_preview);
            textTimestamp = itemView.findViewById(R.id.text_image_timestamp);
            textLanguage = itemView.findViewById(R.id.text_image_language);
            imgThumbnail = itemView.findViewById(R.id.img_history_thumbnail);
        }

        public void bind(ImageHistory imageHistory) {
            String preview = imageHistory.getExtractedText();
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            textPreview.setText(preview != null ? preview : "No text extracted");

            if (imageHistory.getTimestamp() != null) {
                textTimestamp.setText(dateFormat.format(imageHistory.getTimestamp()));
            } else {
                textTimestamp.setText("Date unavailable");
            }

            String targetLang = imageHistory.getTargetLanguage() != null ?
                    imageHistory.getTargetLanguage().toUpperCase() : "EN";
            textLanguage.setText("To: " + targetLang);

            if (imageHistory.getImageUrl() != null && !imageHistory.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(imageHistory.getImageUrl())
                        .placeholder(R.drawable.ic_photo_placeholder)
                        .error(R.drawable.ic_delete)
                        .resize(60, 60)
                        .centerCrop()
                        .into(imgThumbnail);
            } else {
                imgThumbnail.setImageResource(R.drawable.ic_photo_placeholder);
            }
        }
    }
}