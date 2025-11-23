package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Translation;

/**
 * RecyclerView Adapter to display a list of {@link Translation} objects
 * Used in {@link rttc.dssmv_projectdroid_1231562_1230985.view.TranslationHistoryActivity}
 * Displays original text, translated text, languages code and favorite indicator
 */
public class TranslationAdapter extends RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder> {

    private List<Translation> translationList;
    private OntranslationClickListener listener;

    /**
     * Handles click events on a history record
     */
    public interface OntranslationClickListener {
        void ontranslationClick(Translation translation);
    }

    public void setOntranslationClickListener(OntranslationClickListener listener) {
        this.listener = listener;
    }

    public TranslationAdapter(List<Translation> translationList) {
        this.translationList = translationList;
    }

    @NonNull
    @Override
    public TranslationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_translation, parent, false);
        return new TranslationViewHolder(view);
    }

    /**
     * Joins/Binds translation data to the ViewHolder
     * Handles language code format (e.g., "PT -> EN") also handles favorite icon
     */
    @Override
    public void onBindViewHolder(@NonNull TranslationViewHolder holder, int position) {
        Translation translation = translationList.get(position);

        holder.txtOriginal.setText(translation.getOriginalText());
        holder.txtTranslatedItem.setText(translation.getTranslatedText());

        // Formats target and source language
        String langs = (translation.getSourceLanguage() != null ? translation.getSourceLanguage().toUpperCase() : "AUTO")
                + " → "
                + (translation.getTargetLanguage() != null ? translation.getTargetLanguage().toUpperCase() : "EN");
        holder.txtLangs.setText(langs);

        // Toggles favorite icon visibility based on translation.getFavorite value
        if (translation.getFavorite()) {
            holder.iconFavoriteStar.setVisibility(View.VISIBLE);
        } else {
            holder.iconFavoriteStar.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.ontranslationClick(translation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return translationList.size();
    } // returns the translation history list size

    /**
     * Updates the list of translations and updates the RecyclerView efficiently using DiffUtil
     * Must be called when the data is changed in the ViewModel
     * @param newList a new list of translation objects
     */
    public void updatetranslations(List<Translation> newList) {
        if (newList == null) {
            newList = new ArrayList<>();
        }
        if (this.translationList == null) {
            this.translationList = new ArrayList<>();
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TranslationDiffCallback(this.translationList, newList));
        this.translationList = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * DiffUtil.Callback for calculating the difference between two lists of translations
     */
    private static class TranslationDiffCallback extends DiffUtil.Callback {
        private final List<Translation> oldList;
        private final List<Translation> newList;

        public TranslationDiffCallback(List<Translation> oldList, List<Translation> newList) {
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
            Translation oldItem = oldList.get(oldItemPosition);
            Translation newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Translation oldItem = oldList.get(oldItemPosition);
            Translation newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getOriginalText(), newItem.getOriginalText()) &&
                   Objects.equals(oldItem.getTranslatedText(), newItem.getTranslatedText()) &&
                   Objects.equals(oldItem.getFavorite(), newItem.getFavorite()) &&
                   Objects.equals(oldItem.getSourceLanguage(), newItem.getSourceLanguage()) &&
                   Objects.equals(oldItem.getTargetLanguage(), newItem.getTargetLanguage());
        }
    }
    static class TranslationViewHolder extends RecyclerView.ViewHolder {
        TextView txtOriginal, txtTranslatedItem, txtLangs;
        ImageView iconTranslate, iconFavoriteStar;

        public TranslationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOriginal = itemView.findViewById(R.id.txtOriginal);
            txtTranslatedItem = itemView.findViewById(R.id.txtTranslatedItem);
            txtLangs = itemView.findViewById(R.id.txtLangs);
            iconTranslate = itemView.findViewById(R.id.iconTranslate);
            iconFavoriteStar = itemView.findViewById(R.id.icon_favorite_star);
        }
    }
}