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

public class RegisterFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button registerButton, goToLoginButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.editText_email);
        passwordEditText = view.findViewById(R.id.editText_password);
        registerButton = view.findViewById(R.id.button_register);
        goToLoginButton = view.findViewById(R.id.button_go_to_login);

        registerButton.setOnClickListener(v -> registerUser());
        goToLoginButton.setOnClickListener(v -> navigateToLogin());

        return view;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getContext(), getString(R.string.toast_password_min_length), Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).navigateToMainContent();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), getString(R.string.toast_authentication_failed) + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadAuthFragment(new LoginFragment());
        }
    }
}