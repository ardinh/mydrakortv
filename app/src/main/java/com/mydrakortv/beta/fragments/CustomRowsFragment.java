package com.mydrakortv.beta.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.NetworkInst;
import com.mydrakortv.beta.utils.PreferenceUtils;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.Channel;
import com.mydrakortv.beta.model.HomeContent;
import com.mydrakortv.beta.model.LiveTvCategory;
import com.mydrakortv.beta.model.VideoContent;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.HomeApi;
import com.mydrakortv.beta.network.api.LiveTvApi;
import com.mydrakortv.beta.ui.BackgroundHelper;
import com.mydrakortv.beta.ui.Utils;
import com.mydrakortv.beta.ui.activity.ErrorActivity;
import com.mydrakortv.beta.ui.activity.LeanbackActivity;
import com.mydrakortv.beta.ui.activity.PlayerActivity;
import com.mydrakortv.beta.ui.activity.VideoDetailsActivity;
import com.mydrakortv.beta.ui.presenter.CardPresenter;
import com.mydrakortv.beta.ui.presenter.LiveTvCardPresenter;
import com.mydrakortv.beta.ui.presenter.SliderCardPresenter;
import com.mydrakortv.beta.ui.presenter.TvPresenter;
import com.mydrakortv.beta.utils.PaidDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CustomRowsFragment extends RowsSupportFragment {
    private boolean loadedHomeContent;
    private boolean loadedLiveTvContent;
    private BackgroundHelper bgHelper;
    private ArrayObjectAdapter rowsAdapter;
    private CardPresenter cardPresenter;
    private LiveTvCardPresenter liveTvCardPresenter;
    private View v;
    private LeanbackActivity activity;
    private int menuPos;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bgHelper = new BackgroundHelper(getActivity());
//        bgHelper.prepareBackgroundManager();

        activity = (LeanbackActivity) getActivity();
        activity.showLogo();


        setOnItemViewClickedListener(getDefaultItemViewClickedListener());
        setOnItemViewSelectedListener(getDefaultItemSelectedListener());

        Bundle bundle = getArguments();
        if (bundle != null) {
            menuPos = bundle.getInt("menu");

            if (menuPos == 0) { // for Home content

                if (new NetworkInst(activity).isNetworkAvailable()) {
                    if (!loadedHomeContent) {
                        loadHomeContent();
                        loadedHomeContent = true;
                    }
                } else {
                    Intent intent = new Intent(activity, ErrorActivity.class);
                    startActivity(intent);
                    activity.finish();
                }

            } else if (menuPos == 3) {

                if (new NetworkInst(activity).isNetworkAvailable()) {
                    if (!loadedLiveTvContent) {
                        loadLiveTvContent();
                        loadedLiveTvContent = true;
                    }
                } else {
                    Intent intent = new Intent(activity, ErrorActivity.class);
                    startActivity(intent);
                    activity.finish();
                }

            }

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }


    private void loadChannelRows(List<LiveTvCategory> liveTvCategories) {

        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        liveTvCardPresenter = new LiveTvCardPresenter();

        for (int i = 0; i < liveTvCategories.size(); i++) {
            ArrayObjectAdapter listRowAdapter;
            HeaderItem header;

            listRowAdapter = new ArrayObjectAdapter(liveTvCardPresenter);
            header = new HeaderItem(i, liveTvCategories.get(i).getTitle());

            for (Channel channel : liveTvCategories.get(i).getChannels()) {
                listRowAdapter.add(channel);
            }

            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        setAdapter(rowsAdapter);

        //setCustomPadding
        //Objects.requireNonNull(getView()).setPadding(Utils.dpToPx(-24, Objects.requireNonNull(getActivity())), Utils.dpToPx(100, getActivity()), 0, 0);
    }


    private void loadRows(List<HomeContent> homeContents) {
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        cardPresenter = new CardPresenter();
        SliderCardPresenter sliderCardPresenter = new SliderCardPresenter();
        TvPresenter tvPresenter = new TvPresenter();


        int i;
        for (i = 0; i < homeContents.size(); i++) {
            ArrayObjectAdapter listRowAdapter;
            HeaderItem header;
            if (i == 0) {
                // load slider
                listRowAdapter = new ArrayObjectAdapter(sliderCardPresenter);
                header = new HeaderItem(i, "");
            } else if (i == 1) {

                //load tv layout
                listRowAdapter = new ArrayObjectAdapter(tvPresenter);
                header = new HeaderItem(i, homeContents.get(i).getTitle());

            } else if (i == 2) {
                //radio content
                listRowAdapter = new ArrayObjectAdapter(tvPresenter);
                header = new HeaderItem(i, homeContents.get(i).getTitle());
            } else {
                listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                header = new HeaderItem(i, homeContents.get(i).getTitle());
            }
            //for (int j = 0; j < NUM_COLS; j++) {
            for (int j = 0; j < homeContents.get(i).getContent().size(); j++) {

                VideoContent videoContent = homeContents.get(i).getContent().get(j);
                if (videoContent.getIsTvseries() != null && videoContent.getIsTvseries().equals("0")) {
                    videoContent.setType("movie");
                } else if (videoContent.getIsTvseries() != null && videoContent.getIsTvseries().equals("1")) {
                    videoContent.setType("tvseries");
                } else {
                    videoContent.setType(homeContents.get(i).getType());
                }

                listRowAdapter.add(videoContent);
            }
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        setAdapter(rowsAdapter);

        setCustomPadding();


    }


    public void refresh() {
        if (menuPos == 3) {
            getView().setPadding(Utils.dpToPx(-24, getContext()), Utils.dpToPx(100, getContext()), 0, 0);
        } else {
            getView().setPadding(Utils.dpToPx(-24, getContext()), Utils.dpToPx(70, getContext()), 0, 0);
        }
    }

    private void setCustomPadding() {
        getView().setPadding(Utils.dpToPx(-24, getContext()), Utils.dpToPx(70, getContext()), 0, 0);
    }


    // click listener
    private OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o,
                                      RowPresenter.ViewHolder viewHolder2, Row row) {


                if (menuPos == 0) {
                    VideoContent videoContent = (VideoContent) o;
                    Log.d("CustomRow", videoContent.getType());

                    if (videoContent.getType().equals("tv")) {

                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra("id", videoContent.getId());
                        intent.putExtra("videoType", videoContent.getStreamFrom());
                        intent.putExtra("category", "tv");
                        intent.putExtra("streamUrl", videoContent.getStreamUrl());
                        startActivity(intent);

                    } else {
                        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                        intent.putExtra("id", videoContent.getId());
                        intent.putExtra("type", videoContent.getType());
                        intent.putExtra("thumbImage", videoContent.getThumbnailUrl());

                        startActivity(intent);
                    }


                } else if (menuPos == 3) {
                    String status = new DatabaseHelper(getContext()).getActiveStatusData().getStatus();
                    Channel channel = (Channel) o;
                    if (channel.getIsPaid().equals("1")) {

                        if (PreferenceUtils.isValid(getContext())) {
                            if (status.equals("active")) {
                                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                                intent.putExtra("id", channel.getLiveTvId());
                                intent.putExtra("videoType", channel.getStreamFrom());
                                intent.putExtra("category", "tv");
                                intent.putExtra("streamUrl", channel.getStreamUrl());
                                startActivity(intent);
                            } else {
                                //subscription is not active
                                //new PaidDialog(getActivity()).showPaidContentAlertDialog();
                                PaidDialog dialog = new PaidDialog(getContext());
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                                dialog.show();
                            }
                        } else {
                            PreferenceUtils.updateSubscriptionStatus(getActivity());
                        }

                    } else {
                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra("id", channel.getLiveTvId());
                        intent.putExtra("videoType", channel.getStreamFrom());
                        intent.putExtra("category", "tv");
                        intent.putExtra("streamUrl", channel.getStreamUrl());
                        startActivity(intent);
                    }

                }

            }
        };
    }


    //listener for setting blur background each time when the item will select.
    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, final Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {

                if (item instanceof VideoContent) {
                    bgHelper = new BackgroundHelper(getActivity());
                    bgHelper.prepareBackgroundManager();
                    bgHelper.startBackgroundTimer(((VideoContent) item).getPosterUrl());
                } else if (item instanceof Channel) {

                    bgHelper = new BackgroundHelper(getActivity());
                    bgHelper.prepareBackgroundManager();
                    bgHelper.startBackgroundTimer(((Channel) item).getPosterUrl());

                }

            }
        };
    }

    public void loadHomeContent() {

        final SpinnerFragment mSpinnerFragment = new SpinnerFragment();
        final FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.custom_frame_layout, mSpinnerFragment).commit();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        HomeApi api = retrofit.create(HomeApi.class);
        Call<List<HomeContent>> call = api.getHomeContent(Config.API_KEY);
        call.enqueue(new Callback<List<HomeContent>>() {
            @Override
            public void onResponse(Call<List<HomeContent>> call, Response<List<HomeContent>> response) {

                if (response.isSuccessful()) {
                    List<HomeContent> homeContents = response.body();
                    //Log.d("size:", homeContents.size()+"");

                    if (homeContents.size() > 0) {
                        loadRows(homeContents);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getContext(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }

                // hide the spinner
                fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();

            }

            @Override
            public void onFailure(Call<List<HomeContent>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the spinner
                getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
            }
        });


    }

    private void loadLiveTvContent() {

        final SpinnerFragment mSpinnerFragment = new SpinnerFragment();
        final FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(R.id.custom_frame_layout, mSpinnerFragment).commit();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LiveTvApi api = retrofit.create(LiveTvApi.class);
        api.getLiveTvCategories(Config.API_KEY).enqueue(new Callback<List<LiveTvCategory>>() {
            @Override
            public void onResponse(Call<List<LiveTvCategory>> call, Response<List<LiveTvCategory>> response) {

                List<LiveTvCategory> liveTvCategories = response.body();
                if (liveTvCategories.size() > 0) {
                    loadChannelRows(liveTvCategories);
                } else {
                    Toast.makeText(activity, getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                }

                // hide the spinner
                fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();
            }

            @Override
            public void onFailure(Call<List<LiveTvCategory>> call, Throwable t) {
                t.printStackTrace();
                // hide the spinner
                Toast.makeText(activity, t.getMessage(), Toast.LENGTH_SHORT).show();
                fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();
            }
        });

    }

}
