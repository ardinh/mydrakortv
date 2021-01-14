package com.mydrakortv.beta.network.api;

import com.mydrakortv.beta.model.CountryModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CountryApi {

    @GET("all_country")
    Call<List<CountryModel>> getAllCountry(@Header("API-KEY") String apiKey);

}
