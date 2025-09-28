package com.example.moviebrew.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.moviebrew.BuildConfig;
import com.example.moviebrew.R;
import com.example.moviebrew.network.ApiClient;
import com.example.moviebrew.network.MovieDetail;
import com.example.moviebrew.network.OmdbApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_IMDB_ID = "imdb_id";

    private ImageView posterImageView;
    private TextView titleTextView, genreTextView, directorTextView, actorsTextView, plotTextView;
    private ProgressBar progressBar;

    private String imdbId;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(String imdbId) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMDB_ID, imdbId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imdbId = getArguments().getString(ARG_IMDB_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        posterImageView = view.findViewById(R.id.imageView_detail_poster);
        titleTextView = view.findViewById(R.id.textView_detail_title);
        genreTextView = view.findViewById(R.id.textView_detail_genre);
        directorTextView = view.findViewById(R.id.textView_detail_director);
        actorsTextView = view.findViewById(R.id.textView_detail_actors);
        plotTextView = view.findViewById(R.id.textView_detail_plot);
        progressBar = view.findViewById(R.id.progressBar_detail);

        if (imdbId != null) {
            fetchMovieDetails(imdbId);
        } else {
            Toast.makeText(getContext(), "Movie ID not found.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchMovieDetails(String imdbId) {
        progressBar.setVisibility(View.VISIBLE);

        OmdbApiService apiService = ApiClient.getClient().create(OmdbApiService.class);
        Call<MovieDetail> call = apiService.getMovieDetails(imdbId, BuildConfig.OMDB_API_KEY);

        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().response.equals("True")) {
                    MovieDetail movieDetail = response.body();
                    titleTextView.setText(movieDetail.title + " (" + movieDetail.year + ")");
                    genreTextView.setText("Genre: " + movieDetail.genre);
                    directorTextView.setText("Director: " + movieDetail.director);
                    actorsTextView.setText("Actors: " + movieDetail.actors);
                    plotTextView.setText(movieDetail.plot);

                    Glide.with(getContext())
                            .load(movieDetail.poster)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(posterImageView);
                } else {
                    Toast.makeText(getContext(), "Failed to load movie details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
