package com.mydrakortv.beta.network.api;

import com.mydrakortv.beta.model.Otp;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface EmailOtpApi {
        @FormUrlEncoded
        @POST("verify_email")
        Call<Otp> postotpcode(@Header("API-KEY") String apiKey,
                                  @Field("request_token") String token,
                                  @Field("otp") String otp);

}
