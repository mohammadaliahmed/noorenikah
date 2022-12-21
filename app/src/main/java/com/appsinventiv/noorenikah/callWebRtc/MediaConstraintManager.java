package com.appsinventiv.noorenikah.callWebRtc;

import androidx.annotation.NonNull;


import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.call.CallManager;

import org.webrtc.MediaConstraints;

/**
 * Created by JunaidAy on 11/2/2017.
 */

public class MediaConstraintManager {
    private MediaConstraints mVideoConstraint;
    private MediaConstraints mAudioContraint;
    private MediaConstraints mConstraints;

    @NonNull
    public static MediaConstraintManager getInstance() {
        return new MediaConstraintManager();
    }

    public MediaConstraints getmVideoConstraint() {
        return mVideoConstraint;
    }

    public void setmVideoConstraint() {
        //todo when video work will start make find video constraints...
        this.mVideoConstraint = new MediaConstraints();
    }

    public MediaConstraints getmAudioContraint() {
        return mAudioContraint;
    }

    public void setmAudioContraint() {
        mAudioContraint = new MediaConstraints();

        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_LEVEL_CONTROL_CONSTRAINT, "true"));
//        mAudioContraint.mandatory.add(
//                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_CODEC_PARAM_BITRATE, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_NOISE_DETECTION_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_NOISE_REDUCTION_CONSTRAINT, "true"));
        mAudioContraint.mandatory.add(
                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_LEAKY_BUCKET_CONSTRAINT, "true"));
//        mAudioContraint.mandatory.add(
//                new MediaConstraints.KeyValuePair(Constants.Constraints.AUDIO_CODEC_ISAC, "true"));
    }

    public MediaConstraints getmConstraints() {
        return mConstraints;
    }

    public void setmConstraints() {
        this.mConstraints = new MediaConstraints();
        this.mConstraints.mandatory.add(new MediaConstraints.KeyValuePair(Constants.Constraints.OFFER_TO_RECEIVE_AUDIO, "true"));
        this.mConstraints.mandatory.add(new MediaConstraints.KeyValuePair(Constants.Constraints.OFFER_TO_RECEIVE_VIDEO, "true"));
    }

    public MediaConstraints getMediaConstraints(CallManager.CallType callType) {
        MediaConstraints mediaConstraints = new MediaConstraints();
        switch (callType) {
            case INDIVIDUAL_VIDEO:
            case GROUP_VIDEO:
                mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(Constants.Constraints.OFFER_TO_RECEIVE_AUDIO, "true"));
                mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(Constants.Constraints.OFFER_TO_RECEIVE_VIDEO, "true"));
                break;
            case INDIVIDUAL_AUDIO:
            case GROUP_AUDIO:
                mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(Constants.Constraints.OFFER_TO_RECEIVE_AUDIO, "true"));
                break;
            default:
                break;
        }
        return mediaConstraints;
    }
}
