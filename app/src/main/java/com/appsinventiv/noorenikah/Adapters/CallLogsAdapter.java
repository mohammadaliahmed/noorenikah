package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.MainActivity;
import com.appsinventiv.noorenikah.Models.CallModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallLogsAdapter extends RecyclerView.Adapter<CallLogsAdapter.ViewHolder> {
    Context context;
    List<CallModel> itemList;
    CallLogsCallback callback;

    public CallLogsAdapter(Context context, List<CallModel> itemList, CallLogsCallback callback) {
        this.context = context;
        this.callback = callback;
        this.itemList = itemList;
    }

    public void setItemList(List<CallModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_item_layout, parent, false);
        CallLogsAdapter.ViewHolder viewHolder = new CallLogsAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallModel callModel = itemList.get(position);
        Glide.with(context)
                .load(callModel.getPic())
                .placeholder(R.drawable.picked)
                .into(holder.image);
        holder.name.setText(callModel.getName());
        holder.time.setText(CommonUtils.getFormattedDateOnlyy(callModel.getStartTime()));
        holder.seconds.setText(CommonUtils.getDuration(callModel.getSeconds()));

        if (callModel.getCallType() != null) {
            if (callModel.getCallType().equalsIgnoreCase(Constants.CALL_INCOMING)) {
                Glide.with(context).load(R.drawable.ic_incoming_call).into(holder.incomingOutgoing);
            } else {
                Glide.with(context).load(R.drawable.ic_outgoing_call).into(holder.incomingOutgoing);
            }

            if (callModel.isVideo()) {
                Glide.with(context).load(R.drawable.ic_video_call).into(holder.callType);
            } else {
                Glide.with(context).load(R.drawable.ic_audio_call).into(holder.callType);
            }
        } else {
            holder.callType.setVisibility(View.GONE);
        }
        holder.callType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.DialCall(callModel.getPhone(), callModel.isVideo());
            }
        });

        if (MainActivity.canCall) {
            holder.callType.setVisibility(View.VISIBLE);
        } else {
            holder.callType.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, seconds, time;
        CircleImageView image;
        ImageView incomingOutgoing, callType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            seconds = itemView.findViewById(R.id.seconds);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            time = itemView.findViewById(R.id.time);
            incomingOutgoing = itemView.findViewById(R.id.incomingOutgoing);
            callType = itemView.findViewById(R.id.callType);

        }
    }

    public interface CallLogsCallback {
        public void DialCall(String phone, boolean video);
    }

}
