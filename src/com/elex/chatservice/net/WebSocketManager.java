package com.elex.chatservice.net;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.WsClient;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
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
import com.elex.chatservice.model.DanmuInfo.DanmuInfoBuilder;
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
import com.elex.chatservice.model.RoomGroupCmd;
import com.elex.chatservice.model.RoomInfoCommand;
import com.elex.chatservice.model.RoomInfoPush;
import com.elex.chatservice.model.RoomPushData;
import com.elex.chatservice.model.RoomResponseAll;
import com.elex.chatservice.model.RoomResponseCommand;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.ChatTable;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.util.AQUtility;
import com.elex.chatservice.util.HeadPicUtil.MD5;
import com.elex.chatservice.util.HttpRequestUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.LogUtilTest;
import com.elex.chatservice.util.NetworkUtil;
import com.elex.chatservice.util.RoomUtils;
import com.elex.chatservice.util.SharePreferenceUtil;
import com.elex.chatservice.view.ChatActivity;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.emoj.EmojGroupEntity;
import com.google.gson.Gson;

import android.text.TextUtils;
import android.util.Log;

public class WebSocketManager
{
	private final static String							APP_ID							= "100001";
	public final static String							WS_SERVER_LIST_URL				= "http://80.85.139.79/server/links";
	public final static String							WS_ALL_SERVER_LIST_URL			= "http://80.85.139.79/server/all";
	private final static StandaloneServerInfo			DEFAULT_SERVER					= new StandaloneServerInfo("ws", "80.85.139.79", "80");

	public static final String							GROUP_COUNTRY					= "country";
	public static final String							GROUP_ALLIANCE					= "alliance";
	public static final String							GROUP_ORIGINAL					= "original";
	private static final String							GROUP_RANDOM_LOCAL				= "local";
	private static final String							GROUP_RANDOM_GLOBAL				= "global";
	public static final String							GROUP_DRAGON_OBSERVER			= "observer";
	public static final String							GROUP_DRAGON_DANMU				= "danmu";

	private static final int							USER_CHAT_MSG_TYPE_TEXT			= 0;
	private static final int							USER_CHAT_MSG_TYPE_AUDIO		= 1;

	public static final int								USER_CHAT_MODE_NORMAL			= 1;
	public static final int								USER_CHAT_MODE_MOD				= 2;
	public static final int								USER_CHAT_MODE_DRIFTING_BOTTLE	= 3;
	public static final int								USER_CHAT_MODE_NEARBY			= 4;

	private static WebSocketManager						instance;

	private WsClient									client;
	private IWebSocketStatusListener					statusListener;
	private ScheduledExecutorService					getServerListService;
	private CopyOnWriteArrayList<StandaloneServerInfo>	serversInfos					= new CopyOnWriteArrayList<StandaloneServerInfo>();
	/* 是否在客户端测试server的连接，并在下次登录时选择最快的连接 */
	public boolean										enableNetworkOptimization		= false;
	/* 测试结果更新的有效时间，秒为单位；注意此值配置在后台，得在登陆ws之后才能获取到，要与测试结果使用的有效时间区分开 */
	public long											networkOptimizationTimeout		= DEFAULT_TEST_RESULT_VALID_TIME;
	/* 测试结果使用的默认有效时间 */
	public static final int								DEFAULT_TEST_RESULT_VALID_TIME	= 5 * 24 * 3600 * 1000;
	/* server测试的延迟时间(从onGetNewMsg开始)，秒为单位 */
	public long											networkOptimizationTestDelay	= 0;
	public boolean										connectAsSupervisor				= false;

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

	public static WebSocketManager getInstance()
	{
		if (instance == null)
		{
			instance = new WebSocketManager();
		}
		return instance;
	}

	protected WebSocketManager()
	{
	}

	public void setStatusListener(IWebSocketStatusListener listener)
	{
		statusListener = listener;
	}

	public IWebSocketStatusListener getStatusListener()
	{
		return statusListener;
	}

	private long startConnectTime;

