package com.example.moviebrew.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviebrew.R;
import com.example.moviebrew.network.Movie;
import com.example.moviebrew.ui.adapter.LibraryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView recyclerView;
    private LibraryAdapter adapter;
    private List<Movie> movieList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Spinner sortSpinner;
    private String currentSortOption = "По названию";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_library);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        movieList = new ArrayList<>();
        adapter = new LibraryAdapter(getContext(), movieList);
        recyclerView.setAdapter(adapter);

        sortSpinner = view.findViewById(R.id.spinner_sort_options);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = parent.getItemAtPosition(position).toString();
                sortMovies();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        updateLibraryUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLibraryUI();
    }

    public void updateLibraryUI() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            if (databaseReference == null || !databaseReference.getParent().getKey().equals(userId)) {
                databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("movies");
            }
            fetchLibraryMovies();
        } else {
            movieList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), getString(R.string.toast_user_not_logged_in), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLibraryMovies() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    Movie movie = movieSnapshot.getValue(Movie.class);
                    movieList.add(movie);
                }
                sortMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.toast_failed_to_load_library), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortMovies() {
        if (movieList == null || movieList.isEmpty()) {
            return;
        }

        switch (currentSortOption) {
            case "По названию":
                Collections.sort(movieList, (m1, m2) -> m1.title.compareToIgnoreCase(m2.title));
                break;
            case "По году":
                Collections.sort(movieList, (m1, m2) -> m2.year.compareTo(m1.year));
                break;
        }
        adapter.notifyDataSetChanged();
    }
}
