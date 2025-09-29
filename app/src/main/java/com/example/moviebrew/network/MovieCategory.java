package com.example.moviebrew.network;

import java.util.List;

public class MovieCategory {
    public String categoryName;
    public List<Movie> movies;

    public MovieCategory(String categoryName, List<Movie> movies) {
        this.categoryName = categoryName;
        this.movies = movies;
    }
}