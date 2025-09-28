package com.example.moviebrew.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.mAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.title.setText(movie.title);
        holder.year.setText(movie.year);

        Glide.with(context)
                .load(movie.poster)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.poster);

        holder.addButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                databaseReference.child("users").child(userId).child("movies").child(movie.imdbID).setValue(movie)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Фильм добавлен", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Не удалось добавить фильм", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            }
        });

        // Make the whole item clickable to view details
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadAuthFragment(MovieDetailFragment.newInstance(movie.imdbID)); // Use loadAuthFragment
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        TextView year;
        Button addButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imageView_poster);
            title = itemView.findViewById(R.id.textView_title);
            year = itemView.findViewById(R.id.textView_year);
            addButton = itemView.findViewById(R.id.button_add_to_library);
        }
    }
}

