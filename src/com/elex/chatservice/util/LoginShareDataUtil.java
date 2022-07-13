package com.elex.chatservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.net.XiaoMiToolManager;

public class LoginShareDataUtil
{
	private static final String fileName = "user_session";
	private static final String fileDir = "/data/data/com.elex.im.cok.app/config/";
	public static void saveLoginData()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_ALL);
		String pathName = getDBDirectoryPath();
		if (pathName == null || !isLoginShareDataJsonValid())
		{
			return;
		}

		File path = new File(pathName);
		File file = new File(pathName + fileName);
		FileOutputStream fos = null;
		try
		{
			if (!path.exists())
			{
				path.mkdir();
			}
			if (!file.exists())
			{
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			// fos = openFileOutput("my.txt", Context.MODE_PRIVATE);
			String text = getLoginShareDataJson().toString();
			fos.write(text.getBytes());
			fos.close();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		
		if(ChatServiceController.isValidateChat)
		{
			ChatServiceController.isValidateChat = false;
			LogUtil.tracker.exitToApp();
		}
	}

	public static LoginShareData getSavedLoginShareData()
	{
		JSONObject json = getSavedLoginDataJson();
		if (json != null)
		{
			return new LoginShareData(json);
		}
		return null;
	}
	
	public static JSONObject getSavedLoginDataJson()
	{
		String pathName = getDBDirectoryPath();
		if (pathName == null)
		{
			return null;
		}

		File path = new File(pathName);
		File file = new File(pathName + fileName);
		if (!path.exists() || !file.exists())
		{
			return null;
		}
		
		FileInputStream fin = null;
		try
		{
            fin = new FileInputStream(file);
            int length = fin.available();  
            byte[] buffer = new byte[length];  
            fin.read(buffer);  
            String text = EncodingUtils.getString(buffer, "UTF-8");  
            fin.close();
            
            JSONObject result = new JSONObject(text);
            return result;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		
		return null;
	}
	
	private static boolean isLoginShareDataJsonValid()
	{
//		if(ChatServiceController.getInstance().isWrapperApp())
//			return false;

		if(UserManager.getInstance().getCurrentUserId() == null)
			return false;
		
		return true;
	}

	private static JSONObject getLoginShareDataJson()
	{
		JSONObject shareData = new JSONObject();
		try
		{
//			String user = StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) ? "unknownUser" : UserManager.getInstance()
//					.getCurrentUserId();
			shareData.put("uid", UserManager.getInstance().getCurrentUserId());
			shareData.put("session", SharePreferenceUtil.getSharePreferenceString(ChatServiceController.hostActivity, SharePreferenceUtil.getChatSessionTokenKey(), ""));
			shareData.put("sessionExpire", SharePreferenceUtil.getSharePreferenceInt(ChatServiceController.hostActivity, SharePreferenceUtil.getChatSessionExpireKey(), 0));
			shareData.put("packageName", ChatServiceController.getPackageName());
			shareData.put("chatSessionEnable", SwitchUtils.chatSessionEnable);
			shareData.put("gameLang", ConfigManager.getInstance().gameLang);
			shareData.put("versionName", ChatServiceController.getApplicationVersionName());
			shareData.put("versionCode", ChatServiceController.getApplicationVersionCode());
			shareData.put("xmlVersion", ConfigManager.getInstance().xmlVersion);
			shareData.put("currentUser", UserManager.getInstance().getCurrentUser().getJson());
			
			JSONObject xiaomi = new JSONObject();
			xiaomi.put("pSkey", XiaoMiToolManager.pSkey);
			xiaomi.put("appId", XiaoMiToolManager.appId);
			xiaomi.put("appKey", XiaoMiToolManager.appKey);
			xiaomi.put("pId", XiaoMiToolManager.pId);
			xiaomi.put("gUid", XiaoMiToolManager.gUid);
			xiaomi.put("b2Token", XiaoMiToolManager.b2Token);
			shareData.put("xiaomi", xiaomi);
		}
		catch (JSONException e)
		{
			LogUtil.printException(e);
		}

		return shareData;
	}

	public static String getDBDirectoryPath()
	{
		if (DBHelper.isSDCardWritable())
		{
			// SD卡应用目录（卸载会删除）：context.getExternalFilesDir(null) + File.separator
			// + user + "/database/"
			String directory = Environment.getExternalStorageDirectory() + fileDir;
			// + File.separator + user + "/database/";
			if (DBHelper.prepareDirectory(directory))
			{
				return directory;
			}
		}

		return null;
	}
	
	public static class LoginShareData
	{
		public String uid;
		public String packageName;
		public String session;
		public int sessionExpire;
		public boolean chatSessionEnable;
		public JSONObject currentUser;
		public JSONObject xiaomi;
		
		public LoginShareData(JSONObject json)
		{
			if(json != null)
			{
				uid = json.optString("uid");
				packageName = json.optString("packageName");
				session = json.optString("session");
				sessionExpire = json.optInt("sessionExpire");
				chatSessionEnable = json.optBoolean("chatSessionEnable");
				currentUser = json.optJSONObject("currentUser");
				xiaomi = json.optJSONObject("xiaomi");
			}
		}
	}
}
