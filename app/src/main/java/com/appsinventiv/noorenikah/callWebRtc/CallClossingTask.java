package com.appsinventiv.noorenikah.callWebRtc;


import android.os.AsyncTask;

import com.appsinventiv.noorenikah.callWebRtc.callBroadCast.CallEndType;


/**
 * Created by JunaidAy on 22/01/2018.
 */

public class CallClossingTask {
    private Long mCallId;
    Enum mCallType;
    private Long currentTime;
    private Long duration;
    private int numberofConnection;
    private Long mAttendentId;

    public CallClossingTask(Long callId, CallEndType callType) {
        this.mCallId = callId;
        this.mCallType = callType;
    }

    public CallClossingTask(Long callId, Long attendentId, CallEndType callType) {
        this.mCallId = callId;
        this.mCallType = callType;
        this.mAttendentId = attendentId;
    }

    public CallClossingTask(Long callId, Long currentTime, Long duration, Enum callType) {
        this.mCallId = callId;
        this.mCallType = callType;
        this.currentTime = currentTime;
        this.duration = duration;
    }

    public CallClossingTask(Long callId, Long currentTime, Long duration, int numberOfConnection, Enum callType) {
        this.mCallId = callId;
        this.mCallType = callType;
        this.currentTime = currentTime;
        this.duration = duration;
        this.numberofConnection = numberOfConnection;
    }

    public void startEndCallProcedure() {
        new CallEndingTask().execute();
    }

    private class CallEndingTask extends AsyncTask<Void, Void, Void> {
        @Override

