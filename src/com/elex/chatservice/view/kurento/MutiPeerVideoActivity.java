package com.elex.chatservice.view.kurento;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.RendererCommon;

import com.elex.chatservice.R;
import com.elex.chatservice.model.kurento.Constants;
import com.elex.chatservice.model.kurento.LooperExecutor;
import com.elex.chatservice.model.kurento.PeerUserInfo;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.model.kurento.room.KurentoRoomAPI;
import com.elex.chatservice.model.kurento.room.RoomError;
import com.elex.chatservice.model.kurento.room.RoomListener;
import com.elex.chatservice.model.kurento.room.RoomNotification;
import com.elex.chatservice.model.kurento.room.RoomResponse;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMMediaConfiguration;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMPeerConnection;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMWebRTCPeer;


public class MutiPeerVideoActivity extends Activity implements NBMWebRTCPeer.Observer, RoomListener {
    private static final String TAG = "MutiPeerVideoActivity";


    private NBMWebRTCPeer nbmWebRTCPeer;
    private List<PeerUserInfo> peerList;

    private SurfaceViewRenderer masterView;
    private SurfaceViewRenderer localView;

    private Map<Integer, String> videoRequestUserMapping;
    private int publishVideoRequestId;
    private TextView mCallStatus;
    private boolean backPressed = false;
    private Thread backPressedThread = null;


    private String username, roomname;
    private LooperExecutor executor;
    private static KurentoRoomAPI kurentoRoomAPI;
    private int roomId = 0;
    private String wsUri;

    private Handler mHandler = null;
    private CallState callState;

    private enum CallState {
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_video_chat);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        masterView = (SurfaceViewRenderer) findViewById(R.id.masterView);
        localView = (SurfaceViewRenderer) findViewById(R.id.small_peer_video);
        this.mCallStatus = (TextView) findViewById(R.id.call_status);
        callState = CallState.IDLE;

        executor = new LooperExecutor();
        executor.requestStart();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Constants.SERVER_ADDRESS_SET_BY_USER = mSharedPreferences.getString(Constants.SERVER_NAME, Constants.DEFAULT_SERVER);
        wsUri = mSharedPreferences.getString(Constants.SERVER_NAME, Constants.DEFAULT_SERVER);
        kurentoRoomAPI = new KurentoRoomAPI(executor, wsUri, this);
        mHandler = new Handler();



        this.username = mSharedPreferences.getString(Constants.USER_NAME, "");
        this.roomname = mSharedPreferences.getString(Constants.ROOM_NAME, "");
        Log.i(TAG, "username: " + this.username);
        Log.i(TAG, "roomname: " + this.roomname);

//        // Load test certificate from assets
//        CertificateFactory cf;
//        try {
//            cf = CertificateFactory.getInstance("X.509");
//            InputStream caInput = new BufferedInputStream(getAssets().open("kurento_room_base64.cer"));
//            Certificate ca = cf.generateCertificate(caInput);
//            kurentoRoomAPI.addTrustedCertificate("ca", ca);
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        kurentoRoomAPI.useSelfSignedCertificate(true);
        kurentoRoomAPI.addObserver(this);

//        peerVideoAdapter = new PeerVideoListAdapter(this, peerList, masterView);
//        peer_video_list.setAdapter(peerVideoAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!kurentoRoomAPI.isWebSocketConnected()) {
            Log.i(TAG, "Connecting to room at " + wsUri);
            kurentoRoomAPI.connectWebSocket();
        }
        videoRequestUserMapping = new HashMap<Integer, String>();

        EglBase rootEglBase = EglBase.create();
        masterView.init(rootEglBase.getEglBaseContext(), null);
        masterView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);


        NBMMediaConfiguration peerConnectionParameters = new NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 0,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 0,
                new NBMMediaConfiguration.NBMVideoFormat(352, 288, PixelFormat.RGB_888, 20),
                NBMMediaConfiguration.NBMCameraPosition.FRONT);

        nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, localView, this);
