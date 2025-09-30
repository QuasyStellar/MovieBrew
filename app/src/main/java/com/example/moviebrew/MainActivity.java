package com.example.moviebrew;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.moviebrew.ui.auth.LoginFragment;
import com.example.moviebrew.ui.auth.RegisterFragment;
import com.example.moviebrew.ui.library.LibraryFragment;
import com.example.moviebrew.ui.profile.ProfileFragment;
import com.example.moviebrew.ui.recommendations.RecommendationsFragment;
import com.example.moviebrew.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        if (savedInstanceState == null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                hideBottomNavigation();
                loadFragment(new LoginFragment());
            } else {
                showBottomNavigation();
                loadFragment(new RecommendationsFragment());
                bottomNavigationView.setSelectedItemId(R.id.navigation_recommendations);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_recommendations) {
            selectedFragment = new RecommendationsFragment();
        } else if (itemId == R.id.navigation_search) {
            selectedFragment = new SearchFragment();
        } else if (itemId == R.id.navigation_library) {
            selectedFragment = new LibraryFragment();
        } else if (itemId == R.id.navigation_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
        return true;
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public void showBottomNavigation() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigation() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void navigateToMainContent() {
        showBottomNavigation();
        loadFragment(new RecommendationsFragment());
        bottomNavigationView.setSelectedItemId(R.id.navigation_recommendations);
    }
    public void loadAuthFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
