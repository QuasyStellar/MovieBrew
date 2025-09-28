package com.example.moviebrew.ui.profile;

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

import com.example.moviebrew.MainActivity;
import com.example.moviebrew.R;
import com.example.moviebrew.ui.auth.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView emailTextView;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        emailTextView = view.findViewById(R.id.textView_profile_email);
        logoutButton = view.findViewById(R.id.button_logout);

        updateProfileUI();

        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileUI();
    }

    public void updateProfileUI() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            emailTextView.setText(getString(R.string.profile_not_logged_in));
            logoutButton.setVisibility(View.GONE);
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), getString(R.string.toast_logged_out_successfully), Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigation();
            ((MainActivity) getActivity()).loadAuthFragment(new LoginFragment());
        }
    }
}
