package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
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

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ViewPagerAdapter extends PagerAdapter {
    LayoutInflater mLayoutInflater;
    Context context;
    List<User> userList;
    UsersAdapterCallbacks callbacks;
    List<String> requestedList;


    public ViewPagerAdapter(Context context, List<User> userList, UsersAdapterCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.userList = userList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setUserList(List<User> userList) {
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
        ImageView image = itemView.findViewById(R.id.image);
        LinearLayout lockedInfo = itemView.findViewById(R.id.lockedInfo);
        User user = userList.get(position);
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
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (SharedPrefs.getUser().getFriends() != null) {
//                    if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
//                        Intent i = new Intent(context, ViewFriendProfile.class);
//                        i.putExtra("phone", user.getPhone());
//                        context.startActivity(i);
//                    } else {
//                        CommonUtils.showToast("Profile is locked\nPlease send Request");
//                    }
//                } else {
//                    CommonUtils.showToast("Profile is locked\nPlease send Request");
//                }
//            }
//        });
        if (SharedPrefs.getUser().getFriends() != null) {
            if (SharedPrefs.getUser().getFriends().containsKey(user.getPhone())) {
                lockedInfo.setVisibility(View.GONE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .into(image);
            } else {
                lockedInfo.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(user.getLivePicPath())
                        .apply(bitmapTransform(new BlurTransformation(50)))

                        .into(image);
            }
        } else {
            lockedInfo.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(user.getLivePicPath())
                    .apply(bitmapTransform(new BlurTransformation(50)))

                    .into(image);
        }
        name.setText(user.getName() );
//        details.setText("Education: " + user.getEducation() + "\n" + "City: " + user.getCity() + "\nCast: " + user.getCast());

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }

    public interface UsersAdapterCallbacks {
        public void onLikeClicked(User user);

        public void onRequestClicked(User user);
    }
}
