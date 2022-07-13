package com.elex.im.core.net;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.WsClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.event.EventCenter;
import com.elex.im.core.event.LogEvent;
import com.elex.im.core.event.WSStatusEvent;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.AppInfoUtil;
import com.elex.im.core.util.HeadPicUtil.MD5;
import com.elex.im.core.util.HttpRequestUtil;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.NetworkUtil;
import com.elex.im.core.util.SharePreferenceUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;

public class WebSocketManager
{
	public final static String					WS_SERVER_LIST_URL				= "http://api.cok.chat/server/links";
	public final static String					WS_ALL_SERVER_LIST_URL			= "http://api.cok.chat/server/all";
	private final static WSServerInfo			DEFAULT_SERVER					= new WSServerInfo("ws", "default.servers.cok.chat", "80");

	private static WebSocketManager				instance;

	private WsClient							client;
	private ScheduledExecutorService			getServerListService;
	private CopyOnWriteArrayList<WSServerInfo>	serversInfos					= new CopyOnWriteArrayList<WSServerInfo>();
	/** 是否在客户端测试server的连接，并在下次登录时选择最快的连接 */
	public boolean								enableNetworkOptimization		= false;
	/** 测试结果更新的有效时间，秒为单位；注意此值配置在后台，得在登陆ws之后才能获取到，要与测试结果使用的有效时间区分开 */
	public long									networkOptimizationTimeout		= DEFAULT_TEST_RESULT_VALID_TIME;
	/** 测试结果使用的默认有效时间 */
	public static final int						DEFAULT_TEST_RESULT_VALID_TIME	= 5 * 24 * 3600 * 1000;
	/** server测试的延迟时间(从onGetNewMsg开始)，秒为单位 */
	public long									networkOptimizationTestDelay	= 0;
	public boolean								connectAsSupervisor				= false;
    public static boolean                        chatSessionEnable                = false;
    
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
	
	private long startConnectTime;
	
	/**
	 * 应该只调一次，以后断线会自动触发重连
	 */
	public void connect()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
//		if(!isWebSocketEnabled()) return;
		if(getServerListService != null && !getServerListService.isShutdown()) return;
		
		if(!SharePreferenceUtil.checkSession())
		{
			return;
		}

		// 连接状态时不重连，以免重登陆触发重连。但游戏真的重登陆时需要重新收取历史消息
		if (client != null && client.isOpen)
		{
			getNewMsgs();
			return;
		}
		
