package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.InviteHistory;
import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Activities.PaymentActivity;
import com.appsinventiv.noorenikah.Activities.PaymentsHistory;
import com.appsinventiv.noorenikah.Activities.ViewFriendProfile;
import com.appsinventiv.noorenikah.Models.NotificationModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    Context context;
    List<NotificationModel> itemList;
    NotificationAdapterCallbacks callbacks;

    public NotificationAdapter(Context context, List<NotificationModel> itemList, NotificationAdapterCallbacks callbacks) {
        this.context = context;
        this.itemList = itemList;
        this.callbacks = callbacks;
    }

    public void setItemList(List<NotificationModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item_layout, parent, false);
        NotificationAdapter.ViewHolder viewHolder = new NotificationAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel item = itemList.get(position);

        Glide.with(context)
                .load(item.getPicUrl())
                .into(holder.image);
        holder.title.setText(item.getTitle());
        holder.msg.setText(item.getMessage());
        holder.time.setText(CommonUtils.getFormattedDate(item.getTime()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getType().equalsIgnoreCase("request")) {
                    Constants.REQUEST_RECEIVED = true;
                    context.startActivity(new Intent(context, MainActivity.class));
                } else if (item.getType().equalsIgnoreCase("accepted")) {
                    Intent i = new Intent(context, ViewFriendProfile.class);
                    i.putExtra("phone", item.getHisId());
                    context.startActivity(i);
                } else if (item.getType().equalsIgnoreCase("payout")) {
                    Intent i = new Intent(context, InviteHistory.class);
                    context.startActivity(i);
                } else if (item.getType().equalsIgnoreCase("payment")) {
                    Intent i = new Intent(context, PaymentsHistory.class);
                    context.startActivity(i);
                }else if (item.getType().equalsIgnoreCase("profile")) {
                    Intent i = new Intent(context, MainActivity.class);
                    context.startActivity(i);
                }
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onDeleteClicked(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView remove;
        TextView title, msg, time;
        CircleImageView image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            remove = itemView.findViewById(R.id.remove);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);

        }
    }

    public interface NotificationAdapterCallbacks {

        public void onDeleteClicked(NotificationModel model);
    }
}
