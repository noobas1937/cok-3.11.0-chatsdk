package com.elex.im.core.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.event.WSStatusEvent;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.net.WebSocketManager;


public class SharePreferenceUtil
{
	private static final String	PREFERENCES_FILE_NAME	= "chat_service_share_preferences";
	public static final String	CUSTOM_CHANNEL_TYPE		= "custom_channel_type_" + UserManager.getInstance().getCurrentUser().uid;
	public static final String	CUSTOM_CHANNEL_ID		= "custom_channel_id_" + UserManager.getInstance().getCurrentUser().uid;
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
		if (!WebSocketManager.chatSessionEnable)
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
		boolean result = StringUtils.isNotEmpty(getSharePreferenceString(IMCore.hostActivity, getChatSessionTokenKey(), ""));
		if(!result)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "token", getSharePreferenceString(IMCore.hostActivity, getChatSessionTokenKey(), ""));
		}
		return result;
	}

	private static boolean isSessionExpired()
	{
		boolean result = getSharePreferenceInt(IMCore.hostActivity, getChatSessionExpireKey(), 0) < TimeManager.getInstance().getCurrentTime();
		if(result)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL, "time", getSharePreferenceInt(IMCore.hostActivity, getChatSessionExpireKey(), 0));
		}
		return result;
	}

	private static boolean isAccountChanged()
	{
		String allianceIdSaved = getSharePreferenceString(IMCore.hostActivity, getChatSessionAllianceKey(), "");
		int serverIdSaved = getSharePreferenceInt(IMCore.hostActivity, getChatSessionServerKey(), 0);
		int crossServerIdSaved = getSharePreferenceInt(IMCore.hostActivity, getChatSessionCrossServerKey(), 0);
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
	public static void createSession()
	{
		isCreatingSession = true;
		if(WebSocketManager.getInstance().isConnected())
		{
			WebSocketManager.getInstance().forceClose();
		}
		IMCore.getInstance().getAppConfig().excuteJNIVoidMethod("createChatSession", null);
		
		startAuthTime = TimeManager.getInstance().getCurrentTimeMS();
		
		IMCore.getInstance().dispatch(new WSStatusEvent(WSStatusEvent.AUTHORISING));
		
		createSessionCount++;
	}
	
	public static void setChatSession(String session, int expire)
	{
		setPreferenceString(IMCore.hostActivity, getChatSessionTokenKey(), session);
		setPreferenceInt(IMCore.hostActivity, getChatSessionExpireKey(), expire);
		setPreferenceString(IMCore.hostActivity, getChatSessionAllianceKey(), UserManager.getInstance().getCurrentUser().allianceId);
		setPreferenceInt(IMCore.hostActivity, getChatSessionServerKey(), UserManager.getInstance().getCurrentUser().serverId);
		setPreferenceInt(IMCore.hostActivity, getChatSessionCrossServerKey(), UserManager.getInstance().getCurrentUser().crossFightSrcServerId);
	}
}
