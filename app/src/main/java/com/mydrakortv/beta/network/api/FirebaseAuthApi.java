package com.mydrakortv.beta.network.api;


import com.mydrakortv.beta.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FirebaseAuthApi {

    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> getPhoneAuthStatus(@Header("API-KEY") String apiKey,
                                  @Field("uid") String uid,
                                  @Field("phone") String phoneNo);

    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> getGoogleAuthStatus(@Header("API-KEY") String apiKey,
                                   @Field("uid") String uid,
                                   @Field("email") String phoneNo,
                                   @Field("name") String name);


    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> getFacebookAuthStatus(@Header("API-KEY") String apiKey,
                                     @Field("uid") String uid,
                                     @Field("name") String name,
                                     @Field("email") String email,
                                     @Field("photo_url") String photoUrl);

}
