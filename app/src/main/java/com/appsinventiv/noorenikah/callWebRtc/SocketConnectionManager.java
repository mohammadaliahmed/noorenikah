package com.appsinventiv.noorenikah.callWebRtc;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.appsinventiv.noorenikah.Models.UserModel;
import com.appsinventiv.noorenikah.Utils.Constants;
import com.appsinventiv.noorenikah.Utils.UserManager;
import com.appsinventiv.noorenikah.call.CallManager;
import com.appsinventiv.noorenikah.callWebRtc.CallListner.AudioCallBackInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by JunaidAy on 11/2/2017.
 */

public class SocketConnectionManager {
    //todo handle all cases it is just a basic structure....
    //todo separate peerConnection Logic from this class....
    private final static String TAG = "Server_logs";
    //SocketConnectionManager.class.getCanonicalName();
    private final static String TAG_SIGNALLING = "Signalling_Events";//SocketConnectionManager.class.getCanonicalName();
    private Socket mSocket;
    private Long mGroupId;
    //    private CallWebRTCClient mCallWebRTCClient;
    private HashMap<String, CallWebRTCClient> mCallPeerClientsMap = new HashMap<>();
    private SetMediaStream mSetMediaStream;
    private ArrayList<MediaStream> remoteStreams = new ArrayList<>();
    private PeerConnectionFactory mPeerConnectionFactory;
    private MediaConstraints mMediaConstraint;
    private CallManager.CallType mCalltype;
    //    PeerConnection mPeerConnection;
    private AudioCallBackInterface mCallInterface;
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private boolean updateStatus = true;
    HashMap<String, String> mapReportPrevious = new HashMap<String, String>();
    private double totalBandwidth = 0;
    boolean isSocketInitialize = true;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private boolean isConnected = false;
    private boolean isReciever = false;
    private boolean isVideoCall = false;
    private VideoTrack remoteVideoTrack;
    List<VideoRenderer.Callbacks> remoteRenderers;

    public SocketConnectionManager(Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface) {
        this.mGroupId = groupId;
        this.mCalltype = callType;
        // isSocketInitialize = true;
        this.mCallInterface = mCallInterface;
        this.isReciever = true;
    }

    public SocketConnectionManager(Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface, List<VideoRenderer.Callbacks> remoteRenderers) {
        this.mGroupId = groupId;
        this.mCalltype = callType;
        // isSocketInitialize = true;
        this.mCallInterface = mCallInterface;
        this.remoteRenderers = remoteRenderers;
        this.isReciever = true;
        isVideoCall = true;
    }

    public SocketConnectionManager(Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface, boolean isSocketInitialize) {
        this.mGroupId = groupId;
        this.mCalltype = callType;
        this.isSocketInitialize = isSocketInitialize;
        this.mCallInterface = mCallInterface;
        isReciever = true;
    }

    public SocketConnectionManager(SetMediaStream setMediaStream, PeerConnectionFactory peerConnectionFactory,
                                   MediaConstraints mediaConstraints, Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface) {
        this.mSetMediaStream = setMediaStream;
        this.mPeerConnectionFactory = peerConnectionFactory;
        this.mMediaConstraint = mediaConstraints;
        this.mGroupId = groupId;
        this.mCalltype = callType;
        this.mCallInterface = mCallInterface;
    }

    public SocketConnectionManager(SetMediaStream setMediaStream, PeerConnectionFactory peerConnectionFactory,
                                   MediaConstraints mediaConstraints, Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface, List<VideoRenderer.Callbacks> remoteRenderers) {
        this.mSetMediaStream = setMediaStream;
        this.mPeerConnectionFactory = peerConnectionFactory;
        this.mMediaConstraint = mediaConstraints;
        this.mGroupId = groupId;
        this.mCalltype = callType;
        this.mCallInterface = mCallInterface;
        this.remoteRenderers = remoteRenderers;
        if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
            isVideoCall = true;
        }
    }

