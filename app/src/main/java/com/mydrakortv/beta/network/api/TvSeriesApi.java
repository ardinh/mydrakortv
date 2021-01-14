package com.mydrakortv.beta.network.api;


import com.mydrakortv.beta.model.Movie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TvSeriesApi {

    @GET("tvseries")
    Call<List<Movie>> getTvSeries(@Header("API-KEY") String apiKey,
                                  @Query("page") int page);

}
