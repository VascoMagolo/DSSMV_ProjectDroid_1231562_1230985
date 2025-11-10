package rttc.dssmv_projectdroid_1231562_1230985.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import rttc.dssmv_projectdroid_1231562_1230985.R;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;
import rttc.dssmv_projectdroid_1231562_1230985.utils.SessionManager;
import rttc.dssmv_projectdroid_1231562_1230985.view.ConversationHistoryActivity;
import rttc.dssmv_projectdroid_1231562_1230985.view.LoginActivity;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.AccountViewModel;

import java.util.Calendar;
import java.util.Date;

public class AccountFragment extends Fragment {
    private AccountViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        sessionManager = new SessionManager(requireContext());

        TextView textGreeting = view.findViewById(R.id.textGreeting);
        MaterialButton btnHistory = view.findViewById(R.id.btnHistory);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        MaterialButton btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        User user = sessionManager.getUser();
        Date currentTime = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        if (user != null) {
            String displayName = user.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getEmail();
            }
            textGreeting.setText(getTimeofTheDayGreeting(displayName));
        }
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ConversationHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account?")
                .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteUserAccount(requireContext());
                })
                .show();
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private String getTimeofTheDayGreeting(String displayName) {
        Date currentTime = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good Morning, " + displayName + "! \uD83D\uDC4B";
        } else if (hour >= 12 && hour < 18) {
            return "Good Afternoon, " + displayName + "! \uD83D\uDC4B";
        } else {
            return "Good Evening, " + displayName + "! \uD83D\uDC4B";
        }
    }
}