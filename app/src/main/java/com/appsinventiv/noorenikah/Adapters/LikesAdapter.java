package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.EditProfile;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.ViewHolder> {
    Context context;
    List<NewUserModel> itemList;


    public LikesAdapter(Context context, List<NewUserModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<NewUserModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.like_item_layout, parent, false);
        LikesAdapter.ViewHolder viewHolder = new LikesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewUserModel commentModel = itemList.get(position);
        Glide.with(context)
                .load(commentModel.getLivePicPath())
                .placeholder(R.drawable.picked)
                .into(holder.image);
        holder.name.setText(commentModel.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentModel.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                    context.startActivity(new Intent(context, EditProfile.class));
                } else {
                    Intent i = new Intent(context, ViewRequestProfile.class);
                    i.putExtra("phone", commentModel.getPhone());
                    context.startActivity(i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);

        }
    }



}
