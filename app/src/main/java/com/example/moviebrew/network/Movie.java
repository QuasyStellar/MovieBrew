package com.example.moviebrew.network;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("Title")
    public String title;

    @SerializedName("Year")
    public String year;

    @SerializedName("imdbID")
    public String imdbID;

    @SerializedName("Type")
    public String type;

    @SerializedName("Poster")
    public String poster;

    // Required for Firebase Realtime Database
    public Movie() {
    }
}
