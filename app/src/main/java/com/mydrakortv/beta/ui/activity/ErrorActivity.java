package com.mydrakortv.beta.ui.activity;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.fragments.ErrorFragment;

public class ErrorActivity extends FragmentActivity {

    private static final String TAG = ErrorActivity.class.getSimpleName();

    private ErrorFragment mErrorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leanback);

        testError();
    }

    private void testError() {
        mErrorFragment = new ErrorFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.custom_frame_layout, mErrorFragment).commit();
    }

}
