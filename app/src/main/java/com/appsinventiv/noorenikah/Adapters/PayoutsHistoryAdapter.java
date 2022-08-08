package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.appsinventiv.noorenikah.Models.RequestPayoutModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

public class PayoutsHistoryAdapter extends RecyclerView.Adapter<PayoutsHistoryAdapter.ViewHolder> {
    Context context;
    List<RequestPayoutModel> itemList;

    public PayoutsHistoryAdapter(Context context, List<RequestPayoutModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<RequestPayoutModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.payout_history_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestPayoutModel model = itemList.get(position);
        Glide.with(context).load(SharedPrefs.getUser().getLivePicPath()).into(holder.image);
        holder.name.setText("Name: " + model.getName());
        holder.amountRequested.setText("Rs. " + model.getAmount());
        holder.details.setText("Phone: 0" + model.getPhone() + "\nPay Via: " + model.getPayoutOption()
                + "\nRequested Date: " + CommonUtils.getFormattedDate(model.getTime()));
        if (model.isPaid()) {
            holder.status.setText("Paid\nDate: " + CommonUtils.getFormattedDate(model.getPayoutTime()));
            holder.status.setBackgroundResource(R.drawable.approved);
        } else {
            holder.status.setText("Pending");
            holder.status.setBackgroundResource(R.drawable.pending);
        }


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, amountRequested, name, details, status;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            amountRequested = itemView.findViewById(R.id.amountRequested);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            details = itemView.findViewById(R.id.details);

        }
    }


}