//    public SocketConnectionManager(SetMediaStream setMediaStream, PeerConnectionFactory peerConnectionFactory,
//                                   MediaConstraints mediaConstraints, Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface, ArrayList<UserModel> mParticipants, Calls call) {
//        this.mSetMediaStream = setMediaStream;
//        this.mPeerConnectionFactory = peerConnectionFactory;
//        this.mMediaConstraint = mediaConstraints;
//        this.mGroupId = groupId;
//        this.mCalltype = callType;
//        this.mCallInterface = mCallInterface;
//        this.mParticipants = mParticipants;
//        this.mCall = call;
//    }

    public void initializeComponents(SetMediaStream setMediaStream, PeerConnectionFactory peerConnectionFactory,
                                     MediaConstraints mediaConstraints, Long groupId, CallManager.CallType callType, AudioCallBackInterface mCallInterface) {
        this.mSetMediaStream = setMediaStream;
        this.mPeerConnectionFactory = peerConnectionFactory;
        this.mMediaConstraint = mediaConstraints;
        this.mGroupId = groupId;
        this.mCalltype = callType;
        this.mCallInterface = mCallInterface;
    }

    public void initializeSocketConnection() {
        try {
            mSocket = IO.socket(Constants.WEB_RTC_BASE_URL);
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeOutError);
            mSocket.on(Constants.SocketEvents.NEW_CANDIDATE, newCandiate);
            mSocket.on(Constants.SocketEvents.RECIEVE_ANSWER, handleAnswer);
            mSocket.on(Constants.SocketEvents.RECIEVE_OFFER, handleOffer);
            mSocket.on(Constants.SocketEvents.ROOM_CANDIDATE, roomCandidate);
            mSocket.on(Constants.SocketEvents.SOCKET_LOGS, logs);
            mSocket.on(Constants.SocketEvents.ROOM_JOINED, roomJoined);
            mSocket.on(Constants.SocketEvents.IPADDRESS, ipAddress);
            mSocket.on(Constants.SocketEvents.ROOM_FULL, roomFull);
            mSocket.on(Constants.SocketEvents.ROOM_CREATED, roomCreated);
            mSocket.on(Constants.SocketEvents.END_CALL, endCall);
            mSocket.on(Constants.SocketEvents.HOLD, hold);
            mSocket.on(Constants.SocketEvents.UNHOLD, unhold);
            mSocket.on(Constants.SocketEvents.MUTE, mute);
            mSocket.on(Constants.SocketEvents.UNMUTE, unMute);
            mSocket.on(Constants.SocketEvents.RINGING, ringing);
            mSocket.on(Constants.SocketEvents.GROUP_RINGING, groupRingingEvent);
            mSocket.on(Constants.SocketEvents.GROUP_REJOIN, groupRejoinEvent);
            mSocket.on(Constants.SocketEvents.END_CALL_TO_ALL, callEndToAll);
            mSocket.on(Constants.SocketEvents.NUMBER_OF_CLIENTS, numberOfClients);
            mSocket.on(Constants.SocketEvents.ENABLE_VIDEO, enableVideo);
            mSocket.on(Constants.SocketEvents.DISABLE_VIDEO, disableVideo);
            mSocket.connect();
//            mCallWebRTCClient = new CallWebRTCClient();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Long getmGroupId() {
        return mGroupId;
    }

    public void setmGroupId(Long mGroupId) {
        this.mGroupId = mGroupId;
    }

    public Socket getmSocket() {
        return mSocket;
    }

    public void setmSocket(Socket mSocket) {
        this.mSocket = mSocket;
    }

    private boolean isRoomCreated = false;
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "On Connect");
            displayToast("On Connect");
            if (!isRoomCreated) {
                Log.i(TAG, "Connected to Signalling server");
                displayToast("send " + "create or join");
                sendingEventToSignalingServer("create or join", mGroupId);
            } else {
                switch (mCalltype) {
                    case GROUP_AUDIO:
                        if (mSocket.connected()) {
//                            Logs.webRTCLogs("rejoiniong to old room");
                            sendGroupRejoinEvent();
                        }
                        break;
                    case INDIVIDUAL_AUDIO:
                    case INDIVIDUAL_VIDEO:
                        sendingEventToSignalingServer("rejoin", mGroupId); // todo for testing
                        break;
                }

            }
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "On Disconnect");
            displayToast("On Disconnect");
            Log.i(TAG, "disconnected from Signalling server");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("On Connect error.");
            Log.i(TAG_SIGNALLING, "On Connect Error.");
            Log.i(TAG, "event connect error from Signalling server");
        }
    };

    private Emitter.Listener newCandiate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("new candidate recieve");
            Log.i(TAG_SIGNALLING, "new Connection");
            JSONObject data = (JSONObject) args[0];
            try {
                String clientId = data.getString("socketId");
                Long groupId = data.getLong("groupId");
                Long userId = data.getLong("userId");
                if (isSocketInitialize) { // todo check if  factory  is nul
                    createPeerConnection(clientId, userId);
                    createOffer(clientId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener handleAnswer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "Recieve answer");
            displayToast("handle answer");
            JSONObject data = (JSONObject) args[0];
            try {
                String clientId = data.getString("clientId");
                String socketId = data.getString("socketId");
                JSONObject jsonObject = data.getJSONObject("desc");
                String type = jsonObject.getString("type");
                String sdp = jsonObject.getString("sdp");
                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                handleAnswer(clientId, sessionDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener handleOffer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "Recieve Offer");
            displayToast("handle offer");
            JSONObject data = (JSONObject) args[0];
            try {
                String clientId = data.getString("clientId");
                String socketId = data.getString("socketId");
                Long userId = data.getLong("userId");
                JSONObject jsonObject = data.getJSONObject("desc");
                String type = jsonObject.getString("type");
                String sdp = jsonObject.getString("sdp");
                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                handleOffer(clientId, sessionDescription, userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private Emitter.Listener roomCandidate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "Candidate");
            displayToast("room candidate");
            JSONObject data = (JSONObject) args[0];
            try {
                String clientId = data.getString("clientId");
                String socketId = data.getString("socketId");
                JSONObject jsonObject = data.getJSONObject("candidate");
                String candidate = jsonObject.getString("candidate");
                String sdpMid = jsonObject.getString("sdpMid");
                int sdpMLineIndex = jsonObject.getInt("sdpMLineIndex");
                IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
                PeerConnection peerConnection = mCallPeerClientsMap.get(clientId).getmPeerConnection();
                peerConnection.addIceCandidate(iceCandidate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener logs = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "Log: " + args[0].toString());
            displayToast("Log: " + args[0].toString());
        }
    };

    private String mLocalSocketId;

    private Emitter.Listener roomJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
//            mCallPeerClientsMap.get()
//            mCallWebRTCClient.setRemoteSocketId((String) args[1]);
//            mCallWebRTCClient.setInitiator(false);
            Log.i(TAG_SIGNALLING, "room joined");
            displayToast("room joined");

            mLocalSocketId = (String) args[1];
            Log.i(TAG, "Joined room: " + args[0] + " of Id: " + args[1]);
            //todo send ringing event....
            switch (mCalltype) {
                case INDIVIDUAL_AUDIO:
                case INDIVIDUAL_VIDEO:
                    sendingEventToSignalingServer("ringing", mGroupId);
                    break;

                case GROUP_AUDIO:
                    sendGroupRingingEvent();
                    mHandler = new Handler(Looper.getMainLooper());
                    startRepeatingTask();
                    break;
            }
        }
    };

    public void sendGroupRingingEvent() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        displayToast("sent ringing");
        sendingEventToSignalingServer("groupRinging", jsonObject);
    }

    public void sendGroupRejoinEvent() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        displayToast("sent ringing");
        sendingEventToSignalingServer("groupRejoin", jsonObject);
    }


    public void sendReadyEvent() {
        isSocketInitialize = true;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        displayToast("sent ready");
        sendingEventToSignalingServer("ready", jsonObject);
    }

    private Emitter.Listener ipAddress = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "ip Address: " + args[0]);
        }
    };


    private Emitter.Listener roomFull = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "Room Full: " + args[0]);
        }
    };

    private Emitter.Listener numberOfClients = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG_SIGNALLING, "number of clients");
            try {
                int numberOfClients = (int) args[0];
                Log.i("NumberofClients", "" + numberOfClients);
                if (numberOfClients <= 1) {
                    callerCancelCall();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConnectTimeOutError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("socket connection timeout");
            Log.i(TAG_SIGNALLING, "On Connection timeout");
            Log.i(TAG, "timeout error from Signalling server");
        }
    };

    private Emitter.Listener roomCreated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("room created");
            Log.i(TAG_SIGNALLING, "room created");
            Log.i(TAG, "Created room: " + args[0] + " of Id: " + args[1]);
