package com.mydrakortv.beta.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.ActiveStatus;
import com.mydrakortv.beta.model.User;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.PhoneNumberApi;
import com.mydrakortv.beta.network.api.SubscriptionApi;
import com.mydrakortv.beta.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PhoneNumberActivity extends Activity {
    private EditText phone_number;
    private ProgressBar progressBar;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
        phone_number = findViewById(R.id.phone);
        progressBar = findViewById(R.id.progress_login);
//        email = getIntent().getStringExtra("email");

    }

    public void OtpBtn(View view) {
        if (phone_number.getText().toString().isEmpty()) {
            new ToastMsg(PhoneNumberActivity.this).toastIconError("Please enter Your Phone Number");
        } else {
            login(phone_number.getText().toString());
        }
    }

    private void login(String otp) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PhoneNumberApi api = retrofit.create(PhoneNumberApi.class);
        Call<User> call = api.postLoginNumberStatus(Config.API_KEY,  otp);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        User user = response.body();
//                        Log.e("TAG", "onResponse: "+response.body().getUserId());
                        Intent intent = new Intent(PhoneNumberActivity.this, OtpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra("req_token",user.getUserId());
                        intent.putExtra("type","phone");

                        startActivity(intent);
                        finish();
                        progressBar.setVisibility(View.GONE);
//                        Otp user = response.body();
//
//                        DatabaseHelper db = new DatabaseHelper(PhoneNumberActivity.this);
//                        if (db.getUserDataCount() > 1) {
//                            db.deleteUserData();
//                        } else {
//                            if (db.getUserDataCount() == 0) {
//                                db.insertUserData(user);
//                            } else {
//                                db.updateUserData(user, 1);
//                            }
//                        }
//                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
//                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
//                        preferences.apply();
//                        preferences.commit();
//
//                        //save user login time, expire time
//                        updateSubscriptionStatus(user.getUserId());

                    } else {
                        new ToastMsg(PhoneNumberActivity.this).toastIconError(response.body().getData());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(PhoneNumberActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
    }

    public void updateSubscriptionStatus(String userId) {
        //get saved user id
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();
                        DatabaseHelper db = new DatabaseHelper(PhoneNumberActivity.this);

                        if (db.getActiveStatusCount() > 1) {
                            db.deleteAllActiveStatusData();
                        } else {

                            if (db.getActiveStatusCount() == 0) {
                                db.insertActiveStatusData(activeStatus);
                            } else {
                                db.updateActiveStatus(activeStatus, 1);
                            }
                        }
                        Intent intent = new Intent(PhoneNumberActivity.this, LeanbackActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