	/**
	 * 应该只调一次，以后断线会自动触发重连
	 */
	public void connect()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "isWebSocketEnabled()", isWebSocketEnabled());
		if (!isWebSocketEnabled())
			return;
		if (getServerListService != null && !getServerListService.isShutdown())
			return;
		if (ChatServiceController.isValidateChat)
			return;

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "connectAsSupervisor", connectAsSupervisor);

		if (!connectAsSupervisor)
		{
			loadInitMsgs();
		}

		if (!SharePreferenceUtil.checkSession())
		{
			return;
		}

		// 连接状态时不重连，以免重登陆触发重连。但游戏真的重登陆时需要重新收取历史消息
		if (client != null && client.isOpen)
		{
			getNewMsgs();
			return;
		}

		if (statusListener == null)
		{
			statusListener = WebSocketStatusHandler.getInstance();
		}

		connect2ws();
		startGetServerList();
		startReconnectChecker();
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

	private void startGetServerList()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		if (getServerListService != null && !getServerListService.isShutdown())
			return;

		serversInfos = new CopyOnWriteArrayList<StandaloneServerInfo>();

		getServerListService = Executors.newSingleThreadScheduledExecutor();
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					getServerList();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		getServerListService.scheduleWithFixedDelay(timerTask, 100, 5000, TimeUnit.MILLISECONDS);
	}

	private void getServerList()
	{
		// statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST));
		// ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);

		String timeStr = Integer.toString(TimeManager.getInstance().getCurrentTime());
		String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3)) + MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
		String sign = MD5.stringMD5(APP_ID + UserManager.getInstance().getCurrentUserId() + secret);
		String serverListUrl = !connectAsSupervisor ? WS_SERVER_LIST_URL : WS_ALL_SERVER_LIST_URL;
		long callTime = TimeManager.getInstance().getCurrentTimeMS();
		String param = "t=" + timeStr + "&s=" + sign + "&a=" + APP_ID + "&u=" + UserManager.getInstance().getCurrentUserId() + "&ct=" + callTime +"&sid=" + UserManager.getInstance().getCurrentUser().serverId;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "getServerUrl", serverListUrl + "?" + param);
		statusListener.onConsoleOutput("getServerList: " + serverListUrl + "?" + param);
		String serverlistJson = HttpRequestUtil.sendGet(serverListUrl, param);
		if (!StringUtils.isEmpty(serverlistJson))
		{
			LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - callTime, LogUtil.CHAT_CONNECT_SERVER_LIST);
			onGetServerList(serverlistJson);
		}
		else
		{
			LogUtil.trackChatConnectTimeTime(-1, LogUtil.CHAT_CONNECT_SERVER_LIST);
		}

		if (serversInfos.size() == 0)
		{
			// statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST_ERROR));
			// ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTE_FAILED);
		}
		else
		{
			serverListLoaded = true;
			if (connectAsSupervisor)
			{
				testServerAsSupervisor();
			}
			else if (canTestServer())
			{
				testServers();
			}
			getServerListService.shutdown();
		}
	}

	private static int	connectionCount	= 0;
	private boolean		forceDisconnect	= false;

	private void connect2ws()
	{
		connectionCount++;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "connectionCount", connectionCount);

		if (client != null)
		{
			try
			{
				forceDisconnect = true;
				client.closeBlocking();
			}
			catch (InterruptedException e)
			{
				// TODO do something
				LogUtil.printException(e);
			}
			client = null;
		}

		if (client == null)
		{
			createClient();
		}

		if (client != null)
		{
			forceDisconnect = false;
			statusListener.onConsoleOutput("Connecting");
			statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECTING));
			ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
			client.connect();
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

	public static int		draftVersion	= -1;

	StandaloneServerInfo	currentServer;

	private void createClient()
	{
		try
		{
			currentServer = null;
			currentServer = StandaloneServerInfoManager.getInstance().selectPrimaryServer(getServersInfosCopy());

			if (currentServer == null || !currentServer.isValid())
			{
				currentServer = DEFAULT_SERVER;
			}

			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "connectAsSupervisor", connectAsSupervisor, "connecting server", currentServer);
			statusListener.onConsoleOutput("Connecting server: " + currentServer);

			startConnectTime = TimeManager.getInstance().getCurrentTimeMS();
			// Draft draft;
			// switch (draftVersion)
			// {
			// case 0:
			// draft = new Draft_10();
			// break;
			// case 1:
			// draft = new Draft_17();
			// break;
			// case 2:
			// draft = new Draft_75();
			// break;
			// case 3:
			// draft = new Draft_76();
			// break;
			//
			// default:
			// draft = new Draft_10();
			// break;
			// }
			// "ws://cokchat-s2.elexapp.com:8088"
			if (!connectAsSupervisor)
			{
				client = new WsClient(currentServer.protocol + "://" + currentServer.address + ":" + currentServer.port, getHeader(), this, statusListener);
			}
			else
			{
				client = new WsClient(currentServer.protocol + "://" + currentServer.address + ":" + currentServer.port + "/supervisor/100001", new HashMap<String, String>(), this, statusListener);
			}
		}
		catch (URISyntaxException e)
		{
			// TODO reconnect?
			e.printStackTrace();
		}
	}

	public Map<String, String> getHeader()
	{
		Map<String, String> header = new HashMap<String, String>();
		String uid = UserManager.getInstance().getCurrentUserId();
		long time = TimeManager.getInstance().getCurrentTimeMS();
		header.put("APPID", APP_ID);
		header.put("TIME", String.valueOf(time));
		header.put("UID", uid);
		header.put("SIGN", calcSign(APP_ID, uid, time));
		header.put("SESSION", SharePreferenceUtil.getSharePreferenceString(ChatServiceController.hostActivity, SharePreferenceUtil.getChatSessionTokenKey(), ""));
		return header;
	}

	/**
	 * 关闭连接，且不会自动重连
	 * 如果再由其它代码触发一次连接，会重置forceDisconnect的状态
	 */
	public void forceClose()
	{
		if (client != null)
		{
			forceDisconnect = true;
			closeClient();
		}
	}

	private void closeClient()
	{
		client.close();
		// 关闭连接需要时间（等待后台返回），但重连会马上触发，可能调到connect()的时候还没有成功关闭，就不会重连，所以手动设置flag
		client.isOpen = false;
	}

	private boolean forceReconnect = false;

	/**
	 * 仅在测试工具中使用
	 */
	public void forceReconnect()
	{
		forceReconnect = true;
		closeClient();
	}

	private int					reconnectRetryAvailableCount	= RECONNECT_MAX_RETRY;
	private final static int	RECONNECT_INTERVAL				= 5;
	private int					reconnectCountDown				= 0;
	private int					reconnectAdditionalInterval		= -5;

	public void resetReconnectInterval()
	{
		reconnectAdditionalInterval = -5;
	}

	private final static int			RECONNECT_MAX_RETRY	= 99;

	private ScheduledExecutorService	reconnectService;
	private TimerTask					reconnectTimerTask;

	public synchronized void onConnectError()
	{
		if (currentServer != null)
		{
			currentServer.lastErrorTime = TimeManager.getInstance().getCurrentTimeMS();
			StandaloneServerInfoManager.getInstance().updateLastErrorTime(currentServer);
		}

		if (connectAsSupervisor)
		{
			if (forceReconnect)
			{
				// cokChatTest中会触发重连
				statusListener.onConnectError();
				forceReconnect = false;
			}

			return;
		}

		synchronized (this)
		{
			if (reconnectCountDown == 0 && reconnectRetryAvailableCount >= 0 && !forceDisconnect)
			{
				if (reconnectAdditionalInterval < 10)
				{
					reconnectAdditionalInterval += 5;
				}
				reconnectCountDown = RECONNECT_INTERVAL + 1 + reconnectAdditionalInterval;
				// resetClientID();
			}
		}
	}

	/**
	 * 只会执行一次
	 */
	private synchronized void startReconnectChecker()
	{
		if (reconnectService != null)
		{
			return;
		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);

		reconnectService = Executors.newSingleThreadScheduledExecutor();
		reconnectTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					checkReconnect();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		reconnectService.scheduleWithFixedDelay(reconnectTimerTask, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	private synchronized void checkReconnect()
	{
		synchronized (this)
		{
			if (reconnectCountDown <= 0)
				return;

			reconnectCountDown--;
			if (reconnectCountDown <= 0 && !forceDisconnect)
			{
				statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_RECONNECTING));
				ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
				reconnectRetryAvailableCount--;
				resetState();
				// 不需要重新获取ServerList，现在是根据ip返回所有适合的服务器，重新获取不会改变结果
				connect2ws();
			}
			else
			{
				statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_CONNECT_FAIL, reconnectCountDown + ""));
				ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
			}
		}
	}

	public static final String bytesToHexString(byte[] bArray)
	{
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++)
		{
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	private JSONObject roomsParams;

	/**
	 * 为了防止在发送getNewMsg请求前收到push、改变时间戳，在连接成功后马上设置参数
	 */
	public void onOpen()
	{
		LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - startConnectTime, LogUtil.CHAT_CONNECT_CONNECT);
		roomsParams = getRoomsParams();
	}

	public void onLoginSuccess(JSONObject json)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		statusListener.onConsoleOutput("Login success");
		try
		{
			if (json.optBoolean("enableNetworkOptimization"))
			{
				enableNetworkOptimization = json.getBoolean("enableNetworkOptimization");
			}

			if (json.opt("networkOptimizationTimeout") != null)
			{
				networkOptimizationTimeout = json.optLong("networkOptimizationTimeout");
			}

			if (json.opt("networkOptimizationTestDelay") != null)
			{
				networkOptimizationTestDelay = json.optLong("networkOptimizationTestDelay");
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		testConfigLoaded = true;

		if (!connectAsSupervisor)
		{
			setUserInfo();
			initUser();
			setDevice();
		}
	}

	public void onLoginFailed(JSONObject json)
	{
		try
		{
			JSONObject data = json.getJSONObject("data");

			if (data.opt("error") != null && data.optString("error").equals("session error") && SharePreferenceUtil.createSessionCount < 3)
			{
				SharePreferenceUtil.createSession();
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private void testServerAsSupervisor()
	{
		enableNetworkOptimization = true;
		networkOptimizationTestDelay = 3;
		testServers();
	}

	private static String calcSign(String appid, String uid, long time)
	{
		return MD5.stringMD5(MD5.stringMD5(appid + uid) + time);
	}

	private final static String	SEND_USER_MSG_COMMAND				= "chat.user";
	private final static String	LOGIN_SUCCESS_COMMAND				= "login.success";
	private final static String	LOGIN_FAILED_COMMAND				= "login.failed";
	private final static String	RECIEVE_USER_MSG_COMMAND			= "push.chat.user";
	// private final static String JOIN_ROOM_COMMAND = "room.join";
	private final static String	JOIN_ROOM_MULTI_COMMAND				= "room.joinMulti";
	private final static String	LEAVE_ROOM_COMMAND					= "room.leave";
	private final static String	SEND_ROOM_MSG_COMMAND				= "chat.room";
	private final static String	RECIEVE_ROOM_MSG_COMMAND			= "push.chat.room";
	private final static String	SET_USER_INFO_COMMAND				= "user.setInfo";
	/** 用于http接口获取用户信息 */
	private final static String	INIT_USER_COMMAND					= "user.init";
	// private final static String GET_NEW_MSGS_COMMAND = "history.rooms";
	private final static String	GET_NEW_MSGS_BY_TIME_COMMAND		= "history.roomsv2";
	// private final static String GET_HISTORY_MSGS_COMMAND = "history.room";
	private final static String	GET_HISTORY_MSGS_BY_TIME_COMMAND	= "history.roomv2";
	private final static String	ANOTHER_LOGIN_COMMAND				= "another.login";
	private final static String	SET_DEVICE_COMMAND					= "user.setDevice";
	private final static String	GET_NEW_USERCHAT_BY_TIME_COMMAND	= "history.users";
	private final static String	GET_RANDOM_CHATROOM_LOCAL			= "room.getLocalRoom";
	private final static String	GET_RANDOM_CHATROOM_GLOBAL			= "room.getGlobalRoom";
	private final static String	PUSH_RANDOM_CHATROOM_DESTORY		= "push.room.change";
	private final static String	PUSH_ROOM_MID_SYS_MSG				= "push.room.midSystemMsg";
	private final static String	UPLOAD_LOCATION						= "user.setPosition";
	private final static String	CLEAR_LOCATION						= "user.delPosition";
	private final static String	GET_NEARBY_USER						= "user.getNearBy";
	private final static String	CHAT_USER_MARK_READ					= "chat.userMarkRead";
	private final static String	CHAT_USER_DEL						= "chat.userDel";
	private final static String	NEARBY_LIKE							= "like.add";
	private final static String	NEARBY_LIKE_CANCEL					= "like.del";
	private final static String	PUSH_EXEPRESSION_BUY				= "push.player.expression";
	private final static String	REALTIME_VOICE_JOIN_COMMAND			= "voip.join";
	private final static String	REALTIME_VOICE_LEAVE_COMMAND		= "voip.leave";
	private final static String	REALTIME_VOICE_CHANGE_COMMAND		= "voip.change";
	private final static String	PUSH_REALTIME_VOICE_CHANGE			= "push.voip.change";

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
		JSONObject params = user.getJson();
		return params;
	}

	public void sendUserMsg(String fromUid, int sendLocalTime, String msg, int contactMode, int msgType)
	{
		int type = getUserChatTypeByContactMode(contactMode);
		int post = getUserChatPostByMsgType(msgType);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, DBDefinition.CHANNEL_TYPE_USER+msg);
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

	public void setDevice()
	{
		needInit = true;
		sendCommand(SET_DEVICE_COMMAND, "info", getDeviceInfo());
	}

	private boolean needInit = true;

	public void sendDevice()
	{
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

	public void onSetDevice()
	{
		joinRoom();
	}

	private String	rooms	= "";
	private boolean	roomsChanged;

	public void joinRoom()
	{
		JSONArray roomsArr = getJoinRoomsArray();
		if (!rooms.equals(roomsArr.toString()))
		{
			rooms = roomsArr.toString();
			roomsChanged = true;
		}
		else
		{
			roomsChanged = false;
		}
		sendCommand(JOIN_ROOM_MULTI_COMMAND, "rooms", roomsArr);
	}

	private void onJoinRoom()
	{
		statusListener.onStatus("");
		ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTED);
		LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - startConnectTime, LogUtil.CHAT_CONNECT_TOTAL);
		// if (roomsChanged)
		// {
		getNewMsgs();
		// }
	}

	private void resetState()
	{
		rooms = "";
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

	public String getExtraRoomId(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && ChatServiceController.kingdomBattleEnemyServerId != -1 && !ChatServiceController.canEnterDragonObserverRoom())
			return getRoomIdPrefix() + GROUP_COUNTRY + "_" + ChatServiceController.kingdomBattleEnemyServerId;
		return "";
	}

	private JSONArray getJoinRoomsArray()
	{
		JSONArray array = null;
		try
		{
			array = new JSONArray();

			if (ChatServiceController.isBattleChatEnable)
			{
				JSONObject country = new JSONObject();
				country.put("id", getCountryRoomId());
				country.put("group", GROUP_ORIGINAL);
				array.put(country);

				JSONObject battleField = new JSONObject();
				battleField.put("id", getBattleFieldRoomId());
				battleField.put("group", getBattleFieldGroup());
				array.put(battleField);
			}
			else
			{
				JSONObject country = new JSONObject();
				country.put("id", getCountryRoomId());
				country.put("group", GROUP_COUNTRY);
				array.put(country);
			}

			if (UserManager.getInstance().isCurrentUserInAlliance())
			{
				JSONObject alliance = new JSONObject();
				alliance.put("id", getAllianceRoomId());
				alliance.put("group", GROUP_ALLIANCE);
				array.put(alliance);
			}

			if (ChatServiceController.canEnterDragonObserverDanmuRoom())
			{
				JSONObject danmuJson = new JSONObject();
				danmuJson.put("id", getDanmuRoomId());
				danmuJson.put("group", GROUP_DRAGON_DANMU);
				array.put(danmuJson);
			}

		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return array;
	}

	public void uploadLocation(String logitude, String latitude)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "logitude", logitude, "latitude", latitude);
		sendCommand(UPLOAD_LOCATION, "longitude", logitude, "latitude", latitude);
	}

	public void clearLocation()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		sendCommand(CLEAR_LOCATION);
	}

	public void leaveAllianceRoom()
	{
		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getAllianceRoomId());
	}

	public void leaveCountryRoom()
	{
		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getCountryRoomId());
	}

	public void leaveOriginalCountryRoom()
	{
		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getRoomIdPrefix() + GROUP_ORIGINAL + "_" + getOriginalCountryId());
	}

	public void sendRoomMsg(String messageText, int sendLocalTime, ChatChannel channel)
	{
		sendRoomMsg(messageText, sendLocalTime, channel, MsgItem.MSGITEM_TYPE_MESSAGE, null);
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
			sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "group", group, "roomId", roomId, "type", 2, "extraRoom", getExtraRoomId(channel.channelType), "msg", messageText,
					"sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
		else
		{
			sendCommand(SEND_ROOM_MSG_COMMAND, "channelType", channel.channelType, "group", group, "roomId", roomId, "extraRoom", getExtraRoomId(channel.channelType), "msg", messageText, "sendTime",
					sendLocalTime, "extra", getMsgExtra(post, media));
		}
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
						JSONArray atJson = new JSONArray(atUids);
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
					JSONObject shareObj = new JSONObject(media);
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

	private long startGetNewTime;

	/**
	 * 后台返回包括db最后时间在内（可能有时间一样的新消息）的所有新数据（或至多N条数据）
	 * 仅在onOpen后的第一次getNewMsgs()才使用roomsParams，游戏重登陆不会走onOpen，但这时候需要刷新roomsParams
	 */
	public void getNewMsgs()
	{
		if (roomsParams != null)
		{
			sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", roomsParams);
			roomsParams = null;
		}
		else
		{
			sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", getRoomsParams());
		}
		startGetNewTime = TimeManager.getInstance().getCurrentTimeMS();
		getNewUserMsg();
		if(SwitchUtils.customWebsocketEnable){
			roomGroupAll();
		}
		getRealtimeVoiceRoomInfo();
	}

    /**
     * 获取自定义聊天室历史
     *
     * @param channelList
     */
    public void getRoomGroupNewMsgs(List<ChatChannel> channelList) {
        if (channelList == null || channelList.size() == 0) return;
        JSONObject params = null;
        try {
            params = new JSONObject();
            for (int i = 0; i < channelList.size(); i++) {
                ChatChannel channel = channelList.get(i);
                if (channel == null)
                    continue;
                params.put(channel.channelID, TimeManager.getTimeInMS(channel.getLatestTime()));
            }
        } catch (JSONException e) {
            LogUtil.printException(e);
        }
        sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", params);
    }

	public void getNewUserMsg()
	{
		if (!(ConfigManager.useWebSocketServer && (ConfigManager.pm_standalone_read || (!ConfigManager.pm_standalone_read && MailManager.nearbyEnable))))
			return;
		long time = ChannelManager.getInstance().getLatestUserChatTime();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_SEND, "time", TimeManager.getTimeInMS(time));
		sendCommand(GET_NEW_USERCHAT_BY_TIME_COMMAND, "time", TimeManager.getTimeInMS(time));
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
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_WS_SEND, "latestTime", TimeManager.getTimeInMS(channel.getLatestTime()));
					statusListener.onConsoleOutput("latestTime = " + channel.getLatestTime());
					statusListener.onConsoleOutput("latestTime = " + TimeManager.getTimeInMS(channel.getLatestTime()));
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

	private void getHistoryMsgs(String roomId, int startSeqId, int endSeqId)
	{
		sendCommand(GET_HISTORY_MSGS_BY_TIME_COMMAND, "roomId", roomId, "start", startSeqId, "end", endSeqId);
	}

	public static boolean isWebSocketEnabled()
	{
		// || ChatServiceController.isBetaVersion()
		return ConfigManager.useWebSocketServer;// && (ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost());
	}

	public static boolean isRecieveFromWebSocket(int channelType)
	{
		return isWebSocketEnabled() && ((ConfigManager.isRecieveFromWebSocket && channelType != DBDefinition.CHANNEL_TYPE_USER)
				|| (ConfigManager.pm_standalone_read && channelType == DBDefinition.CHANNEL_TYPE_USER))
				&& isSupportedType(channelType);
	}

	public static boolean isSendFromWebSocket(int channelType)
	{
		return isWebSocketEnabled() &&
				((ConfigManager.isSendFromWebSocket && channelType != DBDefinition.CHANNEL_TYPE_USER) ||
						(ConfigManager.pm_standalone_write && channelType == DBDefinition.CHANNEL_TYPE_USER) ||
						ChatServiceController.getInstance().isUsingDummyHost())
				&& isSupportedType(channelType);
	}

	public static boolean isSupportedType(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS || channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
				|| channelType == DBDefinition.CHANNEL_TYPE_USER;
	}

	public boolean isConnected()
	{
		return client != null && client.isOpen();
	}

	public void sendCommand(String command, Object... args)
	{
		if (!isConnected())
			return;

		statusListener.onConsoleOutput("send: " + command);
		try
		{
			JSONObject params = new JSONObject();
			for (int i = 0; i < args.length; i += 2)
			{
				if ((i + 1) < args.length)
				{
					params.put((String) args[i], args[i + 1]);
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
		if (!isConnected())
			return;

		JSONObject jsonobj = new JSONObject();
		jsonobj.put("cmd", command);
		if (params.has("sendTime"))
		{
			jsonobj.put("sendTime", params.getInt("sendTime"));
			params.remove("sendTime");
		}
		else
		{
			long time = TimeManager.getInstance().getCurrentTimeMS();
			jsonobj.put("sendTime", time);
		}

		jsonobj.put("params", params);

		String output = String.format("%s: %s", command, jsonobj.toString());
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_SEND, output);
		statusListener.onConsoleOutput(output);

		client.send(jsonobj.toString());
	}

	public void handleMessage(String message)
	{
		if (message.equals("heartbeat"))
		{
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, "heartbeat");
			return;
		}

		try
		{
			JSONObject json = new JSONObject(message);
			String command = json.getString("cmd");

			String output = String.format("%s: %s", command, message);
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, output);
			// statusListener.onConsoleOutput(output);

			if (client.isMyMessage(json))
			{
				return;
			}

			if (json.has("data")) // 由服务端主动推送的数据
			{
				statusListener.onConsoleOutput("push: " + command);
				onRecieveMessage(message);
			}
			else
			// 客户端发送命令时，服务端处理完命令推送的数据
			{
				if (json.has("result"))
				{
					statusListener.onConsoleOutput("send success: " + command);
					onCommandSuccess(message);
				}
				else if (json.has("error")) // 发生错误
				{
					// ServiceInterface.flyHint(null, "", command + " error: " + message, 0, 0, false);
					statusListener.onConsoleOutput("send error: " + command);
				}
			}

			if (!json.has("sendTime") && json.has("pushtime") && json.optLong("pushtime") > 0)
			{
				LogUtil.trackWSPushTime(TimeManager.getInstance().getCurrentTimeMS() - json.optLong("pushtime"));
			}
		}
		catch (JSONException e)
		{
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, "JSONException: " + message);
			LogUtil.printException(e);
		}
	}

	public void joinDragonObserverRoom()
	{
		if (!ChatServiceController.dragonObserverEnable)
			return;
		try
		{
			startJoinRoomTimer();
			joinDragonObserverRoomStatus = 1;
			JSONArray roomsArr = new JSONArray();
			JSONObject room = new JSONObject();
			room.put("id", ChatServiceController.dragonObserverRoomId);
			room.put("group", GROUP_DRAGON_OBSERVER);
			roomsArr.put(room);
			sendCommand(JOIN_ROOM_MULTI_COMMAND, "rooms", roomsArr);
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
			sendCommand(LEAVE_ROOM_COMMAND, "group", GROUP_DRAGON_OBSERVER, "roomId", ChatServiceController.dragonObserverRoomId);
	}

	public void getRealtimeVoiceRoomInfo()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		int role = WebRtcPeerManager.getInstance().canControllerRole() ? 2 : 1;
		sendCommand(REALTIME_VOICE_JOIN_COMMAND, "roomId", UserManager.getInstance().getCurrentUser().allianceId, "role", role);
	}

	public void leaveRealtimeVoiceRoom()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (!ChatServiceController.realtime_voice_enable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		sendCommand(REALTIME_VOICE_LEAVE_COMMAND, "roomId", UserManager.getInstance().getCurrentUser().allianceId);
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
			sendCommand(LEAVE_ROOM_COMMAND, "group", GROUP_DRAGON_DANMU, "roomId", ChatServiceController.dragonObserverDanmuRoomId);
	}

	private void onCommandSuccess(String message)
	{
		try
		{
			JSONObject json = new JSONObject(message);
			String command = json.getString("cmd");
			JSONObject result = json.getJSONObject("result");

			if (command.equals(SET_DEVICE_COMMAND))
			{
				if (result.opt("status") != null && result.getString("status").equals("ok") && needInit)
				{
					onSetDevice();
				}
			}
			else if (command.equals(JOIN_ROOM_MULTI_COMMAND))
			{
				// {"status":true,"id":["id1","id2"]}
				if (result.optBoolean("status") && result.getBoolean("status"))
				{
					boolean JoinRandom = false;
					if (result.opt("id") != null)
					{
						JSONArray jsonArr = result.getJSONArray("id");
						if (jsonArr != null)
						{
							for (int i = 0; i < jsonArr.length(); i++)
							{
								String obj = jsonArr.getString(i);
								if (StringUtils.isNotEmpty(obj) && (obj.startsWith(GROUP_RANDOM_LOCAL) || obj.startsWith(GROUP_RANDOM_GLOBAL)))
								{
									JoinRandom = true;
									break;
								}
							}
						}
					}

					if (result.opt("ids") != null)
					{
						JSONArray jsonArr = result.getJSONArray("ids");
						if (jsonArr != null)
						{
							for (int i = 0; i < jsonArr.length(); i++)
							{
								JSONObject obj = jsonArr.getJSONObject(i);
								if (obj != null)
								{
									if (obj.opt("group") != null)
									{
										String group = obj.getString("group");
										if (StringUtils.isNotEmpty(group))
										{
											if (group.equals(GROUP_DRAGON_OBSERVER))
											{
												if (ChatServiceController.dragonObserverEnable)
												{
													if (obj.opt("roomId") != null)
													{
														String roomId = obj.getString("roomId");
														if (StringUtils.isNotEmpty(roomId) && roomId.equals(ChatServiceController.dragonObserverRoomId))
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
												}
											}
											else if (group.equals(GROUP_DRAGON_DANMU))
											{
												if (ChatServiceController.dragonObserverDanmuEnable)
												{
													if (obj.opt("roomId") != null)
													{
														String roomId = obj.getString("roomId");
														if (StringUtils.isNotEmpty(roomId) && roomId.equals(ChatServiceController.dragonObserverDanmuRoomId))
														{
															isInDragonObserverDanmuRoom = true;
														}
													}
												}
											}
										}
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
					else
					{
						onJoinRoom();
					}
				}
			}
			else if (command.equals(GET_NEW_MSGS_BY_TIME_COMMAND))
			{
				onGetNewMsg(result);
			}
			else if (command.equals(GET_NEW_USERCHAT_BY_TIME_COMMAND))
			{
				onGetNewUserMsg(result);
			}
			else if (command.equals(LEAVE_ROOM_COMMAND))
			{
				if (result.opt("status") != null && result.getBoolean("status"))
				{
					if (result.opt("group") != null)
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
			else if (command.equals(GET_RANDOM_CHATROOM_LOCAL) || command.equals(GET_RANDOM_CHATROOM_GLOBAL))
			{
				isJoinRoom = false;
				if (!ChatServiceController.randomChatEnable)
					return;
				if (result.opt("group") != null)
				{
					String group = result.getString("group");
					if (StringUtils.isNotEmpty(group))
						randomGroup = group;
				}
				if (result.opt("id") != null)
				{
					String roomId = result.getString("id");
					if (StringUtils.isNotEmpty(roomId))
						randomRoomId = roomId;
				}
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_RECIEVE, "randomGroup", randomGroup, "randomRoomId", randomRoomId);
				if (StringUtils.isNotEmpty(randomGroup) && StringUtils.isNotEmpty(randomRoomId))
				{

					try
					{
						JSONArray roomsArr = new JSONArray();
						JSONObject room = new JSONObject();
						room.put("id", randomRoomId);
						room.put("group", randomGroup);
						roomsArr.put(room);
						sendCommand(JOIN_ROOM_MULTI_COMMAND, "rooms", roomsArr);

					}
					catch (JSONException e)
					{
						LogUtil.printException(e);
					}
				}
				else
				{
					if (ChatServiceController.getChatFragment() != null)
						ChatServiceController.getChatFragment().refreshRandomJoinTip(true, false);
				}
			}
			else if (command.equals(SEND_ROOM_MSG_COMMAND) || command.equals(SEND_USER_MSG_COMMAND))
			{
			if (result.opt("code") != null)
			{
				// 收到消息状态，进行排查，输出服务端返回参数
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "返回来的参数信息 + code ！=null"+message);

				String errorCode = result.getString("code");
				long banTime = 0;
				if (result.opt("banTime") != null)
					banTime = result.getLong("banTime");
				String sendLocalTime = "";
				if (json.opt("sendTime") != null)
					sendLocalTime = json.getString("sendTime");
				ChatChannel channel = null;

				String roomId = "";
				if (command.equals(SEND_ROOM_MSG_COMMAND))
				{
					LogUtilTest.writeLogToFile("D", "cammand", "SEND_ROOM_MSG_COMMAND");
					String group = "";
					if (result.opt("group") != null)
						group = result.getString("group");
					if (result.opt("roomId") != null)
						roomId = result.getString("roomId");
					channel = getChannel(group, roomId);
				}
				else if (command.equals(SEND_USER_MSG_COMMAND))
				{
					// 接受到消息
					LogUtilTest.writeLogToFile("D", "cammand", "SEND_USER_MSG_COMMAND");
					String channelId = "";
					if (result.opt("other") != null)
						channelId = result.getString("other");
					channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER,channelId);
				}
				if(channel != null)
				{
					ServiceInterface.postChatSendError(channel.channelType, channel.channelID, sendLocalTime, errorCode, banTime, roomId);
				}
				else
				{
					if(ChatServiceController.getChatFragment()!=null)
						ChatServiceController.getChatFragment().showSendMsgCode(errorCode);
				}
			}
			else
			{
				if (command.equals(SEND_USER_MSG_COMMAND))
				{
					try
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "返回来的参数信息 + code = null "+message);
						String sendTime = "";
						if (json.opt("sendTime") != null)
							sendTime = json.getString("sendTime");
						int sendLocalTime = Integer.parseInt(sendTime);
						onRecieveUserChatMessage(result, sendLocalTime);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				} else if (command.equals(SEND_ROOM_MSG_COMMAND)) {
					String group = "";
					String roomId = "";
					if (result.has("group"))
						group = result.optString("group");
					if (result.has("roomId"))
						roomId = result.optString("roomId");
					int groupType = group2channelType(group);
					if (groupType != DBDefinition.CHANNEL_TYPE_CHATROOM) return;
					if (TextUtils.isEmpty(roomId)) return;
					ChatChannel channel = getChannel(group, roomId);
					MsgItem item = parseMsgItem(result);
					if (item != null && channel != null)
						ServiceInterface.handleMessage(new MsgItem[]{item}, channel.channelID, "", false, false);
				}
			}
		}
			else if (command.equals(UPLOAD_LOCATION))
			{
				if (result.opt("status") != null && result.getString("status").equals("ok"))
				{
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "upload location sucess!");
					NearByManager.getInstance().setHasUploadLocation(true);
					getNearByUserList();
				}
			}
			else if (command.equals(CLEAR_LOCATION))
			{
				if (result.opt("status") != null && result.getString("status").equals("ok"))
				{
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "clear location sucess!");
					ServiceInterface.exitChatActivityFrom2dx(false);
					NearByManager.getInstance().setClearLocation(true);
					NearByManager.getInstance().setHasUploadLocation(false);
				}
			}
			else if (command.equals(GET_NEARBY_USER))
			{
				if (result.opt("mylike") != null)
				{
					JSONArray mylikeArr = result.getJSONArray("mylike");
					if (mylikeArr != null)
					{
						List<String> uidList = new ArrayList<String>();
						for (int i = 0; i < mylikeArr.length(); i++)
						{
							String uid = mylikeArr.getString(i);
							if (StringUtils.isNotEmpty(uid) && !uidList.contains(uid))
								uidList.add(uid);
						}
						NearByManager.getInstance().initTodayLikeUid(uidList);
					}
				}

				if (result.opt("users") != null)
				{
					NearByManager.getInstance().setHasUploadLocation(true);
					NearByManager.getInstance().setHasSearchNearByUser(true);
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "get nearby user sucess!");
					JSONArray userArr = result.getJSONArray("users");
					for (int i = 0; i < userArr.length(); i++)
					{
						JSONObject userObj = userArr.getJSONObject(i);
						if (userObj != null)
						{
							String uid = "";
							if (userObj.opt("uid") != null)
								uid = userObj.getString("uid");
							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "get nearby user:" + uid);

							double distance = 0;
							if (userObj.opt("dis") != null)
								distance = userObj.getDouble("dis");

							long lastLoginTime = 0;
							if (userObj.opt("time") != null)
								lastLoginTime = userObj.getLong("time");
							if (lastLoginTime <= 0)
								lastLoginTime = System.currentTimeMillis();

							int likes = 0;
							if (userObj.opt("likes") != null)
								likes = userObj.getInt("likes");

							String freshNewsText = null;
							List<ColorFragment> freshNewsColorList = null;
							if (userObj.opt("freshNews") != null)
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
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "no user data!");
					ChatServiceController.getNearByListActivity().notifyDataSetChanged();
				}

			}
			else if (command.equals(CHAT_USER_MARK_READ) || command.equals(CHAT_USER_DEL))
			{
				if (result.opt("status") != null && result.getBoolean("status"))
				{
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "CHAT_USER_ operate sucess!");
					String fromUids = "";
					if (result.opt("uids") != null)
						fromUids = result.getString("uids");
					int type = 0;
					if (result.opt("type") != null)
						type = result.getInt("type");
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
			else if (command.equals(NEARBY_LIKE) || command.equals(NEARBY_LIKE_CANCEL))
			{
				if (result.opt("status") != null && result.getBoolean("status"))
				{
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "NEARBY_LIKE operate sucess!");
					String uid = "";
					if (result.opt("uid") != null)
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
			else if (command.equals(REALTIME_VOICE_JOIN_COMMAND))
			{
				onGetRealtimeVoiceRoomInfo(result);
			}
			else if (command.equals(REALTIME_VOICE_LEAVE_COMMAND))
			{
				onLeaveRealtimeVoiceRoom(result);
			}
			else if (RoomGroupCmd.contains(command))
			{
				if(SwitchUtils.customWebsocketEnable){
					onRoomGroupCommandSuccess(message);
				}
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void onRoomGroupCommandSuccess(String data) {
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		String cmd = "";
		try {
			JSONObject jsonObject = new JSONObject(data);
			cmd = jsonObject.optString("cmd");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ChatChannel channel;
		if (RoomGroupCmd.get(cmd) != RoomGroupCmd.ROOMGROUP_ROOMALL) {
			RoomResponseCommand roomGroupResponse = new Gson().fromJson(data, RoomResponseCommand.class);
			if (roomGroupResponse == null) return;
			RoomInfoCommand roomInfo = roomGroupResponse.roomInfo;
			if (roomInfo == null) return;
			channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, roomInfo.id));
			// receive只处理我发出的command
			if (!UserManager.getInstance().isMySelf(roomGroupResponse.uid)) return;
			MsgItem[] msgItems = {};
			switch (RoomGroupCmd.get(cmd)) {
				case ROOMGROUP_CREATE:
					channel.roomOwner = UserManager.getInstance().getCurrentUserId();
					channel.isMember = true;
					if (!TextUtils.isEmpty(roomInfo.members)) {
						channel.memberUidArray = RoomUtils.removeDuplicate(RoomUtils.getUidArr(roomInfo.members));
					}
					if (!TextUtils.isEmpty(roomInfo.name)) {
						channel.customName = roomInfo.name;
					}
					DBManager.getInstance().updateChannel(channel);
					msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupResponse,
							RoomUtils.getMsgStr(RoomGroupCmd.TYPE_INVITE, roomGroupResponse.uid, roomInfo.members), true)};
					if (ChatServiceController.getCurrentActivity() != null) {
						ServiceInterface.setMailInfo(3, channel.channelID, channel.customName);
						ServiceInterface.showMaillListActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_USER, MailManager.CHANNELID_MESSAGE);
						AQUtility.postDelayed(new Runnable() {
							@Override
							public void run() {
								ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_CHATROOM, false);
							}
						}, 1000);
					}
					break;
				case ROOMGROUP_INVITE:
					if (TextUtils.isEmpty(roomInfo.members))
						return;
					channel.memberUidArray = RoomUtils.addUidsToArr(channel.memberUidArray, RoomUtils.getUidArr(roomInfo.members));
					DBManager.getInstance().updateChannel(channel);
					msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupResponse,
							RoomUtils.getMsgStr(RoomGroupCmd.TYPE_INVITE, roomGroupResponse.uid, roomInfo.members), true)};
					break;
				case ROOMGROUP_MODIFYNAME:
					channel.customName = roomInfo.name;
					DBManager.getInstance().updateChannel(channel);
					msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupResponse,
							RoomUtils.getMsgStr(RoomGroupCmd.TYPE_MODIFY, roomGroupResponse.uid, roomInfo.name), true)};
					ServiceInterface.notifyChatRoomNameChanged(roomInfo.name);
					break;
				case ROOMGROUP_KICK:
					if (TextUtils.isEmpty(roomInfo.members))
						return;
					channel.memberUidArray = RoomUtils.removeUidsFromArr(channel.memberUidArray, RoomUtils.getUidArr(roomInfo.members));
					DBManager.getInstance().updateChannel(channel);
					msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupResponse,
							RoomUtils.getMsgStr(RoomGroupCmd.TYPE_KICK, roomGroupResponse.uid, roomInfo.members), true)};
					break;
				case ROOMGROUP_QUIT:
//                    channel.memberUidArray = RoomUtils.removeUidsFromArr(channel.memberUidArray, RoomUtils.getUidArr(UserManager.getInstance().getCurrentUserId()));
//                    channel.isMember = false;
					DBManager.getInstance().dropTable(channel.getChatTable().getTableName());
					DBManager.getInstance().deleteChannel(channel.getChatTable());
					if (ChatServiceController.getChatRoomSettingActivity() != null
							&& !ChatServiceController.getChatRoomSettingActivity().isFinishing())
						ChatServiceController.getChatRoomSettingActivity().finish();
					if (ChatServiceController.getCurrentActivity() != null
							&& ChatServiceController.getCurrentActivity() instanceof ChatActivity
							&& !ChatServiceController.getCurrentActivity().isFinishing())
						ChatServiceController.getCurrentActivity().finish();
					notifyChatListener(RoomGroupCmd.get(cmd), channel, null);
					break;
				default:
					break;
			}
			if (msgItems.length == 0) return;
			ServiceInterface.handleMessage(msgItems, channel.channelID, channel.customName, false, true);
			if (ChatServiceController.getChatRoomSettingActivity() != null)
				ChatServiceController.getChatRoomSettingActivity().refreshData();
		} else {
			RoomResponseAll roomGroupResponseAll = new Gson().fromJson(data, RoomResponseAll.class);
			if (roomGroupResponseAll != null && roomGroupResponseAll.rooms != null && roomGroupResponseAll.rooms.roomInfoList != null) {
				List<RoomInfoPush> roomInfoList = roomGroupResponseAll.rooms.roomInfoList;
				List<ChatChannel> channelList = new ArrayList<ChatChannel>();
				for (RoomInfoPush roomInfo : roomInfoList) {
					channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, roomInfo.id));
					channel.roomOwner = roomInfo.founder;
					channel.memberUidArray = RoomUtils.removeDuplicate(roomInfo.members);
					if (channel.memberUidArray != null && channel.memberUidArray.contains(UserManager.getInstance().getCurrentUserId())) {
						channel.isMember = true;
					}
					DBManager.getInstance().updateChannel(channel);
					channelList.add(channel);
				}
				getRoomGroupNewMsgs(channelList);
			}
		}
	}

    private void onRoomGroupPush(String data) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        RoomPushData roomGroupPush = new Gson().fromJson(data, RoomPushData.class);
        if (roomGroupPush == null) return;
        // 我自己发的在receive里处理
        if (UserManager.getInstance().isMySelf(roomGroupPush.sender)) return;
        String roomId = roomGroupPush.roomId;
        RoomInfoPush roomInfo = roomGroupPush.roomInfo;
        ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, roomId));
        if (roomInfo != null) {
            channel.memberUidArray = RoomUtils.removeDuplicate(roomInfo.members);
            channel.roomOwner = roomInfo.founder;
            channel.isMember = true;
            channel.customName = roomInfo.name;
        }
        MsgItem[] msgItems = {};
        switch (roomGroupPush.type) {
            case RoomGroupCmd.TYPE_SYSTEM:
                break;
            case RoomGroupCmd.TYPE_CREATE:
            case RoomGroupCmd.TYPE_INVITE:
                DBManager.getInstance().updateChannel(channel);
                // members在msg里
                msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupPush, true, RoomUtils.getMsgStr(RoomGroupCmd.TYPE_INVITE, roomGroupPush.sender, roomGroupPush.msg))};
                break;
            case RoomGroupCmd.TYPE_MODIFY:
                DBManager.getInstance().updateChannel(channel);
                msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupPush, true, RoomUtils.getMsgStr(RoomGroupCmd.TYPE_MODIFY, roomGroupPush.sender, roomGroupPush.msg))};
                ServiceInterface.notifyChatRoomNameChanged(roomGroupPush.msg);
                break;
            case RoomGroupCmd.TYPE_KICK:
                boolean isMemberKick = true;
                if (UserManager.getInstance().isMySelf(roomGroupPush.msg)) {
                    isMemberKick = false;// 被踢界面刷新
                    ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								if (ChatServiceController.getChatFragment() != null) {
									if (ChatServiceController.getChatFragment().isSelectMemberBtnEnable()) {
										ChatServiceController.getChatFragment().refreshMemberSelectBtn();
										ChatServiceController.getChatFragment().setSelectMemberBtnState();
									}
								}
							} catch (Exception e) {
								LogUtil.printException(e);
							}
						}
					});
                }
                channel.isMember = isMemberKick;
                DBManager.getInstance().updateChannel(channel);
                msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupPush, true, RoomUtils.getMsgStr(RoomGroupCmd.TYPE_KICK, roomGroupPush.sender, roomGroupPush.msg))};
                break;
            case RoomGroupCmd.TYPE_QUIT:
                DBManager.getInstance().updateChannel(channel);
                msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupPush, true, RoomUtils.getMsgStr(RoomGroupCmd.TYPE_QUIT, roomGroupPush.sender, roomGroupPush.msg))};
                break;
            case RoomGroupCmd.TYPE_MESSAGE:
            default:
                DBManager.getInstance().updateChannel(channel);
                msgItems = new MsgItem[]{MsgItem.createMsgItem(roomGroupPush, false, null)};
                break;
        }
        if (msgItems.length == 0) return;
        ServiceInterface.handleMessage(msgItems, channel.channelID, channel.customName, true, true);
        if (ChatServiceController.getChatRoomSettingActivity() != null)
            ChatServiceController.getChatRoomSettingActivity().refreshData();
    }

	private ParseResult parseTextFromExtra(JSONObject json)
	{
		ParseResult result = null;
		try
		{
			if (json != null && json.opt("useDialog") != null && json.opt("text") != null)
			{
				String text = json.getString("text");
				if (json.getInt("useDialog") == 1)
				{
					if (json.opt("dialogExtra") != null)
					{
						JSONArray dialogExtraArr = json.getJSONArray("dialogExtra");
						if (dialogExtraArr != null && dialogExtraArr.length() > 0)
						{
							List<ColorFragment> dialogArr = new ArrayList<ColorFragment>();
							for (int i = 0; i < dialogExtraArr.length(); i++)
							{
								JSONObject extra = dialogExtraArr.getJSONObject(i);
								if (extra != null)
								{
									String dialogExtra = "";
									if (extra.opt("type") != null)
									{
										int type = extra.getInt("type");
										if (type == 0 && extra.opt("text") != null)
										{
											dialogExtra = extra.getString("text");
										}
										else if (type == 1 && extra.opt("xmlId") != null && extra.opt("proName") != null)
										{
											dialogExtra = JniController.getInstance().excuteJNIMethod("getPropById",
													new Object[] { extra.getString("xmlId"), extra.getString("proName") });
										}
										else if (type == 2 && extra.opt("dialog") != null)
										{
											dialogExtra = LanguageManager.getLangByKey(extra.getString("dialog"));
										}
									}
									int color = 0;
									if (extra.opt("textColor") != null)
										color = ChatServiceController.parseColor(extra.getString("textColor"));
									boolean needLink = false;
									if (extra.opt("isLink") != null)
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

	public boolean isInRandomRoom()
	{
		return StringUtils.isNotEmpty(randomGroup) && StringUtils.isNotEmpty(randomRoomId);
	}

	public void leaveCurrentRandomRoom()
	{
		if (isInRandomRoom())
		{
			sendCommand(LEAVE_ROOM_COMMAND, "group", randomGroup, "roomId", randomRoomId);
		}
	}

	private void onGetServerList(String serverlist)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "serverlist", serverlist);
		statusListener.onConsoleOutput("onGetServerList:" + serverlist);

		try
		{
			JSONObject json = new JSONObject(serverlist);

			if (json.opt("data") != null && json.getJSONArray("data") instanceof JSONArray)
			{
				JSONArray datas = json.getJSONArray("data");
				for (int i = 0; i < datas.length(); i++)
				{
					JSONObject data = datas.getJSONObject(i);
					if (isStringExist(data, "protocol") && isStringExist(data, "ip") && isStringExist(data, "port"))
					{
						StandaloneServerInfo server = new StandaloneServerInfo(data.getString("protocol"), data.getString("ip"), data.getString("port"));
						serversInfos.add(server);
					}
				}
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void onRecieveMessage(String message)
	{
		try
		{
			JSONObject json = new JSONObject(message);
			String command = json.getString("cmd");
			JSONObject data = json.getJSONObject("data");

			if (command.equals(RECIEVE_USER_MSG_COMMAND))
			{
				onRecieveUserChatMessage(data, 0);
			}
			else if (command.equals(RECIEVE_ROOM_MSG_COMMAND))
			{
				onRecieveRoomMessage(data);
			}
			else if (command.equals(ANOTHER_LOGIN_COMMAND))
			{
				// 同一个uid在不同地方登陆会这样，发生这种情况游戏应该就不让登陆了
				statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.ANOTHER_LOGIN));
				ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_SERVER_DISCONNECTED);
				forceClose();
			}
			else if (command.equals(LOGIN_SUCCESS_COMMAND))
			{
				client.setClientID(json);
				this.onLoginSuccess(json);
			}
			else if (command.equals(LOGIN_FAILED_COMMAND))
			{
				this.onLoginFailed(json);
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
			else if (command.equals(RoomGroupCmd.ROOMGROUP_PUSH.cmd))
			{
				if(SwitchUtils.customWebsocketEnable){
					onRoomGroupPush(data.toString());
				}
			}

		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
	}

	private void onRealtimeVoiceRoomRoleChanged(JSONObject data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (data == null || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		try
		{
			if (data.opt("roomId") != null)
			{
				String roomId = data.getString("roomId");
				if (!UserManager.getInstance().getCurrentUser().allianceId.equals(roomId))
					return;
			}

			if (data.opt("speakers") != null)
			{
				JSONArray speakers = data.getJSONArray("speakers");
				List<String> speakerList = new ArrayList<String>();
				for (int i = 0; i < speakers.length(); i++)
				{
					String speaker = speakers.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						speakerList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initSpeak(speakerList);
			}

			if (data.opt("all") != null)
			{
				JSONArray all = data.getJSONArray("all");
				List<String> allList = new ArrayList<String>();
				for (int i = 0; i < all.length(); i++)
				{
					String user = all.getString(i);
					if (StringUtils.isNotEmpty(user))
						allList.add(user);
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
		if (data.opt("groupId") != null && data.opt("endTime") != null)
		{
			String groupId = data.optString("groupId");
			long endTime = data.optLong("endTime");
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
		if (data.opt("roomId") != null)
		{
			String roomId = data.optString("roomId");
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

			if (msg.opt("read") != null)
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
			if (msg.opt("content") != null)
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

			if (msg.opt("originalLang") != null)
				item.originalLang = msg.getString("originalLang");
			if (msg.opt("translationMsg") != null)
				item.translateMsg = msg.getString("translationMsg");

			item.mailId = "";
			if (msg.opt("seqid") != null)
			{
				item.mailId = msg.getString("seqid");
			}

			int type = 1;
			if (msg.opt("type") != null)
				type = msg.getInt("type");

			item.post = 0;
			if (msg.opt("post") != null)
			{
				int post = msg.getInt("post");
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
			if (msg.optJSONObject("extra") != null)
			{
				extra = msg.getJSONObject("extra");

				if (extra.opt("seqId") != null && extra.getInt("seqId") > 0)
				{
					item.sequenceId = extra.getInt("seqId");
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

				if (extra.opt("post") != null)
				{
					item.post = extra.getInt("post");
				}

				if (extra.opt("media") != null)
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

			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "解析msg,如果失败了，就不会进行任何的刷新操作");
			if (item != null)
			{
				String customName = "";
				int type = 1;
				if (data.opt("type") != null)
					type = data.getInt("type");
				String channelId = "";
				if (data.opt("other") != null)
					channelId = data.getString("other");
				if (type == MailManager.MAIL_Alliance_ALL)
				{
					String prefix = "(" + LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_ALL_MAIL) + ")";
					if (StringUtils.isNotEmpty(item.msg) && !item.msg.startsWith(prefix))
						item.msg = prefix + item.msg;
				}
				MsgItem[] dbItemsArray = { item };
				// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "解析msg,成功"+handleUserChatMessage);
				handleUserChatMessage(type, channelId, dbItemsArray, item.isNewMsg, customName);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	private void onLeaveRealtimeVoiceRoom(JSONObject data)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		try
		{
			if (data.opt("id") != null)
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
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		try
		{
			if (data.opt("server") != null)
			{
				JSONObject server = data.getJSONObject("server");
				String protocol = "ws";
				String ip = "";
				String port = "";
				if (server.opt("protocol") != null)
					protocol = server.getString("protocol");
				if (server.opt("ip") != null)
					ip = server.getString("ip");
				if (server.opt("port") != null)
					port = server.getString("port");
				if (StringUtils.isNotEmpty(ip) && StringUtils.isNotEmpty(port))
				{
					WebRtcPeerManager.webRtcUrl = protocol + "://" + ip + ":" + port + "/room";
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "webRtcUrl", WebRtcPeerManager.webRtcUrl);
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

			if (data.opt("speakers") != null)
			{
				JSONArray speakers = data.getJSONArray("speakers");
				List<String> speakerList = new ArrayList<String>();
				for (int i = 0; i < speakers.length(); i++)
				{
					String speaker = speakers.getString(i);
					if (StringUtils.isNotEmpty(speaker))
						speakerList.add(speaker);
				}
				WebRtcPeerManager.getInstance().initSpeak(speakerList);
			}

			if (data.opt("all") != null)
			{
				JSONArray all = data.getJSONArray("all");
				List<String> allList = new ArrayList<String>();
				for (int i = 0; i < all.length(); i++)
				{
					String user = all.getString(i);
					if (StringUtils.isNotEmpty(user))
						allList.add(user);
				}
				WebRtcPeerManager.getInstance().initAll(allList);
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
			if (data.opt("group") != null && StringUtils.isNotEmpty(data.getString("group")) && data.getString("group").equals(GROUP_DRAGON_DANMU))
			{
				String msg = "";
				if (data.opt("msg") != null)
					msg = data.getString("msg");
				boolean isSelf = false;
				if (data.opt("sender") != null)
				{
					String sender = data.getString("sender");
					isSelf = StringUtils.isNotEmpty(sender) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
							&& sender.equals(UserManager.getInstance().getCurrentUserId());
				}
				int colorIndex = 0;
				if (data.optJSONObject("extra") != null)
				{
					JSONObject extra = data.getJSONObject("extra");
					if (extra.opt("colorIndex") != null)
						colorIndex = extra.getInt("colorIndex");
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
							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "latestHornMsg", latestHornMsg);
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

	public static boolean isStringExist(JSONObject obj, String key)
	{
		try
		{
			return obj.opt(key) != null && StringUtils.isNotEmpty(obj.getString(key));
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

			if (msg.opt("originalLang") != null)
			{
				item.originalLang = msg.getString("originalLang");
			}
			if (msg.opt("translationMsg") != null)
			{
				item.translateMsg = msg.getString("translationMsg");
			}

			item.post = 0;
			item.mailId = "";

			JSONObject extra = null;
			if (msg.optJSONObject("extra") != null)
			{
				extra = msg.getJSONObject("extra");

				if (extra.opt("seqId") != null && extra.getInt("seqId") > 0)
				{
					item.sequenceId = extra.getInt("seqId");
				}

				if (extra.opt("atUids") != null)
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

				if (extra.opt("post") != null)
				{
					item.post = extra.getInt("post");
				}
				else
				{
					item.post = 0;
				}

				if (extra.opt("media") != null)
				{
					item.media = extra.getString("media");
				}

				try
				{
					parseAttachment(extra, item);
					if (item.isEquipMessage())
					{
						if (extra.opt("attachmentId") != null && StringUtils.isNotEmpty(extra.getString("attachmentId")))
							item.msg = extra.getString("attachmentId");
					}
					else if (!item.isOldDialogMessage())
						item.attachmentId = extra.toString();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}

				if (extra.opt("dialog") != null)
				{
					String dialog = extra.getString("dialog");
				}
				if (extra.opt("inviteAlliance") != null)
				{
					String inviteAlliance = extra.getString("inviteAlliance");
				}
				if (extra.optJSONArray("msgarr") != null)
				{
					JSONArray msgarr = extra.getJSONArray("msgarr");
				}
				if (extra.opt("reportDef") != null)
				{
					String reportDef = extra.getString("reportDef");
				}
				if (extra.opt("reportAtt") != null)
				{
					String reportAtt = extra.getString("reportAtt");
				}
			}

			// 自定义聊天室系统消息
            if (msg.has("type")) {
                int type = msg.optInt("type");
                if (RoomGroupCmd.isSystemCmd(type)) {
                    String msgStr = RoomUtils.getMsgStr(type, item.uid, item.msg);
                    item.msg = msgStr;
                    item.post = MsgItem.MSG_TYPE_CHATROOM_TIP;
                }
            }
			
			if (msg.optJSONObject("senderInfo") != null)
			{
				JSONObject senderInfo = msg.getJSONObject("senderInfo");
				if (senderInfo.opt("lastUpdateTime") != null)
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
		if (extra.opt("allianceId") != null)
			item.attachmentId = extra.getString("allianceId");
		else if (extra.opt("reportUid") != null)
			item.attachmentId = extra.getString("reportUid");
		else if (extra.opt("detectReportUid") != null)
			item.attachmentId = extra.getString("detectReportUid");
		else if (extra.opt("equipId") != null)
			item.attachmentId = extra.getString("equipId");
		else if (extra.opt("teamUuid") != null)
			item.attachmentId = extra.getString("teamUuid");
		else if (extra.opt("lotteryInfo") != null)
			item.attachmentId = extra.getString("lotteryInfo");
		else if (extra.opt("redPackets") != null && extra.opt("server") != null)
			item.attachmentId = extra.getString("redPackets") + "_" + extra.getString("server");
		else if (extra.opt("attachmentId") != null)
			item.attachmentId = extra.getString("attachmentId");
	}

	private void parseAttachment(JSONObject extra, MsgItem item, String propName) throws JSONException
	{
		if (extra.opt(propName) != null)
		{
			item.attachmentId = extra.getString(propName);
		}
	}

	private ChatChannel getChannel(String group, String roomId)
	{
		return ChannelManager.getInstance().getChannel(group2channelType(group), roomId);
	}

	private void onGetNewMsg(JSONObject result)
	{
		// LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - startGetNewTime, LogUtil.CHAT_CONNECT_GET_NEW);
		try
		{
			JSONArray rooms = result.getJSONArray("rooms");
			for (int i = 0; i < rooms.length(); i++)
			{
				JSONObject room = rooms.getJSONObject(i);

				String roomId = room.getString("roomId");
				ChatChannel channel = getChannel(room.getString("group"), roomId);
				if (channel == null)
				{
					continue;
				}

				long firstMsgTime = room.getLong("firstMsgTime");
				long lastMsgTime = room.getLong("lastMsgTime");
				int firstSeqId = room.getInt("firstSeqId");
				int lastSeqId = room.getInt("lastSeqId");

				channel.serverMaxTime = lastMsgTime;
				channel.serverMinTime = firstMsgTime;

				JSONArray msgs = room.getJSONArray("msgs");

				channel.wsNewMsgCount = msgs.length();

				if (msgs.length() == 0)
				{
					channel.loadMoreMsg();
					continue;
				}

				MsgItem[] msgArr = new MsgItem[msgs.length()];
				MsgItem firstMsg = null;
				for (int j = 0; j < msgs.length(); j++)
				{
					JSONObject msg = msgs.getJSONObject(j);
					MsgItem item = parseMsgItem(msg);
					JSONObject roomInfoObj = msg.optJSONObject("room_info");
					if(roomInfoObj != null){
						RoomInfoPush roomInfo = new Gson().fromJson(roomInfoObj.toString(),RoomInfoPush.class);
						channel.customName = roomInfo.name;
						channel.memberUidArray = RoomUtils.removeDuplicate(roomInfo.members);
						channel.roomOwner = roomInfo.founder;
						DBManager.getInstance().updateChannel(channel);
					}
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
				if (msgs.length() > 1)
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

		if (!connectAsSupervisor && canTestServer())
			testServers();
	}

	private void onGetNewUserMsg(JSONObject result)
	{
		if (!ConfigManager.useWebSocketServer)
			return;
		try
		{
			Iterator<String> keyIter = result.keys();
			while (keyIter.hasNext())
			{
				String key = keyIter.next();
				if (StringUtils.isEmpty(key))
					continue;
				if (result.opt(key) != null)
				{
					JSONArray newMsgArr = result.getJSONArray(key);
					if (newMsgArr != null)
					{
						MsgItem[] dbItemsArray = new MsgItem[newMsgArr.length()];
						int count = 0;
						int type = 0;
						boolean isNewMsg = false;
						String channelId = key;
						for (int i = 0; i < newMsgArr.length(); i++)
						{
							JSONObject newMsgObj = newMsgArr.getJSONObject(i);
							MsgItem item = parseUserMsgItem(newMsgObj);
							if (item != null)
							{
								dbItemsArray[count++] = item;
								if (type == 0 && newMsgObj.opt("type") != null)
									type = newMsgObj.getInt("type");
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
		if (channel != null) {
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "handleUserChatMessage - ServiceInterface.handleMessage"+channelId);
			ServiceInterface.handleMessage(dbItemsArray, channelId, customName, isNewMsg, true);
		}
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
					NetworkUtil.testServerAndSaveResult(getServersInfosCopy(), statusListener);
					isTestingServers = false;

					statusListener.onConsoleOutput("Ping complete");
					statusListener.onTestComplete();
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

	private static String roomId2channelId(String roomId)
	{
		return roomId.substring(roomId.lastIndexOf("_") + 1);
	}

	public static int group2channelType(String group)
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
		return DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	public void handleDisconnect()
	{
		// ServiceInterface.flyHint(null, "", "disconnect", 0, 0, false);
	}

	/**
	 * id格式：
	 * <p>
	 * country_1
	 * <p>
	 * alliance_1_c79be2b653224cb4b1aeb5138ad15118
	 * <p>
	 * 
	 * test_country_1
	 * <p>
	 * test_alliance_1_c79be2b653224cb4b1aeb5138ad15118
	 * <p>
	 * 
	 * beta_country_107
	 * <p>
	 * beta_alliance_107_c79be2b653224cb4b1aeb5138ad15118
	 * <p>
	 */
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
		return UserManager.getInstance().isInBattleField() ? UserManager.getInstance().getCurrentUser().crossFightSrcServerId : UserManager.getInstance().getCurrentUser().serverId;
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
		if (ChatServiceController.isNotTestServer() && (ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost() || ChatServiceController.isBetaVersion()))
		{
			return "test_";
		}
		else
		{
			return "";
		}
	}

    /**
     * 自定义聊天室
     */
    public void roomGroupCreate(String roomName, String memberId) {
        sendCommand(RoomGroupCmd.ROOMGROUP_CREATE.cmd, "name", roomName, "members", memberId);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupInvite(String roomId, String memberId) {
        sendCommand(RoomGroupCmd.ROOMGROUP_INVITE.cmd, "roomId", roomId, "members", memberId);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupModifyName(String roomId, String roomName) {
        sendCommand(RoomGroupCmd.ROOMGROUP_MODIFYNAME.cmd, "roomId", roomId, "name", roomName);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupKick(String roomId, String memberId) {
        sendCommand(RoomGroupCmd.ROOMGROUP_KICK.cmd, "roomId", roomId, "members", memberId);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupQuit(String roomId) {
        sendCommand(RoomGroupCmd.ROOMGROUP_QUIT.cmd, "roomId", roomId);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupAll() {
        sendCommand(RoomGroupCmd.ROOMGROUP_ROOMALL.cmd);
    }

    /**
     * 自定义聊天室
     */
    public void roomGroupSendMsg(String channelID, String messageText, int post, String media, int sendTime) {
        int type = 0;
        // 文字消息 喇叭消息 服务器需要过滤 设置为type=1
        if (post != MsgItem.MSG_TYPE_HORN && post != MsgItem.MSGITEM_TYPE_MESSAGE) {
            type = 1;
        }
        // 据说服务端要求语音消息 type设为2  便于消息过滤
        if (post == MsgItem.MSG_TYPE_AUDIO) {
            type = 2;
        }
        sendCommand(SEND_ROOM_MSG_COMMAND, "roomId", channelID, "msg", messageText, "type", type, "extra", getExtra(post, media), "sendTime", sendTime);
    }

    private JSONObject getExtra(int post, String media) {
        JSONObject extra = null;
        try {
            extra = new JSONObject();
            extra.put("post", post);
            if (StringUtils.isNotEmpty(media)) {
                extra.put("media", media);
            }
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        return extra;
    }

    private final List<WeakReference<IChatListener>> mChatListenerRefs = new ArrayList<WeakReference<IChatListener>>();

    public void registerChatListener(IChatListener listener) {
        WeakReference<IChatListener> ref = new WeakReference<IChatListener>(listener);
        mChatListenerRefs.add(ref);
    }

    public void removeChatListener(IChatListener listener) {
        for (int i = 0; i < mChatListenerRefs.size(); i++) {
            WeakReference<IChatListener> ref = mChatListenerRefs.get(i);
            if (ref != null && ref.get() != null && listener == ref.get()) {
                ref.clear();
                mChatListenerRefs.remove(ref);
                break;
            }
        }
    }

    public void notifyChatListener(RoomGroupCmd roomGroupCmd, ChatChannel channel, MsgItem msgItem) {
        for (int i = 0; i < mChatListenerRefs.size(); i++) {
            WeakReference<IChatListener> ref = mChatListenerRefs.get(i);
            if (ref != null && ref.get() != null) {
                IChatListener listener = ref.get();
                if (listener != null) listener.onReceiveChatCmd(roomGroupCmd, channel, msgItem);
            }
        }
    }

}