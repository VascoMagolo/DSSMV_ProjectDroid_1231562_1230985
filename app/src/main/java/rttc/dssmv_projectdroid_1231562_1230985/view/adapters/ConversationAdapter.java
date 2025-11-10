package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversationList;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public ConversationAdapter(List<Conversation> conversationList) {
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.txtOriginal.setText(conversation.getOriginalText());
        holder.txtTranslatedItem.setText(conversation.getTranslatedText());

        String langs = (conversation.getSourceLanguage() != null ? conversation.getSourceLanguage().toUpperCase() : "AUTO")
                + " → "
                + (conversation.getTargetLanguage() != null ? conversation.getTargetLanguage().toUpperCase() : "EN");
        holder.txtLangs.setText(langs);

        if (conversation.getFavorite()) {
            holder.iconFavoriteStar.setVisibility(View.VISIBLE);
;
        } else {
            holder.iconFavoriteStar.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void updateConversations(List<Conversation> newList) {
        this.conversationList = newList;
        notifyDataSetChanged();
    }
    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView txtOriginal, txtTranslatedItem, txtLangs;
        ImageView iconTranslate, iconFavoriteStar;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOriginal = itemView.findViewById(R.id.txtOriginal);
            txtTranslatedItem = itemView.findViewById(R.id.txtTranslatedItem);
            txtLangs = itemView.findViewById(R.id.txtLangs);
            iconTranslate = itemView.findViewById(R.id.iconTranslate);
            iconFavoriteStar = itemView.findViewById(R.id.icon_favorite_star);
        }
    }
}