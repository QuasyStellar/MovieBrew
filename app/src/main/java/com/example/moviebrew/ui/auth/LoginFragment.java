package com.example.moviebrew.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moviebrew.MainActivity;
import com.example.moviebrew.R;
import com.example.moviebrew.ui.search.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, goToRegisterButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.editText_email);
        passwordEditText = view.findViewById(R.id.editText_password);
        loginButton = view.findViewById(R.id.button_login);
        goToRegisterButton = view.findViewById(R.id.button_go_to_register);

        loginButton.setOnClickListener(v -> loginUser());
        goToRegisterButton.setOnClickListener(v -> navigateToRegister());

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).navigateToMainContent(); // Navigate to main content
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRegister() {
        // Sign out current user if any, to ensure a clean state for registration
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadAuthFragment(new RegisterFragment());
        }
    }
}