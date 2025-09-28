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

            ft.add(R.id.fragment_container, searchFragment, SearchFragment.class.getName()).hide(searchFragment);
            ft.add(R.id.fragment_container, libraryFragment, LibraryFragment.class.getName()).hide(libraryFragment);
            ft.add(R.id.fragment_container, profileFragment, ProfileFragment.class.getName()).hide(profileFragment);
            ft.add(R.id.fragment_container, loginFragment, LoginFragment.class.getName()).hide(loginFragment);
            ft.add(R.id.fragment_container, registerFragment, RegisterFragment.class.getName()).hide(registerFragment);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                hideBottomNavigation();
                ft.show(loginFragment);
                activeFragment = loginFragment;
            } else {
                showBottomNavigation();
                ft.show(searchFragment);
                activeFragment = searchFragment;
                bottomNavigationView.setSelectedItemId(R.id.navigation_search);
            }
            ft.commit();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            searchFragment = (SearchFragment) fm.findFragmentByTag(SearchFragment.class.getName());
            libraryFragment = (LibraryFragment) fm.findFragmentByTag(LibraryFragment.class.getName());
            profileFragment = (ProfileFragment) fm.findFragmentByTag(ProfileFragment.class.getName());
            loginFragment = (LoginFragment) fm.findFragmentByTag(LoginFragment.class.getName());
            registerFragment = (RegisterFragment) fm.findFragmentByTag(RegisterFragment.class.getName());

            FragmentTransaction ft = fm.beginTransaction();
            if (searchFragment != null) ft.hide(searchFragment);
            if (libraryFragment != null) ft.hide(libraryFragment);
            if (profileFragment != null) ft.hide(profileFragment);
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
                ft.show(searchFragment);
                activeFragment = searchFragment;
            }
            ft.commit();
            if (activeFragment == searchFragment) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_search);
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment targetFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_search) {
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

        for (Fragment fragment : fm.getFragments()) {
            ft.remove(fragment);
        }

        ft.add(R.id.fragment_container, searchFragment, SearchFragment.class.getName());
        activeFragment = searchFragment;
        ft.commit();

        fm.executePendingTransactions();

        if (profileFragment != null) {
            profileFragment.updateProfileUI();
        }
        if (libraryFragment != null) {
            libraryFragment.updateLibraryUI();
        }
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeFragment != null) {
            outState.putString("activeFragmentTag", activeFragment.getTag());
        }
    }
}