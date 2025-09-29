package com.example.moviebrew.ui.recommendations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviebrew.BuildConfig;
import com.example.moviebrew.R;
import com.example.moviebrew.network.ApiClient;
import com.example.moviebrew.network.MovieCategory;
import com.example.moviebrew.network.MovieSearchResponse;
import com.example.moviebrew.network.OmdbApiService;
import com.example.moviebrew.ui.adapter.CategoryAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<MovieCategory> categoryList;
    private OmdbApiService omdbApiService;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ValueEventListener libraryListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_recommendations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), categoryList);
        recyclerView.setAdapter(adapter);

        omdbApiService = ApiClient.getClient().create(OmdbApiService.class);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("movies");
            fetchLibraryMovieIds();
        }

        fetchRecommendations();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && libraryListener != null) {
            databaseReference.removeEventListener(libraryListener);
        }
    }

    private void fetchRecommendations() {
        Map<String, String> genreMap = new LinkedHashMap<>();
        genreMap.put("Action", getString(R.string.genre_action));
        genreMap.put("Comedy", getString(R.string.genre_comedy));
        genreMap.put("Drama", getString(R.string.genre_drama));
        genreMap.put("Horror", getString(R.string.genre_horror));
        genreMap.put("Sci-Fi", getString(R.string.genre_scifi));

        for (Map.Entry<String, String> entry : genreMap.entrySet()) {
            fetchMoviesForCategory(entry.getKey(), entry.getValue());
        }
    }

    private void fetchMoviesForCategory(String englishGenre, String russianGenre) {
        String searchTerm = englishGenre;

        Call<MovieSearchResponse> call = omdbApiService.searchMovies(searchTerm, "", "", BuildConfig.OMDB_API_KEY);
        call.enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().search != null) {
                    MovieCategory movieCategory = new MovieCategory(russianGenre, response.body().search);
                    categoryList.add(movieCategory);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load movies for " + russianGenre, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error for " + russianGenre + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
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