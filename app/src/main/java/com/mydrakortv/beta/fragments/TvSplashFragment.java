package com.mydrakortv.beta.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.ui.activity.LeanbackActivity;
import com.mydrakortv.beta.ui.activity.LoginChooserActivity;
import com.mydrakortv.beta.utils.PreferenceUtils;

public class TvSplashFragment extends Fragment {
    private static final int REQUEST_CODE = 123;
    private static final String TAG = "TvSplashScreen";
    public static boolean COMPLETED_SPLASH = false;
    private static final long SPLASH_DURATION_MS = 2000;

    final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkStoragePermission();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_splash_screen, container, false);

        if (getContext().getPackageManager().hasSystemFeature(AMAZON_FEATURE_FIRE_TV)) {
            Log.v(TAG, "Yes, this is a Fire TV device.");
        } else {
            Log.v(TAG, "No, this is not a Fire TV device.");
        }

        return view;
    }

    private void checkUserData() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PreferenceUtils.isLoggedIn(getContext())) {
                    Intent intent = new Intent(getContext(), LeanbackActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    startActivity(new Intent(getActivity(), LoginChooserActivity.class));
                    getActivity().finish();
                }

            }
        }, SPLASH_DURATION_MS);
    }

    // ------------------ checking storage permission ------------
    private void checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int grant = ActivityCompat.checkSelfPermission(getActivity(), permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            requestPermissions( permission_list, 1);
        }else {
            checkUserData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // perform your action here
                checkUserData();
            } else {
                Toast.makeText(getActivity(),"permission not granted", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        }

    }
}