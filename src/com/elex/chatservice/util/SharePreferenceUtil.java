package com.elex.chatservice.util;
import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import java.util.Timer;
import java.util.TimerTask;

public class SharePreferenceUtil
{
	private static final String	PREFERENCES_FILE_NAME	= "chat_service_share_preferences";
	public static final String	CUSTOM_CHANNEL_TYPE		= "custom_channel_type_" + UserManager.getInstance().getCurrentUserId();
	public static final String	CUSTOM_CHANNEL_ID		= "custom_channel_id_" + UserManager.getInstance().getCurrentUserId();
	public static final String	MAIL_UPODATE_DATA		= "init_mail_update_data";// 保存最新拉取的mail信息
	public static String getSharePreferenceString(Context context, String key, final String defaultValue)
	{
		if (context == null)
			return defaultValue;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.getString(key, defaultValue);
	}

	public static void setPreferenceString(Context context, final String key, final String value)
	{
		if (context == null)
			return;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		settings.edit().putString(key, value).commit();
	}

	public static boolean getSharePreferenceBoolean(Context context, final String key, final boolean defaultValue)
	{
		if (context == null)
			return defaultValue;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(key, defaultValue);
	}

	public static boolean hasKey(Context context, final String key)
	{
		if (context == null)
			return false;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.contains(key);
	}

	public static void setPreferenceBoolean(Context context, final String key, final boolean value)
	{
		if (context == null)
			return;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		settings.edit().putBoolean(key, value).commit();
	}

	public static void setPreferenceInt(Context context, final String key, final int value)
	{
		if (context == null)
			return;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		settings.edit().putInt(key, value).commit();
	}

	public static int getSharePreferenceInt(Context context, final String key, final int defaultValue)
	{
		if (context == null)
			return defaultValue;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.getInt(key, defaultValue);
	}

	public static void setPreferenceFloat(Context context, final String key, final float value)
	{
		if (context == null)
			return;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		settings.edit().putFloat(key, value).commit();
	}

	public static float getSharePreferenceFloat(Context context, final String key, final float defaultValue)
	{
		if (context == null)
			return defaultValue;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.getFloat(key, defaultValue);
	}

	public static void setSettingLong(Context context, final String key, final long value)
	{
		if (context == null)
			return;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		settings.edit().putLong(key, value).commit();
	}

	public static long getSharePreferenceLong(Context context, final String key, final long defaultValue)
	{
		if (context == null)
			return defaultValue;
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		return settings.getLong(key, defaultValue);
	}

	public static void clearPreference(Context context, SharedPreferences p)
	{
		if (context == null)
			return;
		Editor editor = p.edit();
		editor.clear();
		editor.commit();
	}

	private static String getChatSessionBaseKey()
	{
		return "chat.login.session." + UserManager.getInstance().getCurrentUserId();
	}

	public static String getChatSessionTokenKey()
	{
		return getChatSessionBaseKey() + ".token";
	}

	public static String getChatSessionExpireKey()
	{
		return getChatSessionBaseKey() + ".expire";
	}

	public static String getChatSessionAllianceKey()
	{
		return getChatSessionBaseKey() + ".alliance";
	}

	public static String getChatSessionServerKey()
	{
		return getChatSessionBaseKey() + ".server";
	}

	public static String getChatSessionCrossServerKey()
	{
		return getChatSessionBaseKey() + ".cross.server";
	}
	
	private static boolean isSessionValid()
	{
		if (!SwitchUtils.chatSessionEnable)
		{
			return true;
		}
		else
		{
			return isSessionExist() && !isSessionExpired() && !isAccountChanged();
		}
	}

