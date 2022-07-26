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
import com.appsinventiv.noorenikah.Models.User;
import com.appsinventiv.noorenikah.R;
import com.bumptech.glide.Glide;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {
    Context context;
    List<User> userList;
    RequestsAdapterCallbacks callbacks;

    public RequestsAdapter(Context context, List<User> userList, RequestsAdapterCallbacks callbacks) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.request_item_layout, parent, false);
        RequestsAdapter.ViewHolder viewHolder = new RequestsAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        Glide.with(context)
                .load(user.getLivePicPath())
                .apply(bitmapTransform(new BlurTransformation(20)))

                .into(holder.image);
        holder.name.setText(user.getName() + ", " + user.getAge());
        holder.details.setText("Education: " + user.getEducation() + "\n" + "City: " + user.getCity() + "\nCast: " + user.getCast());

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onAcceptClicked(user);
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onRejectClicked(user);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(context, ViewRequestProfile.class);
                i.putExtra("user",user);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button reject, accept;
        TextView name, details;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reject = itemView.findViewById(R.id.reject);
            accept = itemView.findViewById(R.id.accept);
            details = itemView.findViewById(R.id.details);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);

        }
    }

    public interface RequestsAdapterCallbacks {
        public void onAcceptClicked(User user);

        public void onRejectClicked(User user);
    }
}
