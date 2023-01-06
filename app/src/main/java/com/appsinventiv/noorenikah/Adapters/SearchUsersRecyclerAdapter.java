package com.appsinventiv.noorenikah.Adapters;

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

import com.appsinventiv.noorenikah.Activities.ChatScreen;
import com.appsinventiv.noorenikah.Activities.Comments.CommentsActivity;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

public class SearchUsersRecyclerAdapter extends RecyclerView.Adapter<SearchUsersRecyclerAdapter.ViewHolder> {
    Context context;
    List<UserModel> userList;
    UsersAdapterCallbacks callbacks;
    List<String> requestedList;

    public SearchUsersRecyclerAdapter(Context context, List<UserModel> userList,
                                      UsersAdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.userList = userList;
    }


    public void setUserList(List<UserModel> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setData(List<UserModel> userList, List<String> requestedList) {
        this.requestedList = requestedList;
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
        SearchUsersRecyclerAdapter.ViewHolder viewHolder = new SearchUsersRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        callbacks.onShown(user);
//        if (user.isPhoneVerified()) {
//            holder.verified.setVisibility(View.VISIBLE);
//        } else {
//            holder.verified.setVisibility(View.GONE);
//
//        }
        if(user.getMatchMakerId()!=null){
            holder.matchMakerProfile.setVisibility(View.VISIBLE);

        }else{
            holder.matchMakerProfile.setVisibility(View.GONE);
        }

        holder.matchMakerProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.showToast("This profile is created by matchmaker");
            }
        });

        holder.verified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.showToast("This user is verified");
            }
        });

        HashMap<String, String> map = SharedPrefs.getLikedMap();
        if (map != null) {
            if (map.containsKey(user.getPhone())) {
                user.setLiked(true);
            } else {
                user.setLiked(false);
            }
        }
        if (user.isLiked()) {
            holder.likeUnlike.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.likeUnlike.setImageResource(R.drawable.ic_like_empty);

        }
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CommentsActivity.class);
                i.putExtra("id", user.getPhone());
                i.putExtra("fcmKey", user.getFcmKey());
                i.putExtra("post", "true");
                context.startActivity(i);
            }
        });
        holder.share.setOnClickListener(v -> {
            String shareUrl = "http://noorenikah.com/profile?id=" + CommonUtils.getUserShareId(user.getPhone());
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
            context.startActivity(Intent.createChooser(shareIntent, "Share link via.."));
        });
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ChatScreen.class);
                i.putExtra("phone", user.getPhone());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity(i);
            }
        });
        holder.likeUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isLiked()) {
                    HashMap<String, String> map = SharedPrefs.getLikedMap();
                    if (map != null) {
                        if (map.containsKey(user.getPhone())) {
                            map.remove(user.getPhone());
                            user.setLiked(false);
                            SharedPrefs.setLikedMap(map);
                        }
                    }
                    holder.likeUnlike.setImageResource(R.drawable.ic_like_empty);
                } else {
                    holder.likeUnlike.setImageResource(R.drawable.ic_like_filled);
                    HashMap<String, String> map = SharedPrefs.getLikedMap();
                    if (map != null) {
                        map.put(user.getPhone(), user.getPhone());
                        SharedPrefs.setLikedMap(map);
                    } else {
                        map = new HashMap<>();
                        map.put(user.getPhone(), user.getPhone());
                        SharedPrefs.setLikedMap(map);
                    }
                    user.setLiked(true);
                    callbacks.onLikeClicked(user);


                }
            }
        });
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

//        if (SharedPrefs.getUser().getFriends() != null) {
//            if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
//                holder.lockedInfo.setVisibility(View.GONE);
//                Glide.with(context)
//                        .load(user.getLivePicPath())
//                        .placeholder(R.drawable.picked)
//                        .into(holder.image);
//            } else {
//                holder.lockedInfo.setVisibility(View.VISIBLE);
//                Glide.with(context)
//                        .load(user.getLivePicPath())
//                        .apply(bitmapTransform(new BlurTransformation(50)))
//                        .placeholder(R.drawable.picked)
//
//                        .into(holder.image);
//            }
//        } else {
//            holder.lockedInfo.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getLivePicPath())
                    .placeholder(R.drawable.picked)
                    .into(holder.image);
//        }
        String nm=user.getName();
        if(user.isMatchMakerProfile()){
            nm+="(Matchmaker)";
        }
        holder.name.setText(nm);
        holder.details.setText(
                "Sect: " + (user.getSect()==null?"":user.getSect()) +
                "\nCity: " + (user.getCity()==null?"":user.getCity()) +
                "\nEducation: " + (user.getEducation()==null?"":user.getEducation())
                + "\nMarital Status: " + (user.getMaritalStatus()==null?"":user.getMaritalStatus()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button requestBtn;
        TextView name, details;
        ImageView share, comment, chat, verified, image, likeUnlike,matchMakerProfile;
        LinearLayout lockedInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestBtn = itemView.findViewById(R.id.requestBtn);
            name = itemView.findViewById(R.id.name);
            details = itemView.findViewById(R.id.details);
            share = itemView.findViewById(R.id.share);
            comment = itemView.findViewById(R.id.comment);
            chat = itemView.findViewById(R.id.chat);
            verified = itemView.findViewById(R.id.verified);
            image = itemView.findViewById(R.id.image);
            matchMakerProfile = itemView.findViewById(R.id.matchMakerProfile);
            likeUnlike = itemView.findViewById(R.id.likeUnlike);
            lockedInfo = itemView.findViewById(R.id.lockedInfo);

        }
    }

    public interface UsersAdapterCallbacks {
        public void onLikeClicked(UserModel user);

        public void onRequestClicked(UserModel user);

        public void onShown(UserModel user);
    }
}