		connect2ws();
		startGetServerList();
	}
	
	private void startGetServerList()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		if(getServerListService != null && !getServerListService.isShutdown()) return;

		serversInfos = new CopyOnWriteArrayList<WSServerInfo>();
		
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
//		statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST));
//		ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTING);
		
		String timeStr = Integer.toString(TimeManager.getInstance().getCurrentTime());
		String secret = MD5.stringMD5(MD5.stringMD5(timeStr.substring(0, 3)) + MD5.stringMD5(timeStr.substring(timeStr.length() - 3, timeStr.length())));
		String sign = MD5.stringMD5(IMCore.getInstance().getAppConfig().getAppId() + UserManager.getInstance().getCurrentUser().uid + secret);
		String serverListUrl = !connectAsSupervisor ? WS_SERVER_LIST_URL : WS_ALL_SERVER_LIST_URL;
		long callTime = TimeManager.getInstance().getCurrentTimeMS();
		String param = "t=" + timeStr + "&s=" + sign + "&a=" + IMCore.getInstance().getAppConfig().getAppId() + "&u=" + UserManager.getInstance().getCurrentUser().uid + "&ct=" + callTime;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "getServerUrl", serverListUrl + "?" + param);
		EventCenter.getInstance().dispatchEvent(new LogEvent("getServerList: " + serverListUrl + "?" + param));
		String serverlistJson = HttpRequestUtil.sendGet(serverListUrl, param);
		if(!StringUtils.isEmpty(serverlistJson))
		{
			LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - callTime, LogUtil.CHAT_CONNECT_SERVER_LIST);
			onGetServerList(serverlistJson);
		}else{
			LogUtil.trackChatConnectTimeTime(-1, LogUtil.CHAT_CONNECT_SERVER_LIST);
		}

		if(serversInfos.size() == 0)
		{
//			statusListener.onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_GET_SERVERLIST_ERROR));
//			ServiceInterface.notifyWebSocketEventType(ConfigManager.WEBSOCKET_NETWORK_CONNECTE_FAILED);
		}else{
			serverListLoaded = true;
			if(connectAsSupervisor)
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

	private void onGetServerList(String serverlist)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "serverlist", serverlist);
		EventCenter.getInstance().dispatchEvent(new LogEvent("onGetServerList:" + serverlist));
		
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
						WSServerInfo server = new WSServerInfo(data.getString("protocol"), data.getString("ip"), data.getString("port"));
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

    private static int connectionCount = 0;
	private boolean forceDisconnect = false;
	private void connect2ws()
	{
		connectionCount++;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "connectionCount", connectionCount);
		
		if(client != null){
			try
			{
				forceDisconnect = true;
				client.closeBlocking();
			}
			catch (InterruptedException e)
			{
				LogUtil.printException(e);
			}
			client = null;
		}

		if(client == null)
		{
			createClient();
		}

		if (client != null)
		{
			forceDisconnect = false;
			EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.CONNECTING));
			client.connect();
		}
	}

	private ArrayList<WSServerInfo> getServersInfosCopy()
	{
		ArrayList<WSServerInfo> servers = new ArrayList<WSServerInfo>();
		for (Iterator<WSServerInfo> iterator = serversInfos.iterator(); iterator.hasNext();)
		{
			servers.add((WSServerInfo) iterator.next());
		}
		return servers;
	}
	
	WSServerInfo currentServer;
	private void createClient()
	{
		try {
			currentServer = null;
			currentServer = WSServerInfoManager.getInstance().selectPrimaryServer(getServersInfosCopy());
			
			if(currentServer == null || !currentServer.isValid())
			{
				currentServer = DEFAULT_SERVER;
			}

			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS, "connectAsSupervisor", connectAsSupervisor,
					"connecting server", currentServer, "UID", UserManager.getInstance().getCurrentUser().uid);
			EventCenter.getInstance().dispatchEvent(
					new LogEvent("connectAsSupervisor:" + connectAsSupervisor + " Connecting server: " + currentServer));

			startConnectTime = TimeManager.getInstance().getCurrentTimeMS();
			// "ws://cokchat-s2.elexapp.com:8088"
			if(!connectAsSupervisor)
			{
				// "://10.1.37.56:8080" 王宗坤本地测试
				client = new WsClient(currentServer.protocol + "://" + currentServer.address + ":" + currentServer.port, getHeader(), this);
			}else{
				client = new WsClient(currentServer.protocol + "://" + currentServer.address + ":" + currentServer.port + "/supervisor/100001", new HashMap<String, String>(), this);
			}
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		}
	}
	
	public Map<String, String> getHeader()
	{
		Map<String, String> header = new HashMap<String, String>();
		long time = TimeManager.getInstance().getCurrentTimeMS();
		header.put("APPID", IMCore.getInstance().getAppConfig().getAppId());
		header.put("TIME", String.valueOf(TimeManager.getInstance().getCurrentTimeMS()));
		header.put("UID", UserManager.getInstance().getCurrentUser().uid);
		header.put("SIGN", calcSign(IMCore.getInstance().getAppConfig().getAppId(), UserManager.getInstance().getCurrentUser().uid, time));
		header.put("SESSION", SharePreferenceUtil.getSharePreferenceString(IMCore.hostActivity, SharePreferenceUtil.getChatSessionTokenKey(), ""));
		return header;
	}
	
	/**
	 * 关闭连接，且不会自动重连
	 * 如果再由其它代码触发一次连接，会重置forceDisconnect的状态
	 */
	public void forceClose()
	{
		if(client != null)
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
	
	private int	reconnectRetryAvailableCount = RECONNECT_MAX_RETRY;
	private final static int RECONNECT_INTERVAL = 5;
	private int reconnectCountDown = 0;
	private int reconnectAdditionalInterval = -5;
	public void resetReconnectInterval()
	{
		reconnectAdditionalInterval = -5;
	}
	private final static int RECONNECT_MAX_RETRY = 99;

	private ScheduledExecutorService reconnectService;
	private TimerTask reconnectTimerTask;
	
	public synchronized void onConnectError()
	{
		if(currentServer != null)
		{
			currentServer.lastErrorTime = TimeManager.getInstance().getCurrentTimeMS();
			WSServerInfoManager.getInstance().updateLastErrorTime(currentServer);
		}
		
		if(connectAsSupervisor)
		{
			if (forceReconnect){
				// cokChatTest中会触发重连
				EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.CONNECT_ERROR));
				forceReconnect = false;
			}
			
			return;
		}
		
		synchronized (this)
		{
			if (reconnectCountDown == 0 && reconnectRetryAvailableCount >= 0 && !forceDisconnect)
			{
				if(reconnectAdditionalInterval < 10){
					reconnectAdditionalInterval += 5;
				}
				reconnectCountDown = RECONNECT_INTERVAL + 1 + reconnectAdditionalInterval;
//				resetClientID();
				startReconnect();
			}
		}
	}
	
	/**
	 * 只会执行一次
	 */
	private synchronized void startReconnect()
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
			if(reconnectCountDown <= 0){
//				EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.CONNECTION_FAILED));
				return;
			}
			
			reconnectCountDown--;
			if(reconnectCountDown <= 0 && !forceDisconnect){
				EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.RECONNECTING));
				reconnectRetryAvailableCount--;
				resetState();
				// 不需要重新获取ServerList，现在是根据ip返回所有适合的服务器，重新获取不会改变结果
				connect2ws();
			}else{
				EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.RECONNECTING, reconnectCountDown));
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
		EventCenter.getInstance().dispatchEvent(new LogEvent("Login success"));
		
		try
		{
			if(json.optBoolean("enableNetworkOptimization"))
			{
				enableNetworkOptimization = json.getBoolean("enableNetworkOptimization");
			}

			if(json.opt("networkOptimizationTimeout") != null)
			{
				networkOptimizationTimeout = json.optLong("networkOptimizationTimeout");
			}

			if(json.opt("networkOptimizationTestDelay") != null)
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
			setDevice();
		}
	}
	
	public void onLoginFailed(JSONObject json)
	{
		try
		{
			JSONObject data = json.getJSONObject("data");

			if(data.opt("error") != null && json.optString("error").equals("session error") && SharePreferenceUtil.createSessionCount < 3)
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

	private boolean testConfigLoaded = false;
	private boolean serverListLoaded = false;
	private boolean canTestServer()
	{
		return testConfigLoaded && serverListLoaded;
	}
	private boolean isTestingServers = false;
	private Timer testServerTimer;
	private synchronized void testServers()
	{
		if(!enableNetworkOptimization || isTestingServers) return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		
		if(testServerTimer != null)
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
					NetworkUtil.testServerAndSaveResult(getServersInfosCopy());
					isTestingServers = false;

					EventCenter.getInstance().dispatchEvent(new LogEvent("Ping complete"));
					EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.TEST_COMPLETE));
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

	private static String calcSign(String appid, String uid, long time)
	{
		return MD5.stringMD5(MD5.stringMD5(appid + uid) + time);
	}
	
//	public static boolean isWebSocketEnabled()
//	{
//		// || ChatServiceController.isBetaVersion()
//		return ConfigManager.useWebSocketServer;// && (ChatServiceController.isInnerVersion() || ChatServiceController.getInstance().isUsingDummyHost());
//	}
//	
//	public static boolean isRecieveFromWebSocket(int channelType)
//	{
//		return isWebSocketEnabled() && ConfigManager.isRecieveFromWebSocket
//				&& isSupportedType(channelType);
//	}
//	
//	public static boolean isSendFromWebSocket(int channelType)
//	{
//		return isWebSocketEnabled() && (ConfigManager.isSendFromWebSocket || ChatServiceController.getInstance().isUsingDummyHost())
//				&& isSupportedType(channelType);
//	}
	
	public boolean isConnected()
	{
		return client != null && client.isOpen();
	}

	public void handleDisconnect()
	{
//		ServiceInterface.flyHint(null, "", "disconnect", 0, 0, false);
	}

	private final static String	LOGIN_SUCCESS_COMMAND				= "login.success";
	private final static String	LOGIN_FAILED_COMMAND				= "login.failed";
//	private final static String	JOIN_ROOM_COMMAND					= "room.join";
	private final static String	JOIN_ROOM_MULTI_COMMAND				= "room.joinMulti";
	private final static String	LEAVE_ROOM_COMMAND					= "room.leave";
	private final static String	SET_USER_INFO_COMMAND				= "user.setInfo";
	private final static String	SET_DEVICE_COMMAND					= "user.setDevice";
//	private final static String	GET_NEW_MSGS_COMMAND				= "history.rooms";
	private final static String	GET_NEW_MSGS_BY_TIME_COMMAND		= "history.roomsv2";
//	private final static String	GET_HISTORY_MSGS_COMMAND			= "history.room";
	private final static String	GET_HISTORY_MSGS_BY_TIME_COMMAND	= "history.roomv2";
	private final static String	ANOTHER_LOGIN_COMMAND				= "another.login";
	
	private final static String	SEND_USER_MSG_COMMAND				= "chat.user";
	private final static String	RECIEVE_USER_MSG_COMMAND			= "push.chat.user";
	private final static String	SEND_ROOM_MSG_COMMAND				= "chat.room";
	private final static String	RECIEVE_ROOM_MSG_COMMAND			= "push.chat.room";

	public void setUserInfo()
	{
		sendCommand(SET_USER_INFO_COMMAND, "info", getUserInfo());
	}
	
	private JSONObject getUserInfo()
	{
		try
		{
			JSONObject params = new JSONObject();
			params.put("userName", UserManager.getInstance().getCurrentUser().userName);
//			params.put("uid", UserManager.getInstance().getCurrentUser().uid);
//			params.put("lang", ConfigManager.getInstance().gameLang);
			return params;
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	public void sendUserMsg()
	{
		sendCommand(SEND_USER_MSG_COMMAND, "uid", UserManager.getInstance().getCurrentUser().uid, "msg", "test msg");
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
			params.put("appName", AppInfoUtil.getApplicationName());
			params.put("serverNum", getCurrentCountryId());
			params.put("appVersion", AppInfoUtil.getApplicationVersionName());
			params.put("deviceType", "android");
			params.put("systemVerson", android.os.Build.VERSION.RELEASE);
			return params;
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return null;
	}
	
	private static int getCurrentCountryId()
	{
		return UserManager.getInstance().getCurrentUser().serverId;
	}
	
	public void onSetDevice()
	{
		joinRoom();
	}
	
	private String rooms = "";
	private boolean roomsChanged;
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
		EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.CONNECTED));
		LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - startConnectTime, LogUtil.CHAT_CONNECT_TOTAL);
