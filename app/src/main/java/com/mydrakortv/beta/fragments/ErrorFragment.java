package com.mydrakortv.beta.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.leanback.app.ErrorSupportFragment;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.ui.activity.ErrorActivity;


public class ErrorFragment extends ErrorSupportFragment {

    private static final String TAG = ErrorFragment.class.getSimpleName();
    private static final boolean TRANSLUCENT = true;

    ErrorActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        activity = (ErrorActivity) getActivity();

        setTitle(getResources().getString(R.string.app_name));
        setErrorContent();
    }

    void setErrorContent() {
        setImageDrawable(getActivity().getResources().getDrawable(R.drawable.lb_ic_sad_cloud));
        setMessage(getResources().getString(R.string.no_internet));
        setDefaultBackground(TRANSLUCENT);

        setButtonText(getResources().getString(R.string.close));
        setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit();
                activity.finish();
            }
        });
    }
}