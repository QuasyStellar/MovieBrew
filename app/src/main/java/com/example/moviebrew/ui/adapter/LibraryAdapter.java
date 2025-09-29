package com.example.moviebrew.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviebrew.MainActivity;
import com.example.moviebrew.R;
import com.example.moviebrew.network.Movie;
import com.example.moviebrew.ui.detail.MovieDetailFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder> {

    private List<Movie> movies;
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    public LibraryAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.mAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_library, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.title.setText(movie.title);
        holder.year.setText(movie.year);

        Glide.with(context)
                .load(movie.poster)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.poster);

        holder.userRating.setRating(movie.userRating);
        holder.userRating.setClickable(true);
        holder.userRating.setFocusable(true);

        holder.userRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    databaseReference.child("users").child(userId).child("movies").child(movie.imdbID).child("userRating").setValue(rating);
                }
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                databaseReference.child("users").child(userId).child("movies").child(movie.imdbID).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, context.getString(R.string.toast_movie_removed), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, context.getString(R.string.toast_failed_to_remove_movie), Toast.LENGTH_SHORT).show());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadAuthFragment(MovieDetailFragment.newInstance(movie.imdbID));
            }
        });

        holder.detailsButton.setOnClickListener(v -> openMovieDetails(movie));
    }

    private void openMovieDetails(Movie movie) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).loadAuthFragment(MovieDetailFragment.newInstance(movie.imdbID));
        }
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    public static class LibraryViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        TextView year;
        Button removeButton;
        Button detailsButton;
        RatingBar userRating;

        public LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imageView_poster);
            title = itemView.findViewById(R.id.textView_title);
            year = itemView.findViewById(R.id.textView_year);
            removeButton = itemView.findViewById(R.id.button_remove_from_library);
            detailsButton = itemView.findViewById(R.id.button_details);
            userRating = itemView.findViewById(R.id.ratingBar_user_rating);
        }
    }
}