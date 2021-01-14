package com.mydrakortv.beta.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.VerticalGridSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;


import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.NetworkInst;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.model.Genre;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.GenreApi;
import com.mydrakortv.beta.ui.BackgroundHelper;
import com.mydrakortv.beta.ui.activity.ErrorActivity;
import com.mydrakortv.beta.ui.activity.ItemGenreActivity;
import com.mydrakortv.beta.ui.activity.LeanbackActivity;
import com.mydrakortv.beta.ui.presenter.GenreCardPresenter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class GenreFragment extends VerticalGridSupportFragment {
    public static final String GENRE = "genre";
    private static final String TAG = GenreFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 8;
    private int pageCount = 1;
    private boolean dataAvailable = true;

    //private BackgroundHelper bgHelper;
    private List<Genre> genres = new ArrayList<>();
    private ArrayObjectAdapter mAdapter;
    private LeanbackActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        activity = (LeanbackActivity) getActivity();
        activity.hideLogo();
        setTitle(getResources().getString(R.string.genre));
        //bgHelper = new BackgroundHelper(getActivity());

        setOnItemViewClickedListener(getDefaultItemViewClickedListener());
        setOnItemViewSelectedListener(getDefaultItemSelectedListener());

        // setup
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new GenreCardPresenter());
        setAdapter(mAdapter);

        fetchGenreData(pageCount);

    }

    // click listener
    private OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o, RowPresenter.ViewHolder viewHolder2, Row row) {
                Genre genre = (Genre) o;
                Intent intent = new Intent(getActivity(), ItemGenreActivity.class);
                intent.putExtra("id", genre.getGenreId());
                intent.putExtra("title", genre.getName());
                startActivity(intent);

            }
        };
    }


    // selected listener for setting blur background each time when the item will select.
    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, final Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {


                if (item instanceof Genre) {
                    /*bgHelper = new BackgroundHelper(getActivity());
                    bgHelper.prepareBackgroundManager();
                    bgHelper.startBackgroundTimer(((Genre) item).getImageUrl());*/


                }
            }
        };
    }

    public void fetchGenreData(int pageCount) {


        if (!new NetworkInst(activity).isNetworkAvailable()) {
            Intent intent = new Intent(activity, ErrorActivity.class);
            startActivity(intent);
            activity.finish();
            return;
        }

        final SpinnerFragment mSpinnerFragment = new SpinnerFragment();
        final FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.custom_frame_layout, mSpinnerFragment).commit();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        GenreApi api = retrofit.create(GenreApi.class);
        Call<List<Genre>> call = api.getGenres(Config.API_KEY, pageCount);
        call.enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
                if (response.code() == 200) {
                    List<Genre> genreList = response.body();
                    if (genreList.size() <= 0) {
                        dataAvailable = false;
                        Toast.makeText(activity, getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                    }

                    for (Genre genre : genreList) {
                        mAdapter.add(genre);
                    }

                    mAdapter.notifyArrayItemRangeChanged(genreList.size() - 1, genreList.size() + genres.size());
                    genres.addAll(genreList);

                    // hide the spinner
                    fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();

                }
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                t.printStackTrace();
                // hide the spinner
                fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        genres = new ArrayList<>();
        pageCount = 1;
        dataAvailable = true;

    }

}