//            mCallWebRTCClient.setRemoteSocketId((String) args[1]);
//            mCallWebRTCClient.setInitiator(true);

            mLocalSocketId = (String) args[1];
//            mCallPeerClientsMap.get((String) args[1]).setInitiator(true);
        }

    };

    private Emitter.Listener endCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("end call recieve");
            Log.i(TAG_SIGNALLING, "end Call");
            JSONObject data = (JSONObject) args[0];
            switch (mCalltype) {
                case INDIVIDUAL_AUDIO:
                    breakPeerConnection(data);
                    break;
                case GROUP_AUDIO:
                    breakPartcipantPeerConnection(data);
                    break;
                case INDIVIDUAL_VIDEO:
                    breakPeerConnection(data);
                    break;
            }
        }

    };

    private Emitter.Listener callEndToAll = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("endcall to call recieve");
            Log.i(TAG_SIGNALLING, "end Call");
            JSONObject data = (JSONObject) args[0];
            switch (mCalltype) {
                case GROUP_AUDIO:
                    endAllClients(false);
                    break;
            }
        }

    };

    private Emitter.Listener ringing = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("ringing recieve");
            Log.i(TAG, "Riniging SocketId / GroupId: " + args[0]);
            Log.i(TAG_SIGNALLING, "ringing");
            updateStatusToRining();
        }
    };

    private Emitter.Listener groupRingingEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("ringing recieve");
            Log.i(TAG_SIGNALLING, "group ringing event");
            Log.i(TAG, "Riniging SocketId / GroupId: " + args[0]);
            switch (mCalltype) {
                case GROUP_AUDIO:
                    updateStatusToRiningCallback(args);
                    break;
            }
        }
    };

    private void updateStatusToRiningCallback(Object... args) {
        try {
            JSONObject data = (JSONObject) args[0];
            Long group = data.getLong("groupId");
            Long userId = data.getLong("userId");
            if (!Objects.equals(userId, UserManager.getInstance().getUserIfLoggedIn().getId())) {
                UserModel user = UserManager.getInstance().getUserIfLoggedIn();
                if (user != null) {
                    updateStatusToRining(user);
                }
            }
//            if (mCall != null) {
//                Long callerId = mCall.getCallerId();
//                if (Objects.equals(callerId, UserManager.getInstance().getUserIfLoggedIn().getId())) {
//                    JSONObject jsonObject = new JSONObject();
//                    try {
//                        jsonObject.put("groupId", mGroupId);
//                        jsonObject.put("userId", userId);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    sendingEventToSignalingServer("groupRinging", jsonObject);
//                }
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener groupRejoinEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("group rejoin");
            Log.i(TAG, "Group Rejoin " + args[0]);
            Log.i(TAG_SIGNALLING, "Group Rejoin Event");
            try {
                JSONObject data = (JSONObject) args[0];
                Long group = data.getLong("groupId");
                Long userId = data.getLong("userId");
                String socketId = data.getString("socketId");
                if (Objects.equals(UserManager.getInstance().getUserIfLoggedIn().getId(), userId)) {
                    updateLocalSocketId(userId, socketId);
                } else {
                    updateRemoteSocketId(userId, socketId);
                }

//                UserModel user = UstadApp.getInstance().getHubDatabase().userDao().getUserIfLoggedIn(userId);
//                if (user != null) {
//
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener hold = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("hold recieve");
            Log.i(TAG_SIGNALLING, "hold ");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                boolean isOnNativeCall = data.getBoolean("isOnNativeCall");
                holdMediaStream(userId, isOnNativeCall);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("userFound", " " + "not found");
            }
        }

    };

    private Emitter.Listener mute = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("mute recieve");
            Log.i(TAG_SIGNALLING, "mute ");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                muteEvent(userId);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("userFound", " " + "not found");
            }
        }
    };

    private Emitter.Listener unMute = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("unMute recieve");
            Log.i(TAG_SIGNALLING, "unMute ");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                unMuteEvent(userId);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("userFound", " " + "not found");
            }
        }
    };

    private Emitter.Listener disableVideo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("disableVideo recieve");
            Log.i(TAG_SIGNALLING, "disableVideo ");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                disableVideoEvent(userId);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("userFound", " " + "not found");
            }
        }
    };

    private Emitter.Listener enableVideo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("enableVideo recieve");
            Log.i(TAG_SIGNALLING, "enableVideo ");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                enableVideoEvent(userId);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("userFound", " " + "not found");
            }
        }
    };


    private Emitter.Listener unhold = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            displayToast("unhold recieve");
            Log.i(TAG, "Room with SocketId: " + args[0]);
            Log.i(TAG_SIGNALLING, "unhold");
            JSONObject data = (JSONObject) args[0];
            try {
                Long userId = data.getLong("userId");
                unholdMediaStream(userId);
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }

    };

    private void updateLocalSocketId(Long userId, String socketId) {
        HashMap<String, CallWebRTCClient> map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
        for (String key : map.keySet()) {
            if (Objects.equals(mCallPeerClientsMap.get(key).getmUser().getId(), userId)) {
                //todo...check need to be done
                Log.i(TAG, "check what need to be done");
            } else {
                mCallPeerClientsMap.get(key).setLocalSocketId(socketId);
            }
        }
    }

    private void updateRemoteSocketId(Long userId, String socketId) {

//        HashMap<String,CallWebRTCClient > map = mCallPeerClientsMap;

        HashMap<String, CallWebRTCClient> map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
        for (String key : map.keySet()) {
            if (Objects.equals(mCallPeerClientsMap.get(key).getmUser().getId(), userId)) {
                CallWebRTCClient webRTCClient = mCallPeerClientsMap.get(key);
                webRTCClient.setRemoteSocketId(socketId);
                mCallPeerClientsMap.remove(key);
                mCallPeerClientsMap.put(socketId, webRTCClient);
            } else {
                mCallPeerClientsMap.get(key).setLocalSocketId(socketId);
            }
        }
    }


    private void breakPeerConnection(JSONObject jsonObject) {
        try {
            displayToast("breaking peerconnection for all");
            Long groupId = jsonObject.getLong("groupId");
            String sockectId = jsonObject.getString("socketId");
            removePeerConnection();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void breakPartcipantPeerConnection(JSONObject jsonObject) {
        try {
            displayToast("breaking peerconnection for one user");
            Long groupId = jsonObject.getLong("groupId");
            String sockectId = jsonObject.getString("socketId");
            Long userId = jsonObject.getLong("userId");
            if (Objects.equals(userId, UserManager.getInstance().getUserIfLoggedIn().getId())) {
                endAllClients(false);
            } else {
                disconnectUser(userId, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void muteEvent(Long id) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.muteCall(user);
    }

    private void unMuteEvent(Long id) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.unMuteCall(user);
    }

    private void disableVideoEvent(Long id) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.disableVideoCall(user);
    }

    private void enableVideoEvent(Long id) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.enableVideoCall(user);
    }

    private void holdMediaStream(Long id, boolean isOnNativeCall) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.holdAudioCall(user, isOnNativeCall);
    }

    private void unholdMediaStream(Long id) {
        UserModel user = UserManager.getInstance().getUserIfLoggedIn();
        mCallInterface.unholdAudioCall(user);
    }

    public int getNumberOfConnections() {
        return mCallPeerClientsMap.size();
    }

    private void sendingEventToSignalingServer(final String event, Object object) {
        if (mSocket != null) {
            mSocket.emit(event, object);
            Log.i(TAG, event + " sent");
            displayToast("sent  :" + event);
        }
    }

    private void handleOffer(String clientId, SessionDescription remoteSessionDescription, Long userId) {
        createPeerConnection(clientId, userId);
        setRemoteSessionDescription(clientId, remoteSessionDescription);
        createAnswer(clientId);
    }

    private void createPeerConnection(final String clientId, final Long userId) {
        if (mCallPeerClientsMap.containsKey(clientId)) {

        } else {
            CallWebRTCClient callWebRTCClient = new CallWebRTCClient();
            callWebRTCClient.setRemoteSocketId(clientId);
            callWebRTCClient.setLocalSocketId(mLocalSocketId);
            UserModel user = UserManager.getInstance().getUserIfLoggedIn();
            callWebRTCClient.setmUser(user);
            mCallPeerClientsMap.put(clientId, callWebRTCClient);
            List<PeerConnection.IceServer> iceServers = new LinkedList<>();
            iceServers.add(new PeerConnection.IceServer(Constants.GOOGLE_STUN_SERVER_BASE_URL));
            iceServers.add(new PeerConnection.IceServer(Constants.COTURN_UDP_SERVER_BASE_URL, "test", "test"));
            iceServers.add(new PeerConnection.IceServer(Constants.COTURN_TCP_SERVER_BASE_URL, "test", "test"));
            PeerConnection mPeerConnection = mPeerConnectionFactory.createPeerConnection(
                    iceServers,
                    mMediaConstraint,
                    new PeerConnection.Observer() {
                        @Override
                        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                            switch (signalingState) {
                                case HAVE_LOCAL_PRANSWER:
                                    Log.i(TAG, "local peer answer");
                                    displayToast("local peer answer");
                                    break;
                                case HAVE_LOCAL_OFFER:
                                    Log.i(TAG, "local peer offer");
                                    displayToast("local peer offer");
                                    break;
                                case HAVE_REMOTE_OFFER:
                                    Log.i(TAG, "remote peer offer");
                                    displayToast("remote peer offer");
                                    break;
                                case HAVE_REMOTE_PRANSWER:
                                    Log.i(TAG, "remote peer answer");
                                    displayToast("remote peer answer");
                                    break;
                                case CLOSED:
                                    Log.i(TAG, "session end");
                                    displayToast("session end");
                                    break;
                                case STABLE:
                                    Log.i(TAG, "new, not going offer and answer ");
                                    displayToast("new, not going offer and answer ");
                                    break;
                            }

                        }

                        @Override
                        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                            switch (iceConnectionState) {
                                case NEW:
                                    Log.i(TAG, "new");
                                    displayToast("new connection of peer");
                                    break;
                                case CHECKING:
                                    Log.i(TAG, "checking");
                                    displayToast("checkinng connection for peer");
                                    break;
                                case CONNECTED:
                                    isConnected = true;
                                    stopRepeatingTask();
                                    if (mCallPeerClientsMap.get(clientId).isRemoteStreamAdded) { // todo check what happend...
                                        if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_AUDIO)) {
                                            reconnectionEstablish();
                                        } else if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
                                            reconnectionEstablish();
                                        } else if (mCalltype.equals(CallManager.CallType.GROUP_AUDIO)) {
                                            reconnectionEstablish(mCallPeerClientsMap.get(clientId).getmUser());
                                        }
                                    }
                                    Log.i(TAG, "connected");
                                    displayToast("connected to for peer");
                                    break;
                                case COMPLETED:
                                    isConnected = true;
                                    Log.i(TAG, "complete");
                                    displayToast("complete connection for peer");
                                    break;
                                case CLOSED:
                                    Log.i(TAG, "close");
                                    displayToast("close for peer");
                                    break;
                                case DISCONNECTED:
                                    Log.i(TAG, "disconnected");
                                    displayToast("disconnected with peer");
                                    if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_AUDIO)) {
                                        sendReconnectEvent();
                                    }
                                    if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
                                        sendReconnectEvent();
                                    } else if (mCalltype.equals(CallManager.CallType.GROUP_AUDIO)) {
                                        sendReconnectEvent(mCallPeerClientsMap.get(clientId).getmUser());
                                    }
                                    break;
                                case FAILED:
                                    Log.i(TAG, "failed");
                                    displayToast("failed to recover with peer");
                                    //todo for group testing
                                    if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_AUDIO)) {
                                        sendReconnectFail(clientId, mCallPeerClientsMap.get(clientId).getmUser());
                                    }
                                    if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
                                        sendReconnectFail(clientId, mCallPeerClientsMap.get(clientId).getmUser());
                                    } else if (mCalltype.equals(CallManager.CallType.GROUP_AUDIO)) {
                                        removeFailedPeer(clientId, mCallPeerClientsMap.get(clientId).getmUser());
                                    }
                                    break;
                            }
                        }

                        @Override
                        public void onIceConnectionReceivingChange(boolean b) {
                            if (b) {
                                Log.i(TAG, "ice connection receiving change true");
                            } else {
                                Log.i(TAG, "ice connection receiving change false");
                            }

                        }

                        @Override
                        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

                        }

                        @Override
                        public void onIceCandidate(IceCandidate iceCandidate) {

                            JSONObject jsonCandidate = new JSONObject();
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("type", "candidate");
                                jsonObject.put("candidate", iceCandidate.sdp);
                                jsonObject.put("sdpMid", iceCandidate.sdpMid);
                                jsonObject.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                                jsonCandidate.put("clientId", mCallPeerClientsMap.get(clientId).getLocalSocketId());
                                jsonCandidate.put("socketId", mCallPeerClientsMap.get(clientId).getRemoteSocketId());
                                jsonCandidate.put("candidate", jsonObject);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocket.emit("candidate", jsonCandidate);
                            displayToast("sent candidate");
                        }

                        @Override
                        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

                        }

                        @Override
                        public void onAddStream(final MediaStream mediaStream) {
                            // todo provide these streams to activity  some how
                            remoteStreams.add(mediaStream);
                            if (isVideoCall && (mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO))) {
                                mCallPeerClientsMap.get(clientId).isRemoteStreamAdded = true;
                                if (mediaStream.videoTracks.size() == 1) {
                                    remoteVideoTrack = mediaStream.videoTracks.get(0);
                                    remoteVideoTrack.setEnabled(true);
                                    mediaStream.audioTracks.get(0).setEnabled(true);
                                    for (VideoRenderer.Callbacks remoteRender : remoteRenderers) {
                                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                                        startTimer();
                                    }
                                }
                            }

                            mCallPeerClientsMap.get(clientId).setmMediaStream(mediaStream);
                            if (mCalltype.equals(CallManager.CallType.INDIVIDUAL_AUDIO)) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (updateStatus) {
                                            try {
                                                if (mCallPeerClientsMap.get(clientId) != null && mCallPeerClientsMap.get(clientId).getmPeerConnection() != null) {
                                                    mCallPeerClientsMap.get(clientId).getmPeerConnection().getStats(new StatsObserver() {
                                                        @Override
                                                        public void onComplete(StatsReport[] statsReports) {
                                                            HashMap<String, String> mapReport = getReportMap(statsReports);
                                                            updateStats(mapReport);
                                                        }
                                                    }, null);
                                                    Thread.sleep(1000);
                                                } else {
                                                    updateStatus = false;
                                                }
                                            } catch (InterruptedException e) {

                                            }
                                        }
                                    }
                                }).start();
                            }
                            for (int i = 0; i < remoteStreams.size(); i++) {
                                isConnected = true;
                                switch (mCalltype) {
                                    case INDIVIDUAL_AUDIO:
                                    case INDIVIDUAL_VIDEO:
                                        mCallPeerClientsMap.get(clientId).isRemoteStreamAdded = true;
                                        startTimer();
                                        break;
                                    case GROUP_AUDIO:
                                        stopRepeatingTask();
                                        mCallPeerClientsMap.get(clientId).isRemoteStreamAdded = true;
                                        startTimer(mCallPeerClientsMap.get(clientId).getmUser());
                                }
                                //    Log.e("Server",mCallPeerClientsMap.get(clientId).getmUser().getDisplayName());
                                mediaStream.audioTracks.get(0).setEnabled(true);
                            }
