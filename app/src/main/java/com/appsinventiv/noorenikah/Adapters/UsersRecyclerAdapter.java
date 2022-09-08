package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.PaymentProof;
import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {
    Context context;
    List<User> userList;
    UsersAdapterCallbacks callbacks;
    List<String> requestedList;

    public UsersRecyclerAdapter(Context context, List<User> userList, UsersAdapterCallbacks callbacks) {
        this.context = context;
        this.userList = userList;
        this.callbacks = callbacks;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setRequestedList(List<String> requestedList) {
        this.requestedList = requestedList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false);
        UsersRecyclerAdapter.ViewHolder viewHolder = new UsersRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if (requestedList != null && requestedList.size() > 0 && requestedList.contains(user.getPhone())) {

            holder.requestBtn.setText("Request  Sent!");
            holder.requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_white_outline));
            holder.requestBtn.setEnabled(false);
        } else {
            holder.requestBtn.setText("Send request");
            holder.requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
            holder.requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_bg));
            holder.requestBtn.setEnabled(true);

        }
        holder.requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callbacks.onRequestClicked(user);
                CommonUtils.showToast("Request sent");
                holder.requestBtn.setText("Request Sent!");
                holder.requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
                holder.requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_white_outline));

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedPrefs.getUser().getFriends() != null) {
                    if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
                        Intent i = new Intent(context, ViewFriendProfile.class);
                        i.putExtra("phone", user.getPhone());
                        context.startActivity(i);
                    } else {
                        CommonUtils.showToast("Profile is locked\nPlease send Request");
                    }
                } else {
                    CommonUtils.showToast("Profile is locked\nPlease send Request");
                }
            }
        });
        if (SharedPrefs.getUser().getFriends() != null) {
            if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
                holder.lockedInfo.setVisibility(View.GONE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .into(holder.image);
            } else {
                holder.lockedInfo.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .apply(bitmapTransform(new BlurTransformation(50)))

                        .into(holder.image);
            }
        } else {
            holder.lockedInfo.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getLivePicPath())
                    .apply(bitmapTransform(new BlurTransformation(50)))

                    .into(holder.image);
        }
        holder.name.setText(user.getName() + ", " + user.getAge());
        holder.details.setText("Education: " + user.getEducation() + "\n" + "City: " + user.getCity() + "\nCast: " + user.getCast());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button requestBtn;
        TextView name, details;
        ImageView image;
        LinearLayout lockedInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestBtn = itemView.findViewById(R.id.requestBtn);
            details = itemView.findViewById(R.id.details);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            lockedInfo = itemView.findViewById(R.id.lockedInfo);

        }
    }

    public interface UsersAdapterCallbacks {
        public void onLikeClicked(User user);

        public void onRequestClicked(User user);
    }
}
