package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.appsinventiv.noorenikah.Activities.ChatScreen;
import com.appsinventiv.noorenikah.Activities.Comments.CommentsActivity;
import com.appsinventiv.noorenikah.Models.NewUserModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ViewPagerAdapter extends PagerAdapter {
    LayoutInflater mLayoutInflater;
    Context context;
    List<NewUserModel> userList;
    UsersAdapterCallbacks callbacks;
    List<String> requestedList;


    public ViewPagerAdapter(Context context, List<NewUserModel> userList, UsersAdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.userList = userList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setUserList(List<NewUserModel> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setData(List<NewUserModel> userList, List<String> requestedList) {
        this.requestedList = requestedList;
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setRequestedList(List<String> requestedList) {
        this.requestedList = requestedList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.user_item_layout, container, false);
        Button requestBtn = itemView.findViewById(R.id.requestBtn);
        TextView name = itemView.findViewById(R.id.name);
        TextView details = itemView.findViewById(R.id.details);
        ImageView share = itemView.findViewById(R.id.share);
        ImageView comment = itemView.findViewById(R.id.comment);
        ImageView chat = itemView.findViewById(R.id.chat);
        ImageView verified = itemView.findViewById(R.id.verified);
        ImageView image = itemView.findViewById(R.id.image);
        ImageView likeUnlike = itemView.findViewById(R.id.likeUnlike);
        LinearLayout lockedInfo = itemView.findViewById(R.id.lockedInfo);
        NewUserModel user = userList.get(position);
        callbacks.onShown(user);


        if (user.isPhoneVerified()) {
            verified.setVisibility(View.VISIBLE);
        } else {
            verified.setVisibility(View.GONE);

        }
        verified.setOnClickListener(new View.OnClickListener() {
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
            likeUnlike.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeUnlike.setImageResource(R.drawable.ic_like_empty);

        }
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CommentsActivity.class);
                i.putExtra("id", user.getPhone());
                i.putExtra("fcmKey", user.getFcmKey());
                context.startActivity(i);
            }
        });
        share.setOnClickListener(v -> {
            String shareUrl = "http://noorenikah.com/profile?id=" + CommonUtils.getUserShareId(user.getPhone());
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
            context.startActivity(Intent.createChooser(shareIntent, "Share link via.."));
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ChatScreen.class);
                i.putExtra("phone", user.getPhone());
                context.startActivity(i);
            }
        });
        likeUnlike.setOnClickListener(new View.OnClickListener() {
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
                    likeUnlike.setImageResource(R.drawable.ic_like_empty);
                } else {
                    likeUnlike.setImageResource(R.drawable.ic_like_filled);
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

            requestBtn.setText("Request  Sent!");
            requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
            requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_white_outline));
            requestBtn.setEnabled(false);
        } else {
            requestBtn.setText("Send request");
            requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
            requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_bg));
            requestBtn.setEnabled(true);

        }
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callbacks.onRequestClicked(user);
                CommonUtils.showToast("Request sent");
                requestBtn.setText("Request Sent!");
                requestBtn.setTextColor(context.getResources().getColor(R.color.colorWhite));
                requestBtn.setBackground(context.getResources().getDrawable(R.drawable.btn_white_outline));

            }
        });

        if (SharedPrefs.getUser().getFriends() != null) {
            if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
                lockedInfo.setVisibility(View.GONE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .placeholder(R.drawable.picked)
                        .into(image);
            } else {
                lockedInfo.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .apply(bitmapTransform(new BlurTransformation(50)))
                        .placeholder(R.drawable.picked)

                        .into(image);
            }
        } else {
            lockedInfo.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getLivePicPath())
                    .apply(bitmapTransform(new BlurTransformation(50)))
                    .placeholder(R.drawable.picked)
                    .into(image);
        }
        name.setText(user.getName());
        details.setText(user.getDetails());
//        details.setText("Education: " + user.getEducation() +
//                "\nMarital Status: " + user.getMaritalStatus() +
//                "\nCast: " + user.getCast() +
//                "\n" + "City: " + user.getCity());

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }

    public interface UsersAdapterCallbacks {
        public void onLikeClicked(NewUserModel user);

        public void onRequestClicked(NewUserModel user);

        public void onShown(NewUserModel user);
    }
}
