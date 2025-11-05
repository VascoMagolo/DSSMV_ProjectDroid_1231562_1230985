package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.view.ConversationHistoryActivity;
import rttc.dssmv_projectdroid_1231562_1230985.view.LoginActivity;

public class AccountFragment extends Fragment {

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        TextView textGreeting = view.findViewById(R.id.textGreeting);
        Button btnHistory = view.findViewById(R.id.btnHistory);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        sessionManager = new SessionManager(requireContext());

        User user = sessionManager.getUser();
        if (user != null) {
            String displayName = user.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getEmail();
            }
            textGreeting.setText("Hello, " + displayName + "! 👋");
        }

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ConversationHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