        protected Void doInBackground(Void... params) {
//            if (mCallType.equals(CallEndType.CALLER_ENDED_INDIVIDUAL_CALL)) {
//                callerCancelAudioCall();
//            } else if (mCallType.equals(CallEndType.CALLER_ENDED_INDIVIDUAL_VIDEO_CALL)) {
//                callerCancelVideoCall();
//            } else if (mCallType.equals(CallEndType.END_INDIVIDUAL_CALL)) {
//                finishIndividualConnectedCall();
//            } else if (mCallType.equals(CallEndType.END_INDIVIDUAL_VIDEO_CALL)) {
//                finishIndividualVideoConnectedCall();
//            } else if (mCallType.equals(CallEndType.INDIVIDUAL_MISSED_CALL)) {
//                individualMissedCall();
//            }  else if (mCallType.equals(CallEndType.INDIVIDUAL_MISSED_VIDEO_CALL)) {
//                individualVideoMissedCall();
//            } else if (mCallType.equals(CallEndType.RECEIVER_REJECT_INCOMIING_CALL)) {
//                rejectIncomingIndividualCall();
//            } else if (mCallType.equals(CallEndType.RECEIVER_REJECT_INCOMIING_VIDEO_CALL)) {
//                rejectIncomingIndividualVideoCall();
//            } else if (mCallType.equals(CallEndType.CALLER_ENDED_GROUP_CALL)) {
//                callerCancelGroupAudioCall();
//            } else if (mCallType.equals(CallEndType.END_GROUP_CALL)) {
//                finishConnectedGroupCall();
//            } else if (mCallType.equals(CallEndType.GROUP_MISS_CALL)) {
//                groupMissCall();
//            } else if (mCallType.equals(CallEndType.RECIEVER_REJECT_GROUP_CALL)) {
//                rejectIncommingGroupAudioCall();
//            }

            return null;
        }
    }

//    private void callerCancelAudioCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        final CallRepository callRepository = new CallRepository();
//        callRepository.callerCancelAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType);
//                    //   closeConnectionFromSocket();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//                //todo save missed call on db...
//                //   closeConnectionFromSocket();
//            }
//        });
//    }
//
//    private void callerCancelVideoCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        final CallRepository callRepository = new CallRepository();
//        callRepository.callerCancelVideoCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//
//            }
//        });
//    }
//
//
//
//    private void finishIndividualConnectedCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("duration", duration);
//        params.put("callEndTime", currentTime);
//        params.put("lastActivityTime", currentTime);
//        CallRepository callRepository = new CallRepository();
//        callRepository.endOneToOneCall(params, new INetworkRequestListener() { // todo correct this...
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, currentTime, duration);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//
//            }
//        });
//    }
//
//    private void finishIndividualVideoConnectedCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("duration", duration);
//        params.put("callEndTime", currentTime);
//        params.put("lastActivityTime", currentTime);
//        CallRepository callRepository = new CallRepository();
//        callRepository.endOneToOneVideoCall(params, new INetworkRequestListener() { // todo correct this...
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, currentTime, duration);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//
//            }
//        });
//    }
//
//    private void rejectIncomingIndividualCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.rejectCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, 0L, 0L);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//    private void rejectIncomingIndividualVideoCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.rejectVideoCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, 0L, 0L);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//
//    private void individualMissedCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", mAttendentId);
//        params.put("callId", mCallId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.individualMissedCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, 0L, 0L);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//    private void individualVideoMissedCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", mAttendentId);
//        params.put("callId", mCallId);
//        params.put("type","video");
//        CallRepository callRepository = new CallRepository();
//        callRepository.individualMissedCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    Long callStateCode = jsonObject.getLong("callStateCode");
//                    Long responseType = jsonObject.getLong("responseTypeCode");
//                    updateCallLogs(mCallId, callStateCode, responseType, 0L, 0L);
//                    GlobalMethods.updateUiToUpdateCallLogs();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//
//    private void callerCancelGroupAudioCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("type", "callerCancelGroupAudioCall");
//        params.put("lastActivityTime", Calendar.getInstance().getTimeInMillis());
//        final CallRepository callRepository = new CallRepository();
//        callRepository.callerCancelGroupAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    mCallId = jsonCall.getLong("callId");
//                    Log.e("result", result.toString());
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    WateenHubApplication.getInstance().getHubDatabase().callsDao().updateCall(call);
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        WateenHubApplication.getInstance().getHubDatabase().participantsDao().updateParticipants(participant);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//
//            }
//        });
//    }
//
//    private void finishConnectedGroupCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("duration", duration);
//        params.put("callEndTime", currentTime);
//        params.put("lastActivityTime", currentTime);
//        params.put("endForAll", numberofConnection);
//        CallRepository callRepository = new CallRepository();
//        callRepository.endGroupAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    mCallId = jsonCall.getLong("callId");
//                    Log.e("result", result.toString());
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    call.setDuration(duration);
//                    WateenHubApplication.getInstance().getHubDatabase().callsDao().updateCall(call);
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        WateenHubApplication.getInstance().getHubDatabase().participantsDao().updateParticipantDuration(participant.getDuration(), participant.getParticipantId());
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//    private void groupMissCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", mAttendentId);
//        params.put("callId", mCallId);
//        CallRepository callRepository = new CallRepository();
//        callRepository.groupMissedCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    Log.e("result", result.toString());
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    WateenHubApplication.getInstance().getHubDatabase().callsDao().updateCall(call);
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        WateenHubApplication.getInstance().getHubDatabase().participantsDao().updateParticipants(participant);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }
//
//    public void rejectIncommingGroupAudioCall() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", UserManager.getInstance().getUserIfLoggedIn().getUserId());
//        params.put("callId", mCallId);
//        params.put("type", "participantCancelGroupAudioCall");
//        CallRepository callRepository = new CallRepository();
//        callRepository.rejectGroupAudioCall(params, new INetworkRequestListener() {
//            @Override
//            public void onSuccess(Response result) {
//                String response = (String) result.getmObj();
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    jsonObject = jsonObject.getJSONObject("data");
//                    JSONObject jsonCall = jsonObject.getJSONObject("call");
//                    JSONArray jsonParticipants = jsonObject.getJSONArray("participants");
//                    mCallId = jsonCall.getLong("callId");
//                    Log.e("result", result.toString());
//                    Calls call = StringUtils.getGson().fromJson(jsonCall.toString(), Calls.class);
//                    WateenHubApplication.getInstance().getHubDatabase().callsDao().updateCall(call);
//                    for (int i = 0; i < jsonParticipants.length(); i++) {
//                        JSONObject object = jsonParticipants.getJSONObject(i);
//                        Participants participant = StringUtils.getGson().fromJson(object.toString(), Participants.class);
//                        WateenHubApplication.getInstance().getHubDatabase().participantsDao().updateParticipants(participant);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Response result) {
//            }
//        });
//    }


}