//        nbmWebRTCPeer.registerMasterRenderer(masterView);
        Log.i(TAG, "Initializing nbmWebRTCPeer...");
        nbmWebRTCPeer.initialize();
        callState = CallState.PUBLISHING;
        mCallStatus.setText("Publishing...");
    }

    public NBMWebRTCPeer getNbmWebRTCPeer() {
        return nbmWebRTCPeer;
    }

    @Override
    protected void onStop() {
        endCall();
        super.onStop();
    }

    @Override
    protected void onPause() {
        nbmWebRTCPeer.stopLocalMedia();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nbmWebRTCPeer.startLocalMedia();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendLeaveRoom(roomId,username);
            kurentoRoomAPI.disconnectWebSocket();
        }
        executor.requestStop();
        super.onDestroy();
    }

    @Override
    public void onRoomResponse(RoomResponse response) {
        if (response == null)
            return;
        Log.i(TAG, "onRoomResponse: callState:"+callState+"\n"+response.toString());
        int requestId = response.getId();

        if(requestId == roomId)
        {
            nbmWebRTCPeer.generateOffer(username, true);
        }
        else if (requestId == publishVideoRequestId){

            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                    response.getValue("sdpAnswer").get(0));

            // Check if we are waiting for publication of our own vide
            if (callState == CallState.PUBLISHING){
                callState = CallState.PUBLISHED;
                nbmWebRTCPeer.processAnswer(sd, username);
                mHandler.postDelayed(offerWhenReady, 2000);

                // Check if we are waiting for the video publication of the other peer
            } else if (callState == CallState.WAITING_REMOTE_USER){
                //String user_name = Integer.toString(publishVideoRequestId);
                callState = CallState.RECEIVING_REMOTE_USER;
                String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
                nbmWebRTCPeer.processAnswer(sd, connectionId);
            }
        }


    }

    @Override
    public void onRoomError(RoomError error) {
        Log.wtf(TAG, error.toString());
        if (error.getCode() == 104) {
            showFinishingError("Room error", "Username already taken");
        }
    }

    @Override
    public void onRoomNotification(RoomNotification notification) {
        Log.i(TAG, "onRoomNotification\n"+notification.toString());
        Map<String, Object> map = notification.getParams();


        // Somebody left the room
        if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_LEFT)) {
            final String user = map.get("name").toString();
            Log.i(TAG, user + " PARTICIPANT_LEFT");
        }
        // Somebody joined the room
        else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_JOINED)) {
            final String user = map.get("id").toString();
            Log.i(TAG, user + " PARTICIPANT_JOINED");
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mTextMessageTV.setText(getString(R.string.participant_joined, user));
//                    mHandler.removeCallbacks(clearMessageView);
//                    mHandler.postDelayed(clearMessageView, 3000);
//                }
//            });
        }
        // Somebody in the room published their video
        else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED)) {
            final String user = map.get("id").toString();
            WebRtcPeerManager.getInstance().changePeerPublishStatus(user, true);
            Log.i(TAG, "I'm " + username + " DERP: Other peer published already:" + notification.toString());
            mHandler.postDelayed(offerWhenReady, 2000);
        } else if (notification.getMethod().equals(RoomListener.METHOD_ICE_CANDIDATE)) {
            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();
            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, this.username);
            } else {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString());
            }
        }

        // Somebody in the room published their video
        else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED)) {

        }

    }

    @Override
    public void onRoomConnected() {
        if (kurentoRoomAPI.isWebSocketConnected() && !roomJoinRoomed) {
            joinRoom();
        }
    }

    @Override
    public void onRoomDisconnected() {
        showFinishingError("Disconnected", "You have been disconnected from room.");
    }

    private void showFinishingError(String title, String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG);
