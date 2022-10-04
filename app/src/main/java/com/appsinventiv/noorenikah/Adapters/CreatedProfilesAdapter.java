package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.R;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatedProfilesAdapter extends RecyclerView.Adapter<CreatedProfilesAdapter.ViewHolder> {
    Context context;
    List<NewUserModel> itemList;

    public CreatedProfilesAdapter(Context context, List<NewUserModel> itemList) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.profile_item_layout, parent, false);
        CreatedProfilesAdapter.ViewHolder viewHolder = new CreatedProfilesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewUserModel item = itemList.get(position);

        Glide.with(context)
                .load(item.getLivePicPath())
                .placeholder(R.drawable.picked)
                .into(holder.image);
        holder.name.setText(item.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context, ViewFriendProfile.class);
                i.putExtra("phone",item.getPhone());
                context.startActivity(i);
            }
        });



    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CircleImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);

            image = itemView.findViewById(R.id.image);

        }
    }

}
