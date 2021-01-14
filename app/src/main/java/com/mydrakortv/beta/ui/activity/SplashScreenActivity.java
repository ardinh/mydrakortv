package com.mydrakortv.beta.ui.activity;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;

public class SplashScreenActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);


    }
}
