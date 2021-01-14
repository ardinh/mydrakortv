package com.mydrakortv.beta.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mydrakortv.beta.Constants;
import com.mydrakortv.beta.utils.PreferenceUtils;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.ActiveStatus;
import com.mydrakortv.beta.ui.activity.LoginChooserActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountFragment extends Fragment {
    private Button sign_out;
    private TextView user_name;
    private TextView user_email;
    private TextView expire_date;
    private TextView active_plan;
    private DatabaseHelper db;


    public MyAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        db = new DatabaseHelper(getContext());
        initViews(view);

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        return view;
    }

    private void initViews(View view) {
        sign_out = view.findViewById(R.id.sign_out_button);
        user_name = view.findViewById(R.id.userNameTv);
        user_email = view.findViewById(R.id.userEmailTv);
        active_plan = view.findViewById(R.id.activePlanTv);
        expire_date = view.findViewById(R.id.expireDateTv);

        if (PreferenceUtils.isLoggedIn(getContext())) {
            user_name.setText(new DatabaseHelper(getContext()).getUserData().getName());
            user_email.setText(new DatabaseHelper(getContext()).getUserData().getEmail());
        }

        ActiveStatus activeStatus = db.getActiveStatusData();

        active_plan.setText(activeStatus.getPackageTitle());
        expire_date.setText(activeStatus.getExpireDate());
    }


    private void signOut() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        String userId = databaseHelper.getUserData().getUserId();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseAuth.getInstance().signOut();
        }
        if (userId != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
            editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
            editor.apply();
            editor.commit();

            databaseHelper.deleteUserData();
            PreferenceUtils.clearSubscriptionSavedData(getContext());

            startActivity(new Intent(getContext(), LoginChooserActivity.class));
            getActivity().finish();
        }
    }

}