//                                for (int i = 0; i < remoteStreams.size(); i++) {=
//                                    if (i == 0) {
//                                        VideoRenderer renderer = null;
//                                        try {
//                                            //  renderer = VideoRendererGui.createGui(50, 0, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        //  mediaStream.videoTracks.get(0).addRenderer(renderer);
//                                        mediaStream.videoTracks.get(0).setEnabled(false);
//                                    } else if (i == 1) {
//                                        VideoRenderer renderer = null;
//                                        try {
//                                            //     renderer = VideoRendererGui.createGui(0, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        //  mediaStream.videoTracks.get(0).addRenderer(renderer);
//                                        mediaStream.videoTracks.get(0).setEnabled(false);
//                                    } else if (i == 2) {
//                                        VideoRenderer renderer = null;
//                                        try {
//                                            //renderer = VideoRendererGui.createGui(50, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        //  mediaStream.videoTracks.get(0).addRenderer(renderer);
//                                        mediaStream.videoTracks.get(0).setEnabled(false);
//                                    }
//                                }

                        }

                        @Override
                        public void onRemoveStream(MediaStream mediaStream) {

                        }

                        @Override
                        public void onDataChannel(DataChannel dataChannel) {

                        }

                        @Override
                        public void onRenegotiationNeeded() {

                        }

                        @Override
                        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

                        }
                    });

            mPeerConnection.addStream(mSetMediaStream.getmLocalMediaStream());
            mCallPeerClientsMap.get(clientId).setmPeerConnection(mPeerConnection);
        }
    }

