package com.elex.chatservice.mqtt;

public interface MqttConstants
{
	public static final String	MQTT_PUSH							= "mqtt.push";
	public final static String	TAG_MQTT_SEND						= "mqtt.send";
	public final static String	TAG_MQTT_RECIEVE					= "mqtt.recieve";
	public final static String	TAG_HTTP_SEND						= "http.send";
	public final static String	TAG_HTTP_RECIEVE					= "http.recieve";
	public final static String	TAG_MQTT_ERROR_RESPONSE				= "mqtt.error";

	public static final String	APP_ID								= "100001";
	// public static final String TEST_SESSION = "586f44ff764c5210000116542458";
	// public static final String TEST_SESSION = "586f4cd02bf09610000169866512";
	public static final String	TEST_SESSION						= "";
	public static final String	MQTT_HTTP_URL						= "http://api.cok.chat/commond/json";
	public static final String	DEBUG_TAG							= "MqttService";						// Debug TAG
	public static final String	MQTT_THREAD_NAME					= "MqttService[" + DEBUG_TAG + "]";		// Handler Thread ID
	public static final String	MQTT_BROKER							= "m2m.eclipse.org";					// Broker URL or IP Address
	public static final String	MQTT_PROTOCOL						= "tcp";
	public static final String	MQTT_ADDRESS						= "104.196.220.201";
	public static final String	MQTT_PORT							= "1883";								// Broker Port
	public static final int		MQTT_QOS_0							= 0;									// QOS Level 0 ( Delivery Once no confirmation )
	public static final int		MQTT_QOS_1							= 1;									// QOS Level 1 ( Delevery at least Once with
																											// confirmation )
	public static final int		MQTT_QOS_2							= 2;									// QOS Level 2 ( Delivery only once with
																											// confirmation
																											// with handshake )
	public static final int		MQTT_KEEP_ALIVE						= 240000;								// KeepAlive Interval in MS
	public static final String	MQTT_KEEP_ALIVE_TOPIC_FORAMT		= "/users/%s/keepalive";				// Topic format for KeepAlives
	public static final byte[]	MQTT_KEEP_ALIVE_MESSAGE				= { 0 };								// Keep Alive message to send
	public static final int		MQTT_KEEP_ALIVE_QOS					= MQTT_QOS_0;							// Default Keepalive QOS
	public static final boolean	MQTT_CLEAN_SESSION					= true;									// Start a clean session?
	public static final String	MQTT_URL_FORMAT						= "%s://%s:%s";							// URL Format normally don't change
	public static final String	ACTION_START						= DEBUG_TAG + ".START";					// Action to start
	public static final String	ACTION_STOP							= DEBUG_TAG + ".STOP";					// Action to stop
	public static final String	ACTION_KEEPALIVE					= DEBUG_TAG + ".KEEPALIVE";				// Action to keep alive used by alarm manager
	public static final String	ACTION_RECONNECT					= DEBUG_TAG + ".RECONNECT";				// Action to reconnect
	public static final String	DEVICE_ID_FORMAT					= "andr_%s";							// Device ID Format, add any prefix you'd like
	/* 测试结果使用的默认有效时间 */
	public static final int		DEFAULT_TEST_RESULT_VALID_TIME		= 5 * 24 * 3600 * 1000;

	public static final String	GROUP_COUNTRY						= "country";
	public static final String	GROUP_ALLIANCE						= "alliance";
	public static final String	GROUP_ORIGINAL						= "original";
	public static final String	GROUP_RANDOM_LOCAL					= "local";
	public static final String	GROUP_RANDOM_GLOBAL					= "global";
	public static final String	GROUP_DRAGON_OBSERVER				= "observer";
	public static final String	GROUP_DRAGON_DANMU					= "danmu";
	public static final String	GROUP_VOIP							= "voip";

	public static final int		USER_CHAT_MSG_TYPE_TEXT				= 0;
	public static final int		USER_CHAT_MSG_TYPE_AUDIO			= 1;

	public static final int		USER_CHAT_MODE_NORMAL				= 1;
	public static final int		USER_CHAT_MODE_MOD					= 2;
	public static final int		USER_CHAT_MODE_DRIFTING_BOTTLE		= 3;
	public static final int		USER_CHAT_MODE_NEARBY				= 4;

	// command
	public static final String	GET_SERVER_LIST_COMMAND				= "mqtt.server";
	public static final String	GET_SUB_LIST_COMMAND				= "mqtt.subs";
	public static final String	GET_NEW_MSGS_COMMAND				= "history.roomsv2";
	public static final String	SEND_ROOM_MSG_COMMAND				= "chat.room";
	public final static String	GET_NEW_USERCHAT_BY_TIME_COMMAND	= "history.users";

	public static final String	SEND_USER_MSG_COMMAND				= "chat.user";
	public static final String	LEAVE_ROOM_COMMAND					= "room.leave";
	public static final String	SET_USER_INFO_COMMAND				= "user.setInfo";
	public static final String	INIT_USER_COMMAND					= "user.init";
	public static final String	ANOTHER_LOGIN_COMMAND				= "another.login";
	public static final String	SET_DEVICE_COMMAND					= "user.setDevice";
	public static final String	GET_RANDOM_CHATROOM_LOCAL			= "room.getLocalRoom";
	public static final String	GET_RANDOM_CHATROOM_GLOBAL			= "room.getGlobalRoom";
	public static final String	UPLOAD_LOCATION						= "user.setPosition";
	public static final String	CLEAR_LOCATION						= "user.delPosition";
	public static final String	GET_NEARBY_USER						= "user.getNearBy";
	public static final String	CHAT_USER_MARK_READ					= "chat.userMarkRead";
	public static final String	CHAT_USER_DEL						= "chat.userDel";
	public static final String	NEARBY_LIKE							= "like.add";
	public static final String	NEARBY_LIKE_CANCEL					= "like.del";
	public static final String	REALTIME_VOICE_JOIN_COMMAND			= "voip.join";
	public static final String	REALTIME_VOICE_LEAVE_COMMAND		= "voip.leave";
	public static final String	REALTIME_VOICE_CHANGE_COMMAND		= "voip.change";
	public static final String	RECIEVE_USER_MSG_COMMAND			= "push.chat.user";
	public static final String	RECIEVE_ROOM_MSG_COMMAND			= "push.chat.room";
	public static final String	PUSH_RANDOM_CHATROOM_DESTORY		= "push.room.change";
	public static final String	PUSH_ROOM_MID_SYS_MSG				= "push.room.midSystemMsg";
	public static final String	PUSH_EXEPRESSION_BUY				= "push.player.expression";
	public static final String	PUSH_REALTIME_VOICE_CHANGE			= "push.voip.change";
	public static final String	GET_MQTT_NETWORK_COMMAND	  		= "mqtt.net";
	
}
