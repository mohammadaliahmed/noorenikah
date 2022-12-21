package com.appsinventiv.noorenikah.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {
    public static boolean ACCEPTED = false;
    public static boolean REQUEST_RECEIVED = false;
    public static int PAYOUT_AMOUNT = 20;
    public static boolean MARKETING_MSG = false;
    public static String MARKETING_MSG_TITLE = "";
    public static String MARKETING_MSG_MESSAGE = "";
    public static String MARKETING_MSG_IMAGE = "";
    public static String MESSAGE_TYPE_IMAGE = "IMAGE";
    public static String MESSAGE_TYPE_TEXT = "TEXT";
    public static String MESSAGE_TYPE_AUDIO = "AUDIO";

    public static DatabaseReference M_DATABASE = FirebaseDatabase.getInstance("https://noorenikah-default-rtdb.firebaseio.com/").getReference();
    public static final String FirebaseMessaging = "AAAA91yjyaU:APA91bFM4SHeu_4MCchQSW19DODeaHGCyZP2fPw7vPgNYnEu020By2uAMuXFUDRagCHOs86VZWRfI6ZjtPyrYJu-vNeuEHPDlNmZrQpQuSlTvg3TU8CZ2655G3fwu7bCPBO8OVUunDxn";


    public static final String BASE_URL = "http://172.29.28.19:9000/";  // ubuntu Ip

    public static final String GOOGLE_STUN_SERVER_BASE_URL = "stun:stun.l.google.com:19302";


//    public static final String COTURN_UDP_SERVER_BASE_URL_LIVE = "turn:relay.metered.ca:443?transport=udp";//https://dashboard.metered.ca/
//    public static final String USERNAME = "87a99e3ed188595b03b4cb42";
//    public static final String PASSWORD = "oxt102lc6uhJCuA2";
//    public static final String COTURN_TCP_SERVER_BASE_URL_LIVE = "turn:relay.metered.ca:443?transport=tcp";

    public static final String COTURN_UDP_SERVER_BASE_URL_LIVE = "turn:3.82.150.37:8437?transport=udp";
    public static final String COTURN_TCP_SERVER_BASE_URL_LIVE = "turn:3.82.150.37:8437?transport=tcp";

    public static final String COTURN_UDP_SERVER_BASE_URL = COTURN_UDP_SERVER_BASE_URL_LIVE;
    public static final String COTURN_TCP_SERVER_BASE_URL = COTURN_TCP_SERVER_BASE_URL_LIVE;

    public static final String WEB_RTC_BASE_URL_LIVE = "http://3.82.150.37:8435"; // public live signaling server
    public static final String WEB_RTC_BASE_URL = WEB_RTC_BASE_URL_LIVE; // public signaling server

    public static final String UTF_8_FORMAT = "utf_8";


    public static class Broadcasts {

        public static final String BROADCAST_NATIVE_CALL_STARTED = "incomingNativeCall";


        public static final String BROADCAST_CALL_REJECTED = "callRejected";
        public static final String BROADCAST_MISSED_CALL = "missedCall";
        public static final String ADD_MEMBER_TO_GROUP_CALL = "addMemberToGroupCall";
        public static final String BROADCAST_CALLER_CANCEL_AUDIO_CAll = "callerCancelAudioCall";
        public static final String BROADCAST_REMOVE_MEMBER_FROM_CAll = "removeMemberFromCall";
        public static final String BROADCAST_REDIAL_TO_A_GROUP_MEMBER = "redialToAGroupMember";
        public static final String BROADCAST_PARTICIPANT_REJOIN_CALL = "participantRejoined";
        public static final String BROADCAST_UPDATE_CALL_END_TIME = "updateCallEndTime";
        public static final String BROADCAST_UPDATE_CALL_LOGS = "updateCallLogScreen";
        public static final String BROADCAST_CALL_Ended = "callEnded";
        public static final String BROADCAST_FROM_SERVICE = "broadCastFromService";
        public static final String BROADCAST_FROM_ACTIVITY = "broadCastFromActivity";


    }

    public static final int CALL_TOTAL_TIME = 60;

    public static class Constraints {
        public static final String AUDIO_NOISE_DETECTION_CONSTRAINT = "googTypingNoiseDetection";
        public static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
        public static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
        public static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
        public static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
        public static final String AUDIO_NOISE_REDUCTION_CONSTRAINT = "googNoiseReduction";
        public static final String AUDIO_LEAKY_BUCKET_CONSTRAINT = "googLeakyBucket";
        public static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
        public static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
        public static final String AUDIO_CODEC_ISAC = "ISAC";
        public static final String OFFER_TO_RECEIVE_AUDIO = "offerToReceiveAudio";
        public static final String OFFER_TO_RECEIVE_VIDEO = "offerToReceiveVideo";

    }

    public static class ForegroundService {
        public static final String ACTION_MAIN = "MainAction";
        public static final String ACCEPT_CALL = "acceptCall";
        public static final String END_CALL = "endCall";
        public static final String CANCEL_INGOING_CALL = "cancel_ingoing_call";
        public static final String CANCEL_INCOMING_CALL = "cancel_incomming_call";
    }

    public static class SocketEvents {
        public static final String ROOM_CREATED = "created";
        public static final String ROOM_FULL = "full";
        public static final String IPADDRESS = "ipaddr";
        public static final String ROOM_JOINED = "joined";
        public static final String SOCKET_LOGS = "log";
        public static final String ROOM_CANDIDATE = "candidate";
        public static final String RECIEVE_OFFER = "offer";
        public static final String RECIEVE_ANSWER = "answer";
        public static final String NEW_CANDIDATE = "new";
        public static final String END_CALL = "endCall";
        public static final String HOLD = "hold";
        public static final String UNHOLD = "unhold";
        public static final String MUTE = "mute";
        public static final String UNMUTE = "unMute";
        public static final String RINGING = "ringing";
        public static final String GROUP_RINGING = "groupRinging";
        public static final String GROUP_REJOIN = "groupRejoin";
        public static final String END_CALL_TO_ALL = "endCallToAll";
        public static final String NUMBER_OF_CLIENTS = "numberOfClients";
        public static final String ENABLE_VIDEO = "enableVideo";
        public static final String DISABLE_VIDEO = "disableVideo";
    }

    public static class IntentExtra {
        public static final String INTENT_GROUP_NAME = "INTENT_GROUP_NAME";
        public static final String USER_FIREBASEID = "USER_FIREBASEID";
        public static final String USER_ID = "USER_ID";
        public static final String INTENT_GROUP_CREATED_BY = "group_createdBy";
        public static final String INTENT_GROUP_ID = "group_id";
        public static final String INTENT_ROOM_ID = "roomId";
        public static final String CALL_TYPE = "callType";
        public static final String USERSTRING = "USERSTRING";

        public static final String CALLER_USER_ID = "user_id";
        public static final String CALL_ID = "call_id";
        public static final String CALLING_TYPE = "calling_type";
        public static final String IS_CALL_STARTED_FROM_SERVICE = "isCallStartedFromService";
        public static final String PARTICIPANTS = "participants";



        public static final String CALL_PARTICIPANTS = "call_participants";
        public static final String USER_NAME = "userName";
        public static final String USER_POST = "post";
        public static final String ATTENDENT_ID = "attendent";
        public static final String USER_IMAGE = "userImage";
        public static final String CALL_STATE = "callState";
        public static final String EVENT_FROM_UI = "eventFromUi";
        public static final String NEW_CALL_PARTICIPANT = "newCallParticipant";
        public static final String EVENT_FROM_SERVICE = "eventFromService";
        public static final String TIME_ELAPSED = "timeElapsed";
        public static final String POSITION = "pos";
        public static final String IMAGE_URL = "imageUrl";
        public static final String MESSAGE = "message";
        public static final String USER_STATE = "userState";
        public static final String IS_LOUDSPEAKER_PRESSED = "loudSpeakerPressed";
        public static final String IS_MUTE_PRESSED = "muteButtonPressed";

        public static final String UPDATE_SCH_CALL_END = "updateSchdCallTime";

        public static final String LOUDSPEAKER_STATE = "loudSpeakerState";
        public static final String MICROPHONE_STATE = "microphoneState";
        public static final String HOLD_STATE = "holdState";
        public static final String VIDEO_STATE = "videoState";
        public static final String PARTICIPANT_HOLD_STATUS = "participantHoldStatus";
        public static final String PARTICIPANT_MUTE_STATUS = "participantMuteStatus";
        public static final String BANDWIDTH = "bandwidth";
        public static final String BITRATE = "bitrate";
        public static final String TOTAL_BANDWIDTH = "totalBandwidth";
        public static final String PACKETS = "packets";

    }


    public static class AudioStats {
        public static final String BYTE_SENT = "bytesSent";
        public static final String PACKET_SENT = "packetsSent";
        public static final String TIME_STAMP = "timestamp";
    }

}
