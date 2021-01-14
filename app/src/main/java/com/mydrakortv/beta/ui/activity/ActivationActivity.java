package com.mydrakortv.beta.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.Constants;
import com.mydrakortv.beta.NetworkInst;
import com.mydrakortv.beta.utils.PreferenceUtils;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.ActivationModel;
import com.mydrakortv.beta.model.ActiveStatus;
import com.mydrakortv.beta.model.User;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.ActivationApi;
import com.mydrakortv.beta.network.api.SubscriptionApi;

import static com.mydrakortv.beta.fragments.TvSplashFragment.COMPLETED_SPLASH;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ActivationActivity extends Activity {
    private final String TAG = "ActivationActivity";
    private EditText code_et;
    private Button activation_btn;
    private ProgressBar progress_connect;
    private TextView messageTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        initViews();

        if (PreferenceUtils.isLoggedIn(this)) {
            PreferenceUtils.updateSubscriptionStatus(ActivationActivity.this);
            startActivity(new Intent(this, LeanbackActivity.class));
            finish();
        }

        activation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateCode();
            }
        });
    }

    private void initViews() {
        code_et = findViewById(R.id.code_et);
        activation_btn = findViewById(R.id.validate_button);
        progress_connect = findViewById(R.id.progress_connect);

        String appName = this.getResources().getString(R.string.app_name);
        String message1 = this.getResources().getString(R.string.get_activation_code_following);
        String message_2 = "1. Install " + appName + " to your phone.";
        messageTV = findViewById(R.id.instructionMessage1);
        messageTV.setText(message_2);

    }

    private void validateCode() {
        if (!code_et.getText().toString().isEmpty()) {
            if (new NetworkInst(this).isNetworkAvailable()) {
                activation_btn.setEnabled(false);
                progress_connect.setVisibility(View.VISIBLE);
                String code = code_et.getText().toString().trim();

                Retrofit retrofit = RetrofitClient.getRetrofitInstance();
                ActivationApi api = retrofit.create(ActivationApi.class);
                Call<ActivationModel> call = api.getActivationInfo(Config.API_KEY, code);
                call.enqueue(new Callback<ActivationModel>() {
                    @Override
                    public void onResponse(Call<ActivationModel> call, Response<ActivationModel> response) {
                        Log.e(TAG, "code: " + response.code());
                        if (response.code() == 200) {
                            if (response.body().getStatus().equalsIgnoreCase("success")) {
                                activation_btn.setText(getString(R.string.connecting));
                                User user = response.body().getUserInfo();

                                DatabaseHelper db = new DatabaseHelper(ActivationActivity.this);

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
                                updateSubscriptionStatus(db.getUserData().getUserId());

                                startActivity(new Intent(ActivationActivity.this, LeanbackActivity.class));
                                finish();

                            } else {
                                Log.e(TAG, "code: " + response.message());
                                activation_btn.setEnabled(true);
                                progress_connect.setVisibility(View.GONE);
                            }
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<ActivationModel> call, Throwable t) {
                        Log.e(TAG, t.getLocalizedMessage());
                        activation_btn.setEnabled(true);
                        progress_connect.setVisibility(View.GONE);

                    }
                });
            } else {
                activation_btn.setEnabled(true);
                progress_connect.setVisibility(View.GONE);
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSubscriptionStatus(String userId) {
        //get saved user id

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                ActiveStatus activeStatus = response.body();
                DatabaseHelper db = new DatabaseHelper(ActivationActivity.this);

                if (db.getActiveStatusCount() > 1) {
                    db.deleteAllActiveStatusData();
                } else {

                    if (db.getActiveStatusCount() == 0) {
                        db.insertActiveStatusData(activeStatus);
                    } else {
                        db.updateActiveStatus(activeStatus, 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        COMPLETED_SPLASH = false;
        super.onBackPressed();

    }
}