//        new AlertDialog.Builder(MutiPeerVideoActivity.this)
//                .setTitle(title)
//                .setMessage(message)
//                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }

    private boolean roomJoinRoomed = false;

    private void joinRoom() {
        roomJoinRoomed = true;
        Constants.id++;
        roomId = Constants.id;
        Log.i(TAG, "Joinroom: User: " + this.username + ", Room: " + this.roomname + " id:" + roomId);
        if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendJoinRoom(this.username, this.roomname, true, roomId);
        }
    }


    @Override
    public void onBackPressed() {
        // Data channel test code
        /*DataChannel channel = nbmWebRTCPeer.getDataChannel("local", "test_channel_static");
        if (channel.state() == DataChannel.State.OPEN) {
            sendHelloMessage(channel);
            Log.i(TAG, "[datachannel] Datachannel open, sending hello");
        }
        else {
            Log.i(TAG, "[datachannel] Channel is not open! State: " + channel.state());
        }
        Log.i(TAG, "[DataChannel] Testing for existing channel");
        DataChannel channel =  nbmWebRTCPeer.getDataChannel("local", "default");
        if (channel == null) {
            DataChannel.Init init = new DataChannel.Init();
            init.negotiated = false;
            init.ordered = true;
            Log.i(TAG, "[DataChannel] Channel does not exist, creating...");
            channel = nbmWebRTCPeer.createDataChannel("local", "test_channel", init);
        }
        else {
            Log.i(TAG, "[DataChannel] Channel already exists. State: " + channel.state());
            sendHelloMessage(channel);
        }*/

        // If back button has not been pressed in a while then trigger thread and toast notification
        if (!this.backPressed) {
            this.backPressed = true;
            Toast.makeText(this, "Press back again to end.", Toast.LENGTH_SHORT).show();
            this.backPressedThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        backPressed = false;
                    } catch (InterruptedException e) {
                        Log.d("VCA-oBP", "Successfully interrupted");
                    }
                }
            });
            this.backPressedThread.start();
        }
        // If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
        else {
            if (this.backPressedThread != null)
                this.backPressedThread.interrupt();
            super.onBackPressed();
        }
    }

    public void hangup(View view) {
        finish();
    }

    private void GenerateOfferForRemote(String remote_name) {
        nbmWebRTCPeer.generateOffer(remote_name, false);
        callState = CallState.WAITING_REMOTE_USER;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("等待通话中");
            }
        });
    }

    public void receiveFromRemote(View view) {
        //GenerateOfferForRemote();
    }

    /**
     * Terminates the current call and ends activity
     */
    private void endCall() {
        callState = CallState.IDLE;
        try {
            if (nbmWebRTCPeer != null) {
                nbmWebRTCPeer.close();
                nbmWebRTCPeer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitialize() {
        nbmWebRTCPeer.enableVideo(false);
//        nbmWebRTCPeer.generateOffer("local", true);
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, final NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onLocalSdpOfferGenerated callState:"+callState);
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            Log.d(TAG, "Sending " + sessionDescription.type+"  sessionDescription.description:"+sessionDescription.description);
            publishVideoRequestId = ++Constants.id;
            kurentoRoomAPI.sendPublishVideo(sessionDescription.description, false, publishVideoRequestId);
        } else { // Asking for remote user video
            Log.d(TAG, "Sending receiveVideoFrom" + sessionDescription.type);
            publishVideoRequestId = ++Constants.id;
            String username = nbmPeerConnection.getConnectionId();
            videoRequestUserMapping.put(publishVideoRequestId, username);
            kurentoRoomAPI.sendReceiveVideoFrom(username, "webcam", sessionDescription.description, publishVideoRequestId);
        }
    }

    @Override
    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onIceCandidate callState:"+callState);
        int sendIceCandidateRequestId = ++Constants.id;
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
            kurentoRoomAPI.sendOnIceCandidate(this.username, iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
        } else {
            kurentoRoomAPI.sendOnIceCandidate(nbmPeerConnection.getConnectionId(), iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
        }
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onIceStatusChanged");
    }

    @Override
    public void onRemoteStreamAdded(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded");
        nbmWebRTCPeer.setActiveMasterStream(mediaStream);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("");
            }
        });
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamRemoved");
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError:" + s);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection) {
        Log.i(TAG, "[datachannel] Peer opened data channel");
    }

    @Override
    public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel) {

    }

    public void sendHelloMessage(DataChannel channel) {
        byte[] rawMessage = "Hello Peer!".getBytes(Charset.forName("UTF-8"));
        ByteBuffer directData = ByteBuffer.allocateDirect(rawMessage.length);
        directData.put(rawMessage);
        directData.flip();
        DataChannel.Buffer data = new DataChannel.Buffer(directData, false);
        channel.send(data);
    }

    @Override
    public void onStateChange(NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] DataChannel onStateChange: " + channel.state());
        if (channel.state() == DataChannel.State.OPEN) {
            sendHelloMessage(channel);
            Log.i(TAG, "[datachannel] Datachannel open, sending first hello");
        }
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] Message received: " + buffer.toString());
        sendHelloMessage(channel);
    }

    private Runnable offerWhenReady = new Runnable() {
        @Override
        public void run() {
            // Generate offers to receive video from all peers in the room
            Map<String, Boolean> publishedMap = WebRtcPeerManager.getInstance().getPeerPublishStatusMap();
            for (Map.Entry<String, Boolean> entry : publishedMap.entrySet()) {
                if (entry.getValue()) {
                    GenerateOfferForRemote(entry.getKey());
                    Log.i(TAG, "I'm " + username + " DERP: Generating offer for peer " + entry.getKey());
                    // Set value to false so that if this function is called again we won't
                    // generate another offer for this user
                    entry.setValue(false);
                }
            }
        }
    };

//    @Override
//    public void onRoomResponse(RoomResponse response) {
//        Log.d(TAG, "OnRoomResponse:" + response);
//        int requestId =response.getId();
//
//        if (requestId == publishVideoRequestId){
//
//            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
//                                                            response.getValue("sdpAnswer").get(0));
//
//            // Check if we are waiting for publication of our own vide
//            if (callState == CallState.PUBLISHING){
//                callState = CallState.PUBLISHED;
//                nbmWebRTCPeer.processAnswer(sd, "local");
//                mHandler.postDelayed(offerWhenReady, 2000);
//
//            // Check if we are waiting for the video publication of the other peer
//            } else if (callState == CallState.WAITING_REMOTE_USER){
//                //String user_name = Integer.toString(publishVideoRequestId);
//                callState = CallState.RECEIVING_REMOTE_USER;
//                String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
//                nbmWebRTCPeer.processAnswer(sd, connectionId);
//            }
//        }
//
//    }
//
//    @Override
//    public void onRoomError(RoomError error) {
//        Log.e(TAG, "OnRoomError:" + error);
//    }

}