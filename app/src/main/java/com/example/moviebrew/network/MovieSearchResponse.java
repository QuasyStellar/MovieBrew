package com.example.moviebrew.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieSearchResponse {
    @SerializedName("Search")
    public List<Movie> search;

    @SerializedName("totalResults")
    public String totalResults;

    @SerializedName("Response")
    public String response;
}
