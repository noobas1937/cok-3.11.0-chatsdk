package com.elex.chatservice.view.kurento;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.SessionDescription;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.DanmuManager;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.kurento.Constants;
import com.elex.chatservice.model.kurento.LooperExecutor;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.model.kurento.room.KurentoRoomAPI;
import com.elex.chatservice.model.kurento.room.RoomError;
import com.elex.chatservice.model.kurento.room.RoomListener;
import com.elex.chatservice.model.kurento.room.RoomNotification;
import com.elex.chatservice.model.kurento.room.RoomResponse;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMMediaConfiguration;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMPeerConnection;
import com.elex.chatservice.model.kurento.webrtcpeer.NBMWebRTCPeer;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RealtimeVoiceService extends Service implements NBMWebRTCPeer.Observer, RoomListener
{

	private NBMWebRTCPeer			nbmWebRTCPeer;

	private Map<Integer, String>	videoRequestUserMapping;
	private int						publishVideoRequestId;

	private LooperExecutor			executor;
	private static KurentoRoomAPI	kurentoRoomAPI;
	private int						roomId		= 0;

	private Handler					mHandler	= null;
	private CallState				callState;

	private enum CallState
	{
		IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER
	}

	private RealtimeVoiceBinder danmuBinder = new RealtimeVoiceBinder();

	public class RealtimeVoiceBinder extends Binder
	{
		public RealtimeVoiceService getService()
		{
			return RealtimeVoiceService.this;
		}
	}

	public void startConnectVoiceRoom(Activity activity)
	{
		String rtcUrl = WebRtcPeerManager.getInstance().getWebRtcUrl();
		// if(StringUtils.isEmpty(rtcUrl))
		// rtcUrl = Constants.DEFAULT_SERVER;

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "executor start", executor.isAlive(), "kurentoRoomAPI == null", kurentoRoomAPI == null);
		if (kurentoRoomAPI == null)
			kurentoRoomAPI = new KurentoRoomAPI(executor, rtcUrl, this);
		else
		{
			if (executor != null)
				executor.requestStart();
		}
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "WebRtcUrl", rtcUrl, "kurentoRoomAPI.isWebSocketConnected()", kurentoRoomAPI.isWebSocketConnected(), "roomJoinRoomed",
				roomJoinRoomed);

		// // Load test certificate from assets
		// CertificateFactory cf;
		// try {
		// cf = CertificateFactory.getInstance("X.509");
		// InputStream caInput = new BufferedInputStream(getAssets().open("kurento_room_base64.cer"));
		// Certificate ca = cf.generateCertificate(caInput);
		// kurentoRoomAPI.addTrustedCertificate("ca", ca);
		// } catch (CertificateException e) {
		// e.printStackTrace();
		// }
		// catch (IOException e) {
		// e.printStackTrace();
		// }
		// kurentoRoomAPI.useSelfSignedCertificate(true);

		if (!kurentoRoomAPI.isWebSocketConnected())
		{
			kurentoRoomAPI.connectWebSocket();
		}
		else
		{
			if (!roomJoinRoomed)
				joinRoom();
		}
		if (videoRequestUserMapping == null)
			videoRequestUserMapping = new HashMap<Integer, String>();

		if (nbmWebRTCPeer == null)
		{
			NBMMediaConfiguration peerConnectionParameters = new NBMMediaConfiguration();
			nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, null, this);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "Initializing nbmWebRTCPeer...");
		}
		if (nbmWebRTCPeer != null && !nbmWebRTCPeer.isInitialized())
		{
			nbmWebRTCPeer.initialize();
			callState = CallState.PUBLISHING;
		}
	}

	private void createVoiceFloatWindow()
	{
		WebRtcPeerManager.getInstance().createVoiceFloatBall();
	}

	private void destoryVoiceFloatWindow()
	{
		WebRtcPeerManager.getInstance().destoryVoiceFloatWindow();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		return danmuBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		WebRtcPeerManager.getInstance().destoryVoiceFloatWindow();
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "DEFAULT_SERVER", Constants.DEFAULT_SERVER);
		super.onCreate();

		mHandler = new Handler();
		callState = CallState.IDLE;
		executor = new LooperExecutor();
		executor.requestStart();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		flags = START_STICKY;

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy()
	{
		exitRealtimeVoice();
		ServiceInterface.realtimeVoiceService = null;
		ServiceInterface.realtimeVoiceServiceConnected = false;
		super.onDestroy();
	}

	public void exitRealtimeVoice()
	{
		hungUp();
		executor.requestStop();
		destoryVoiceFloatWindow();
		roomJoinRoomed = false;
		kurentoRoomAPI.removeObserver(this);
		kurentoRoomAPI = null;
		if (ChatServiceController.getCurrentActivity() != null)
			ChatServiceController.getInstance().refreshRealtimeBtnText();
	}

	public void hungUp()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		try
		{
			endCall();
			if (kurentoRoomAPI.isWebSocketConnected())
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "leave room");
				WebRtcPeerManager.published = false;
				kurentoRoomAPI.sendLeaveRoom(roomId, UserManager.getInstance().getCurrentUserId());
				kurentoRoomAPI.disconnectWebSocket();
			}
			JniController.getInstance().excuteJNIVoidMethod("setGameMusicLower", new Object[] { Boolean.valueOf(false) });
			if (!SwitchUtils.mqttEnable)
				WebSocketManager.getInstance().leaveRealtimeVoiceRoom();
			else
				MqttManager.getInstance().leaveRealtimeVoiceRoom();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isInitialized()
	{
		return nbmWebRTCPeer != null && nbmWebRTCPeer.isInitialized();
	}

	@Override
	public void onRoomResponse(RoomResponse response)
	{
		// joinRoom response
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "callState", callState, "response", response);
		if (response == null)
			return;
		int requestId = response.getId();

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "requestId", requestId, "roomId", roomId, "publishVideoRequestId", publishVideoRequestId);

		Map<String, Boolean> userMap = response.getUsers();
		if (userMap != null)
			WebRtcPeerManager.getInstance().addPeer(userMap);

		if (requestId == roomId)
		{
			nbmWebRTCPeer.generateOffer(UserManager.getInstance().getCurrentUserId(), true);
		}
		else if (requestId == publishVideoRequestId)
		{

			SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
					response.getValue("sdpAnswer").get(0));

			// Check if we are waiting for publication of our own vide
			if (callState == CallState.PUBLISHING)
			{
				callState = CallState.PUBLISHED;
				WebRtcPeerManager.published = true;
				JniController.getInstance().excuteJNIVoidMethod("setGameMusicLower", new Object[] { Boolean.valueOf(true) });
				createVoiceFloatWindow();
				if (ChatServiceController.getCurrentActivity() != null)
					ChatServiceController.getInstance().refreshRealtimeBtnText();
				WebRtcPeerManager.getInstance().changePeerPublishStatus(UserManager.getInstance().getCurrentUserId(), true);
				WebRtcPeerManager.getInstance().updateFloatBallImage();
				nbmWebRTCPeer.processAnswer(sd, UserManager.getInstance().getCurrentUserId());
				mHandler.postDelayed(offerWhenReady, 2000);

				// Check if we are waiting for the video publication of the other peer
			}
			else if (callState == CallState.WAITING_REMOTE_USER)
			{
				// String user_name = Integer.toString(publishVideoRequestId);
				callState = CallState.RECEIVING_REMOTE_USER;
				String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
				nbmWebRTCPeer.processAnswer(sd, connectionId);
			}
		}

	}

	@Override
	public void onRoomError(RoomError error)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "error", error);
		if (error.getCode() == 104)
		{
			roomJoinRoomed = false;
			if (ChatServiceController.getRealtimeVoiceRoomSettingActivity() != null)
				ChatServiceController.getRealtimeVoiceRoomSettingActivity().hideProgress();
			kurentoRoomAPI.sendLeaveRoom(roomId, UserManager.getInstance().getCurrentUserId());
			MenuController.showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REALTIME_ROOM_JOIN_ERROR));
			// showFinishingError("Room error", "Username already taken");
		}
	}

	@Override
	public void onRoomNotification(RoomNotification notification)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "notification", notification);
		Map<String, Object> map = notification.getParams();

		// Somebody left the room
		if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_LEFT))
		{
			final String user = map.get("name").toString();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "user", user, "PARTICIPANT_LEFT");
			WebRtcPeerManager.getInstance().removePeer(user);
		}
		// Somebody joined the room
		else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_JOINED))
		{
			final String user = map.get("id").toString();
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "user", user, "PARTICIPANT_JOINED");
			// MainActivity.this.runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			// mTextMessageTV.setText(getString(R.string.participant_joined, user));
			// mHandler.removeCallbacks(clearMessageView);
			// mHandler.postDelayed(clearMessageView, 3000);
			// }
			// });
		}
		// Somebody in the room published their video
		else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED))
		{
			final String user = map.get("id").toString();
			WebRtcPeerManager.getInstance().changePeerPublishStatus(user, true);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "user", user, "published already", "notification", notification);
			mHandler.postDelayed(offerWhenReady, 2000);
		}
		else if (notification.getMethod().equals(RoomListener.METHOD_ICE_CANDIDATE))
		{
			String sdpMid = map.get("sdpMid").toString();
			int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
			String sdp = map.get("candidate").toString();
			IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

			if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED)
			{
				nbmWebRTCPeer.addRemoteIceCandidate(ic, UserManager.getInstance().getCurrentUserId());
			}
			else
			{
				nbmWebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString());
			}
		}

		// Somebody in the room published their video
		else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED))
		{

		}

	}

	@Override
	public void onRoomConnected()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (kurentoRoomAPI.isWebSocketConnected() && !roomJoinRoomed)
		{
			joinRoom();
		}
	}

	@Override
	public void onRoomDisconnected()
	{
		showFinishingError("Disconnected", "You have been disconnected from room.");
	}

	private void showFinishingError(String title, String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG);
		// new AlertDialog.Builder(MutiPeerVideoActivity.this)
		// .setTitle(title)
		// .setMessage(message)
		// .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// finish();
		// }
		// })
		// .setIcon(android.R.drawable.ic_dialog_alert)
		// .show();
	}

	private boolean roomJoinRoomed = false;

	private void joinRoom()
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		roomJoinRoomed = true;
		Constants.id++;
		roomId = Constants.id;
		String userName = UserManager.getInstance().getCurrentUserId();
		String roomName = UserManager.getInstance().getCurrentUser().allianceId;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "joinRoom userName:", userName, "roomName", roomName);
		if (kurentoRoomAPI.isWebSocketConnected())
		{
			// kurentoRoomAPI.sendLeaveRoom(roomId);
			kurentoRoomAPI.sendJoinRoom(userName, roomName, true, roomId);
		}
	}

	private void GenerateOfferForRemote(String remote_name)
	{
		nbmWebRTCPeer.generateOffer(remote_name, false);
		callState = CallState.WAITING_REMOTE_USER;
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "remote_name", remote_name);
	}

	/**
	 * Terminates the current call and ends activity
	 */
	private void endCall()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		callState = CallState.IDLE;
		try
		{
			if (nbmWebRTCPeer != null)
			{
				nbmWebRTCPeer.close();
				nbmWebRTCPeer = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onInitialize()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		nbmWebRTCPeer.enableVideo(false);
		// nbmWebRTCPeer.generateOffer("local", true);
	}

	public void enableAudio(boolean enable)
	{
		if (nbmWebRTCPeer != null)
			nbmWebRTCPeer.enableAudio(enable);
	}

	@Override
	public void onLocalSdpOfferGenerated(final SessionDescription sessionDescription, final NBMPeerConnection nbmPeerConnection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "callState", callState);
		if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED)
		{
			publishVideoRequestId = ++Constants.id;
			kurentoRoomAPI.sendPublishVideo(sessionDescription.description, false, publishVideoRequestId);
		}
		else
		{ // Asking for remote user video
			publishVideoRequestId = ++Constants.id;
			String username = nbmPeerConnection.getConnectionId();
			videoRequestUserMapping.put(publishVideoRequestId, username);
			kurentoRoomAPI.sendReceiveVideoFrom(username, "webcam", sessionDescription.description, publishVideoRequestId);
		}
	}

	@Override
	public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection)
	{
	}

	@Override
	public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "callState", callState);
		int sendIceCandidateRequestId = ++Constants.id;
		if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED)
		{
			kurentoRoomAPI.sendOnIceCandidate(UserManager.getInstance().getCurrentUserId(), iceCandidate.sdp,
					iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
		}
		else
		{
			kurentoRoomAPI.sendOnIceCandidate(nbmPeerConnection.getConnectionId(), iceCandidate.sdp,
					iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
		}
	}

	@Override
	public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, NBMPeerConnection nbmPeerConnection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "iceConnectionState", iceConnectionState, "nbmPeerConnection", nbmPeerConnection.getConnectionId());
		if (iceConnectionState == IceConnectionState.CLOSED)
		{
			WebRtcPeerManager.getInstance().removePeer(nbmPeerConnection.getConnectionId());
		}

	}

	@Override
	public void onRemoteStreamAdded(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "callState", callState);
		nbmWebRTCPeer.setActiveMasterStream(mediaStream);
		// To-do
	}

	@Override
	public void onRemoteStreamRemoved(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "callState", callState);
	}

	@Override
	public void onPeerConnectionError(String s)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "s", s);
	}

	@Override
	public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
	}

	@Override
	public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel)
	{

	}

	@Override
	public void onStateChange(NBMPeerConnection connection, DataChannel channel)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channel.state()", channel.state());
		if (channel.state() == DataChannel.State.OPEN)
		{
			// sendHelloMessage(channel);
			// Log.i(TAG, "[datachannel] Datachannel open, sending first hello");
		}
	}

	@Override
	public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "buffer.toString()", buffer.toString());
	}

	private Runnable offerWhenReady = new Runnable()
	{
		@Override
		public void run()
		{
			// Generate offers to receive video from all peers in the room
			Map<String, Boolean> publishedMap = WebRtcPeerManager.getInstance().getPeerPublishStatusMap();
			for (Map.Entry<String, Boolean> entry : publishedMap.entrySet())
			{
				if (entry.getValue())
				{
					String uid = entry.getKey();
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "offerWhenReady uid", uid);
					if (StringUtils.isNotEmpty(uid) && !uid.equals(UserManager.getInstance().getCurrentUserId()))
					{
						GenerateOfferForRemote(entry.getKey());
						// Set value to false so that if this function is called again we won't
						// generate another offer for this user
						entry.setValue(false);
					}
				}
			}
		}
	};

}
