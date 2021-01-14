package com.mydrakortv.beta.ui.presenter;

import android.content.Context;
import android.util.Log;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.model.MovieSingleDetails;


public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {


    private final Context mContext;

    private DetailsDescriptionPresenter() {
        super();
        this.mContext = null;
    }

    public DetailsDescriptionPresenter(Context ctx) {
        super();
        this.mContext = ctx;
    }


    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        MovieSingleDetails video = (MovieSingleDetails) item;

        if (video != null) {
            Log.d("Presenter", String.format("%s, %s, %s", video.getTitle(), video.getThumbnailUrl(), video.getDescription()));
            viewHolder.getTitle().setText(video.getTitle());
            viewHolder.getSubtitle().setBackground(mContext.getResources().getDrawable(R.drawable.rounded_black_transparent));
            viewHolder.getSubtitle().setText(mContext.getString(R.string.rating) + 5);
            viewHolder.getBody().setText(video.getDescription());
        }
    }


}
