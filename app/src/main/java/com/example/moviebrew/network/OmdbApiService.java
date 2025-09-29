package com.example.moviebrew.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbApiService {
    @GET("/")
    Call<MovieSearchResponse> searchMovies(
        @Query("s") String title,
        @Query("y") String year,
        @Query("type") String type,
        @Query("apikey") String apiKey
    );

    @GET("/")
    Call<MovieDetail> getMovieDetails(
        @Query("i") String imdbId,
        @Query("apikey") String apiKey
    );
}