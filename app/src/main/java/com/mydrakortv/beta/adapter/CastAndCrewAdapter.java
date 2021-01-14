package com.mydrakortv.beta.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.model.CastAndCrew;
import com.mydrakortv.beta.utils.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CastAndCrewAdapter extends RecyclerView.Adapter<CastAndCrewAdapter.ViewHolder> {

    private Context context;
    private List<CastAndCrew> castCrewList;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public CastAndCrewAdapter(Context context, List<CastAndCrew> castCrewList) {
        this.context = context;
        this.castCrewList = castCrewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.layout_tv_cast_crew, parent,
                false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CastAndCrew castCrew = castCrewList.get(position);
        if (castCrew != null) {

            holder.castNameTv.setText(castCrew.getName());
            Picasso.get().load(castCrew.getImageUrl()).into(holder.castIv);

        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return castCrewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView castIv;
        TextView castNameTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            castIv = itemView.findViewById(R.id.cast_iv);
            castNameTv = itemView.findViewById(R.id.crew_name_tv);

        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }

        });


        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }
}

