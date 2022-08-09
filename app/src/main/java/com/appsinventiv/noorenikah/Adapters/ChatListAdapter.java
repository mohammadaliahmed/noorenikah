package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.ChatScreen;
import com.appsinventiv.noorenikah.Activities.ViewRequestProfile;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    Context context;
    List<ChatModel> itemList;

    public ChatListAdapter(Context context, List<ChatModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<ChatModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_item_layout, parent, false);
        ChatListAdapter.ViewHolder viewHolder = new ChatListAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel model = itemList.get(position);

        Glide.with(context)
                .load(model.getHisPic())
                .into(holder.picture);
            if (model.isRead()) {
                holder.unreadDot.setVisibility(View.GONE);
                holder.name.setTypeface(null,Typeface.NORMAL);
                holder.message.setTypeface(null, Typeface.NORMAL);
                holder.time.setTypeface(null, Typeface.NORMAL);
            } else {
                holder.unreadDot.setVisibility(View.VISIBLE);
                holder.name.setTypeface(null, Typeface.BOLD);
                holder.message.setTypeface(null, Typeface.BOLD);
                holder.time.setTypeface(null, Typeface.BOLD);
            }

        holder.name.setText(model.getHisName());
        holder.time.setText(CommonUtils.getFormattedDate(model.getTime()));
        holder.message.setText(model.getName() + ": " + model.getMessage());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatScreen.class);
                i.putExtra("phone", model.getHisPhone());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, message, time;
        CircleImageView picture;
        ImageView unreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            unreadDot = itemView.findViewById(R.id.unreadDot);
            picture = itemView.findViewById(R.id.picture);
            name = itemView.findViewById(R.id.name);
        }
    }


}
