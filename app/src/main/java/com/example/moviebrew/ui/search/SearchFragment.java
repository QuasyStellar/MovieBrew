package com.example.moviebrew.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviebrew.BuildConfig;
import com.example.moviebrew.R;
import com.example.moviebrew.network.ApiClient;
import com.example.moviebrew.network.Movie;
import com.example.moviebrew.network.MovieSearchResponse;
import com.example.moviebrew.network.OmdbApiService;
import com.example.moviebrew.ui.adapter.MovieAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText searchTitle;
    private EditText searchYear;
    private Spinner searchType;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ValueEventListener libraryListener;
    private TextView searchPlaceholder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchTitle = view.findViewById(R.id.editText_search_title);
        searchButton = view.findViewById(R.id.button_search);
        searchYear = view.findViewById(R.id.editText_search_year);
        searchType = view.findViewById(R.id.spinner_search_type);
        recyclerView = view.findViewById(R.id.recyclerView_search_results);
        progressBar = view.findViewById(R.id.progressBar_search);
        searchPlaceholder = view.findViewById(R.id.textView_search_placeholder);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.search_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchType.setAdapter(typeAdapter);

        setupRecyclerView();

        searchPlaceholder.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("movies");
            fetchLibraryMovieIds();
        }

        searchButton.setOnClickListener(v -> {
            String query = searchTitle.getText().toString().trim();
            String year = searchYear.getText().toString().trim();
            String type = searchType.getSelectedItem().toString();

            if (type.equals(getString(R.string.search_type_any))) {
                type = "";
            } else if (type.equals(getString(R.string.search_type_movie))) {
                type = "movie";
            } else if (type.equals(getString(R.string.search_type_series))) {
                type = "series";
            } else if (type.equals(getString(R.string.search_type_episode))) {
                type = "episode";
            }

            if (!query.isEmpty()) {
                searchMovies(query, year, type);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && libraryListener != null) {
            databaseReference.removeEventListener(libraryListener);
        }
    }

    private void setupRecyclerView() {
        adapter = new MovieAdapter(getContext(), movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void searchMovies(String title, String year, String type) {
        progressBar.setVisibility(View.VISIBLE);
        searchPlaceholder.setVisibility(View.GONE);

        OmdbApiService apiService = ApiClient.getClient().create(OmdbApiService.class);
        Call<MovieSearchResponse> call = apiService.searchMovies(title, year, type, BuildConfig.OMDB_API_KEY);

        call.enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                progressBar.setVisibility(View.GONE);
                movieList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().search != null) {
                    movieList.clear();
                    movieList.addAll(response.body().search);
                    adapter.notifyDataSetChanged();
                    if (movieList.isEmpty()) {
                        searchPlaceholder.setVisibility(View.VISIBLE);
                    } else {
                        searchPlaceholder.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_movies_not_found), Toast.LENGTH_SHORT).show();
                    searchPlaceholder.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.toast_network_error) + t.getMessage(), Toast.LENGTH_SHORT).show();
                searchPlaceholder.setVisibility(View.VISIBLE);
            }
        });
    }

    private void fetchLibraryMovieIds() {
        libraryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> movieIds = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    movieIds.add(movieSnapshot.getKey());
                }
                adapter.setLibraryMovieIds(movieIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load library movie IDs.", Toast.LENGTH_SHORT).show();
            }
        };
        databaseReference.addValueEventListener(libraryListener);
    }
}