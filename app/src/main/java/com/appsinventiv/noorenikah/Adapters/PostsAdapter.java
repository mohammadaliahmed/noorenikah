package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.Posts.PostComments;
import com.appsinventiv.noorenikah.Activities.Posts.PostLikes;
import com.appsinventiv.noorenikah.Activities.ViewUserProfile;
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
        if (postModel.getUserId().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
            holder.removePost.setVisibility(View.VISIBLE);
            holder.postMenu.setVisibility(View.GONE);
        } else {
            holder.removePost.setVisibility(View.GONE);
            holder.postMenu.setVisibility(View.VISIBLE);

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
        holder.postMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.postMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.options_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_report:
                                //handle menu1 click
                                callbacks.onReportPost(postModel, position);
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }
        });
        holder.userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewUserProfile.class);
                i.putExtra("phone", postModel.getUserId());
                context.startActivity(i);
            }
        });
        holder.removePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onRemovePost(postModel);
            }
        });


        if (postModel.getText().length() > 100) {

        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, text, commentCount, likesCount, time;
        ImageView postImage, likeUnlike, comment, share, removePost, postMenu;
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
            postMenu = itemView.findViewById(R.id.postMenu);
            removePost = itemView.findViewById(R.id.removePost);

        }
    }

    public interface PostsAdapterCallbacks {
        public void onLiked(PostModel model, boolean liked);

        public void onComment(PostModel model);

        public void onShare(PostModel model);

        public void onRemovePost(PostModel model);

        public void onReportPost(PostModel model, int position);
    }

}
