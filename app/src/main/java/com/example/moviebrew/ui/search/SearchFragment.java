package com.example.moviebrew.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText searchTitle;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchTitle = view.findViewById(R.id.editText_search_title);
        searchButton = view.findViewById(R.id.button_search);
        recyclerView = view.findViewById(R.id.recyclerView_search_results);
        progressBar = view.findViewById(R.id.progressBar_search);

        setupRecyclerView();

        searchButton.setOnClickListener(v -> {
            String query = searchTitle.getText().toString().trim();
            if (!query.isEmpty()) {
                searchMovies(query);
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new MovieAdapter(getContext(), movieList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void searchMovies(String title) {
        progressBar.setVisibility(View.VISIBLE);

        OmdbApiService apiService = ApiClient.getClient().create(OmdbApiService.class);
        Call<MovieSearchResponse> call = apiService.searchMovies(title, BuildConfig.OMDB_API_KEY);

        call.enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().search != null) {
                    movieList.clear();
                    movieList.addAll(response.body().search);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_movies_not_found), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.toast_network_error) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

