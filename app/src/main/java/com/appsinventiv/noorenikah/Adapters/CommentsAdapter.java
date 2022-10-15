package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.EditProfile;
import com.appsinventiv.noorenikah.Activities.ViewMyProfile;
import com.appsinventiv.noorenikah.Activities.ViewUserProfile;
import com.appsinventiv.noorenikah.Models.CommentReplyModel;
import com.appsinventiv.noorenikah.Models.CommentsModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    Context context;
    List<CommentsModel> itemList;
    CommentsAdapterCallbacks callbacks;


    public CommentsAdapter(Context context, List<CommentsModel> itemList, CommentsAdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
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
        CommentsModel commentModel = itemList.get(position);
        Glide.with(context)
                .load(commentModel.getPicUrl())
                .placeholder(R.drawable.picked)
                .into(holder.image);
        holder.name.setText(commentModel.getCommentByName());
        holder.comment.setText(commentModel.getComment());
        holder.time.setText(CommonUtils.getFormattedDate(commentModel.getTime()));
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentModel.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                    context.startActivity(new Intent(context, ViewMyProfile.class));
                } else {
                    Intent i = new Intent(context, ViewUserProfile.class);
                    i.putExtra("phone", commentModel.getPhone());
                    context.startActivity(i);
                }
            }
        });
        holder.commentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (commentModel.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())) {
                    callbacks.onShowOptions(commentModel);
                }
                return false;
            }
        });
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onReplyClicked(commentModel);
            }
        });
        HashMap<String, CommentReplyModel> map = commentModel.getReplies();
        if (map != null && map.size() > 0) {
            holder.recycler.setVisibility(View.VISIBLE);

            List<CommentReplyModel> commentList = new ArrayList<>(map.values());
            Collections.sort(commentList, new Comparator<CommentReplyModel>() {
                @Override
                public int compare(CommentReplyModel listData, CommentReplyModel t1) {
                    Long ob1 = listData.getTime();
                    Long ob2 = t1.getTime();
                    return ob1.compareTo(ob2);

                }
            });
            holder.recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            CommentsRepliesAdapter adapter = new CommentsRepliesAdapter(context,
                    commentList, new CommentsRepliesAdapter.CommentsReplyAdapterCallbacks() {
                @Override
                public void onShowOptions(CommentReplyModel model) {
                    callbacks.onShowReplyOptions(model);
                }
            });
            holder.recycler.setAdapter(adapter);
        }else{
            holder.recycler.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, comment, time, reply;
        ImageView image;
        LinearLayout commentView;
        RecyclerView recycler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reply = itemView.findViewById(R.id.reply);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            name = itemView.findViewById(R.id.name);
            commentView = itemView.findViewById(R.id.commentView);
            recycler = itemView.findViewById(R.id.recycler);
            image = itemView.findViewById(R.id.image);

        }
    }

    public interface CommentsAdapterCallbacks {
        public void onReplyClicked(CommentsModel model);

        public void onShowOptions(CommentsModel model);

        public void onShowReplyOptions(CommentReplyModel model);
    }

}
