package com.mydrakortv.beta.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.Constants;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.ActiveStatus;
import com.mydrakortv.beta.model.Otp;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.EmailOtpApi;
import com.mydrakortv.beta.network.api.PhoneOtpApi;
import com.mydrakortv.beta.network.api.SubscriptionApi;
import com.mydrakortv.beta.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class OtpActivity extends Activity {
    private EditText otp_code;
    private ProgressBar progressBar;
    private String req_token, type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otp_code = findViewById(R.id.otp);
        progressBar = findViewById(R.id.progress_login);
        req_token = getIntent().getStringExtra("req_token");
        type = getIntent().getStringExtra("type");

    }

    public void otpBtn(View view) {
//        Log.e("TAG", "otpBtn: "+req_token);
        if(otp_code.getText().toString().isEmpty()){
            new ToastMsg(OtpActivity.this).toastIconError("Please enter OTP Code");
        }else{
            if(type.equals("email")){
                login_email(req_token,otp_code.getText().toString());
            }else{
                login_phone(req_token,otp_code.getText().toString());
            }
        }
    }

    private void login_email(String req_token, String otp) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        EmailOtpApi api = retrofit.create(EmailOtpApi.class);
        Call<Otp> call = api.postotpcode(Config.API_KEY, req_token,otp);
        call.enqueue(new Callback<Otp>() {
            @Override
            public void onResponse(Call<Otp> call, Response<Otp> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        Otp user = response.body();

                        DatabaseHelper db = new DatabaseHelper(OtpActivity.this);
                        if (db.getUserDataCount() > 1) {
                            db.deleteUserData();
                        } else {
                            if (db.getUserDataCount() == 0) {
                                db.insertUserData(user);
                            } else {
                                db.updateUserData(user, 1);
                            }
                        }
                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());

                    } else {
                        new ToastMsg(OtpActivity.this).toastIconError("Wrong OTP Code");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Otp> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(OtpActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
    }

    private void login_phone(String email, String otp) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PhoneOtpApi api = retrofit.create(PhoneOtpApi.class);
        Call<Otp> call = api.postotpcode(Config.API_KEY, email,otp);
        call.enqueue(new Callback<Otp>() {
            @Override
            public void onResponse(Call<Otp> call, Response<Otp> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        Otp user = response.body();

                        DatabaseHelper db = new DatabaseHelper(OtpActivity.this);
                        if (db.getUserDataCount() > 1) {
                            db.deleteUserData();
                        } else {
                            if (db.getUserDataCount() == 0) {
                                db.insertUserData(user);
                            } else {
                                db.updateUserData(user, 1);
                            }
                        }
                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUserId());

                    } else {
                        new ToastMsg(OtpActivity.this).toastIconError("Wrong OTP Code");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Otp> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(OtpActivity.this).toastIconError("");
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
                        DatabaseHelper db = new DatabaseHelper(OtpActivity.this);

                        if (db.getActiveStatusCount() > 1) {
                            db.deleteAllActiveStatusData();
                        } else {

                            if (db.getActiveStatusCount() == 0) {
                                db.insertActiveStatusData(activeStatus);
                            } else {
                                db.updateActiveStatus(activeStatus, 1);
                            }
                        }
                        Intent intent = new Intent(OtpActivity.this, LeanbackActivity.class);
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
