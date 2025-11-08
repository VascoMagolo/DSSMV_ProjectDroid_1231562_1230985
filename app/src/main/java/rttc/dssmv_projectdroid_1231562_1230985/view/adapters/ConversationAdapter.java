package rttc.dssmv_projectdroid_1231562_1230985.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.Conversation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> conversations;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
    private onConversationClickListener onConversationClickListener;

    public interface onConversationClickListener {
        void onConversationClick(Conversation conversation);
    }
    public ConversationAdapter(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    public void setOnConversationClickListener(onConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position){
        Conversation conversation = conversations.get(position);

        String original = conversation.getOriginalText() != null ? conversation.getOriginalText() : "";
        String translated = conversation.getTranslatedText() != null ? conversation.getTranslatedText() : "";
        String src = conversation.getSourceLanguage() != null ? conversation.getSourceLanguage() : "";
        String tgt = conversation.getTargetLanguage() != null ? conversation.getTargetLanguage() : "";
        holder.textOriginal.setText(original);
        holder.textTranslated.setText(translated);
        holder.textLanguages.setText(String.format(Locale.getDefault(), "%s → %s", src, tgt));
        if (conversation.getTimestamp() != null) {
            holder.textTimeStamp.setText(dateFormat.format(conversation.getTimestamp()));
        } else {
            holder.textTimeStamp.setText("");
        }

        holder.itemView.setOnClickListener(view -> {
            if (onConversationClickListener != null) {
                onConversationClickListener.onConversationClick(conversation);
            }
        });

    }

    @Override
    public int getItemCount(){
        return conversations != null ? conversations.size() : 0;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder{
        TextView textOriginal, textTranslated, textLanguages, textTimeStamp;

        public ConversationViewHolder(@NonNull View itemView){
            super(itemView);
            textOriginal = itemView.findViewById(R.id.txtOriginal);
            textTranslated = itemView.findViewById(R.id.txtTranslatedItem);
            textLanguages = itemView.findViewById(R.id.txtLangs);
            textTimeStamp = new TextView(itemView.getContext());
        }
    }
}
