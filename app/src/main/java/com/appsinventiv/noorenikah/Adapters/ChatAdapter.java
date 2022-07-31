package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    Context context;
    List<ChatModel> itemList;
    public int RIGHT_CHAT = 1;
    public int LEFT_CHAT = 0;
    public ChatAdapter(Context context, List<ChatModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<ChatModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        ChatModel model = itemList.get(position);
        if (model.getSenderId() != null) {
            if (model.getSenderId().equals(SharedPrefs.getUser().getPhone())) {
                return RIGHT_CHAT;
            } else {
                return LEFT_CHAT;
            }
        }
        return -1;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (viewType == RIGHT_CHAT) {
            View view = LayoutInflater.from(context).inflate(R.layout.right_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.left_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       ChatModel model=itemList.get(position);
        Glide.with(context).load(model.getPicUrl()).into(holder.picture);
        holder.message.setText(model.getMessage());
        holder.time.setText(CommonUtils.getFormattedDate(model.getTime()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView message,time;
        ImageView picture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            time = itemView.findViewById(R.id.time);
            message = itemView.findViewById(R.id.message);

        }
    }

}
