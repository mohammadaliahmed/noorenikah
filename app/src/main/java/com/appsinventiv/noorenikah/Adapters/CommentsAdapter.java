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
import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.CommentsModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    Context context;
    List<CommentsModel> itemList;

    public CommentsAdapter(Context context, List<CommentsModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }
    public void setItemList(List<CommentsModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item_layout, parent, false);
        CommentsAdapter.ViewHolder viewHolder = new CommentsAdapter.ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentsModel user = itemList.get(position);
        Glide.with(context)
                .load(user.getPicUrl())
                .placeholder(R.drawable.picked)
                .into(holder.image);
        holder.name.setText(user.getCommentByName());
        holder.comment.setText(user.getComment());
        holder.time.setText(CommonUtils.getFormattedDate(user.getTime()));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                    context.startActivity(new Intent(context, EditProfile.class));
                } else {
                    Intent i = new Intent(context, ViewRequestProfile.class);
                    i.putExtra("phone", user.getPhone());
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
        TextView name, comment, time;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);

        }
    }

}
