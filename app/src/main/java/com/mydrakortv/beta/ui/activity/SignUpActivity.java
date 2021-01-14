package com.mydrakortv.beta.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.Constants;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.ActiveStatus;
import com.mydrakortv.beta.model.User;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.SignUpApi;
import com.mydrakortv.beta.network.api.SubscriptionApi;
import com.mydrakortv.beta.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends Activity {
    private EditText nameEt, emailEt, passwordEt;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEt = findViewById(R.id.name_edit_text);
        emailEt = findViewById(R.id.email_edit_text);
        passwordEt = findViewById(R.id.password_edit_text);
        progressBar = findViewById(R.id.progress_sign_up);
    }

    public void signUpBtn(View view) {
        if (!isValidEmailAddress(emailEt.getText().toString())) {
            new ToastMsg(SignUpActivity.this).toastIconError("please enter valid email");
        } else if (passwordEt.getText().toString().equals("")) {
            new ToastMsg(SignUpActivity.this).toastIconError("please enter password");
        } else if (nameEt.getText().toString().equals("")) {
            new ToastMsg(SignUpActivity.this).toastIconError("please enter name");
        } else {
            String email = emailEt.getText().toString();
            String pass = passwordEt.getText().toString();
            String name = nameEt.getText().toString();
            signUp(email, pass, name);
        }
    }

    private void signUp(String email, String pass, String name) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SignUpApi signUpApi = retrofit.create(SignUpApi.class);
        Call<User> call = signUpApi.signUp(Config.API_KEY, email, pass, name);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
                        new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
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
                    } else if (response.body().getStatus().equals("error")) {
                        new ToastMsg(SignUpActivity.this).toastIconError(response.body().getData());
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(SignUpActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
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
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);

                        if (db.getActiveStatusCount() > 1) {
                            db.deleteAllActiveStatusData();
                        } else {

                            if (db.getActiveStatusCount() == 0) {
                                db.insertActiveStatusData(activeStatus);
                            } else {
                                db.updateActiveStatus(activeStatus, 1);
                            }
                        }
                        Intent intent = new Intent(SignUpActivity.this, LeanbackActivity.class);
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
