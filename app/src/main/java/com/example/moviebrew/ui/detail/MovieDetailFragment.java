package com.example.moviebrew.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.moviebrew.BuildConfig;
import com.example.moviebrew.R;
import com.example.moviebrew.network.ApiClient;
import com.example.moviebrew.network.Movie;
import com.example.moviebrew.network.MovieDetail;
import com.example.moviebrew.network.OmdbApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailFragment extends Fragment {

    private static final String ARG_IMDB_ID = "imdb_id";

    private ImageView posterImageView;
    private TextView titleTextView, genreTextView, directorTextView, actorsTextView, plotTextView;
    private ProgressBar progressBar;
    private Button addToLibraryButton;
    private Button removeFromLibraryButton;

    private String imdbId;
    private Movie currentMovie;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    public MovieDetailFragment() {
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
        addToLibraryButton = view.findViewById(R.id.button_add_to_library_detail);
        removeFromLibraryButton = view.findViewById(R.id.button_remove_from_library_detail);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (imdbId != null) {
            fetchMovieDetails(imdbId);
        } else {
            Toast.makeText(getContext(), getString(R.string.toast_movie_id_not_found), Toast.LENGTH_SHORT).show();
        }

        addToLibraryButton.setOnClickListener(v -> addMovieToLibrary());
        removeFromLibraryButton.setOnClickListener(v -> removeMovieFromLibrary());

        return view;
    }

    private void addMovieToLibrary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentMovie != null) {
            String userId = currentUser.getUid();
            databaseReference.child("users").child(userId).child("movies").child(currentMovie.imdbID).setValue(currentMovie)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), getString(R.string.toast_movie_added), Toast.LENGTH_SHORT).show();
                        updateButtonVisibility(true);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.toast_failed_to_add_movie), Toast.LENGTH_SHORT).show());
        } else if (currentUser == null) {
            Toast.makeText(getContext(), getString(R.string.toast_please_login), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMovieFromLibrary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentMovie != null) {
            String userId = currentUser.getUid();
            databaseReference.child("users").child(userId).child("movies").child(currentMovie.imdbID).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), getString(R.string.toast_movie_removed), Toast.LENGTH_SHORT).show();
                        updateButtonVisibility(false);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.toast_failed_to_remove_movie), Toast.LENGTH_SHORT).show());
        } else if (currentUser == null) {
            Toast.makeText(getContext(), getString(R.string.toast_please_login), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonVisibility(boolean isInLibrary) {
        if (isInLibrary) {
            addToLibraryButton.setVisibility(View.GONE);
            removeFromLibraryButton.setVisibility(View.VISIBLE);
        } else {
            addToLibraryButton.setVisibility(View.VISIBLE);
            removeFromLibraryButton.setVisibility(View.GONE);
        }
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
                    currentMovie = new Movie();
                    currentMovie.imdbID = movieDetail.imdbID;
                    currentMovie.title = movieDetail.title;
                    currentMovie.year = movieDetail.year;
                    currentMovie.poster = movieDetail.poster;

                    titleTextView.setText(movieDetail.title + " (" + movieDetail.year + ")");
                    genreTextView.setText(getString(R.string.movie_detail_genre) + " " + movieDetail.genre);
                    directorTextView.setText(getString(R.string.movie_detail_director) + " " + movieDetail.director);
                    actorsTextView.setText(getString(R.string.movie_detail_actors) + " " + movieDetail.actors);
                    plotTextView.setText(movieDetail.plot);

                    Glide.with(getContext())
                            .load(movieDetail.poster)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(posterImageView);

                    checkMovieInLibrary();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_failed_to_load_movie_details), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.toast_network_error_detail) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkMovieInLibrary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentMovie != null) {
            String userId = currentUser.getUid();
            databaseReference.child("users").child(userId).child("movies").child(currentMovie.imdbID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    updateButtonVisibility(snapshot.exists());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to check library status.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateButtonVisibility(false);
        }
    }
}