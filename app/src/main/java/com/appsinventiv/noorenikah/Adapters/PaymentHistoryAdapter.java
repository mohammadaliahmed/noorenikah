package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Models.PaymentsModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.bumptech.glide.Glide;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder> {
    Context context;
    List<PaymentsModel> itemList;

    public PaymentHistoryAdapter(Context context, List<PaymentsModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<PaymentsModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.payment_history_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentsModel model = itemList.get(position);
        Glide.with(context).load(model.getPicUrl()).into(holder.image);
        if (model.isRejected()) {
            holder.paymentStatus.setText("Rejected");
            holder.paymentStatus.setBackgroundResource(R.drawable.rejected);
        } else {
            if (model.isApproved()) {
                holder.paymentStatus.setText("Approved");
                holder.paymentStatus.setBackgroundResource(R.drawable.approved);

            } else {
                holder.paymentStatus.setText("Pending");
                holder.paymentStatus.setBackgroundResource(R.drawable.pending);
            }
        }
        holder.date.setText("Date: "+CommonUtils.getFormattedDateOnly(model.getTime()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, paymentStatus;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            paymentStatus = itemView.findViewById(R.id.paymentStatus);
            image = itemView.findViewById(R.id.image);

        }
    }

}
