package rttc.dssmv_projectdroid_1231562_1230985.utils;

import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.LoginViewModel;
import rttc.dssmv_projectdroid_1231562_1230985.viewmodel.RegisterViewModel;

/**
 * Helper class to set up observers for authentication-related ViewModels
 * Handles states, error messages, and navigation events for login and registration,
 * lowering boilerplate code in activities/fragments.
 */
public class AuthUiHelper {

    /**
     * Sets up observers for the LoginViewModel to handle UI updates based on authentication state.
     * @param activity Activity context for observing LiveData
     * @param viewModel Login ViewModel instance
     * @param actionButton Login button to disable/enable
     * @param onNavigateHome Runnable to execute on successful login navigation
     */
    public static void setupLoginObservers(
            AppCompatActivity activity,
            LoginViewModel viewModel,
            Button actionButton,
            Runnable onNavigateHome
    ) {
        viewModel.isLoading.observe(activity, isLoading -> {
            actionButton.setEnabled(!isLoading);
            actionButton.setText(isLoading ? "Loading..." : "Login");
        });

        viewModel.errorMessage.observe(activity, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
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

    /**
     * Sets up observers for the RegisterViewModel,
     * Similar to login but for registration flow.
     * @param activity Activity context for observing LiveData
     * @param viewModel Register ViewModel instance
     * @param actionButton Register button
     * @param onNavigateToLogin Runnable to execute on successful registration navigation
     */
    public static void setupRegisterObservers(
            AppCompatActivity activity,
            RegisterViewModel viewModel,
            Button actionButton,
            Runnable onNavigateToLogin
    ) {
        viewModel.isLoading.observe(activity, isLoading -> {
            actionButton.setEnabled(!isLoading);
            actionButton.setText(isLoading ? "Registering..." : "Register");
        });

        viewModel.errorMessage.observe(activity, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        viewModel.navigateToLogin.observe(activity, navigate -> {
            if (navigate) {
                Toast.makeText(activity, "Account created successfully", Toast.LENGTH_SHORT).show();
                onNavigateToLogin.run();
                viewModel.onNavigationComplete();
            }
        });
    }
}