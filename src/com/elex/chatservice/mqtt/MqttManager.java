package com.elex.chatservice.mqtt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.DanmuManager;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ColorFragment;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.DanmuInfo;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LatestHornMessage;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.NearByUserInfo;
import com.elex.chatservice.model.ParseResult;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.DanmuInfo.DanmuInfoBuilder;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.mqtt.MqttService.MqttBinder;
import com.elex.chatservice.net.StandaloneServerInfo;
import com.elex.chatservice.net.StandaloneServerInfoManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.NetworkUtil;
import com.elex.chatservice.util.SharePreferenceUtil;
import com.elex.chatservice.util.HeadPicUtil.MD5;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.emoj.EmojGroupEntity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MqttManager implements MqttConstants
{
	private static final String							TAG								= "MqttManager";

	private static MqttManager							mInstance						= null;
	private ExecutorService								mExecutor;
	/* 是否在客户端测试server的连接，并在下次登录时选择最快的连接 */
	private boolean										enableNetworkOptimization		= false;
	/* 测试结果更新的有效时间，秒为单位；注意此值配置在后台，得在登陆ws之后才能获取到，要与测试结果使用的有效时间区分开 */
	public long											networkOptimizationTimeout		= DEFAULT_TEST_RESULT_VALID_TIME;
	/* server测试的延迟时间(从onGetNewMsg开始)，秒为单位 */
	public long											networkOptimizationTestDelay	= 0;
	private CopyOnWriteArrayList<StandaloneServerInfo>	serversInfos					= null;
	private StandaloneServerInfo						currentServer;
	public MqttService									mqttService;
	public boolean										mqttServiceConnected			= false;
	private Map<String, String>							subMap;
	public static String								randomGroup						= "";
	public static String								randomRoomId					= "";
	private boolean										isJoinRoom						= false;
	private boolean										randomChatRoomDestoryed			= false;

	private static boolean								isInDragonObserverChatRoom		= false;
	private static boolean								isInDragonObserverDanmuRoom		= false;
	private static int									joinDragonObserverRoomStatus	= 0;

	private Timer										joinRoomTimer					= null;
	private TimerTask									joinRoomTimerTask				= null;
	private long										joinStartTime					= 0;

	private ServiceConnection							mqttConnection					= new ServiceConnection()
																						{

																							@Override
																							public void onServiceDisconnected(ComponentName name)
																							{
																								if (mqttService != null)
																									mqttService.stop();
																								mqttService = null;
																								mqttServiceConnected = false;
																							}

																							@Override
																							public void onServiceConnected(ComponentName name, IBinder service)
																							{
																								LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG,
																										"connected");
																								MqttBinder MqttBinder = (MqttBinder) service;
																								if (MqttBinder != null)
																								{
																									mqttService = MqttBinder.getService();
																									if (mqttService != null)
																									{
																										mqttServiceConnected = true;
																										mqttService.start();
																									}
																								}
																							}
																						};

	public void startMqttService()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (ChatServiceController.hostActivity == null)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if (ChatServiceController.hostActivity == null)
					return;
				LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "mqttServiceConnected", mqttServiceConnected);
				if (!(mqttServiceConnected && mqttService != null))
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
					Intent intent = new Intent(ChatServiceController.hostActivity, MqttService.class);
					ChatServiceController.hostActivity.bindService(intent, mqttConnection, Context.BIND_AUTO_CREATE);
				}
				else
				{
					if (mqttService != null)
					{
						if (!mqttService.isConnected())
							mqttService.start();
						else
							subMqtt();
					}

				}
			}
		});

	}

	public void unbindMqttService()
	{
		if (!SwitchUtils.mqttEnable)
			return;
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if (ChatServiceController.hostActivity == null)
					return;
				LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "mqttServiceConnected", mqttServiceConnected);
				if (mqttServiceConnected && mqttService != null && mqttConnection != null)
				{
					ChatServiceController.hostActivity.unbindService(mqttConnection);
				}
			}
		});
	}

	private MqttManager()
	{
		mExecutor = Executors.newFixedThreadPool(4);
		serversInfos = new CopyOnWriteArrayList<StandaloneServerInfo>();
		subMap = new HashMap<String, String>();
	}

	public static MqttManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (MqttManager.class)
			{
				if (mInstance == null)
					mInstance = new MqttManager();
			}
		}
		return mInstance;
	}

	public void init()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, TAG);
		if (!SharePreferenceUtil.checkSession())
			return;
		setUserInfo();
		initUser();
		setDevice();
		getNetworkOptimizationConfig();
		getServerListCommand();
		loadInitMsgs();
	}

	private void getNetworkOptimizationConfig()
	{
		sendCommand(GET_MQTT_NETWORK_COMMAND);
	}

	public void pullNewData()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, TAG);
		getNewMsgCommand();
		getNewUserMsgCommand();
		getRealtimeVoiceRoomInfo();
	}

	public void setUserInfo()
	{
		sendCommand(SET_USER_INFO_COMMAND, "info", getUserInfo());
	}

	private JSONObject getUserInfo()
	{
		try
		{
			JSONObject params = new JSONObject();
			long lastUpdateTime = (long) (UserManager.getInstance().getCurrentUser().lastUpdateTime) * 1000;
			params.put("userName", UserManager.getInstance().getCurrentUser().userName);
			params.put("lastUpdateTime", lastUpdateTime);
			// params.put("uid", UserManager.getInstance().getCurrentUserId());
			// params.put("lang", ConfigManager.getInstance().gameLang);
			return params;
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	public void initUser()
	{
		sendCommand(INIT_USER_COMMAND, "info", getCompleteUserInfo());
	}

	private JSONObject getCompleteUserInfo()
	{
		UserInfo user = UserManager.getInstance().getCurrentUser();
		JSONObject params = user.getUserJson();
		return params;
	}

	private boolean needInit = true;

	public void setDevice()
	{
		needInit = true;
		sendCommand(SET_DEVICE_COMMAND, "info", getDeviceInfo());
	}

	public void sendDevice()
	{
		if (!SharePreferenceUtil.checkSession())
			return;
		needInit = false;
		sendCommand(SET_DEVICE_COMMAND, "info", getDeviceInfo());
	}

	private JSONObject getDeviceInfo()
	{
		try
		{
			JSONObject params = new JSONObject();
			params.put("deviceName", android.os.Build.MODEL);
			params.put("appName", ChatServiceController.getApplicationName());
			params.put("serverNum", UserManager.getInstance().getCurrentUser().serverId);
			params.put("appVersion", ChatServiceController.getApplicationVersionName());
			params.put("deviceType", "android");
			params.put("deviceId", MailManager.getInstance().getDeviceId());
			params.put("gameLanguage", ConfigManager.getInstance().gameLang);
			params.put("systemVerson", android.os.Build.VERSION.RELEASE);
			return params;
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private int getContactModeByUserChatType(int type)
	{
		int contactMode = -1;
		if (type == USER_CHAT_MODE_NORMAL)
			contactMode = 0;
		else if (type == USER_CHAT_MODE_MOD)
			contactMode = 1;
		else if (type == USER_CHAT_MODE_DRIFTING_BOTTLE)
			contactMode = 2;
		else if (type == USER_CHAT_MODE_NEARBY)
			contactMode = 3;
		return contactMode;
	}

	private int getUserChatTypeByContactMode(int contactMode)
	{
		int type = 0;
		if (contactMode == 0)
			type = USER_CHAT_MODE_NORMAL;
		else if (contactMode == 1)
			type = USER_CHAT_MODE_MOD;
		else if (contactMode == 2)
			type = USER_CHAT_MODE_DRIFTING_BOTTLE;
		else if (contactMode == 3)
			type = USER_CHAT_MODE_NEARBY;
		return type;
	}

	private int getUserChatPostByMsgType(int msgType)
	{
		int post = USER_CHAT_MSG_TYPE_TEXT;
		if (msgType == MsgItem.MSG_TYPE_AUDIO)
			post = USER_CHAT_MSG_TYPE_AUDIO;
		else if (msgType == MsgItem.MSG_TYPE_EMOJ)
			post = MsgItem.MSG_TYPE_EMOJ;
		return post;
	}

	public void sendUserMsg(String fromUid, int sendLocalTime, String msg, int contactMode, int msgType)
	{
		int type = getUserChatTypeByContactMode(contactMode);
		int post = getUserChatPostByMsgType(msgType);
		sendCommand(SEND_USER_MSG_COMMAND, "channelType", DBDefinition.CHANNEL_TYPE_USER, "sendTime", sendLocalTime, "uid", fromUid, "msg", msg, "type", type, "post", post);
	}

	public void sendNearbyLikeMsg(String fromUid, int sendLocalTime, String msg)
	{
		int type = USER_CHAT_MODE_NEARBY;
		int post = USER_CHAT_MSG_TYPE_TEXT;

		JSONObject extra = null;
		try
		{
			extra = new JSONObject();
			extra.put("post", MsgItem.MSG_TYPE_NEARBY_LIKE);
			extra.put("dialog", LanguageKeys.NEARBY_LIKE_TEXT);

			if (ChatServiceController.newLastUpdateTime > 0)
			{
				long lastUpdateTime = (long) ChatServiceController.newLastUpdateTime * 1000;
				extra.put("lastUpdateTime", lastUpdateTime);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		sendCommand(SEND_USER_MSG_COMMAND, "channelType", DBDefinition.CHANNEL_TYPE_USER, "sendTime", sendLocalTime, "uid", fromUid, "msg", msg, "type", type, "post", post, "extra", extra);
	}

	public void uploadLocation(String logitude, String latitude)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "logitude", logitude, "latitude", latitude);
		sendCommand(UPLOAD_LOCATION, "longitude", logitude, "latitude", latitude);
	}

	public void clearLocation()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, TAG);
		sendCommand(CLEAR_LOCATION);
	}

	public void leaveAllianceRoom()
	{
		leaveRoom(getAllianceRoomId());
	}

	public void leaveCountryRoom()
	{
		leaveRoom(getCountryRoomId());
	}

	private void leaveRoom(String roomId)
	{
		sendCommand(LEAVE_ROOM_COMMAND, "roomId", roomId);
		unscribeTopic(roomId);
	}

	private void unscribeTopic(String roomId)
	{
		if (StringUtils.isEmpty(roomId))
			return;
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "roomId", roomId);
		if (subMap.containsKey(roomId))
		{
			String subTopic = subMap.get(roomId);
			if (StringUtils.isNotEmpty(subTopic))
				unsubscribe(subTopic);
		}
	}

	public void leaveOriginalCountryRoom()
	{
		leaveRoom(getRoomIdPrefix() + GROUP_ORIGINAL + "_" + getOriginalCountryId());
	}

	public boolean isInRandomRoom()
	{
		return StringUtils.isNotEmpty(randomGroup) && StringUtils.isNotEmpty(randomRoomId);
	}

	public void leaveCurrentRandomRoom()
	{
		if (isInRandomRoom())
		{
			leaveRoom(randomRoomId);
			// sendCommand(LEAVE_ROOM_COMMAND, "group", randomGroup, "roomId", randomRoomId);
		}
	}

	public void sendDanmuMsg(String messageText, int colorIndex, int sendLocalTime)
	{
		JSONObject extra = null;
		try
		{
			extra = new JSONObject();
			extra.put("colorIndex", colorIndex);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		sendCommand(SEND_ROOM_MSG_COMMAND, "group", GROUP_DRAGON_DANMU, "roomId", ChatServiceController.dragonObserverDanmuRoomId,
				"type", 1, "msg", messageText, "sendTime", sendLocalTime, "extra", extra);
	}

	public void sendChatMsgFromGame(String messageText, int sendLocalTime, int channelType, JSONObject extra)
	{
		String roomId = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			roomId = getCountryRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			roomId = getAllianceRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			roomId = getBattleFieldRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			roomId = randomRoomId;
		sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channelType, "roomId", roomId, "type", 1, "extraRoom", getExtraRoomId(channelType), "msg", messageText, "sendTime", sendLocalTime, "extra",
				extra);
	}

	public void sendRoomSysMsg(String messageText, int sendLocalTime, int channelType, int post, String media)
	{
		String roomId = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			roomId = getCountryRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			roomId = getAllianceRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			roomId = getBattleFieldRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			roomId = randomRoomId;
		sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channelType, "roomId", roomId, "type", 1, "extraRoom", getExtraRoomId(channelType), "msg", messageText, "sendTime", sendLocalTime, "extra",
				getMsgExtra(post, media));
	}

	public void sendRoomMsg(String messageText, int sendLocalTime, ChatChannel channel)
	{
		sendRoomMsg(messageText, sendLocalTime, channel, MsgItem.MSGITEM_TYPE_MESSAGE, null);
	}

	public void sendRoomMsgWithAt(String messageText, int sendLocalTime, ChatChannel channel, int post, String media, String atUids)
	{
		String roomId = "";
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			roomId = getCountryRoomId();
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			roomId = getAllianceRoomId();
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			roomId = getBattleFieldRoomId();
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			roomId = randomRoomId;
		if (post == MsgItem.MSG_TYPE_AUDIO && StringUtils.isNotEmpty(media))
			sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "roomId", roomId, "type", 2, "extraRoom", getExtraRoomId(channel.channelType), "msg", messageText, "sendTime",
					sendLocalTime, "extra", getMsgExtra(post, media));
		else
		{
			if (channel.isCountryOrAllianceChannel())
			{
				JSONObject extraJson = getMsgExtra(post, media);
				try
				{
					if (extraJson == null)
						extraJson = new JSONObject();
					if (StringUtils.isNotEmpty(atUids))
					{
						JSONArray atJson = JSON.parseArray(atUids);
						extraJson.put("atUids", atJson);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "roomId", roomId, "extraRoom", getExtraRoomId(channel.channelType), "msg", messageText, "sendTime",
						sendLocalTime, "extra", extraJson);
			}
			else
				sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "roomId", roomId, "extraRoom", getExtraRoomId(channel.channelType), "msg", messageText, "sendTime",
						sendLocalTime, "extra", getMsgExtra(post, media));
		}
	}

	public void sendRoomMsg(String messageText, int sendLocalTime, ChatChannel channel, int post, String media)
	{
		String roomId = "";
		String group = "";
		if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			roomId = getCountryRoomId();
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			roomId = getAllianceRoomId();
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			group = getBattleFieldGroup();
			roomId = getBattleFieldRoomId();
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			roomId = randomRoomId;
		if (post == MsgItem.MSG_TYPE_AUDIO && StringUtils.isNotEmpty(media))
			sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "group", group, "roomId", roomId, "type", 2, "extraRoom",
					getExtraRoomId(channel.channelType), "msg", messageText, "sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
		else
		{
			sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "group", group, "roomId", roomId, "extraRoom",
					getExtraRoomId(channel.channelType), "msg", messageText, "sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
		}
	}

	public String getExtraRoomId(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && ChatServiceController.kingdomBattleEnemyServerId != -1 && !ChatServiceController.canEnterDragonObserverRoom())
			return getRoomIdPrefix() + GROUP_COUNTRY + "_" + ChatServiceController.kingdomBattleEnemyServerId;
		return "";
	}

	private JSONObject getMsgExtra(int post, String media)
	{
		JSONObject extra = null;
		try
		{
			extra = new JSONObject();
			if (post != MsgItem.MSGITEM_TYPE_MESSAGE)
				extra.put("post", post);
			if (StringUtils.isNotEmpty(media))
			{
				if (post == MsgItem.MSG_TYPE_NEW_BATTLE_REPORT)
				{
					JSONObject shareObj = JSON.parseObject(media);
					extra.put("shareExtra", shareObj);
				}
				else
					extra.put("media", media);
			}

			if (ChatServiceController.newLastUpdateTime > 0)
			{
				long lastUpdateTime = (long) ChatServiceController.newLastUpdateTime * 1000;
				extra.put("lastUpdateTime", lastUpdateTime);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return extra;
	}

	private void getServerListCommand()
	{
		sendCommand(GET_SERVER_LIST_COMMAND);
	}

	private void getSubListCommand()
	{
		sendCommand(GET_SUB_LIST_COMMAND, "rooms", getJoinRooms());
	}

	private void getNewMsgCommand()
	{
		sendCommand(GET_NEW_MSGS_COMMAND, "rooms", getRoomsParams(), "gzip", true);
	}

	private void getNewUserMsgCommand()
	{
		if (!(ConfigManager.useWebSocketServer && (ConfigManager.pm_standalone_read || (!ConfigManager.pm_standalone_read && MailManager.nearbyEnable))))
			return;
		long time = ChannelManager.getInstance().getLatestUserChatTime();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG, "time", TimeManager.getTimeInMS(time));
		sendCommand(GET_NEW_USERCHAT_BY_TIME_COMMAND, "time", TimeManager.getTimeInMS(time), "gzip", true);
	}

	private JSONObject getRoomsParams()
	{
		JSONObject params = null;
		try
		{
			params = new JSONObject();
			ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>(); // ChannelManager.getInstance().getNewServerChannels()
			channels.add(ChannelManager.getInstance().getCountryChannel());
			channels.add(ChannelManager.getInstance().getAllianceChannel());
			if (ChatServiceController.isBattleChatEnable)
				channels.add(ChannelManager.getInstance().getBattleFieldChannel());
			for (int i = 0; i < channels.size(); i++)
			{
				ChatChannel channel = channels.get(i);
				if (channel == null)
					continue;

				if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "latestTime", TimeManager.getTimeInMS(channel.getLatestTime()));
					params.put(getCountryRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
				{
					params.put(getAllianceRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
				}
				else if (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				{
					params.put(getBattleFieldRoomId(), TimeManager.getTimeInMS(channel.getLatestTime()));
				}
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return params;
	}

	public String getRoomId(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			return getCountryRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			return getAllianceRoomId();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			return getBattleFieldRoomId();
		return "";
	}

	private String getJoinRooms()
	{
		StringBuilder roomBuilder = new StringBuilder();
		try
		{
			String countryRoom = getCountryRoomId();
			if (StringUtils.isNotEmpty(countryRoom))
			{
				roomBuilder.append(countryRoom);
				roomBuilder.append(",");
			}

			if (ChatServiceController.isBattleChatEnable)
			{
				String battleRoom = getBattleFieldRoomId();
				if (StringUtils.isNotEmpty(battleRoom))
				{
					roomBuilder.append(battleRoom);
					roomBuilder.append(",");
				}
			}

			if (UserManager.getInstance().isCurrentUserInAlliance())
			{
				String allianceRoom = getAllianceRoomId();
				if (StringUtils.isNotEmpty(allianceRoom))
				{
					roomBuilder.append(allianceRoom);
					roomBuilder.append(",");
				}
			}

			if (ChatServiceController.canEnterDragonObserverDanmuRoom())
			{
				String danmuRoom = getDanmuRoomId();
				if (StringUtils.isNotEmpty(danmuRoom))
				{
					roomBuilder.append(danmuRoom);
					roomBuilder.append(",");
				}
			}

		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return roomBuilder.toString();
	}

	public void getRandomChatRoomId(String lang)
	{
		if (!ChatServiceController.randomChatEnable)
			return;
		leaveCurrentRandomRoom();
		isJoinRoom = true;
		if (ChatServiceController.getChatFragment() != null)
			ChatServiceController.getChatFragment().refreshRandomJoinTip(true, true);
		if (StringUtils.isNotEmpty(lang))
			sendCommand(GET_RANDOM_CHATROOM_LOCAL, "lang", lang);
		else
			sendCommand(GET_RANDOM_CHATROOM_GLOBAL);
	}

	public static String getCountryRoomId()
	{
		if (ChatServiceController.isBattleChatEnable)
			return getOriginalRoomIdPrefix() + GROUP_ORIGINAL + "_" + getOriginalCountryId();
		else
			return getOriginalRoomIdPrefix() + GROUP_COUNTRY + "_" + UserManager.getInstance().getCurrentUser().serverId;
	}

	private static String getAllianceRoomId()
	{
		return GROUP_ALLIANCE + "_" + getOriginalCountryId() + "_" + UserManager.getInstance().getCurrentUser().allianceId;
	}

	private static String getBattleFieldRoomId()
	{
		if (ChatServiceController.canJoinDragonRoom())
			return ChatServiceController.dragonRoomId;
		else if (ChatServiceController.canEnterDragonObserverRoom())
			return ChatServiceController.dragonObserverRoomId;
		else
			return getRoomIdPrefix() + GROUP_COUNTRY + "_" + UserManager.getInstance().getCurrentUser().serverId;
	}

	private static String getDanmuRoomId()
	{
		if (ChatServiceController.canEnterDragonObserverRoom())
			return ChatServiceController.dragonObserverDanmuRoomId;
		else
			return "";
	}

	private static String getBattleFieldGroup()
	{
		if (ChatServiceController.canJoinDragonRoom())
			return GROUP_COUNTRY;
		else if (ChatServiceController.canEnterDragonObserverRoom())
			return GROUP_DRAGON_OBSERVER;
		else
			return GROUP_COUNTRY;
	}

	private static int getOriginalCountryId()
	{
		return UserManager.getInstance()
				.isInBattleField() ? UserManager.getInstance().getCurrentUser().crossFightSrcServerId : UserManager.getInstance().getCurrentUser().serverId;
	}

	private static String getOriginalRoomIdPrefix()
	{
		if ((ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost() || ChatServiceController.isBetaVersion()))
		{
			return "test_";
		}
		else
		{
			return "";
		}
	}

	private static String getRoomIdPrefix()
	{
		if (ChatServiceController.isNotTestServer()
				&& (ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost() || ChatServiceController.isBetaVersion()))
		{
			return "test_";
		}
		else
		{
			return "";
		}
	}

	public void sendCommand(String command, Object... args)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			JSONObject params = null;
			if (args != null && args.length > 0)
			{
				params = new JSONObject();
				for (int i = 0; i < args.length; i += 2)
				{
					if ((i + 1) < args.length)
					{
						params.put((String) args[i], args[i + 1]);
					}
				}
			}
			actualSendCommand(command, params);
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void actualSendCommand(String command, JSONObject params) throws JSONException
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "command", command);
		JSONObject jsonobj = new JSONObject();
		jsonobj.put("cmd", command);
		if (params != null && params.containsKey("sendTime"))
		{
			jsonobj.put("sendTime", params.getIntValue("sendTime"));
			params.remove("sendTime");
		}
		else
		{
			long time = TimeManager.getInstance().getCurrentTimeMS();
			jsonobj.put("sendTime", time);
		}

		if (params != null)
			jsonobj.put("params", params);

		String output = String.format("%s: %s", command, jsonobj.toString());
		LogUtil.printVariables(Log.DEBUG, TAG_HTTP_SEND, output);

		sendCmdFromHttp(jsonobj.toJSONString());
	}

	public void sendCmdFromHttp(final String json)
	{
		Runnable cmdSendRunnable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
					HttpParams httpParams = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
					HttpConnectionParams.setSoTimeout(httpParams, 20000);
					HttpClient httpClient = new DefaultHttpClient(httpParams);
					HttpPost post = new HttpPost(MQTT_HTTP_URL);
					post.setEntity(new StringEntity(json, "UTF-8"));
					// String uid =
					// UserManager.getInstance().getCurrentUserId();
					// long time = TimeManager.getInstance().getCurrentTimeMS();
					// post.addHeader("APPID", APP_ID);
					// post.addHeader("TIME", String.valueOf(time));
					// post.addHeader("UID", uid);
					// post.addHeader("SIGN", calcSign(APP_ID, uid, time));
					String session = getSession();
					post.addHeader("SESSION", session);
					HttpResponse httpResponse = httpClient.execute(post);
					if (httpResponse.getStatusLine() != null)
					{
						int statusCode = httpResponse.getStatusLine().getStatusCode();
						if (statusCode != 200)
							return;
					}
					String responseStr = EntityUtils.toString(httpResponse.getEntity());
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "responseStr", responseStr);
					handleMessage(responseStr);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		mExecutor.execute(cmdSendRunnable);
	}

	private void handleMessage(String responseStr)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			JSONObject json = JSON.parseObject(responseStr);

			if (json.containsKey("code"))
			{
				int code = json.getIntValue("code");
				if (code == 1)
				{
					if (json.containsKey("data"))
					{
						JSONObject dataJson = json.getJSONObject("data");
						if (dataJson.containsKey("cmd"))
						{
							String command = dataJson.getString("cmd");
							String output = String.format("%s: %s", command, responseStr);
							LogUtil.printVariables(Log.DEBUG, TAG_HTTP_RECIEVE, output);
							if (dataJson.containsKey("result"))
							{
								onCommandSuccess(command, dataJson);
							}
						}
					}
					else
					{
						if (json.containsKey("result"))
						{
							if (json.containsKey("cmd"))
							{
								String command = json.getString("cmd");
								String output = String.format("%s: %s", command, responseStr);
								LogUtil.printVariables(Log.DEBUG, TAG_MQTT_RECIEVE, output);
								onCommandSuccess(command, json);
							}

						}
					}
				}
				else
				{
					LogUtil.printVariables(Log.ERROR, TAG_MQTT_ERROR_RESPONSE, "error", responseStr);
				}
			}
		}
		catch (JSONException e)
		{
			LogUtil.printVariables(Log.ERROR, TAG_MQTT_ERROR_RESPONSE, "JSONException: " + responseStr);
			LogUtil.printException(e);
		}
	}

	private void startJoinRoomTimer()
	{
		if (joinRoomTimer == null)
			joinRoomTimer = new Timer();
		joinStartTime = System.currentTimeMillis();
		joinRoomTimerTask = new TimerTask()
		{

			@Override
			public void run()
			{
				long curTime = System.currentTimeMillis();
				if (joinDragonObserverRoomStatus == 1 && curTime - joinStartTime > 30000)
				{
					joinDragonObserverRoomStatus = 2;
					if (ChatServiceController.getChatFragment() != null)
						ChatServiceController.getChatFragment().refreshDragonObserverJoinStatus();
					stopJoinRoomTimer();
				}
			}
		};

		if (joinRoomTimer != null)
			joinRoomTimer.scheduleAtFixedRate(joinRoomTimerTask, 0, 1000);
	}

	private void stopJoinRoomTimer()
	{
		if (joinRoomTimer != null)
		{
			joinRoomTimer.cancel();
			joinRoomTimer.purge();
			joinRoomTimer = null;
		}

		if (joinRoomTimerTask != null)
			joinRoomTimerTask.cancel();
	}

	public boolean isJoiningRandomRoom()
	{
		return isJoinRoom;
	}

	public boolean isRandomChatRoomDestoryed()
	{
		return randomChatRoomDestoryed;
	}

	public void setRandomChatRoomDestoryed(boolean randomChatRoomDestoryed)
	{
		this.randomChatRoomDestoryed = randomChatRoomDestoryed;
	}

	public void joinDragonObserverRoom()
	{
		if (!ChatServiceController.dragonObserverEnable)
			return;
		try
		{
			startJoinRoomTimer();
			joinDragonObserverRoomStatus = 1;
			sendCommand(GET_SUB_LIST_COMMAND, "rooms", ChatServiceController.dragonObserverRoomId);
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	public void likeNearbyUser(String uid)
	{
		if (!MailManager.nearbyLikeEnable)
			return;
		sendCommand(NEARBY_LIKE, "uid", uid);
		LogUtil.trackNearby("nearby_like");
	}

	public void cancelLikeNearbyUser(String uid)
	{
		if (!MailManager.nearbyLikeEnable)
			return;
		sendCommand(NEARBY_LIKE_CANCEL, "uid", uid);
		LogUtil.trackNearby("nearby_like_cancel");
	}

	public void joinDragonObserverRoomWrap()
	{
		if (!ChatServiceController.dragonObserverEnable)
			return;
		startJoinRoomTimer();
		joinDragonObserverRoomStatus = 1;
	}

	public static boolean isInDragonObserverRoom()
	{
		return ChatServiceController.canEnterDragonObserverRoom() && isInDragonObserverChatRoom;
	}

	public static boolean isInDragonObserverDanmuRoom()
	{
		return ChatServiceController.canEnterDragonObserverDanmuRoom() && isInDragonObserverDanmuRoom;
	}

	public static boolean joinDragonObserverRoomFailed()
	{
		return ChatServiceController.dragonObserverEnable && StringUtils.isNotEmpty(ChatServiceController.dragonObserverRoomId) && !isInDragonObserverRoom() && joinDragonObserverRoomStatus == 2;
	}

	public static boolean joinDragonObserverRooming()
	{
		return ChatServiceController.dragonObserverEnable && StringUtils.isNotEmpty(ChatServiceController.dragonObserverRoomId) && !isInDragonObserverRoom() && joinDragonObserverRoomStatus == 1;
	}

	public void leaveDragonObserverRoom()
	{
		if (!ChatServiceController.dragonObserverEnable)
			return;
		if (isInDragonObserverRoom())
			leaveRoom(ChatServiceController.dragonObserverRoomId);
		// sendCommand(LEAVE_ROOM_COMMAND, "group", GROUP_DRAGON_OBSERVER, "roomId", ChatServiceController.dragonObserverRoomId);
	}

	public void getRealtimeVoiceRoomInfo()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG);
		if (!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		int role = WebRtcPeerManager.getInstance().canControllerRole() ? 2 : 1;
		sendCommand(REALTIME_VOICE_JOIN_COMMAND, "roomId", UserManager.getInstance().getCurrentUser().allianceId, "role", role);
	}

	public void leaveRealtimeVoiceRoom()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG);
		if (!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		sendCommand(REALTIME_VOICE_LEAVE_COMMAND, "roomId", UserManager.getInstance().getCurrentUser().allianceId);
		String subKey = GROUP_VOIP + "_" + UserManager.getInstance().getCurrentUser().allianceId;
		if (subMap.containsKey(subKey))
		{
			String subValue = subMap.get(subKey);
			if (StringUtils.isNotEmpty(subValue))
			{
				unsubscribe(subValue);
			}
		}
	}

	public void changeRealtimeVoiceRoomRole(String speakers)
	{
		if (!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		sendCommand(REALTIME_VOICE_CHANGE_COMMAND, "roomId", UserManager.getInstance().getCurrentUser().allianceId,
				"speakers", speakers);
	}

	public void leaveDragonObserverDanmuRoom()
	{
		if (!ChatServiceController.dragonObserverDanmuEnable)
			return;
		if (isInDragonObserverDanmuRoom())
			leaveRoom(ChatServiceController.dragonObserverDanmuRoomId);
		// sendCommand(LEAVE_ROOM_COMMAND, "group", GROUP_DRAGON_DANMU, "roomId", ChatServiceController.dragonObserverDanmuRoomId);
	}

	private void onSetDevice(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("status") && result.getString("status").equals("ok") && needInit)
			pullNewData();
	}

	private void onGetNewworkConfig(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			if (result.containsKey("enableNetworkOptimization"))
			{
				enableNetworkOptimization = result.getBooleanValue("enableNetworkOptimization");
			}

			if (result.containsKey("networkOptimizationTimeout"))
			{
				networkOptimizationTimeout = result.getLongValue("networkOptimizationTimeout");
			}

			if (result.containsKey("networkOptimizationTestDelay"))
			{
				networkOptimizationTestDelay = result.getLongValue("networkOptimizationTestDelay");
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		testConfigLoaded = true;
	}

	private boolean	testConfigLoaded	= false;
	private boolean	serverListLoaded	= false;

	private boolean canTestServer()
	{
		return testConfigLoaded && serverListLoaded;
	}

	private boolean	isTestingServers	= false;
	private Timer	testServerTimer;

	private synchronized void testServers()
	{
		if (!enableNetworkOptimization || isTestingServers)
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);

		if (testServerTimer != null)
		{
			testServerTimer.cancel();
			testServerTimer.purge();
			testServerTimer = null;
		}

		isTestingServers = true;
		testServerTimer = new Timer();
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					NetworkUtil.testServerAndSaveResult(getServersInfosCopy(), null);
					isTestingServers = false;
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}

		};

		long delayMS = networkOptimizationTestDelay * 1000;
		testServerTimer.schedule(timerTask, delayMS == 0 ? (20 * 1000) : delayMS);
	}

	private void onGetServerList(JSONObject dataJson)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (serversInfos != null)
			serversInfos.clear();
		JSONArray result = dataJson.getJSONArray("result");
		if (result != null)
		{
			for (int i = 0; i < result.size(); i++)
			{
				JSONObject resultJson = result.getJSONObject(i);
				if (resultJson != null)
				{
					String protocol = "";
					if (resultJson.containsKey("protocol"))
						protocol = resultJson.getString("protocol");
					String ip = "";
					if (resultJson.containsKey("ip"))
						ip = resultJson.getString("ip");
					String port = "";
					if (resultJson.containsKey("port"))
						port = resultJson.getString("port");
					StandaloneServerInfo server = new StandaloneServerInfo(protocol, ip, port);
					serversInfos.add(server);
				}

			}
		}

		if (serversInfos!=null && serversInfos.size() > 0)
		{
			serverListLoaded = true;
			if (canTestServer())
				testServers();
		}

		getSubListCommand();
	}

	private void onGetSubList(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);

		boolean JoinRandom = false;
		if (result != null)
		{
			if (result.containsKey("personal"))
			{
				JSONObject personal = result.getJSONObject("personal");
				if (personal.containsKey(UserManager.getInstance().getCurrentUserId()))
					subMap.put(UserManager.getInstance().getCurrentUserId(), personal.getString(UserManager.getInstance().getCurrentUserId()));
			}

			if (result.containsKey("group"))
			{
				JSONObject group = result.getJSONObject("group");
				Set<String> keySet = group.keySet();
				for (String key : keySet)
				{
					if (StringUtils.isNotEmpty(key))
					{
						String value = group.getString(key);
						subMap.put(key, value);
						if (key.startsWith(GROUP_RANDOM_LOCAL) || key.startsWith(GROUP_RANDOM_GLOBAL))
						{
							JoinRandom = true;
							subscribe(value);
						}
						else if (key.startsWith(GROUP_DRAGON_OBSERVER))
						{
							if (ChatServiceController.dragonObserverEnable && key.equals(ChatServiceController.dragonObserverRoomId))
							{
								joinDragonObserverRoomStatus = 0;
								isInDragonObserverChatRoom = true;
								stopJoinRoomTimer();
								if (ChatServiceController.getChatFragment() != null)
								{
									ChatServiceController.getChatFragment().refreshDragonObserverJoinStatus();
									ChatServiceController.getChatFragment().refreshDrgonObserverChannelView();
								}
							}
						}
						else if (key.startsWith(GROUP_DRAGON_DANMU))
						{
							if (ChatServiceController.dragonObserverDanmuEnable && key.equals(ChatServiceController.dragonObserverDanmuRoomId))
							{
								isInDragonObserverDanmuRoom = true;
							}
						}
					}
				}
			}

			if (JoinRandom)
			{
				LocalConfig config = ConfigManager.getInstance().getLocalConfig();
				if (config == null)
					config = new LocalConfig();
				config.randomChannelId = randomRoomId;
				if (randomGroup.equals(GROUP_RANDOM_LOCAL))
					config.randomChatMode = 1;
				else if (randomGroup.equals(GROUP_RANDOM_GLOBAL))
					config.randomChatMode = 2;
				ConfigManager.getInstance().setLocalConfig(config);

				ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_RANDOM_CHAT, randomRoomId);
				if (channel != null)
				{
					String lang = ConfigManager.getInstance().gameLang;
					if (StringUtils.isEmpty(lang))
						lang = "en";
					String nameStr = LanguageManager.getLangByKey(LanguageKeys.BTN_LOCAL_CHATROOM, LanguageManager.getOriginalLangByKey(lang));
					if (randomGroup.equals(GROUP_RANDOM_LOCAL))
					{
						channel.customName = nameStr;
						channel.randomChatMode = 1;
					}
					else if (randomGroup.equals(GROUP_RANDOM_GLOBAL))
					{
						channel.customName = LanguageManager.getLangByKey(LanguageKeys.BTN_GLOBAL_CHATROOM);
						channel.randomChatMode = 2;
					}
					if (channel != null)
						ServiceInterface.sendChatLatestMessage(channel);
				}

				if (ChatServiceController.getChatFragment() != null)
					ChatServiceController.getChatFragment().refreshCustomChatChannel();
				if (ChatServiceController.getChatFragment() != null)
					ChatServiceController.getChatFragment().refreshRandomJoinTip(false, true);
			}
		}

		if (!JoinRandom)
			connectMqtt();
	}

	private void onGetNewMsg(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			JSONArray rooms = result.getJSONArray("rooms");
			for (int i = 0; i < rooms.size(); i++)
			{
				JSONObject room = rooms.getJSONObject(i);

				String roomId = room.getString("roomId");
				ChatChannel channel = getChannel(room.getString("group"), roomId);
				if (channel == null)
				{
					continue;
				}

				long firstMsgTime = room.getLongValue("firstMsgTime");
				long lastMsgTime = room.getLongValue("lastMsgTime");

				channel.serverMaxTime = lastMsgTime;
				channel.serverMinTime = firstMsgTime;

				JSONArray msgs = room.getJSONArray("msgs");

				channel.wsNewMsgCount = msgs.size();

				if (msgs.size() == 0)
				{
					channel.loadMoreMsg();
					continue;
				}

				MsgItem[] msgArr = new MsgItem[msgs.size()];
				MsgItem firstMsg = null;
				for (int j = 0; j < msgs.size(); j++)
				{
					JSONObject msg = msgs.getJSONObject(j);
					MsgItem item = parseMsgItem(msg);
					if (item != null)
					{
						msgArr[j] = item;

						if (firstMsg == null || item.createTime < firstMsg.createTime)
						{
							firstMsg = item;
						}
					}
				}

				// TODO 临时fix，让只有一条消息时（可能是新消息，大部分时候不是），不显示最新消息的标记
				if (msgs.size() > 1)
				{
					if (channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT)
					{
						firstMsg.firstNewMsgState = 1;
					}
					else
					{
						firstMsg.firstNewMsgState = 2;
					}
				}

				ServiceInterface.handleMessage(msgArr, roomId2channelId(roomId), "", true, true);
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void onGetNewUserMsg(JSONObject result)
	{
		if (!ConfigManager.useWebSocketServer)
			return;
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			Set<String> keySet = result.keySet();
			for (String key : keySet)
			{
				if (StringUtils.isEmpty(key))
					continue;
				if (result.containsKey(key))
				{
					JSONArray newMsgArr = result.getJSONArray(key);
					if (newMsgArr != null)
					{
						MsgItem[] dbItemsArray = new MsgItem[newMsgArr.size()];
						int count = 0;
						int type = 0;
						boolean isNewMsg = false;
						String channelId = key;
						for (int i = 0; i < newMsgArr.size(); i++)
						{
							JSONObject newMsgObj = newMsgArr.getJSONObject(i);
							MsgItem item = parseUserMsgItem(newMsgObj);
							if (item != null)
							{
								dbItemsArray[count++] = item;
								if (type == 0 && newMsgObj.containsKey("type"))
									type = newMsgObj.getIntValue("type");
								isNewMsg = isNewMsg ? true : item.isNewMsg;
							}
						}
						if (count > 0)
							handleUserChatMessage(type, channelId, dbItemsArray, isNewMsg, "");
					}
				}
			}

		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private String roomId2channelId(String roomId)
	{
		return roomId.substring(roomId.lastIndexOf("_") + 1);
	}

	private void onClearLocation(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("status") && result.getString("status").equals("ok"))
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "clear location sucess!");
			ServiceInterface.exitChatActivityFrom2dx(false);
			NearByManager.getInstance().setClearLocation(true);
			NearByManager.getInstance().setHasUploadLocation(false);
		}
	}

	private void onNearbyLikeOperated(String command, JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("status") && result.getBooleanValue("status"))
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "NEARBY_LIKE operate sucess!");
			String uid = "";
			if (result.containsKey("uids"))
				uid = result.getString("uid");

			if (command.equals(NEARBY_LIKE))
			{
				NearByManager.getInstance().updateNearbyLikeData(uid, NearByManager.NEARBY_LIKE);
				sendNearbyLikeMsg(uid, TimeManager.getInstance().getCurrentTime(), LanguageManager.getLangByKey(LanguageKeys.NEARBY_LIKE_TEXT));
			}
			else if (command.equals(NEARBY_LIKE_CANCEL))
				NearByManager.getInstance().updateNearbyLikeData(uid, NearByManager.NEARBY_LIKE_CANCEL);
		}
	}

	private void onUserChatOperated(String command, JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);

		if (result.containsKey("status") && result.getBooleanValue("status"))
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "CHAT_USER_ operate sucess!");
			String fromUids = "";
			if (result.containsKey("uids"))
				fromUids = result.getString("uids");
			int type = 0;
			if (result.containsKey("type"))
				type = result.getIntValue("type");
			int contactMode = getContactModeByUserChatType(type);

			if (command.equals(CHAT_USER_MARK_READ))
			{
				ServiceInterface.setMutiChatMailStatusByConfigType(fromUids, DBManager.CONFIG_TYPE_READ, contactMode);
			}
			else if (command.equals(CHAT_USER_DEL))
			{
				ServiceInterface.setMutiChatMailStatusByConfigType(fromUids, DBManager.CONFIG_TYPE_DELETE, contactMode);
			}
		}
	}

	private void onLeaveRoom(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("status") && result.getBoolean("status"))
		{
			if (result.containsKey("group"))
			{
				String group = result.getString("group");
				if (StringUtils.isNotEmpty(group))
				{
					if (group.equals(GROUP_RANDOM_LOCAL) || group.equals(GROUP_RANDOM_GLOBAL))
					{
						randomGroup = "";
						randomRoomId = "";
						if (ChatServiceController.getChatFragment() != null && !isJoinRoom)
							ChatServiceController.getChatFragment().refreshRandomTip();
					}
					else
					{
						ChatServiceController.dragonObserverRoomId = "";
						isInDragonObserverChatRoom = false;
					}
				}
			}

		}
	}

	private void onGetRandomChatRoom(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		isJoinRoom = false;
		if (!ChatServiceController.randomChatEnable)
			return;
		if (result.containsKey("group"))
		{
			String group = result.getString("group");
			if (StringUtils.isNotEmpty(group))
				randomGroup = group;
		}
		if (result.containsKey("id"))
		{
			String roomId = result.getString("id");
			if (StringUtils.isNotEmpty(roomId))
				randomRoomId = roomId;
		}
		LogUtil.printVariablesWithFuctionName(Log.INFO, TAG_MQTT_RECIEVE, "randomGroup", randomGroup, "randomRoomId", randomRoomId);
		if (StringUtils.isNotEmpty(randomGroup) && StringUtils.isNotEmpty(randomRoomId))
		{
			sendCommand(GET_SUB_LIST_COMMAND, "rooms", randomRoomId);
		}
		else
		{
			if (ChatServiceController.getChatFragment() != null)
				ChatServiceController.getChatFragment().refreshRandomJoinTip(true, false);
		}
	}

	private void onSendError(String errorCode, String sendLocalTime, JSONObject result)
	{
		long banTime = 0;
		if (result.containsKey("banTime"))
			banTime = result.getLong("banTime");
		String group = "";
		if (result.containsKey("group"))
			group = result.getString("group");
		String roomId = "";
		if (result.containsKey("roomId"))
			roomId = result.getString("roomId");
		ChatChannel channel = getChannel(group, roomId);
		if (channel != null && StringUtils.isNotEmpty(errorCode))
		{
			ServiceInterface.postChatSendError(channel.channelType, channel.channelID, sendLocalTime, errorCode, banTime, roomId);
		}
	}

	private void onSendUserMsg(JSONObject result, JSONObject dataJson)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		try
		{
			String sendTime = "";
			if (dataJson.containsKey("sendTime"))
				sendTime = dataJson.getString("sendTime");
			int sendLocalTime = Integer.parseInt(sendTime);
			onRecieveUserChatMessage(result, sendLocalTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void onSendRoomMsg(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result != null)
			onRecieveRoomMessage(result);
	}

	private void onUploadLocation(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("status") && result.getString("status").equals("ok"))
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "upload location sucess!");
			NearByManager.getInstance().setHasUploadLocation(true);
			getNearByUserList();
		}
	}

	public void getNearByUserList()
	{
		sendCommand(GET_NEARBY_USER);
	}

	public void readUserChat(int type, String uids)
	{
		sendCommand(CHAT_USER_MARK_READ, "type", type, "uids", uids);
	}

	public void deleteUserChat(int type, String uids)
	{
		sendCommand(CHAT_USER_DEL, "type", type, "uids", uids);
	}

	private void onGetNearbyUser(JSONObject result)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (result.containsKey("mylike"))
		{
			JSONArray mylikeArr = result.getJSONArray("mylike");
			if (mylikeArr != null)
			{
				List<String> uidList = new ArrayList<String>();
				for (int i = 0; i < mylikeArr.size(); i++)
				{
					String uid = mylikeArr.getString(i);
					if (StringUtils.isNotEmpty(uid) && !uidList.contains(uid))
						uidList.add(uid);
				}
				NearByManager.getInstance().initTodayLikeUid(uidList);
			}
		}

		if (result.containsKey("users"))
		{
			NearByManager.getInstance().setHasUploadLocation(true);
			NearByManager.getInstance().setHasSearchNearByUser(true);
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "get nearby user sucess!");
			JSONArray userArr = result.getJSONArray("users");
			for (int i = 0; i < userArr.size(); i++)
			{
				JSONObject userObj = userArr.getJSONObject(i);
				if (userObj != null)
				{
					String uid = "";
					if (userObj.containsKey("uid"))
						uid = userObj.getString("uid");
					LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "get nearby user:" + uid);

					double distance = 0;
					if (userObj.containsKey("dis"))
						distance = userObj.getDouble("dis");

					long lastLoginTime = 0;
					if (userObj.containsKey("time"))
						lastLoginTime = userObj.getLong("time");
					if (lastLoginTime <= 0)
						lastLoginTime = System.currentTimeMillis();

					int likes = 0;
					if (userObj.containsKey("likes"))
						likes = userObj.getIntValue("likes");

					String freshNewsText = null;
					List<ColorFragment> freshNewsColorList = null;
					if (userObj.containsKey("freshNews"))
					{
						JSONObject freshNewsJson = userObj.getJSONObject("freshNews");
						if (freshNewsJson != null)
						{
							ParseResult parseResult = parseTextFromExtra(freshNewsJson);
							freshNewsText = parseResult.getText();
							freshNewsColorList = parseResult.getColorExtraList();
						}
					}
					NearByUserInfo nearByUserInfo = new NearByUserInfo(uid, distance, lastLoginTime, freshNewsText, freshNewsColorList, likes);
					NearByManager.getInstance().addNearByUser(nearByUserInfo);
				}
			}
			NearByManager.getInstance().setClearLocation(false);
			if (ChatServiceController.getNearByListActivity() != null)
				ChatServiceController.getNearByListActivity().notifyDataSetChanged();
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "no user data!");
			ChatServiceController.getNearByListActivity().notifyDataSetChanged();
		}
	}

	private ParseResult parseTextFromExtra(JSONObject json)
	{
		ParseResult result = null;
		try
		{
			if (json != null && json.containsKey("useDialog") && json.containsKey("text"))
			{
				String text = json.getString("text");
				if (json.getIntValue("useDialog") == 1)
				{
					if (json.containsKey("dialogExtra"))
					{
						JSONArray dialogExtraArr = json.getJSONArray("dialogExtra");
						if (dialogExtraArr != null && dialogExtraArr.size() > 0)
						{
							List<ColorFragment> dialogArr = new ArrayList<ColorFragment>();
							for (int i = 0; i < dialogExtraArr.size(); i++)
							{
								JSONObject extra = dialogExtraArr.getJSONObject(i);
								if (extra != null)
								{
									String dialogExtra = "";
									if (extra.containsKey("type"))
									{
										int type = extra.getIntValue("type");
										if (type == 0 && extra.containsKey("text"))
										{
											dialogExtra = extra.getString("text");
										}
										else if (type == 1 && extra.containsKey("xmlId") && extra.containsKey("proName"))
										{
											dialogExtra = JniController.getInstance().excuteJNIMethod("getPropById",
													new Object[] { extra.getString("xmlId"), extra.getString("proName") });
										}
										else if (type == 2 && extra.containsKey("dialog"))
										{
											dialogExtra = LanguageManager.getLangByKey(extra.getString("dialog"));
										}
									}
									int color = 0;
									if (extra.containsKey("textColor"))
										color = ChatServiceController.parseColor(extra.getString("textColor"));
									boolean needLink = false;
									if (extra.containsKey("isLink"))
										needLink = extra.getBoolean("isLink");

									ColorFragment fragment = new ColorFragment();
									fragment.setDialogExtra(dialogExtra);
									fragment.setColor(color);
									fragment.setNeedLink(needLink);
									dialogArr.add(fragment);
								}
							}
							if (dialogArr.size() == 1)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);
								ColorFragment fragment = dialogArr.get(0);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra()));
							}
							else if (dialogArr.size() == 2)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);

								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(), fragment1.getDialogExtra()));
							}
							else if (dialogArr.size() == 3)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);

								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								ColorFragment fragment2 = dialogArr.get(2);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(), fragment1.getDialogExtra(), fragment2.getDialogExtra()));
							}
							else if (dialogArr.size() == 4)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);

								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								ColorFragment fragment2 = dialogArr.get(2);
								ColorFragment fragment3 = dialogArr.get(3);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(), fragment1.getDialogExtra(), fragment2.getDialogExtra(), fragment3.getDialogExtra()));
							}
						}
						else
						{
							result = new ParseResult();
							result.setColorExtraList(null);
							result.setText(LanguageManager.getLangByKey(text));
						}

					}
					else
					{
						result = new ParseResult();
						result.setColorExtraList(null);
						result.setText(LanguageManager.getLangByKey(text));
					}

				}
				else
				{
					result = new ParseResult();
					result.setColorExtraList(null);
					result.setText(text);
					;
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public void onRecieveMessage(String message)
	{
		try
		{
			JSONObject json = JSON.parseObject(message);
			String command = json.getString("cmd");
			JSONObject data = json.getJSONObject("data");

			if (command.equals(RECIEVE_USER_MSG_COMMAND))
			{
				onRecieveUserChatMessage(data, 0);
			}
			else if (command.equals(ANOTHER_LOGIN_COMMAND))
			{
				// 同一个uid在不同地方登陆会这样，发生这种情况游戏应该就不让登陆了
				ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_SERVER_DISCONNECTED);
				forceClose();
			}
			else if (command.equals(RECIEVE_ROOM_MSG_COMMAND))
			{
				boolean isSelf = false;
				if (data.containsKey("sender"))
				{
					String sender = data.getString("sender");
					isSelf = StringUtils.isNotEmpty(sender) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
							&& sender.equals(UserManager.getInstance().getCurrentUserId());
				}
				if (!isSelf)
					onRecieveRoomMessage(data);
			}
			else if (command.equals(ANOTHER_LOGIN_COMMAND))
			{
				// 同一个uid在不同地方登陆会这样，发生这种情况游戏应该就不让登陆了
				ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_SERVER_DISCONNECTED);
			}
			else if (command.equals(PUSH_RANDOM_CHATROOM_DESTORY))
			{
				onDetoryRandomChat(data);
			}
			else if (command.equals(PUSH_ROOM_MID_SYS_MSG))
			{
				onRecieveRoomMessage(data);
			}
			else if (command.equals(PUSH_EXEPRESSION_BUY))
			{
				onSubExpressionResponse(data);
			}
			else if (command.equals(PUSH_REALTIME_VOICE_CHANGE))
			{
				onRealtimeVoiceRoomRoleChanged(data);
			}

		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void onRealtimeVoiceRoomRoleChanged(JSONObject data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG);
		if (data == null || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		try
		{
			if (data.containsKey("roomId"))
			{
				String roomId = data.getString("roomId");
				if (!UserManager.getInstance().getCurrentUser().allianceId.equals(roomId))
					return;
			}

			if (data.containsKey("speakers"))
			{
				JSONArray speakers = data.getJSONArray("speakers");
				List<String> speakerList = new ArrayList<String>();
				for (int i = 0; i < speakers.size(); i++)
				{
					String speaker = speakers.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						speakerList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initSpeak(speakerList);
			}
			
			if (data.containsKey("all"))
			{
				JSONArray all = data.getJSONArray("all");
				List<String> allList = new ArrayList<String>();
				for (int i = 0; i < all.size(); i++)
				{
					String speaker = all.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						allList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initAll(allList);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	private void onSubExpressionResponse(JSONObject data)
	{
		if (data == null)
			return;
		if (data.containsKey("groupId") && data.containsKey("endTime"))
		{
			String groupId = data.getString("groupId");
			long endTime = data.getLong("endTime");
			EmojSubscribeManager.getInstance().addSubedEmojEntity(groupId, endTime);
			EmojSubscribeManager.getInstance().setEmojGroupDownLoadEntity();
			EmojGroupEntity emojGroupEntity = EmojSubscribeManager.getInstance().getEmojGroupEntity(groupId);
			if (emojGroupEntity != null)
				EmojSubscribeManager.getInstance().downEmojGroupResource(emojGroupEntity.getDownLoadEntity());

			if (ChatServiceController.getChatFragment() != null)
				ChatServiceController.getChatFragment().onEmojPanelChanged();
			else
				ChatFragmentNew.emojPanelChanged = true;
			if (ChatServiceController.getEmojSubscribActivity() != null)
			{
				ChatServiceController.getEmojSubscribActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						if (ChatServiceController.getEmojSubscribActivity() != null)
							ChatServiceController.getEmojSubscribActivity().notifyDataSetChanged();
					}
				});
			}
		}
	}

	private void onDetoryRandomChat(JSONObject data)
	{
		if (data == null || !ChatServiceController.randomChatEnable)
			return;
		if (data.containsKey("roomId"))
		{
			String roomId = data.getString("roomId");
			if (StringUtils.isNotEmpty(randomRoomId) && StringUtils.isNotEmpty(roomId) && randomRoomId.equals(roomId))
			{
				randomGroup = "";
				randomRoomId = "";
				randomChatRoomDestoryed = true;
				LocalConfig config = ConfigManager.getInstance().getLocalConfig();
				if (config == null)
					config = new LocalConfig();
				config.setCustomChannelType(-1);
				config.setCustomChannelId("");
				config.randomChannelId = "";
				config.randomChatMode = 0;
				LogUtil.trackAction("custom_channel_removed");
				ChatServiceController.getInstance().postLatestCustomChatMessage(null);
				if (ChatServiceController.getChatFragment() != null && !isJoinRoom)
					ChatServiceController.getChatFragment().refreshRandomDestoryTip();
			}
		}
	}

	private MsgItem parseUserMsgItem(JSONObject msg)
	{
		if (msg == null)
			return null;
		try
		{
			MsgItem item = new MsgItem();

			// 除了从db获取，都为true
			item.isNewMsg = true;
			item.uid = msg.getString("sender");

			if (msg.containsKey("read"))
			{
				try
				{
					if (!item.uid.equals(UserManager.getInstance().getCurrentUserId()) && msg.getBoolean("read"))
						item.isNewMsg = false;
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			item.sequenceId = -1;

			item.msg = "";
			if (msg.containsKey("content"))
				item.msg = msg.getString("content");
			try
			{
				// sendTime可能为字符串或long，不会出错，但预防性加个try
				if (item.uid.equals(UserManager.getInstance().getCurrentUserId()) && isStringExist(msg, "sendTime"))
				{
					item.sendLocalTime = Integer.parseInt(msg.getString("sendTime"));
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			item.createTime = TimeManager.getTimeInS(msg.getLong("time"));
			item.channelType = DBDefinition.CHANNEL_TYPE_USER;

			if (msg.containsKey("originalLang"))
				item.originalLang = msg.getString("originalLang");
			if (msg.containsKey("translationMsg"))
				item.translateMsg = msg.getString("translationMsg");

			item.mailId = "";
			if (msg.containsKey("seqid"))
			{
				item.mailId = msg.getString("seqid");
			}

			int type = 1;
			if (msg.containsKey("type"))
				type = msg.getIntValue("type");

			item.post = 0;
			if (msg.containsKey("post"))
			{
				int post = msg.getIntValue("post");
				if (post == USER_CHAT_MSG_TYPE_TEXT)
				{
					if (type == USER_CHAT_MODE_NORMAL || type == 20)
						item.post = 0;
					else if (type == USER_CHAT_MODE_MOD)
						item.post = MsgItem.MSG_TYPE_MOD;
					else if (type == USER_CHAT_MODE_DRIFTING_BOTTLE)
						item.post = MsgItem.MSG_TYPE_DRIFTING_BOTTLE;
				}
				else if (post == USER_CHAT_MSG_TYPE_AUDIO)
				{
					if (type == USER_CHAT_MODE_NORMAL || type == 20)
						item.post = MsgItem.MSG_TYPE_AUDIO;
					else if (type == USER_CHAT_MODE_MOD)
						item.post = MsgItem.MSG_TYPE_MOD_AUDIO;
					else
						item.post = MsgItem.MSG_TYPE_AUDIO;
				}
				else if (post == MsgItem.MSG_TYPE_EMOJ)
					item.post = post;
			}

			JSONObject extra = null;
			if (msg.containsKey("extra"))
			{
				extra = msg.getJSONObject("extra");

				if (extra.containsKey("seqId") && extra.getIntValue("seqId") > 0)
				{
					item.sequenceId = extra.getIntValue("seqId");
				}

				if (isStringExist(extra, "lastUpdateTime"))
				{
					String lastUpdateTimeStr = extra.getString("lastUpdateTime");
					if (StringUtils.isNotEmpty(lastUpdateTimeStr))
					{
						if (lastUpdateTimeStr.length() == 13)
						{
							long time = Long.parseLong(lastUpdateTimeStr);
							item.lastUpdateTime = (int) (time / 1000);
						}
						else if (lastUpdateTimeStr.length() <= 10)
						{
							item.lastUpdateTime = Integer.parseInt(lastUpdateTimeStr);
						}
					}
				}

				if (extra.containsKey("post"))
				{
					item.post = extra.getIntValue("post");
				}

				if (extra.containsKey("media"))
				{
					item.media = extra.getString("media");
				}

				try
				{
					parseAttachment(extra, item);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}

			if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
				item.sendState = MsgItem.SEND_SUCCESS;
			if (item.isNearbyLikeMsg())
				item.attachmentId = extra.toString();

			return item;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private void onRecieveUserChatMessage(JSONObject data, int sendLocalTime)
	{

		try
		{
			MsgItem item = parseUserMsgItem(data);
			if (item.sendLocalTime <= 0 && sendLocalTime > 0)
				item.sendLocalTime = sendLocalTime;

			if (item != null)
			{
				MsgItem[] dbItemsArray = { item };
				String customName = "";
				int type = 1;
				if (data.containsKey("type"))
					type = data.getIntValue("type");
				String channelId = "";
				if (data.containsKey("other"))
					channelId = data.getString("other");

				handleUserChatMessage(type, channelId, dbItemsArray, item.isNewMsg, customName);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private void handleUserChatMessage(int type, String channelId, MsgItem[] dbItemsArray, boolean isNewMsg, String customName)
	{

		if (!(type == USER_CHAT_MODE_NEARBY || (type != USER_CHAT_MODE_NEARBY && ConfigManager.pm_standalone_read))
				|| StringUtils.isEmpty(channelId)
				|| (type == USER_CHAT_MODE_DRIFTING_BOTTLE && !MailManager.isDriftingBottleEnable)
				|| (type == USER_CHAT_MODE_NEARBY && !MailManager.nearbyEnable))
			return;

		if (type == USER_CHAT_MODE_MOD && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
			channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
		else if (type == USER_CHAT_MODE_DRIFTING_BOTTLE && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
			channelId += DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE;
		else if (type == USER_CHAT_MODE_NEARBY && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
			channelId += DBDefinition.CHANNEL_ID_POSTFIX_NEARBY;
		ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER, channelId);
		if (channel != null)
			ServiceInterface.handleMessage(dbItemsArray, channelId, customName, isNewMsg, true);
	}

	private void onLeaveRealtimeVoiceRoom(JSONObject data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG);
		try
		{
			if (data.containsKey("id"))
			{
				String allianceId = data.getString("id");
				if (StringUtils.isNotEmpty(allianceId) && UserManager.getInstance().getCurrentUser() != null
						&& allianceId.equals(UserManager.getInstance().getCurrentUser().allianceId))
				{
					WebRtcPeerManager.getInstance().removeSpeak(UserManager.getInstance().getCurrentUserId());
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	private void onGetRealtimeVoiceRoomInfo(JSONObject data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG);
		try
		{
			if (data.containsKey("server"))
			{
				JSONObject server = data.getJSONObject("server");
				String protocol = "ws";
				String ip = "";
				String port = "";
				if (server.containsKey("protocol"))
					protocol = server.getString("protocol");
				if (server.containsKey("ip"))
					ip = server.getString("ip");
				if (server.containsKey("port"))
					port = server.getString("port");
				if (StringUtils.isNotEmpty(ip) && StringUtils.isNotEmpty(port))
				{
					WebRtcPeerManager.webRtcUrl = protocol + "://" + ip + ":" + port + "/room";
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, TAG, "webRtcUrl", WebRtcPeerManager.webRtcUrl);
					if (ChatServiceController.getChatActivity() != null)
					{
						ChatServiceController.getChatActivity().runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								if (ChatServiceController.getChatFragment() != null)
								{
									ChatServiceController.getChatFragment().refreshRealtimeBtnVisible();
								}
							}
						});
					}
				}
			}

			if (data.containsKey("speakers"))
			{
				JSONArray speakers = data.getJSONArray("speakers");
				List<String> speakerList = new ArrayList<String>();
				for (int i = 0; i < speakers.size(); i++)
				{
					String speaker = speakers.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						speakerList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initSpeak(speakerList);
			}
			
			if (data.containsKey("all"))
			{
				JSONArray all = data.getJSONArray("all");
				List<String> allList = new ArrayList<String>();
				for (int i = 0; i < all.size(); i++)
				{
					String speaker = all.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						allList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initAll(allList);
			}

			if (data.containsKey("mqtt_sub"))
			{
				String sub = data.getString("mqtt_sub");
				if (StringUtils.isNotEmpty(sub) && UserManager.getInstance().getCurrentUser() != null
						&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().allianceId))
				{
					subMap.put(GROUP_VOIP + "_" + UserManager.getInstance().getCurrentUser().allianceId, sub);
					subscribe(sub);
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * {"cmd":"push.chat.room","serverTime":1447749281156,
	 * "data":{"appId":"100001"
	 * ,"seqId":2,"sender":"909504798000489","roomId":"country_1",
	 * "msg":"vvvb","sendTime":1447749243505,"serverTime":1447749281155} }
	 */
	private void onRecieveRoomMessage(JSONObject data)
	{
		try
		{
			if (data.containsKey("group") && StringUtils.isNotEmpty(data.getString("group")) && data.getString("group").equals(GROUP_DRAGON_DANMU))
			{
				String msg = "";
				if (data.containsKey("msg"))
					msg = data.getString("msg");
				boolean isSelf = false;
				if (data.containsKey("sender"))
				{
					String sender = data.getString("sender");
					isSelf = StringUtils.isNotEmpty(sender) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
							&& sender.equals(UserManager.getInstance().getCurrentUserId());
				}
				int colorIndex = 0;
				if (data.containsKey("extra"))
				{
					JSONObject extra = data.getJSONObject("extra");
					if (extra.containsKey("colorIndex"))
						colorIndex = extra.getIntValue("colorIndex");
				}
				if (StringUtils.isNotEmpty(msg))
				{
					DanmuInfoBuilder danmuBuilder = new DanmuInfo.DanmuInfoBuilder(msg).colorIndex(colorIndex);
					if (isSelf)
						danmuBuilder.isSelf();
					DanmuInfo danmuInfo = danmuBuilder.build();
					DanmuManager.getInstance().addDanmu(danmuInfo);
				}
			}
			else
			{
				MsgItem item = parseMsgItem(data);
				if (item != null)
				{
					boolean randomChatRoomExist = false;
					LocalConfig config = ConfigManager.getInstance().getLocalConfig();
					if (config != null && config.randomChatMode > 0 && StringUtils.isNotEmpty(config.randomChannelId) && !isRandomChatRoomDestoryed())
						randomChatRoomExist = true;

					if (item.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT && (!ChatServiceController.randomChatEnable || !randomChatRoomExist))
						return;

					MsgItem[] dbItemsArray = { item };
					String customName = "";
					ChatChannel channel = getChannel(data.getString("group"), data.getString("roomId"));
					if (channel != null)
						ServiceInterface.handleMessage(dbItemsArray, channel.channelID, customName, false, true);

					if (item.isHornMessage() || item.isStealFailedMessage())
					{
						try
						{
							LatestHornMessage hornMsg = new LatestHornMessage();
							hornMsg.setAsn(item.getASN());
							hornMsg.setChannelType("" + item.channelType);
							hornMsg.setMsg(item.msg);
							hornMsg.setName(item.getName());
							hornMsg.setUid(item.uid);
							String latestHornMsg = JSON.toJSONString(hornMsg);
							LogUtil.printVariablesWithFuctionName(Log.INFO, TAG, "latestHornMsg", latestHornMsg);
							if (StringUtils.isNotEmpty(latestHornMsg))
								JniController.getInstance().excuteJNIVoidMethod("postNewHornMessage", new Object[] { latestHornMsg });
						}
						catch (com.alibaba.fastjson.JSONException e)
						{
							e.printStackTrace();
						}
					}

				}
			}

		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private ChatChannel getChannel(String group, String roomId)
	{
		return ChannelManager.getInstance().getChannel(group2channelType(group), roomId);
	}

	private static int group2channelType(String group)
	{
		if (StringUtils.isNotEmpty(group))
		{
			if (group.equals(GROUP_ORIGINAL))
				return DBDefinition.CHANNEL_TYPE_COUNTRY;
			else if (group.equals(GROUP_ALLIANCE))
				return DBDefinition.CHANNEL_TYPE_ALLIANCE;
			else if (group.equals(GROUP_COUNTRY))
			{
				if (ChatServiceController.isBattleChatEnable)
					return DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
				else
					return DBDefinition.CHANNEL_TYPE_COUNTRY;
			}
			else if (group.equals(GROUP_DRAGON_OBSERVER))
			{
				if (ChatServiceController.dragonObserverEnable)
					return DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
			}
			else if (group.equals(GROUP_DRAGON_DANMU))
			{
				if (ChatServiceController.dragonObserverDanmuEnable)
					return DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
			}
			else if (group.equals(GROUP_RANDOM_LOCAL) || group.equals(GROUP_RANDOM_GLOBAL))
			{
				return DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
			}
		}
		return -1;
	}

	public static boolean isStringExist(JSONObject obj, String key)
	{
		try
		{
			return obj.containsKey(key) && StringUtils.isNotEmpty(obj.getString(key));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private MsgItem parseMsgItem(JSONObject msg)
	{
		try
		{
			MsgItem item = new MsgItem();

			// 除了从db获取，都为true
			item.isNewMsg = true;

			item.sequenceId = -1;

			item.uid = msg.getString("sender");
			item.msg = msg.getString("msg");
			item.isSelfMsg = item.isSelfMsg();
			try
			{
				// sendTime可能为字符串或long，不会出错，但预防性加个try
				if (isStringExist(msg, "sendTime"))
				{
					item.sendLocalTime = TimeManager.getTimeInS(msg.getLong("sendTime"));
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			item.createTime = TimeManager.getTimeInS(msg.getLong("serverTime"));
			item.channelType = group2channelType(msg.getString("group"));

			if (msg.containsKey("originalLang"))
			{
				item.originalLang = msg.getString("originalLang");
			}
			if (msg.containsKey("translationMsg"))
			{
				item.translateMsg = msg.getString("translationMsg");
			}

			item.post = 0;
			item.mailId = "";

			JSONObject extra = null;
			if (msg.containsKey("extra"))
			{
				extra = msg.getJSONObject("extra");

				if (extra != null)
				{
					if (extra.containsKey("seqId") && extra.getIntValue("seqId") > 0)
					{
						item.sequenceId = extra.getIntValue("seqId");
					}

					if (extra.containsKey("atUids"))
					{
						item.atUids = extra.getString("atUids");
						item.parseInputAtList();
						if (item.isSelfMsg)
							item.readStatus = 1;
					}

					if (isStringExist(extra, "lastUpdateTime"))
					{
						try
						{
							String lastUpdateTimeStr = extra.getString("lastUpdateTime");
							if (StringUtils.isNotEmpty(lastUpdateTimeStr))
							{
								if (lastUpdateTimeStr.length() == 13)
								{
									long time = Long.parseLong(lastUpdateTimeStr);
									item.lastUpdateTime = (int) (time / 1000);
								}
								else if (lastUpdateTimeStr.length() <= 10)
								{
									item.lastUpdateTime = Integer.parseInt(lastUpdateTimeStr);
								}
							}

						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}

					if (extra.containsKey("post"))
					{
						item.post = extra.getIntValue("post");
					}
					else
					{
						item.post = 0;
					}

					if (extra.containsKey("media"))
					{
						item.media = extra.getString("media");
					}

					try
					{
						parseAttachment(extra, item);
						if (item.isEquipMessage())
						{
							if (extra.containsKey("attachmentId") && StringUtils.isNotEmpty(extra.getString("attachmentId")))
								item.msg = extra.getString("attachmentId");
						}
						else if (!item.isOldDialogMessage())
							item.attachmentId = extra.toString();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}

				}

			}

			if (msg.containsKey("senderInfo"))
			{
				JSONObject senderInfo = msg.getJSONObject("senderInfo");
				if (senderInfo != null && senderInfo.containsKey("lastUpdateTime"))
				{
					String lastUpdateTimeStr = senderInfo.getString("lastUpdateTime");
					if (StringUtils.isNotEmpty(lastUpdateTimeStr))
					{
						if (lastUpdateTimeStr.length() == 13)
						{
							long time = Long.parseLong(lastUpdateTimeStr);
							item.lastUpdateTime = (int) (time / 1000);
						}
						else if (lastUpdateTimeStr.length() <= 10)
						{
							item.lastUpdateTime = Integer.parseInt(lastUpdateTimeStr);
						}
					}

				}
			}

			if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
				item.sendState = MsgItem.SEND_SUCCESS;

			return item;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private void parseAttachment(JSONObject extra, MsgItem item) throws JSONException
	{
		// 联盟加入：allianceId
		if (extra.containsKey("allianceId"))
			item.attachmentId = extra.getString("allianceId");
		else if (extra.containsKey("reportUid"))
			item.attachmentId = extra.getString("reportUid");
		else if (extra.containsKey("detectReportUid"))
			item.attachmentId = extra.getString("detectReportUid");
		else if (extra.containsKey("equipId"))
			item.attachmentId = extra.getString("equipId");
		else if (extra.containsKey("teamUuid"))
			item.attachmentId = extra.getString("teamUuid");
		else if (extra.containsKey("lotteryInfo"))
			item.attachmentId = extra.getString("lotteryInfo");
		else if (extra.containsKey("redPackets") && extra.containsKey("server"))
			item.attachmentId = extra.getString("redPackets") + "_" + extra.getString("server");
		else if (extra.containsKey("attachmentId"))
			item.attachmentId = extra.getString("attachmentId");
	}

	private void onCommandSuccess(String cmd, JSONObject dataJson)
	{
		if (StringUtils.isEmpty(cmd) || dataJson == null)
			return;

		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "cmd", cmd);
		if (cmd.equals(GET_SERVER_LIST_COMMAND))
			onGetServerList(dataJson);
		else
		{
			JSONObject result = dataJson.getJSONObject("result");
			if (cmd.equals(SET_DEVICE_COMMAND))
				onSetDevice(result);
			else if (cmd.equals(GET_MQTT_NETWORK_COMMAND))
				onGetNewworkConfig(result);
			else if (cmd.equals(GET_SUB_LIST_COMMAND))
				onGetSubList(result);
			else if (cmd.equals(GET_NEW_MSGS_COMMAND))
				onGetNewMsg(result);
			else if (cmd.equals(GET_NEW_USERCHAT_BY_TIME_COMMAND))
				onGetNewUserMsg(result);
			else if (cmd.equals(GET_NEARBY_USER))
				onGetNearbyUser(result);
			else if (cmd.equals(UPLOAD_LOCATION))
				onUploadLocation(result);
			else if (cmd.equals(CLEAR_LOCATION))
				onClearLocation(result);
			else if (cmd.equals(LEAVE_ROOM_COMMAND))
				onLeaveRoom(result);
			else if (cmd.equals(GET_RANDOM_CHATROOM_LOCAL) || cmd.equals(GET_RANDOM_CHATROOM_GLOBAL))
				onGetRandomChatRoom(result);
			else if (cmd.equals(SEND_ROOM_MSG_COMMAND) || cmd.equals(SEND_USER_MSG_COMMAND))
			{
				if (dataJson.containsKey("code"))
				{
					String errorCode = dataJson.getString("code");
					String sendLocalTime = "";
					if (dataJson.containsKey("sendTime"))
						sendLocalTime = dataJson.getString("sendTime");
					onSendError(errorCode, sendLocalTime, result);
				}
				else if (cmd.equals(SEND_ROOM_MSG_COMMAND))
					onSendRoomMsg(result);
				else if (cmd.equals(SEND_USER_MSG_COMMAND))
					onSendUserMsg(result, dataJson);
			}
			else if (cmd.equals(CHAT_USER_MARK_READ) || cmd.equals(CHAT_USER_DEL))
				onUserChatOperated(cmd, result);
			else if (cmd.equals(NEARBY_LIKE) || cmd.equals(NEARBY_LIKE_CANCEL))
				onNearbyLikeOperated(cmd, result);
			else if (cmd.equals(REALTIME_VOICE_LEAVE_COMMAND))
				onLeaveRealtimeVoiceRoom(result);
			else if (cmd.equals(REALTIME_VOICE_JOIN_COMMAND))
				onGetRealtimeVoiceRoomInfo(result);
		}
	}

	private ArrayList<StandaloneServerInfo> getServersInfosCopy()
	{
		ArrayList<StandaloneServerInfo> servers = new ArrayList<StandaloneServerInfo>();
		for (Iterator<StandaloneServerInfo> iterator = serversInfos.iterator(); iterator.hasNext();)
		{
			servers.add((StandaloneServerInfo) iterator.next());
		}
		return servers;
	}

	public StandaloneServerInfo getCurrentServer()
	{
		if (currentServer == null)
		{
			LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
			currentServer = StandaloneServerInfoManager.getInstance().selectPrimaryServer(getServersInfosCopy());

			if (currentServer == null || !currentServer.isValid())
			{
				if (serversInfos != null && serversInfos.size() > 0)
					currentServer = serversInfos.get(0);
			}

			if (currentServer == null)
				currentServer = new StandaloneServerInfo(MQTT_PROTOCOL, MQTT_ADDRESS, MQTT_PORT);
		}
		return currentServer;
	}

	private void loadInitMsgs()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
		channels.add(ChannelManager.getInstance().getCountryChannel());
		channels.add(ChannelManager.getInstance().getAllianceChannel());
		channels.add(ChannelManager.getInstance().getAllianceSysChannel());
		channels.add(ChannelManager.getInstance().getBattleFieldChannel());
		for (int i = 0; i < channels.size(); i++)
		{
			ChatChannel channel = channels.get(i);
			if (channel != null && !channel.hasInitLoaded())
			{
				channel.loadMoreMsg();
			}
		}
	}

	public List<String> getSubList()
	{
		List<String> subList = new ArrayList<String>();
		if (subMap != null)
		{
			Collection<String> subCollection = subMap.values();
			if (subCollection != null)
			{
				for (String sub : subCollection)
				{
					LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "sub", sub);
					subList.add(sub);
				}
			}

		}
		return subList;
	}

	public String getConnectUserName()
	{
		return APP_ID + UserManager.getInstance().getCurrentUserId();
	}

	public String getConnectPassword()
	{
		String userName = getConnectUserName();
		String session = getSession();
		return MD5.stringMD5(userName + session);
	}

	public String getSession()
	{
		String session = SharePreferenceUtil.getSharePreferenceString(ChatServiceController.hostActivity,
				SharePreferenceUtil.getChatSessionTokenKey(), TEST_SESSION);
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "session", session);
		return session;
	}

	private void connectMqtt()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (isMqttClientConnected())
		{
			subMqtt();
		}
		else
		{
			getCurrentServer();
			if (currentServer == null)
				return;
			startMqttService();
		}
	}

	public void forceClose()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (mqttService != null && mqttServiceConnected)
		{
			mqttService.stop();
		}
		reset();
	}
	
	private void reset()
	{
		if(subMap!=null)
			subMap.clear();
	}

	public boolean isMqttClientConnected()
	{
		if (mqttService != null && mqttServiceConnected)
			return mqttService.isConnected();
		else
			return false;
	}

	private void subMqtt()
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG);
		if (mqttService != null && mqttServiceConnected)
		{
			mqttService.subscribe("");
		}
	}

	private void subscribe(String subTopic)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "subTopic", subTopic);
		if (mqttService != null && mqttServiceConnected)
		{
			mqttService.subscribe(subTopic);
		}
	}

	private void unsubscribe(String subTopic)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "subTopic", subTopic);
		if (mqttService != null && mqttServiceConnected)
		{
			mqttService.unsubscribe(subTopic);
		}
	}

	public void onMqttException(int ecode)
	{
		LogUtil.printVariablesWithFuctionName(Log.DEBUG, TAG, "ecode", ecode);
		if (ecode == MqttException.REASON_CODE_FAILED_AUTHENTICATION ||
				ecode == MqttException.REASON_CODE_NOT_AUTHORIZED)
		{
			SharePreferenceUtil.createSession();
		}
	}
}
