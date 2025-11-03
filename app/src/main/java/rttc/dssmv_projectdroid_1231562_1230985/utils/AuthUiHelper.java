package rttc.dssmv_projectdroid_1231562_1230985.utils;

import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.LoginViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.RegisterViewModel;

public class AuthUiHelper {

    public static void setupLoginObservers(
            AppCompatActivity activity,
            LoginViewModel viewModel,
            Button actionButton,
            Runnable onNavigateHome
    ) {
        viewModel.isLoading.observe(activity, isLoading ->
                actionButton.setEnabled(!isLoading)
        );

        viewModel.errorMessage.observe(activity, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.navigateToHome.observe(activity, navigate -> {
            if (navigate) {
                Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show();
                onNavigateHome.run();
                viewModel.onNavigationComplete();
            }
        });
    }

    public static void setupRegisterObservers(
            AppCompatActivity activity,
            RegisterViewModel viewModel,
            Button actionButton,
            Runnable onNavigateHome
    ) {
        viewModel.isLoading.observe(activity, isLoading ->
                actionButton.setEnabled(!isLoading)
        );

        viewModel.errorMessage.observe(activity, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.navigateToHome.observe(activity, navigate -> {
            if (navigate) {
                Toast.makeText(activity, "Account created successfully", Toast.LENGTH_SHORT).show();
                onNavigateHome.run();
                viewModel.onNavigationComplete();
            }
        });
    }
}
