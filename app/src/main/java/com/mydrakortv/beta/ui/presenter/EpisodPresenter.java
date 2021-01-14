package com.mydrakortv.beta.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import com.mydrakortv.beta.Config;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.database.DatabaseHelper;
import com.mydrakortv.beta.model.Episode;
import com.mydrakortv.beta.model.Video;
import com.mydrakortv.beta.network.RetrofitClient;
import com.mydrakortv.beta.network.api.GetParserAPI;
import com.mydrakortv.beta.model.MovieParser;
import com.mydrakortv.beta.ui.activity.PlayerActivity;
import com.mydrakortv.beta.ui.activity.WebviewActivity;
import com.mydrakortv.beta.utils.PaidDialog;
import com.mydrakortv.beta.utils.PreferenceUtils;
import com.mydrakortv.beta.utils.ToastMsg;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EpisodPresenter extends Presenter {
    private static int CARD_WIDTH = 330;
    private static int CARD_HEIGHT = 180;
    private static Context mContext;
    private String parser = "";
    List<Video> videoList = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
//        Log.d("onCreateViewHolder", "creating viewholder");
        mContext = parent.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        cardView.requestLayout();
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final Episode video = (Episode) item;
        ((ViewHolder) viewHolder).mCardView.setTitleText(video.getEpisodesName());
        ((ViewHolder) viewHolder).mCardView.findViewById(R.id.content_text).setVisibility(View.GONE);
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        ((ViewHolder) viewHolder).updateCardViewImage(video.getImageUrl());
        final DatabaseHelper db = new DatabaseHelper(mContext);

        ((ViewHolder) viewHolder).mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.e("onclick", "onClick: "+video.getEpisodesName());
                String body = video.getEpisodesName();
                String body1 = video.getEpisodesName()+";";
                String box = body.substring(body.indexOf(",")+2,body1.indexOf(";"));
//                Log.e("TAG", "onClick: "+box);
                if (video.getIsPaid().equals("1")) {
                    if (db.getActiveStatusData().getStatus().equalsIgnoreCase("active")) {
                        if (PreferenceUtils.isValid(mContext)) {
                            if (box.equalsIgnoreCase("Lotus Box") || box.equalsIgnoreCase("Poppy Box")){
                                Intent playerIntent = new Intent(mContext, PlayerActivity.class);
                                playerIntent.putExtra("id", video.getEpisodesId());
                                playerIntent.putExtra("videoType", video.getFileType());
                                if (video.getFileType().equalsIgnoreCase("embed")){
                                    new ToastMsg(mContext).toastIconError(mContext.getResources().getString(R.string.embed_not_supported));
                                    return;
                                }
                                playerIntent.putExtra("category", "tvseries");
                                playerIntent.putExtra("streamUrl", video.getFileUrl());
                                Video videoModel = new Video();
                                videoModel.setSubtitle(video.getSubtitle());
                                playerIntent.putExtra("video", videoModel); //including subtitle list
                                mContext.startActivity(playerIntent);
                            }else{
                                URL url = null;
                                try {
                                    url = new URL(video.getFileUrl());
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                String host = url.getHost();
                                if (host.matches(".*\\d.*")){
                                    host = host.replace(".","_");
                                }else{
                                    String[] host1 = host.split(Pattern.quote("."));
                                    host = host1[0];
                                }
                                getParser(Config.API_KEY_P, Config.SESSION_ID, host,video);
                            }
                        } else {
                            //saved data is not valid, because it was saved more than 2 hours ago
                            PreferenceUtils.updateSubscriptionStatus(mContext);
                        }
                    }else {
                        //subscription is not active
                        //new PaidDialog(getActivity()).showPaidContentAlertDialog();
                        PaidDialog dialog = new PaidDialog(mContext);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                        dialog.show();
                    }
                } else {
                    if (box.equalsIgnoreCase("Lotus Box") || box.equalsIgnoreCase("Poppy Box")){
                        Intent playerIntent = new Intent(mContext, PlayerActivity.class);
                        playerIntent.putExtra("id", video.getEpisodesId());
                        playerIntent.putExtra("videoType", video.getFileType());
                        if (video.getFileType().equalsIgnoreCase("embed")){
                            new ToastMsg(mContext).toastIconError(mContext.getResources().getString(R.string.embed_not_supported));
                            return;
                        }
                        playerIntent.putExtra("category", "tvseries");
                        playerIntent.putExtra("streamUrl", video.getFileUrl());
                        Video videoModel = new Video();
                        videoModel.setSubtitle(video.getSubtitle());
                        playerIntent.putExtra("video", videoModel); //including subtitle list
                        mContext.startActivity(playerIntent);
                    }else{
                        URL url = null;
                        try {
                            url = new URL(video.getFileUrl());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        String host = url.getHost();
                        if (host.matches(".*\\d.*")){
                            host = host.replace(".","_");
                        }else{
                            String[] host1 = host.split(Pattern.quote("."));
                            host = host1[0];
                        }
                        getParser(Config.API_KEY_P, Config.SESSION_ID, host,video);
                    }
                }
            }
        });

    }
//    getParser(Config.API_KEY_P, Config.SESSION_ID, obj.getLabel());
    private void getParser(String api_key, final String session_id, String site , Episode obj) {
//        Log.e("TAG", "getParser: 1");
        Retrofit retrofit = RetrofitClient.getParserRetrofitInstance();
        GetParserAPI api = retrofit.create(GetParserAPI.class);
        Call<MovieParser> call = api.getParser(api_key, session_id, site);

        call.enqueue(new Callback<MovieParser>() {
            @Override
            public void onResponse(Call<MovieParser> call, Response<MovieParser> response) {
                if (response.code() == 200) {
                    MovieParser singleDetails = new MovieParser();
                    singleDetails = response.body();

                    parser = singleDetails.getScript();
                    Intent webviewIntent = new Intent(mContext, WebviewActivity.class);
                    webviewIntent.putExtra("parser", parser);
                    webviewIntent.putExtra("streamUrl", obj.getFileUrl());
                    if (obj.getFileType().equalsIgnoreCase("embed")){
                        new ToastMsg(mContext).toastIconError(mContext.getResources().getString(R.string.embed_not_supported));
                        return;
                    }
                    ArrayList<Video> videoListForIntent = new ArrayList<>(videoList);
                    webviewIntent.putExtra("videos", videoListForIntent);
                    webviewIntent.putExtra("video", obj);
                    webviewIntent.putExtra("category", "tvseries");
                    mContext.startActivity(webviewIntent);

                } else {
//                    fm.beginTransaction().remove(spinnerFragment).commitAllowingStateLoss();
                }
            }

            @Override
            public void onFailure(Call<MovieParser> call, Throwable t) {
//                fm.beginTransaction().remove(spinnerFragment).commitAllowingStateLoss();
                Log.e("TAG", "onFailure: ", t);
            }

        });

    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }


    class ViewHolder extends Presenter.ViewHolder {

        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext
                    .getResources()
                    .getDrawable(R.drawable.logo);
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(String url) {

            Picasso.get()
                    .load(url)
                    .resize(CARD_WIDTH * 2, CARD_HEIGHT * 2)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);

        }
    }


    class PicassoImageCardViewTarget implements Target {


        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView mImageCardView) {
            this.mImageCardView = mImageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            mImageCardView.setMainImage(errorDrawable);
        }


        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}


