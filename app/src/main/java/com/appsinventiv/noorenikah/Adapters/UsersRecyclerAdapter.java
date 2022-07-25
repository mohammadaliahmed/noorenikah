package com.appsinventiv.noorenikah.Adapters;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.bumptech.glide.Glide;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder> {
    Context context;
    List<User> userList;
    UsersAdapterCallbacks callbacks;

    public UsersRecyclerAdapter(Context context, List<User> userList, UsersAdapterCallbacks callbacks) {
        this.context = context;
        this.userList = userList;
        this.callbacks = callbacks;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
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
        Glide.with(context)
                .load(user.getLivePicPath())
                .apply(bitmapTransform(new BlurTransformation(50)))

                .into(holder.image);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestBtn = itemView.findViewById(R.id.requestBtn);
            details = itemView.findViewById(R.id.details);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);

        }
    }

    public interface UsersAdapterCallbacks {
        public void onLikeClicked(User user);

        public void onRequestClicked(User user);
    }
}
