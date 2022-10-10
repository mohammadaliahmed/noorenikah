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
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.EditProfile;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.CommentReplyModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

public class CommentsRepliesAdapter extends RecyclerView.Adapter<CommentsRepliesAdapter.ViewHolder> {
    Context context;
    List<CommentReplyModel> itemList;
    CommentsReplyAdapterCallbacks callbacks;


    public CommentsRepliesAdapter(Context context, List<CommentReplyModel> itemList, CommentsReplyAdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.itemList = itemList;
    }
    public void setItemList(List<CommentReplyModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_reply_item_layout, parent, false);
        CommentsRepliesAdapter.ViewHolder viewHolder = new CommentsRepliesAdapter.ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentReplyModel commentModel = itemList.get(position);
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
                    context.startActivity(new Intent(context, EditProfile.class));
                } else {
                    Intent i = new Intent(context, ViewRequestProfile.class);
                    i.putExtra("phone", commentModel.getPhone());
                    context.startActivity(i);
                }
            }
        });
        holder.commentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(commentModel.getPhone().equalsIgnoreCase(SharedPrefs.getUser().getPhone())){
                    callbacks.onShowOptions(commentModel);
                }
                return false;
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
        LinearLayout commentView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            name = itemView.findViewById(R.id.name);
            commentView = itemView.findViewById(R.id.commentView);
            image = itemView.findViewById(R.id.image);

        }
    }

    public interface CommentsReplyAdapterCallbacks{
        public void onShowOptions(CommentReplyModel model);
    }

}
