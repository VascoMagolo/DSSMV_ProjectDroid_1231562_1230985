package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;

import java.util.List;

public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.PhraseViewHolder> {

    private List<GenericPhrase> phraseList;

    // Interfaces de Clique
    private OnPhraseClickListener phraseClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnPhraseClickListener {
        void onPhraseClick(GenericPhrase phrase);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(GenericPhrase phrase);
    }

    public PhraseAdapter(List<GenericPhrase> phraseList) {
        this.phraseList = phraseList;
    }

    public void updatePhrases(List<GenericPhrase> newPhrases) {
        this.phraseList = newPhrases;
        notifyDataSetChanged();
    }
    public void setOnPhraseClickListener(OnPhraseClickListener listener) {
        this.phraseClickListener = listener;
    }
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public PhraseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phrase, parent, false);
        return new PhraseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhraseViewHolder holder, int position) {
        GenericPhrase phrase = phraseList.get(position);
        if (phrase == null) return;

        holder.textPhrase.setText(phrase.getText());
        holder.textCategory.setText(phrase.getCategory());

        if (phrase.getLanguage() != null && !phrase.getLanguage().isEmpty()) {
            holder.textLanguage.setText(phrase.getLanguage().toUpperCase());
            holder.textLanguage.setVisibility(View.VISIBLE);
        } else {
            holder.textLanguage.setVisibility(View.GONE);
        }
        if (phrase.isUserPhrase()) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(phrase);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnDelete.setOnClickListener(null);
        }
        holder.itemView.setOnClickListener(v -> {
            if (phraseClickListener != null) {
                phraseClickListener.onPhraseClick(phrase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return phraseList != null ? phraseList.size() : 0;
    }
    public static class PhraseViewHolder extends RecyclerView.ViewHolder {
        public final TextView textPhrase;
        public final TextView textCategory;
        public final TextView textLanguage;
        public final ImageButton btnDelete;

        public PhraseViewHolder(@NonNull View itemView) {
            super(itemView);
            textPhrase = itemView.findViewById(R.id.textPhrase);
            textCategory = itemView.findViewById(R.id.textCategory);
            textLanguage = itemView.findViewById(R.id.textLanguage);
            btnDelete = itemView.findViewById(R.id.btn_delete_phrase);
        }
    }
}