//		if (roomsChanged)
//		{
			getNewMsgs();
//		}
	}
	
	private void resetState()
	{
		rooms = "";
	}
	
	private JSONArray getJoinRoomsArray()
	{
		JSONArray array = null;
		try
		{
			array = new JSONArray();
			
			ArrayList<Channel> channels = IMCore.getInstance().getAppConfig().getPredefinedChannels();
			for (int i = 0; i < channels.size(); i++)
			{
				JSONObject room = new JSONObject();
				room.put("id", IMCore.getInstance().getAppConfig().getRoomId(channels.get(i)));
				room.put("group", IMCore.getInstance().getAppConfig().getGroupId(channels.get(i)));
				array.put(room);
			}
//			JSONObject country = new JSONObject();
//			country.put("id", getCountryRoomId());
//			country.put("group", GROUP_COUNTRY);
//			array.put(country);
//			
//			JSONObject battleField = new JSONObject();
//			battleField.put("id", getBattleFieldRoomId());
//			battleField.put("group", GROUP_BATTLE);
//			array.put(battleField);
//
//			if(UserManager.getInstance().isCurrentUserInAlliance()){
//				JSONObject alliance = new JSONObject();
//				alliance.put("id", getAllianceRoomId());
//				alliance.put("group", GROUP_ALLIANCE);
//				array.put(alliance);
//			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}
		return array;
	}

//	public void leaveAllianceRoom()
//	{
//		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getAllianceRoomId());
//	}
//	
//	public void leaveBattleFieldRoom()
//	{
//		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getBattleFieldRoomId());
//	}
//
//	public void leaveCountryRoom()
//	{
//		sendCommand(LEAVE_ROOM_COMMAND, "roomId", getCountryRoomId());
//	}

	public void sendRoomMsg(String messageText, int sendLocalTime, Channel channel, int post, String media)
	{
		String roomId = IMCore.getInstance().getAppConfig().getRoomId(channel);
		sendCommand(SEND_ROOM_MSG_COMMAND, "roomId", roomId, "msg", messageText, "sendTime", sendLocalTime, "extra", getMsgExtra(post, media));
	}
	
	private JSONObject getMsgExtra(int post, String media)
	{
		JSONObject extra = null;
		try
		{
			extra = new JSONObject();
			if(post != Msg.MSGITEM_TYPE_MESSAGE) extra.put("post", post);
			if(StringUtils.isNotEmpty(media)) extra.put("media", media);
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
		if(roomsParams != null){
			sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", roomsParams);
			roomsParams = null;
		}else{
			sendCommand(GET_NEW_MSGS_BY_TIME_COMMAND, "rooms", getRoomsParams());
		}
		startGetNewTime = TimeManager.getInstance().getCurrentTimeMS();
	}
	
	private JSONObject getRoomsParams()
	{
		JSONObject params = null;
		try
		{
			params = new JSONObject();

			ArrayList<Channel> channels = IMCore.getInstance().getAppConfig().getPredefinedChannels();
			for (int i = 0; i < channels.size(); i++)
			{
				params.put(IMCore.getInstance().getAppConfig().getRoomId(channels.get(i)), channels.get(i).getLatestTime());
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

	public void sendCommand(String command, Object... args)
	{
		if(!isConnected()) return;

		EventCenter.getInstance().dispatchEvent(new LogEvent("send: " + command));
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
		if(!isConnected()) return;
		
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
		EventCenter.getInstance().dispatchEvent(new LogEvent(output));
		
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
			
			String output = "";

			if(client.isMyMessage(json)){
				output = String.format("%s(%s): %s", command, "from myself", message);
				LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, output);
				EventCenter.getInstance().dispatchEvent(new LogEvent(output));
				return;
			}
			
			if (json.has("data")) // 由服务端主动推送的数据
			{
				output = String.format("%s(%s): %s", command, "push", message);
				onRecieveMessage(message);
			}
			else
			// 客户端发送命令时，服务端处理完命令推送的数据
			{
				if (json.has("result"))
				{
					output = String.format("%s(%s): %s", command, "send success", message);
					onCommandSuccess(message);
				}
				else if (json.has("error")) // 发生错误
				{
//					ServiceInterface.flyHint(null, "", command + " error: " + message, 0, 0, false);
					output = String.format("%s(%s): %s", command, "send error", message);
				}
			}
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, output);
			EventCenter.getInstance().dispatchEvent(new LogEvent(output));
		}
		catch (JSONException e)
		{
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_WS_RECIEVE, "JSONException: " + message);
			LogUtil.printException(e);
		}
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
				if(result.opt("status") != null && result.getString("status").equals("ok") && needInit)
				{
					onSetDevice();
				}
			}
			else if (command.equals(JOIN_ROOM_MULTI_COMMAND))
			{
				// {"status":true,"id":["id1","id2"]}
				if(result.optBoolean("status") && result.getBoolean("status"))
				{
					onJoinRoom();
				}
			}
			else if (command.equals(GET_NEW_MSGS_BY_TIME_COMMAND))
			{
				onGetNewMsg(result);
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

			}
			else if (command.equals(RECIEVE_ROOM_MSG_COMMAND))
			{
				onRecieveRoomMessage(data);
			}else if (command.equals(ANOTHER_LOGIN_COMMAND))
			{
				// 同一个uid在不同地方登陆会这样，发生这种情况游戏应该就不让登陆了
				EventCenter.getInstance().dispatchEvent(new WSStatusEvent(WSStatusEvent.DISCONNECTED));
				forceClose();
			}else if (command.equals(LOGIN_SUCCESS_COMMAND))
			{
				client.setClientID(json);
				this.onLoginSuccess(json);
			}else if (command.equals(LOGIN_FAILED_COMMAND))
			{
				this.onLoginFailed(json);
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
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
			Msg item = parseMsgItem(data);
			if (item != null)
			{
				Msg[] dbItemsArray = { item };
				String customName = "";
				Channel channel = getChannel(data.getString("group"));
				item.channelID = channel.getChannelID();
				SyncController.handleMessage(dbItemsArray, channel.getChannelID(), customName, false, true);
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
	
	private Msg parseMsgItem(JSONObject msg)
	{
		try
		{
			Msg item = new Msg();
			
			// 除了从db获取，都为true
			item.isNewMsg = true;
			
			item.sequenceId = -1;
			if (msg.opt("seqId") != null && msg.getInt("seqId") > 0)
			{
				item.sequenceId = msg.getInt("seqId");
			}
			
			item.uid = msg.getString("sender");
			item.msg = msg.getString("msg");
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
			item.channelType = IMCore.getInstance().getAppConfig().group2channelType(msg.getString("group"));
			
			if (msg.opt("originalLang") != null)
			{
				item.originalLang = msg.getString("originalLang");
			}
			if (msg.opt("translationMsg") != null)
			{
				item.translateMsg = msg.getString("translationMsg");
			}
			
			item.post = 0;
			if (msg.opt("type") != null)
			{
				item.post = msg.getInt("type");
			}
//			item.mailId = "";
//			
//			if(msg.optJSONObject("senderInfo") != null){
//				JSONObject senderInfo = msg.getJSONObject("senderInfo");
//			}
			JSONObject extra = null;
			if(msg.optJSONObject("extra") != null){
				extra = msg.getJSONObject("extra");

				if (extra.opt("seqId") != null && extra.getInt("seqId") > 0)
				{
					item.sequenceId = extra.getInt("seqId");
				}
				
				if (isStringExist(extra, "lastUpdateTime"))
				{
					try
					{
						item.lastUpdateTime = Integer.parseInt(extra.getString("lastUpdateTime"));
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
					// 从装备信息来决定消息，得放到扩展中
//					if(item.isEquipMessage() && StringUtils.isNotEmpty(item.attachmentId))
//					{
//						item.msg = item.attachmentId;
//						if(StringUtils.isNotEmpty(item.attachmentId))
//						{
//							String[] equipStrArr = item.attachmentId.split("\\|");
//							if(equipStrArr.length == 2)
//							{
//								item.attachmentId = equipStrArr[1];
//							}
//						}
//					}
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
			
			// TODO 红包消息、他人语音不是我发的，只要收到就认为发送成功，不需要发送状态
			// 这两种消息的sendState有其它用途（标记是否打开、语音是否已读），所以这里不能设置为SEND_SUCCESS
//			if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
				item.sendState = Msg.SEND_SUCCESS;
			
			if(item.sequenceId == -1)
			{
				LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "item.sequenceId", item.sequenceId);
			}
			
			return item;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}
	
	private void parseAttachment(JSONObject extra, Msg item) throws JSONException
	{
		// 联盟加入：allianceId
		parseAttachment(extra, item, "allianceId");
		// 战报：reportUid
		parseAttachment(extra, item, "reportUid");
		// 侦察战报：detectReportUid
		parseAttachment(extra, item, "detectReportUid");
		// 装备：equipId
		parseAttachment(extra, item, "equipId");
		// 集结：teamUuid
		parseAttachment(extra, item, "teamUuid");
		// 转盘：lotteryInfo
		parseAttachment(extra, item, "lotteryInfo");
		// 地块分享：attachmentId
		parseAttachment(extra, item, "attachmentId");
		
		if (extra.opt("redPackets") != null && extra.opt("server") != null)
		{
			item.attachmentId = extra.getString("redPackets") + "_" + extra.getString("server");
		}
	}
	
	private void parseAttachment(JSONObject extra, Msg item, String propName) throws JSONException
	{
		if (extra.opt(propName) != null)
		{
			item.attachmentId = extra.getString(propName);
		}
	}

	private Channel getChannel(String group)
	{
		return ChannelManager.getInstance().getChannel(IMCore.getInstance().getAppConfig().group2channelType(group));
	}
	
	private void onGetNewMsg(JSONObject result)
	{
//		LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - startGetNewTime, LogUtil.CHAT_CONNECT_GET_NEW);
		try
		{
			JSONArray rooms = result.getJSONArray("rooms");
			for (int i = 0; i < rooms.length(); i++)
			{
				JSONObject room = rooms.getJSONObject(i);
				
				Channel channel = getChannel(room.getString("group"));
				if (channel == null)
				{
					continue;
				}
				
				String roomId = room.getString("roomId"); 
				long firstMsgTime = room.getLong("firstMsgTime");
				long lastMsgTime = room.getLong("lastMsgTime");
				int firstSeqId = room.getInt("firstSeqId");
				int lastSeqId = room.getInt("lastSeqId");
				
				channel.serverMaxTime = lastMsgTime;
				channel.serverMinTime = firstMsgTime;
				
				JSONArray msgs = room.getJSONArray("msgs");
				
				channel.wsNewMsgCount = msgs.length();
				
				if(msgs.length() == 0)
				{
					channel.loadMoreMsg();
					continue;
				}
				
				Msg[] msgArr = new Msg[msgs.length()];
				Msg firstMsg = null;
				for (int j = 0; j < msgs.length(); j++)
				{
					JSONObject msg = msgs.getJSONObject(j);
					Msg item = parseMsgItem(msg);
					item.channelID = channel.getChannelID();
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
				if(msgs.length() > 1)
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
				
				SyncController.handleMessage(msgArr, IMCore.getInstance().getAppConfig().roomId2channelId(roomId), "", true, true);
			}
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}

		if(!connectAsSupervisor && canTestServer())
			testServers();
	}

	public void leaveRoom(String roomId)
	{
		sendCommand(LEAVE_ROOM_COMMAND, "roomId", roomId);
	}
}