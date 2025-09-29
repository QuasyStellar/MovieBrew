package com.example.moviebrew.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviebrew.R;
import com.example.moviebrew.network.MovieCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<MovieCategory> categoryList;
    private List<String> libraryMovieIds;

    public CategoryAdapter(Context context, List<MovieCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.libraryMovieIds = new ArrayList<>();
    }

    public void setLibraryMovieIds(List<String> libraryMovieIds) {
        this.libraryMovieIds = libraryMovieIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        MovieCategory category = categoryList.get(position);
        holder.categoryTitle.setText(category.categoryName);

        MovieAdapter movieAdapter = new MovieAdapter(context, category.movies);
        movieAdapter.setLibraryMovieIds(libraryMovieIds);
        holder.categoryMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.categoryMoviesRecyclerView.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView categoryMoviesRecyclerView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.textView_category_title);
            categoryMoviesRecyclerView = itemView.findViewById(R.id.recyclerView_category_movies);
        }
    }
}