//    private void setRemoteSessionDescription(String clientId, SessionDescription sessionDescription) {
//        Log.i(TAG,"remote"+sessionDescription.toString());
//        //  mCallWebRTCClient.isRoomCreated = true; // todo retrying...
//        PeerConnection peerConnection = mCallPeerClientsMap.get(clientId);
//        peerConnection.setRemoteDescription(new SdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                Log.i(TAG, "Remote session description: " + sessionDescription);
//            }
//
//            @Override
//            public void onSetSuccess() {
//                Log.i(TAG, "Remote session description success");
//            }
//
//            @Override
//            public void onCreateFailure(String s) {
//            }
//
//            @Override
//            public void onSetFailure(String s) {
//
//            }
//        }, sessionDescription);
//    }

    private void removeFailedPeer(String clientId, UserModel user) {
        sendUserDisconEventToServer(clientId, user);
        close(clientId);
        mCallPeerClientsMap.remove(clientId);
        if (mCallPeerClientsMap.size() >= 1) {
            sendUserDisconnected(user);
        } else {
            sendFinishCallEvent(user);
        }
    }

    private void sendUserDisconEventToServer(String clientId, UserModel user) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", mGroupId);
            jsonObject.put("userId", user.getId());
            jsonObject.put("socketId", clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendingEventToSignalingServer("callFailed", jsonObject);
    }

    private void updateStats(HashMap<String, String> mapReport) {
        if (mapReportPrevious.containsKey(Constants.AudioStats.BYTE_SENT)) {
            String previousByteSentStr = mapReportPrevious.get(Constants.AudioStats.BYTE_SENT);
            String previousPacketSentStr = mapReportPrevious.get(Constants.AudioStats.PACKET_SENT);
            String previousTimeStampStr = mapReportPrevious.get(Constants.AudioStats.TIME_STAMP);
            Long previousByteSent = Long.parseLong(previousByteSentStr);
            Long previousPacketSent = Long.parseLong(previousPacketSentStr);
            Double previousTimeStamp = Double.parseDouble(previousTimeStampStr);

            String byteSentStr = mapReport.get(Constants.AudioStats.BYTE_SENT);
            String packetSentStr = mapReport.get(Constants.AudioStats.PACKET_SENT);
            String timeStampStr = mapReport.get(Constants.AudioStats.TIME_STAMP);
            Long byteSent = Long.parseLong(byteSentStr);
            Long packetSent = Long.parseLong(packetSentStr);
            Double timeStamp = Double.parseDouble(timeStampStr);

            Log.i("Rate: ", "Bits: " + ((8 * byteSent) - (8 * previousByteSent)));
            Log.i("Rate: ", "Time: " + (timeStamp - previousTimeStamp) / 1000);

            double bytesCount = byteSent - previousByteSent;
            double bits = (8 * byteSent) - (8 * previousByteSent);
            double kilobytes = bytesCount / 1024;

            Double bandwidth = kilobytes / ((timeStamp - previousTimeStamp) / 1000); // kb/s
            Double bitRate = bits / ((timeStamp - previousTimeStamp) / 1000); // b/s

            totalBandwidth = totalBandwidth + bandwidth;
            mCallInterface.updateStats(bandwidth, bitRate, totalBandwidth, (packetSent - previousPacketSent));
            mapReportPrevious.clear();
            mapReportPrevious.putAll(mapReport);


        } else {
            mapReportPrevious.putAll(mapReport);
        }

    }

    private HashMap<String, String> getReportMap(StatsReport[] statsReports) {
        HashMap<String, StatsReport> mapLatestReport = new HashMap<String, StatsReport>();
        HashMap<String, String> mapReport = new HashMap<String, String>();
        for (StatsReport statsReport :
                statsReports) {
            if (statsReport.id.equalsIgnoreCase("Conn-audio-1-0")) {
                mapReport.put(Constants.AudioStats.TIME_STAMP, "" + statsReport.timestamp);
                for (StatsReport.Value value :
                        statsReport.values) {
                    mapReport.put(value.name, value.value);
                }
            }
            mapLatestReport.put(statsReport.id, statsReport);
        }
        return mapReport;
    }

    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        final String[] lines = sdpDescription.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            return sdpDescription;
        }
        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<String>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (int i = 0; i < lines.length; ++i) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Log.w(TAG, "No payload types with name " + codec);
            return sdpDescription;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdpDescription;
        }
        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    private static String movePayloadTypesToFront(List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<String>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<String>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static String setStartBitrate(
            String codec, boolean isVideoCodec, String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet =
                            "a=fmtp:" + codecRtpMap + " " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "="
                            + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }
        return newSdpDescription.toString();
    }

    private void setRemoteSessionDescription(String clientId, SessionDescription sessionDescription) {
        PeerConnection peerConnection = mCallPeerClientsMap.get(clientId).getmPeerConnection();
        SdpObserver sdpObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                //  Log.i(TAG, "Remote session description: " + sessionDescription);
            }

            @Override
            public void onSetSuccess() {
                Log.i(TAG, "Remote session description success");
            }

            @Override
            public void onCreateFailure(String s) {

            }

            @Override
            public void onSetFailure(String s) {

            }
        };
        String sdpDescription = sessionDescription.description;
        sdpDescription = preferCodec(sdpDescription, "ISAC", true);