	private static boolean isSessionExist()
	{
		String localSession = getSharePreferenceString(ChatServiceController.hostActivity, getChatSessionTokenKey(), "");
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "token", localSession);
		boolean result = StringUtils.isNotEmpty(localSession);
		return result;
	}

	private static boolean isSessionExpired()
	{
		int expired = getSharePreferenceInt(ChatServiceController.hostActivity, getChatSessionExpireKey(), 0);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "time", expired);
		boolean result =  expired < TimeManager.getInstance().getCurrentTime();
		return result;
	}

	private static boolean isAccountChanged()
	{
		String allianceIdSaved = getSharePreferenceString(ChatServiceController.hostActivity, getChatSessionAllianceKey(), "");
		int serverIdSaved = getSharePreferenceInt(ChatServiceController.hostActivity, getChatSessionServerKey(), 0);
		int crossServerIdSaved = getSharePreferenceInt(ChatServiceController.hostActivity, getChatSessionCrossServerKey(), 0);
		boolean result = !allianceIdSaved.equals(UserManager.getInstance().getCurrentUser().allianceId)
				|| serverIdSaved != UserManager.getInstance().getCurrentUser().serverId
				|| crossServerIdSaved != UserManager.getInstance().getCurrentUser().crossFightSrcServerId;
		if(result)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, 
					"allianceIdSaved", allianceIdSaved, "allianceId", UserManager.getInstance().getCurrentUser().allianceId,
					"serverIdSaved", serverIdSaved, "serverId", UserManager.getInstance().getCurrentUser().serverId,
					"crossServerIdSaved", crossServerIdSaved, "crossServerId", UserManager.getInstance().getCurrentUser().crossFightSrcServerId);
		}
		return result;
	}

	public static long startAuthTime;
	public static boolean checkSession()
	{
		if(!isSessionValid())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "create chat session");
			createSession();
			return false;
		}
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "valid");
		return true;
	}
	
	public static int createSessionCount = 0;
	public static boolean isCreatingSession = false;
    public static Timer timer  = null;
    public static void createSession()
    {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "isCreatingSession", isCreatingSession);
        if(isCreatingSession) return;
        
        isCreatingSession = true;
        if(!SwitchUtils.mqttEnable)
        {
            if(WebSocketManager.getInstance().isConnected())
            {
                WebSocketManager.getInstance().forceClose();
            }
        }
        else
        {
            if(MqttManager.getInstance().isMqttClientConnected())
            {
                MqttManager.getInstance().forceClose();
            }
        }
        
        // 0代表第一次,从第二次开始，等待5秒，10秒，15秒，20秒，25秒，30秒去请求，最后才重置，从 5秒开始，开始重复循环
        if (createSessionCount < 1 ){
            JniController.getInstance().excuteJNIVoidMethod("createChatSession", null);
        }else {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    JniController.getInstance().excuteJNIVoidMethod("createChatSession", null);
                    timer.cancel();
                    timer = null;
                }
            };
            if (createSessionCount !=0){
                timer.schedule(task, createSessionCount*5000 , 1000);
            }
        }
        
        startAuthTime = TimeManager.getInstance().getCurrentTimeMS();
        if(!SwitchUtils.mqttEnable)
        {
            if (WebSocketManager.getInstance().getStatusListener() != null)
            {
                WebSocketManager.getInstance().getStatusListener()
                .onStatus(LanguageManager.getLangByKey(LanguageKeys.WEB_SOCKET_AUTHORISING));
            }
        }
        
        createSessionCount++;
        if (createSessionCount >= 6){
            // 重置为0
            createSessionCount = 0;
        }
    }
	public static void setChatSession(String session, int expire)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "session", session, "expire", expire);
		setPreferenceString(ChatServiceController.hostActivity, getChatSessionTokenKey(), session);
		setPreferenceInt(ChatServiceController.hostActivity, getChatSessionExpireKey(), expire);
		setPreferenceString(ChatServiceController.hostActivity, getChatSessionAllianceKey(), UserManager.getInstance().getCurrentUser().allianceId);
		setPreferenceInt(ChatServiceController.hostActivity, getChatSessionServerKey(), UserManager.getInstance().getCurrentUser().serverId);
		setPreferenceInt(ChatServiceController.hostActivity, getChatSessionCrossServerKey(), UserManager.getInstance().getCurrentUser().crossFightSrcServerId);
	}
}
