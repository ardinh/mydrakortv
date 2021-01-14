package com.mydrakortv.beta.network.api;

import com.mydrakortv.beta.model.ActivationModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ActivationApi {

    @FormUrlEncoded
    @POST("user_info_by_code")
    Call<ActivationModel> getActivationInfo(@Header("API-KEY") String key,
                                            @Field("code") String code);
}
