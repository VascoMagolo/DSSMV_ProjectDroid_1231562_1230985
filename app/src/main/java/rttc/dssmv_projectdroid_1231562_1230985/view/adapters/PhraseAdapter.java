package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.GenericPhrase;

import java.util.List;

public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.PhraseViewHolder> {

    private List<GenericPhrase> phrases;
    private OnPhraseClickListener phraseClickListener;

    public interface OnPhraseClickListener {
        void onPhraseClick(GenericPhrase phrase);
    }

    public PhraseAdapter(List<GenericPhrase> phrases) {
        this.phrases = phrases;
    }

    public void updatePhrases(List<GenericPhrase> newPhrases) {
        this.phrases = newPhrases;
        notifyDataSetChanged();
    }

    public void setOnPhraseClickListener(OnPhraseClickListener listener) {
        this.phraseClickListener = listener;
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
        GenericPhrase phrase = phrases.get(position);

        if (phrase != null) {
            holder.textPhrase.setText(phrase.getText() != null ? phrase.getText() : "");
            holder.textCategory.setText(phrase.getCategory() != null ? phrase.getCategory() : "");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (phraseClickListener != null) {
                        phraseClickListener.onPhraseClick(phrase);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return phrases != null ? phrases.size() : 0;
    }

    public static class PhraseViewHolder extends RecyclerView.ViewHolder {
        public final TextView textPhrase;
        public final TextView textCategory;

        public PhraseViewHolder(@NonNull View itemView) {
            super(itemView);
            textPhrase = itemView.findViewById(R.id.textPhrase);
            textCategory = itemView.findViewById(R.id.textCategory);
        }
    }
}