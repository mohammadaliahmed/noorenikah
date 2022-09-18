package com.appsinventiv.noorenikah.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appsinventiv.noorenikah.Activities.ViewPictures;
import com.appsinventiv.noorenikah.Models.ChatModel;
import com.appsinventiv.noorenikah.R;
import com.appsinventiv.noorenikah.Utils.CommonUtils;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.SharedPrefs;
import com.bumptech.glide.Glide;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> implements Handler.Callback {
    Context context;
    List<ChatModel> itemList;
    public int RIGHT_CHAT = 1;
    public int LEFT_CHAT = 0;
    private MediaPlayer mediaPlayer;
    private ViewHolder mAudioPlayingHolder;
    private int mPlayingPosition = -1;
    private Handler uiUpdateHandler = new Handler(this);
    private static final int MSG_UPDATE_SEEK_BAR = 1845;

    public ChatAdapter(Context context, List<ChatModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setItemList(List<ChatModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = itemList.get(position);
        if (model.getSenderId() != null) {
            if (model.getSenderId().equals(SharedPrefs.getUser().getPhone())) {
                return RIGHT_CHAT;
            } else {
                return LEFT_CHAT;
            }
        }
        return -1;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (viewType == RIGHT_CHAT) {
            View view = LayoutInflater.from(context).inflate(R.layout.right_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.left_chat_layout, parent, false);
            viewHolder = new ViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatModel model = itemList.get(position);
        Glide.with(context).load(model.getPicUrl()).into(holder.picture);
        holder.message.setText(model.getMessage());
        holder.time.setText(CommonUtils.getFormattedDate(model.getTime()));

        if (model.getType() != null) {
            if (model.getType().equalsIgnoreCase(Constants.MESSAGE_TYPE_IMAGE)) {
                holder.audio.setVisibility(View.GONE);
                holder.message.setVisibility(View.GONE);
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(context).load(model.getImageUrl()).into(holder.image);


            } else if (model.getType().equalsIgnoreCase(Constants.MESSAGE_TYPE_TEXT)) {
                holder.message.setVisibility(View.VISIBLE);
                holder.audio.setVisibility(View.GONE);
                holder.image.setVisibility(View.GONE);
                holder.message.setText(model.getMessage());
            } else if (model.getType().equals(Constants.MESSAGE_TYPE_AUDIO)) {
                holder.audio.setVisibility(View.VISIBLE);
                holder.image.setVisibility(View.GONE);
                holder.message.setVisibility(View.GONE);
                holder.playPause.setVisibility(View.VISIBLE);


                holder.audioTime.setText(CommonUtils.getDuration(model.getMediaTime()));

                if (position == mPlayingPosition) {
                    mAudioPlayingHolder = holder;
                    if (model.getSenderId().equals(SharedPrefs.getUser().getPhone())) {
                        updatePlayingView("right");
                    } else {
                        updatePlayingView("left");
                    }
                } else {
                    if (model.getSenderId().equals(SharedPrefs.getUser().getPhone())) {

                        updateInitialPlayerView(holder, "right");
                    } else {
                        updateInitialPlayerView(holder, "left");

                    }
                }

            } else {
                holder.audio.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                holder.image.setVisibility(View.GONE);
                holder.message.setText(model.getMessage());

            }
        } else {
            holder.audio.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.GONE);
            holder.message.setText(model.getMessage());

        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewPictures.class);
                i.putExtra("url", model.getImageUrl());
                context.startActivity(i);
            }
        });
        holder.playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performPlayButtonClick(model, holder);

            }
        });


    }

    private void updateInitialPlayerView(ViewHolder holder, String leftOrRight) {
        if (holder == mAudioPlayingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.seekBar.setProgress(0);
        if (leftOrRight.equalsIgnoreCase("left")) {

            holder.playPause.setImageResource(R.drawable.play_btn);
        } else {
            holder.playPause.setImageResource(R.drawable.play_btn_red);

        }
    }

    private void updatePlayingView(String leftOrRight) {
        if (mediaPlayer == null || mAudioPlayingHolder == null) return;
        mAudioPlayingHolder.seekBar.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());

        if (mediaPlayer.isPlaying()) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            if (leftOrRight.equalsIgnoreCase("left")) {
                mAudioPlayingHolder.playPause.setImageResource(R.drawable.stop);
            } else {
                mAudioPlayingHolder.playPause.setImageResource(R.drawable.stop_red);

            }

        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            if (leftOrRight.equalsIgnoreCase("left")) {

                mAudioPlayingHolder.playPause.setImageResource(R.drawable.play_btn);
            } else {
                mAudioPlayingHolder.playPause.setImageResource(R.drawable.play_btn_red);

            }
        }
        mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(mediaPlayer.getCurrentPosition()));

    }

    private void startMediaPlayer(ChatModel model) {


        try {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(model.getAudioUrl()));
            } catch (Exception e) {
                e.printStackTrace();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(model.getAudioUrl());
            }
            if (mediaPlayer == null) return;

            if (model.getSenderId().equals(SharedPrefs.getUser().getPhone())) {

                mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer("right"));
            } else {
                mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer("left"));

            }
            if (mAudioPlayingHolder != null)
                mediaPlayer.seekTo(mAudioPlayingHolder.seekBar.getProgress());
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNonPlayingView(ViewHolder holder, String leftOrRight) {
        if (holder == mAudioPlayingHolder) {

            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.seekBar.setProgress(0);
        if (leftOrRight.equalsIgnoreCase("left")) {

            holder.playPause.setImageResource(R.drawable.play_btn);
        } else {
            holder.playPause.setImageResource(R.drawable.play_btn_red);

        }
    }

    private void releaseMediaPlayer(String leftOrRight) {

        if (null != mAudioPlayingHolder) {
            updateNonPlayingView(mAudioPlayingHolder, leftOrRight);
        }
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mPlayingPosition = -1;
    }

//    public void stopPlayer() {
//        if (null != mediaPlayer) {
//            releaseMediaPlayer();
//        }
//    }

    private void performPlayButtonClick(ChatModel recordingItem, ViewHolder myViewHolder) {

        int currentPosition = itemList.indexOf(recordingItem);
        if (currentPosition == mPlayingPosition) {
            // toggle between play/pause of audio
            if (mediaPlayer == null) return;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        } else {
            // start another audio playback
            ChatModel previousPlayObject = mPlayingPosition == -1 ? null : itemList.get(mPlayingPosition);
            mPlayingPosition = currentPosition;
            if (mediaPlayer != null) {
                if (null != mAudioPlayingHolder) {
                    if (previousPlayObject != null) {
                        mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(previousPlayObject.getMediaTime()));
                    }
                    if (recordingItem.getSenderId().equals(SharedPrefs.getUser().getPhone())) {

                        updateNonPlayingView(mAudioPlayingHolder,"right");
                    }else{
                        updateNonPlayingView(mAudioPlayingHolder,"left");

                    }
                }
                mediaPlayer.release();
            }
            mAudioPlayingHolder = myViewHolder;
            startMediaPlayer(recordingItem);
        }
        if (recordingItem.getSenderId().equals(SharedPrefs.getUser().getPhone())) {
            updatePlayingView("right");
        } else {
            updatePlayingView("left");
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_SEEK_BAR: {

                int percentage = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
                mAudioPlayingHolder.seekBar.setProgress(percentage);
                mAudioPlayingHolder.audioTime.setText(CommonUtils.getDuration(mediaPlayer.getCurrentPosition()));
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView message, time, audioTime;
        ImageView picture, image, playPause;
        RelativeLayout audio;
        SeekBar seekBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            image = itemView.findViewById(R.id.image);
            audio = itemView.findViewById(R.id.audio);
            seekBar = itemView.findViewById(R.id.seek);
            playPause = itemView.findViewById(R.id.playPause);
            audioTime = itemView.findViewById(R.id.audioTime);
            time = itemView.findViewById(R.id.time);
            message = itemView.findViewById(R.id.message);

        }
    }

}
