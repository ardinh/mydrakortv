package com.mydrakortv.beta.network.api;

import com.mydrakortv.beta.model.MovieParser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetParserAPI {

    @GET("parser")
    Call<MovieParser> getParser(@Query("api_key") String api_key,
                                @Query("session_id") String sessionid,
                                @Query("site") String name);
}