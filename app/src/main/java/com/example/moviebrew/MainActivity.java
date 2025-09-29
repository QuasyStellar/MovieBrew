package com.example.moviebrew;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.moviebrew.ui.auth.LoginFragment;
import com.example.moviebrew.ui.library.LibraryFragment;
import com.example.moviebrew.ui.profile.ProfileFragment;
import com.example.moviebrew.ui.recommendations.RecommendationsFragment;
import com.example.moviebrew.ui.search.SearchFragment;
import com.example.moviebrew.ui.auth.RegisterFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    private SearchFragment searchFragment;
    private LibraryFragment libraryFragment;
    private ProfileFragment profileFragment;
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;
    private RecommendationsFragment recommendationsFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            searchFragment = new SearchFragment();
            libraryFragment = new LibraryFragment();
            profileFragment = new ProfileFragment();
            loginFragment = new LoginFragment();
            registerFragment = new RegisterFragment();
            recommendationsFragment = new RecommendationsFragment();

            ft.add(R.id.fragment_container, loginFragment, LoginFragment.class.getName()).hide(loginFragment);
            ft.add(R.id.fragment_container, registerFragment, RegisterFragment.class.getName()).hide(registerFragment);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                hideBottomNavigation();
                ft.show(loginFragment);
                activeFragment = loginFragment;
            } else {
                showBottomNavigation();
                ft.add(R.id.fragment_container, recommendationsFragment, RecommendationsFragment.class.getName());
                activeFragment = recommendationsFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_recommendations);
            }
            ft.commit();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            searchFragment = (SearchFragment) fm.findFragmentByTag(SearchFragment.class.getName());
            libraryFragment = (LibraryFragment) fm.findFragmentByTag(LibraryFragment.class.getName());
            profileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.class.getName());
            loginFragment = (LoginFragment) fm.findFragmentByTag(LoginFragment.class.getName());
            registerFragment = (RegisterFragment) fm.findFragmentByTag(RegisterFragment.class.getName());
            recommendationsFragment = (RecommendationsFragment) fm.findFragmentByTag(RecommendationsFragment.class.getName());

            FragmentTransaction ft = fm.beginTransaction();
            if (loginFragment != null) ft.hide(loginFragment);
            if (registerFragment != null) ft.hide(registerFragment);

            Fragment previouslyActiveFragment = null;
            String activeFragmentTag = savedInstanceState.getString("activeFragmentTag");
            if (activeFragmentTag != null) {
                previouslyActiveFragment = fm.findFragmentByTag(activeFragmentTag);
            }

            if (previouslyActiveFragment != null) {
                ft.show(previouslyActiveFragment);
                activeFragment = previouslyActiveFragment;
            } else {
                showBottomNavigation();
                ft.show(recommendationsFragment);
                activeFragment = recommendationsFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_recommendations);
            }
            ft.commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment targetFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_recommendations) {
                targetFragment = recommendationsFragment;
            } else if (itemId == R.id.navigation_search) {
                targetFragment = searchFragment;
            } else if (itemId == R.id.navigation_library) {
                targetFragment = libraryFragment;
            } else if (itemId == R.id.navigation_profile) {
                targetFragment = profileFragment;
            }

            if (targetFragment != null) {
                switchFragment(targetFragment);
            }
            return true;
        });
    }

    public void showBottomNavigation() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigation() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    private void switchFragment(Fragment targetFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (activeFragment != null && activeFragment.isAdded()) {
            ft.hide(activeFragment);
        }

        if (!targetFragment.isAdded()) {
            ft.add(R.id.fragment_container, targetFragment, targetFragment.getClass().getName());
        }
        ft.show(targetFragment);
        ft.commit();
        activeFragment = targetFragment;
    }

    public void loadAuthFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_container, fragment, fragment.getClass().getName());
        ft.commit();
        activeFragment = fragment;
    }

    public void navigateToMainContent() {
        showBottomNavigation();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        LoginFragment currentLoginFragment = (LoginFragment) fm.findFragmentByTag(LoginFragment.class.getName());
        RegisterFragment currentRegisterFragment = (RegisterFragment) fm.findFragmentByTag(RegisterFragment.class.getName());
        RecommendationsFragment currentRecommendationsFragment = (RecommendationsFragment) fm.findFragmentByTag(RecommendationsFragment.class.getName());

        if (currentLoginFragment != null && currentLoginFragment.isAdded()) {
            ft.remove(currentLoginFragment);
        }
        if (currentRegisterFragment != null && currentRegisterFragment.isAdded()) {
            ft.remove(currentRegisterFragment);
        }

        if (currentRecommendationsFragment == null) {
            currentRecommendationsFragment = new RecommendationsFragment();
            ft.add(R.id.fragment_container, currentRecommendationsFragment, RecommendationsFragment.class.getName());
        }

        ft.replace(R.id.fragment_container, currentRecommendationsFragment, RecommendationsFragment.class.getName());
        activeFragment = currentRecommendationsFragment;
        ft.commit();

        fm.executePendingTransactions();

        SearchFragment currentSearchFragment = (SearchFragment) fm.findFragmentByTag(SearchFragment.class.getName());
        LibraryFragment currentLibraryFragment = (LibraryFragment) fm.findFragmentByTag(LibraryFragment.class.getName());
        ProfileFragment currentProfileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.class.getName());

        if (currentProfileFragment != null) {
            currentProfileFragment.updateProfileUI();
        }
        if (currentLibraryFragment != null) {
            currentLibraryFragment.updateLibraryUI();
        }
        bottomNavigationView.setSelectedItemId(R.id.navigation_recommendations);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeFragment != null) {
            outState.putString("activeFragmentTag", activeFragment.getTag());
        }
    }
}