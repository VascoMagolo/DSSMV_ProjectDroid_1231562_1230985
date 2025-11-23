package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.ImageHistory;

/**
 * RecyclerView Adapter for displaying image translation history items
 * Each item has a thumbnail, text preview, timestamp, and target language
 */
public class ImageHistoryAdapter extends RecyclerView.Adapter<ImageHistoryAdapter.ImageHistoryViewHolder> {

    private List<ImageHistory> imageHistoryList;
    private OnImageHistoryClickListener listener;

    /** Interface for handling clicks on image history items */
    public interface OnImageHistoryClickListener {
        void onImageHistoryClick(ImageHistory imageHistory);
    }

    public ImageHistoryAdapter(List<ImageHistory> imageHistoryList) {
        this.imageHistoryList = imageHistoryList;
    }

    public void setOnImageHistoryClickListener(OnImageHistoryClickListener listener) {
        this.listener = listener;
    }

    public void updateImageHistory(List<ImageHistory> newImageHistoryList) {
        if (newImageHistoryList == null) {
            newImageHistoryList = new ArrayList<>();
        }
        if (this.imageHistoryList == null) {
            this.imageHistoryList = new ArrayList<>();
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ImageHistoryDiffCallback(this.imageHistoryList, newImageHistoryList));
        this.imageHistoryList = newImageHistoryList;
        diffResult.dispatchUpdatesTo(this);
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

    /**
     * ViewHolder class for binding image history data to item views
     * Uses Picasso for image loading and formatting for date display
     */
    static class ImageHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textPreview;
        private final TextView textTimestamp;
        private final TextView textLanguage;
        private final ImageView imgThumbnail;
        // Use ThreadLocal for thread-safe SimpleDateFormat access
        private static final ThreadLocal<SimpleDateFormat> dateFormat = 
            ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()));

        public ImageHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textPreview = itemView.findViewById(R.id.text_image_preview);
            textTimestamp = itemView.findViewById(R.id.text_image_timestamp);
            textLanguage = itemView.findViewById(R.id.text_image_language);
            imgThumbnail = itemView.findViewById(R.id.img_history_thumbnail);
        }

        /**
         * Binds an ImageHistory object to the item views
         * Truncates preview text and formats timestamp
         * Loads image thumbnail using Picasso
         * @param imageHistory The ImageHistory object to bind
         */
        public void bind(ImageHistory imageHistory) {
            String preview = imageHistory.getExtractedText();
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            textPreview.setText(preview != null ? preview : "No text extracted");


            if (imageHistory.getTimestamp() != null) {
                textTimestamp.setText(dateFormat.get().format(imageHistory.getTimestamp()));
            } else {
                textTimestamp.setText("Date unavailable");
            }

            String targetLang = imageHistory.getTargetLanguage() != null ?
                    imageHistory.getTargetLanguage().toUpperCase() : "EN";
            textLanguage.setText("To: " + targetLang);
            // Using Picasso to load image URL asynchronously
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

    /**
     * DiffUtil.Callback for calculating the difference between two lists of image history items
     */
    private static class ImageHistoryDiffCallback extends DiffUtil.Callback {
        private final List<ImageHistory> oldList;
        private final List<ImageHistory> newList;

        public ImageHistoryDiffCallback(List<ImageHistory> oldList, List<ImageHistory> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ImageHistory oldItem = oldList.get(oldItemPosition);
            ImageHistory newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ImageHistory oldItem = oldList.get(oldItemPosition);
            ImageHistory newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl()) &&
                   Objects.equals(oldItem.getExtractedText(), newItem.getExtractedText()) &&
                   Objects.equals(oldItem.getTranslatedText(), newItem.getTranslatedText()) &&
                   Objects.equals(oldItem.getTargetLanguage(), newItem.getTargetLanguage()) &&
                   Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
        }
    }
}