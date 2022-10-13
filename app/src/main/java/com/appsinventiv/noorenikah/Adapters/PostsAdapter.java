package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.Posts.PostComments;
import com.appsinventiv.noorenikah.Activities.Posts.PostLikes;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.PostModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    Context context;
    List<PostModel> itemList;
    PostsAdapterCallbacks callbacks;

    public PostsAdapter(Context context, List<PostModel> itemList, PostsAdapterCallbacks callbacks) {
        this.context = context;
        this.itemList = itemList;
        this.callbacks = callbacks;
    }

    public void setItemList(List<PostModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item_layout, parent, false);
        PostsAdapter.ViewHolder viewHolder = new PostsAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel postModel = itemList.get(position);

        if (postModel.getType().equalsIgnoreCase("text")) {
            holder.postImage.setVisibility(View.GONE);
        } else if (postModel.getType().equalsIgnoreCase("image")) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(postModel.getImageUrl()).into(holder.postImage);

        }
        HashMap<String, String> map = SharedPrefs.getPostLikedMap();
        if (map != null) {
            if (map.containsKey(postModel.getId())) {
                postModel.setLiked(true);
            } else {
                postModel.setLiked(false);
            }
        }
        if (postModel.isLiked()) {
            holder.likeUnlike.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.likeUnlike.setImageResource(R.drawable.ic_like_empty);

        }
        holder.likeUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postModel.isLiked()) {
                    HashMap<String, String> map = SharedPrefs.getPostLikedMap();
                    if (map != null) {
                        if (map.containsKey(postModel.getId())) {
                            map.remove(postModel.getId());
                            postModel.setLiked(false);
                            SharedPrefs.setPostLikedMap(map);
                        }
                    }
                    holder.likeUnlike.setImageResource(R.drawable.ic_like_empty);
                    postModel.setLikeCount(postModel.getLikeCount() - 1);
                    holder.likesCount.setText((postModel.getLikeCount()) + " likes");

                    callbacks.onLiked(postModel, false);

                } else {
                    holder.likeUnlike.setImageResource(R.drawable.ic_like_filled);
                    HashMap<String, String> map = SharedPrefs.getPostLikedMap();
                    if (map != null) {
                        map.put(postModel.getId(), postModel.getId());
                    } else {
                        map = new HashMap<>();
                        map.put(postModel.getId(), postModel.getId());
                    }
                    SharedPrefs.setPostLikedMap(map);
                    postModel.setLiked(true);
                    postModel.setLikeCount(postModel.getLikeCount() + 1);

                    holder.likesCount.setText((postModel.getLikeCount()) + " likes");
                    callbacks.onLiked(postModel, true);
                }
            }
        });

        Glide.with(context).load(postModel.getUserPicUrl()).into(holder.userImage);
        holder.name.setText(postModel.getUserName());
        holder.text.setText(postModel.getText());
        holder.commentCount.setText(postModel.getCommentCount() + " comments");
        holder.likesCount.setText(postModel.getLikeCount() + " likes");
        holder.time.setText(CommonUtils.getFormattedDate(postModel.getTime()));
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostComments.class);
                i.putExtra("postId", postModel.getId());
                context.startActivity(i);
            }
        });
        holder.commentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostComments.class);
                i.putExtra("postId", postModel.getId());
                context.startActivity(i);
            }
        });
        holder.likesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostLikes.class);
                i.putExtra("postId", postModel.getId());
                context.startActivity(i);
            }
        });
        holder.userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewRequestProfile.class);
                i.putExtra("phone", postModel.getUserId());
                context.startActivity(i);
            }
        });


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, text, commentCount, likesCount, time;
        ImageView postImage, likeUnlike, comment, share;
        CircleImageView userImage;
        RelativeLayout userView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            name = itemView.findViewById(R.id.name);
            postImage = itemView.findViewById(R.id.postImage);
            likeUnlike = itemView.findViewById(R.id.likeUnlike);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            likesCount = itemView.findViewById(R.id.likesCount);
            time = itemView.findViewById(R.id.time);
            commentCount = itemView.findViewById(R.id.commentCount);
            userView = itemView.findViewById(R.id.userView);
            userImage = itemView.findViewById(R.id.userImage);

        }
    }

    public interface PostsAdapterCallbacks {
        public void onLiked(PostModel model, boolean liked);

        public void onComment(PostModel model);

        public void onShare(PostModel model);
    }

}