//        sdpDescription = setStartBitrate("opus", false, sdpDescription, 8);
        SessionDescription sdpRemote = new SessionDescription(sessionDescription.type, sdpDescription);
        Log.i(TAG, "remote " + sdpRemote.description);
        peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
    }

    private void createAnswer(final String clientId) {
//        mCallWebRTCClient.isRoomCreated = true; // todo retying...
//        mCallPeerClientsMap.get(clientId).isRoomCreated = true;
        isRoomCreated = true;
        PeerConnection peerConnection = mCallPeerClientsMap.get(clientId).getmPeerConnection();
        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i(TAG, "Answer created successfuly");
                setLocalDescription(clientId, sessionDescription);
                JSONObject answer = new JSONObject();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", "answer");
                    jsonObject.put("sdp", sessionDescription.description);
                    answer.put("clientId", mCallPeerClientsMap.get(clientId).getLocalSocketId());
                    answer.put("socketId", mCallPeerClientsMap.get(clientId).getRemoteSocketId());
                    answer.put("desc", jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                var answer  = {'clientId': socket.userId , 'socketId' : clientId , 'desc':desc };
                mSocket.emit("answer", answer);
                displayToast("create answer");
            }

            @Override
            public void onSetSuccess() {
            }

            @Override
            public void onCreateFailure(String s) {
                Log.i(TAG, "failed to create answer: " + s);
            }

            @Override
            public void onSetFailure(String s) {
                Log.i(TAG, "failed to create answer: " + s);
            }
        }, new MediaConstraints());
    }

    private void setLocalDescription(String clientId, final SessionDescription sessionDescriptio) {
        String sdpDescription = sessionDescriptio.description;
        sdpDescription = preferCodec(sdpDescription, "ISAC", true);
        SessionDescription sdpLocal = new SessionDescription(sessionDescriptio.type, sdpDescription);
        PeerConnection peerConnection = mCallPeerClientsMap.get(clientId).getmPeerConnection();
        SdpObserver sdpObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i(TAG, "Local session description: " + sessionDescription);
            }

            @Override
            public void onSetSuccess() {
                Log.i(TAG, "Local session description success");
            }

            @Override
            public void onCreateFailure(String s) {

            }

            @Override
            public void onSetFailure(String s) {

            }
        };
        Log.i(TAG, "local " + sdpLocal.description);
        peerConnection.setLocalDescription(sdpObserver, sdpLocal);
    }

    private void handleAnswer(String clientId, SessionDescription sessionDescription) {
        setRemoteSessionDescription(clientId, sessionDescription);
    }

    private void createOffer(final String clientId) {
        PeerConnection peerConnection = mCallPeerClientsMap.get(clientId).getmPeerConnection();
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i(TAG, "Offer created successfully");
                setLocalDescription(clientId, sessionDescription);
                JSONObject offer = new JSONObject();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("type", "offer");
                    jsonObject.put("sdp", sessionDescription.description);
//                    offer.put("clientId", mCallWebRTCClient.getRemoteSocketId());
                    offer.put("clientId", mCallPeerClientsMap.get(clientId).getLocalSocketId());
                    offer.put("socketId", mCallPeerClientsMap.get(clientId).getRemoteSocketId());
                    offer.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
                    offer.put("desc", jsonObject);
                    switch (mCalltype) {
                        case INDIVIDUAL_AUDIO:
                        case INDIVIDUAL_VIDEO:
                            updateCallingStateToConnecting();
                            break;
                        case GROUP_AUDIO:
                            //todo find which user is connecting based on remote socket.
                            if (mCallPeerClientsMap.get(clientId).getmUser() != null) {
                                updateCallingStateToConnecting(mCallPeerClientsMap.get(clientId).getmUser());
                            }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("offer", offer);
                displayToast("create offer");
            }

            @Override
            public void onSetSuccess() {

            }

            @Override
            public void onCreateFailure(String s) {

            }

            @Override
            public void onSetFailure(String s) {

            }

        }, mMediaConstraint);
    }

    public void removeClientInfo() {
//        mCallWebRTCClient = null;
    }

    public void closeSocket() {
        if (mSocket != null) {
            if (mSocket.connected()) {
                mSocket.disconnect();
                if (!mSocket.connected()) {
                    mSocket.close();
                    mSocket = null;
                }
            }
        }
    }

    public void removePeerConnection() {
        try {
            for (CallWebRTCClient callWebrtClient :
                    mCallPeerClientsMap.values()) {
                close(callWebrtClient.getRemoteSocketId());
            }

//            this.mPeerConnection.close();
//            this.mPeerConnection.dispose();
//            this.mPeerConnection = null;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("endCallStatus", jsonObject);
            this.mPeerConnectionFactory.dispose();
            this.mPeerConnectionFactory = null;
            this.mSetMediaStream = null;
            this.mMediaConstraint = null;
            this.mGroupId = -1L;
            this.remoteStreams = null;
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
            mCallInterface.callEnded();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    public void removePeerConnection(final String clientId) {
        try {
            close(clientId);
            if (mCallPeerClientsMap.containsKey(clientId)) {
                mCallInterface.userEndedCall(mCallPeerClientsMap.get(clientId).getmUser());
                mCallPeerClientsMap.remove(clientId);
            }
            if (mCallPeerClientsMap.size() < 1) { // todo yahan pay issue hai....
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
                sendingEventToSignalingServer("endCallStatus", jsonObject);
                this.mPeerConnectionFactory.dispose();
                this.mPeerConnectionFactory = null;
                this.mSetMediaStream = null;
                this.mMediaConstraint = null;
                this.mGroupId = -1L;
                this.remoteStreams = null;
                mSocket.disconnect();
                mSocket.close();
                mSocket = null;
                mCallInterface.callEnded();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.startTimer();
            }
        };
        mainHandler.post(myRunnable);
    }

    private void callerCancelCall() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (isReciever) {
                    mCallInterface.callerCancelledCall();
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    private void startTimer(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.startTimer(user);
            }
        };
        mainHandler.post(myRunnable);
    }


    private void updateStatusToRining() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (mCallInterface != null)
                    mCallInterface.callRinging();
            }
        };
        mainHandler.post(myRunnable);
    }

    private void updateStatusToRining(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (mCallInterface != null)
                    mCallInterface.callRinging(user);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void reconnectionEstablish() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.reconnectionEstablished();
            }
        };
        mainHandler.post(myRunnable);
    }

    private void reconnectionEstablish(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.reconnectionEstablished(user);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void sendReconnectEvent() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.reConnectingCall();
            }
        };
        mainHandler.post(myRunnable);
    }

    private void sendReconnectEvent(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.reConnectingCall(user);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void sendUserDisconnected(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.userDisconnected(user);
            }
        };
        mainHandler.post(myRunnable);
    }

    private void sendReconnectFail(String clientId, UserModel user) {
        sendUserDisconEventToServer(clientId, user);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.finishCall();
            }
        };
        mainHandler.post(myRunnable);
    }

    private void sendFinishCallEvent(final UserModel user) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.finishCall(user);
            }
        };
        mainHandler.post(myRunnable);
    }


    public void updateCallingStateToConnecting() {
//        mCallWebRTCClient.isRoomCreated = true; //todo retrying
        isRoomCreated = true; //todo retrying
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.updateCallingToConnectingState();
            }
        };
        mainHandler.post(myRunnable);
    }

    public void updateCallingStateToConnecting(final UserModel user) {
//        mCallWebRTCClient.isRoomCreated = true; //todo retrying
        isRoomCreated = true; //todo retrying
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mCallInterface.updateCallingToConnectingState(user);
            }
        };
        mainHandler.post(myRunnable);
    }


    public void closePeerConnection() {
        try {
//            this.mPeerConnection = null;
            //=======
            this.mPeerConnectionFactory.dispose();
            this.mPeerConnectionFactory = null;
            mSetMediaStream.getAudioSource().dispose();
            this.mSetMediaStream = null;
            this.mMediaConstraint = null;
            this.mGroupId = -1L;
            this.remoteStreams = null;
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
            // closeFactory();
            mCallInterface.callerEndedAudioCall();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    public void closePeerConnection(String clientId) {
        try {
            mCallPeerClientsMap.get(clientId).setmPeerConnection(null);
//            this.mPeerConnection = null;
            this.mPeerConnectionFactory.dispose();
            this.mPeerConnectionFactory = null;
            this.mSetMediaStream = null;
            this.mMediaConstraint = null;
            this.mGroupId = -1L;
            this.remoteStreams = null;
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
            mCallInterface.callerEndedAudioCall();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    private void holdAllRemoteMedia() {
        HashMap<String, CallWebRTCClient> map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
        for (String key : map.keySet()) {
            if (map.containsKey(key)) {
                if (map.get(key).getmMediaStream() != null) {
                    map.get(key).getmMediaStream().audioTracks.get(0).setEnabled(false);
                }
            }

        }
        if(mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
            map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
            for (String key : map.keySet()) {
                if (map.containsKey(key)) {
                    if (map.get(key).getmMediaStream() != null) {
                        map.get(key).getmMediaStream().videoTracks.get(0).setEnabled(false);
                    }
                }
            }
        }
    }

    private void unHoldAllRemoteMedia() {
        HashMap<String, CallWebRTCClient> map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
        for (String key : map.keySet()) {
            if (map.containsKey(key)) {
                if (map.get(key).getmMediaStream() != null) {
                    map.get(key).getmMediaStream().audioTracks.get(0).setEnabled(true);
                }
            }
        }
        if(mCalltype.equals(CallManager.CallType.INDIVIDUAL_VIDEO)) {
            map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
            for (String key : map.keySet()) {
                if (map.containsKey(key)) {
                    if (map.get(key).getmMediaStream() != null) {
                        map.get(key).getmMediaStream().videoTracks.get(0).setEnabled(true);
                    }
                }
            }
        }

    }

    public void muteCall() {
        try {
            // holdAllRemoteMedia();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("mute", jsonObject); //todo change here..
            unHoldAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unMuteCall() {
        try {
            //holdAllRemoteMedia();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("unMute", jsonObject); //todo change here..
            unHoldAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disableVideCall() {
        try {
            // holdAllRemoteMedia();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("disableVideo", jsonObject); //todo change here..
            unHoldAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void enableVideoCall() {
        try {
            //holdAllRemoteMedia();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("enableVideo", jsonObject); //todo change here..
            unHoldAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void callHoldEvent(boolean isOnNativeCall) {
        try {
            holdAllRemoteMedia();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            jsonObject.put("isOnNativeCall", isOnNativeCall);
            sendingEventToSignalingServer("hold", jsonObject); //todo change here..
            holdAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void callUnholdEvent() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mGroupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("unhold", jsonObject); //todo change here...
            unHoldAllRemoteMedia();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void closeAll() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("groupId", mGroupId);
            jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
            sendingEventToSignalingServer("endCall", jsonObject);
            for (CallWebRTCClient callWebrtClient :
                    mCallPeerClientsMap.values()) {
                close(callWebrtClient.getRemoteSocketId());
            }
            mPeerConnectionFactory.dispose();
            mPeerConnectionFactory = null;
            mSetMediaStream.getAudioSource().dispose();
            this.mSetMediaStream = null;
            this.mMediaConstraint = null;
            this.mGroupId = -1L;
            this.remoteStreams = null;
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
            mCallInterface.callEnded();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    public void endAllClients(boolean isAdmin) {
        try {
            if (isAdmin) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("groupId", mGroupId);
                jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
                sendingEventToSignalingServer("endCallToAll", jsonObject);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", UserManager.getInstance().getUserIfLoggedIn().getId());
                sendingEventToSignalingServer("endCallStatus", jsonObject);
            }
            for (CallWebRTCClient callWebrtClient :
                    mCallPeerClientsMap.values()) {
                close(callWebrtClient.getRemoteSocketId());
            }
            mPeerConnectionFactory.dispose();
            mPeerConnectionFactory = null;
            mSetMediaStream.getAudioSource().dispose();
            this.mSetMediaStream = null;
            this.mMediaConstraint = null;
            this.mGroupId = -1L;
            this.remoteStreams = null;
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
            mCallInterface.userDisconnected(); // person disconnected...
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }


    public void close(String clientId) {
        try {
            if (mCallPeerClientsMap.containsKey(clientId)) {
                mCallPeerClientsMap.get(clientId).getmPeerConnection().removeStream(mSetMediaStream.getmLocalMediaStream());
                mCallPeerClientsMap.get(clientId).getmPeerConnection().close();
                mCallPeerClientsMap.get(clientId).getmPeerConnection().dispose();
                mCallPeerClientsMap.get(clientId).setmPeerConnection(null);
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    private void closeFactory() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mPeerConnectionFactory.dispose();
                    mPeerConnectionFactory = null;
                    PeerConnectionFactory.stopInternalTracingCapture();
                    PeerConnectionFactory.shutdownInternalTracer();
                    mSetMediaStream.getAudioSource().dispose();
                    mSetMediaStream = null;
                    mMediaConstraint = null;
                    mGroupId = -1L;
                    remoteStreams = null;
                    mSocket.disconnect();
                    mSocket.close();
                    mSocket = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private void displayToast(String msg) {
//          CommonUtils.showToast(msg);
    }

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (!isConnected) {
                    sendGroupRingingEvent();
                }//this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    public void stopRepeatingTask() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mStatusChecker);
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void disconnectUser(Long userId, boolean isAdmin) {
        HashMap<String, CallWebRTCClient> map = new HashMap<String, CallWebRTCClient>(mCallPeerClientsMap);
        for (String key : map.keySet()) {
            if (Objects.equals(mCallPeerClientsMap.get(key).getmUser().getId(), userId)) {
                if (isAdmin) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("groupId", mGroupId);
                        jsonObject.put("userId", userId);
                        sendingEventToSignalingServer("endCall", jsonObject);
                        sendingEventToSignalingServer("endCallStatus", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                removePeerConnection(key);
            }
        }
    }

    public void updateCallType(CallManager.CallType callType) {
        this.mCalltype = callType;
    }
}
