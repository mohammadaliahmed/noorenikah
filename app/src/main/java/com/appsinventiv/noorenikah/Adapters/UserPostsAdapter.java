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

import com.appsinventiv.noorenikah.Activities.Posts.ViewPost;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserPostsAdapter extends RecyclerView.Adapter<UserPostsAdapter.ViewHolder> {
    Context context;
    List<PostModel> itemList;

    public UserPostsAdapter(Context context, List<PostModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<PostModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_post_item_layout, parent, false);
        UserPostsAdapter.ViewHolder viewHolder = new UserPostsAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel postModel = itemList.get(position);
        if (postModel.getType().equalsIgnoreCase("text")) {
            holder.text.setVisibility(View.VISIBLE);
            holder.postImage.setVisibility(View.GONE);
            holder.text.setText(postModel.getText());
        } else if (postModel.getType().equalsIgnoreCase("image")) {
            holder.postImage.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.GONE);
            Glide.with(context).load(postModel.getImageUrl()).into(holder.postImage);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewPost.class);
                i.putExtra("postId", postModel.getId());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            text = itemView.findViewById(R.id.text);

        }
    }

}
