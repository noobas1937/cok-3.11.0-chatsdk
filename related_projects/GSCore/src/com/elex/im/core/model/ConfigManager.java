package com.elex.im.core.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.db.DBHelper;
import com.elex.im.core.util.FileUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.image.AsyncImageLoader;

/**
 * 全局配置
 */
public class ConfigManager
{
	private static ConfigManager	instance;

	/** 只对当前用户有意义，不存数据库 */
	public boolean					scaleFontandUI						= true;
	public boolean					enableChatInputField				= false;
	public boolean					isFirstJoinAlliance					= true;
	/** 用于论坛判定默认显示语言，尚未使用，与游戏进程相关 */
	public String					gameLang							= "en";
	public boolean					isRTLEnable							= false;

	/** 后台可配置，会被复写 */
	public static int				sendInterval						= 1000;
	/** 自定义头像开关 */
	public static boolean			enableCustomHeadImg					= true;
	public static int				autoTranlateMode					= 0;
	public static boolean			enableChatHorn						= true;
	/** 喇叭消息最大输入长度 */
	public static int				maxHornInputLength					= 140;
	public static boolean			isFirstUserHorn						= true;
	public static boolean			isFirstUserCornForHorn				= true;
	public static boolean			isHornBtnEnable						= false;
	public static boolean			useWebSocketServer					= false;
	public static boolean			isRecieveFromWebSocket				= true;
	public static boolean			isSendFromWebSocket					= true;
	public static boolean			isXMEnabled							= true;
	public static boolean			isXMAudioEnabled					= true;
	public static boolean			isXMVedioEnabled					= false;

	private Map<String, String>		dynamicImageMap						= null;
	private boolean					hasMergered							= false;

	public static boolean			playAudioBySpeaker					= true;

	public static final int			NETWORK_DISCONNECTED				= 1;							// 网络信号断开
	public static final int			NETWORK_OPENED						= 2;							// 网络信号打开
	public static final int			SERVER_DISCONNECTED					= 3;							// 服务器断开
	public static final int			NETWORK_CONNECTING					= 4;							// 正在连接
	public static final int			NETWORK_CONNECTED					= 5;							// 连接成功
	public static final int			NETWORK_CONNECTE_FAILED				= 6;							// 连接失败

	public static final int			WEBSOCKET_NETWORK_CONNECTING		= 7;							// websocket网络正在连接
	public static final int			WEBSOCKET_NETWORK_CONNECTED			= 8;							// websocket网络连接成功
	public static final int			WEBSOCKET_NETWORK_CONNECTE_FAILED	= 9;							// websocket网络连接失败
	public static final int			WEBSOCKET_SERVER_DISCONNECTED		= 10;							// websocket网络服务器断开

	public static final int			ACTIVITY_STATE_NORMAL				= 0;
	public static final int			MAIL_PULLING						= 1;

	public static int				network_state						= NETWORK_CONNECTED;
	public static int				websocket_network_state				= WEBSOCKET_NETWORK_CONNECTED;
	public static int				mail_pull_state						= ACTIVITY_STATE_NORMAL;
	
	public static int 			 	localUpdateConfigVersion			= -1 ;
	
	private LocalConfig				localConfig							= null;
	
	public static final String		DYNAMIC_IMAGE_CDN_URL				= "http://cok.eleximg.com/cok/config/chat_service_res/";

	public boolean isArabLang()
	{
		return StringUtils.isNotEmpty(gameLang) && (gameLang.equals("ara") || gameLang.equals("ar"));
	}
	
	public boolean needRTL()
	{
		return isArabLang() && isRTLEnable;
	}
	
	public static boolean isNetWorkConnecting()
	{
		return network_state == NETWORK_CONNECTING;
	}
	
	public static String getCDNUrl(String fileName)
	{
		return DYNAMIC_IMAGE_CDN_URL + fileName;
	}

	public static boolean isNetWorkError()
	{
		if (network_state == NETWORK_DISCONNECTED || network_state == NETWORK_OPENED || network_state == SERVER_DISCONNECTED
				|| network_state == NETWORK_CONNECTE_FAILED)
			return true;
		return false;
	}

	public static boolean isWebSocketNetWorkConnecting()
	{
		return websocket_network_state == WEBSOCKET_NETWORK_CONNECTING;
	}

	public static boolean isWebSocketNetWorkError()
	{
		return websocket_network_state == WEBSOCKET_NETWORK_CONNECTE_FAILED || websocket_network_state == WEBSOCKET_SERVER_DISCONNECTED;
	}

	public static boolean isWebSocketNetWorkNormal()
	{
		return websocket_network_state == WEBSOCKET_NETWORK_CONNECTED;
	}

	public static boolean isNetWorkNormal()
	{
		return network_state == NETWORK_CONNECTED;
	}

	private ConfigManager()
	{
	}

	public static ConfigManager getInstance()
	{
		if (instance == null)
		{
			instance = new ConfigManager();
		}
		return instance;
	}

	private static boolean		calcSizeCompleted	= false;
	public static double		scaleRatio			= 0;
	public static double		scaleRatioButton	= 0;
	private final static double	designWidth			= 640;
	private final static double	designHeight		= 852;

	public static void calcScale(Context context)
	{
		if (calcSizeCompleted || context == null)
			return;

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		double screenWidth = windowManager.getDefaultDisplay().getWidth();
		double screenHeight = windowManager.getDefaultDisplay().getHeight();
		double scaleX = screenWidth / designWidth;
		double scaleY = screenHeight / designHeight;
		double minScale = Math.min(scaleX, scaleY);
		scaleRatio = minScale;
		scaleRatioButton = minScale;
		// 在大屏上字体可能会偏大，可能需要用dp计算才行,先加个修正因子
		scaleRatio = scaleRatio > (1 / 0.84390234277028) ? scaleRatio * 0.84390234277028 : scaleRatio;
		if (scaleRatio > 1)
		{
			// 小米pad是1.873170518056575
			scaleRatio = 1 + (scaleRatio - 1) * 0.5;
		}
		else
		{
			// 小屏幕不要缩放，否则太小（华为 U8800Pro 800x480）
			scaleRatio = 1 - (1 - scaleRatio) * 0.5;
		}
		calcSizeCompleted = true;
	}

	public boolean needScaleInputPanel()
	{
		int density = IMCore.hostActivity.getResources().getDisplayMetrics().densityDpi;

		return density >= DisplayMetrics.DENSITY_XHIGH && ScaleUtil.getScreenWidth() > 1280;
	}

	private String getLocalDynamicConfigPath()
	{
		return DBHelper.getLocalDirectoryPath(IMCore.hostActivity, "config");
	}

	private String getRemoteDynamicConfigPath()
	{
		return DBHelper.getLocalDirectoryPathWithOutSDCard(IMCore.hostActivity, "dresource");
	}
	
	public int getLocalUpdateConfigVersion()
	{
		if (localUpdateConfigVersion == -1)
		{
			String localJsonPath = getLocalDynamicConfigPath() + "pic_update_local.json";
			String json = readJsonFile(localJsonPath);
			if (StringUtils.isNotEmpty(json))
			{
				try
				{
					PicUpdateConfig config = JSON.parseObject(localJsonPath, PicUpdateConfig.class);
					if(config!=null)
						localUpdateConfigVersion = config.getVersion();
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return localUpdateConfigVersion;
	}
	
	public void updateLocalPicConfig()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				int version = getLocalUpdateConfigVersion();
				String updateJson = AsyncImageLoader.getHttpString(DYNAMIC_IMAGE_CDN_URL+"pic_update_remote.json");
				if (StringUtils.isNotEmpty(updateJson))
				{
					try
					{
						PicUpdateConfig config = JSON.parseObject(updateJson, PicUpdateConfig.class);
						if(config!=null && config.getVersion() > version)
						{
							String localJsonPath = getLocalDynamicConfigPath() + "pic_update_local.json";
							writeConfig(updateJson, localJsonPath);
							List<String> updateImageList = config.getUpdate();
							for(String imageName : updateImageList)
							{
								if(FileUtil.isUpdateImageExist(imageName))
								{
									AsyncImageLoader.getInstance().downLoadUpdateImage(DYNAMIC_IMAGE_CDN_URL+imageName, FileUtil.getCommonPicLocalPath(imageName));
								}
							}
							
						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public LocalConfig getLocalConfig()
	{
		if(localConfig == null)
		{
			String localConfigPath = getLocalDynamicConfigPath() + UserManager.getInstance().getCurrentUser().uid + "sharedpref.json";
			String json = readJsonFile(localConfigPath);
			if(StringUtils.isNotEmpty(json))
			{
				try
				{
					localConfig = JSON.parseObject(json, LocalConfig.class);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return localConfig;
	}
	
	public void saveLocalConfig()
	{
		if(localConfig == null)
			return;
		String localConfigPath = getLocalDynamicConfigPath() + UserManager.getInstance().getCurrentUser().uid + "sharedpref.json";
		try
		{
			String localConfigJson = JSON.toJSONString(localConfig);
			writeConfig(localConfigJson, localConfigPath);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setLocalConfig(LocalConfig localConfig)
	{
		this.localConfig = localConfig;
	}

	public Map<String, String> getLocalDynamicImageMap()
	{
		if (dynamicImageMap == null)
		{
			String localJsonPath = getLocalDynamicConfigPath() + UserManager.getInstance().getCurrentUser().uid + "sharedpref.json";
			dynamicImageMap = readLocalImageConfig(localJsonPath);
		}
		return dynamicImageMap;
	}

	public void mergeRemoteDynamicImageMap()
	{
		if (hasMergered)
			return;
		String remoteJsonPath = getRemoteDynamicConfigPath() + "image_config_remote.json";
		Map<String, String> map = readRemoteImageConfig(remoteJsonPath);
		if (map == null || map.size() <= 0)
			return;
		int remoteVersion = getConfigFileVersion(map);
		if (remoteVersion < 1)
			return;
		Map<String, String> localConfigMap = getLocalDynamicImageMap();
		int localVersion = getConfigFileVersion(localConfigMap);
		if (remoteVersion <= localVersion)
			return;
		localConfigMap.putAll(map);
		writeDynamicImageConfig(localConfigMap);
		hasMergered = true;
	}

	private int getConfigFileVersion(Map<String, String> map)
	{
		if (map == null)
			return 0;
		String remoteVersion = map.get("version");
		int version = 0;
		if (StringUtils.isNumeric(remoteVersion) && !remoteVersion.contains("."))
			version = Integer.parseInt(remoteVersion);
		return version;
	}

	private Map<String, String> readLocalImageConfig(String jsonPath)
	{
		String json = readJsonFile(jsonPath);
		Map<String, String> configMap = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(json))
		{
			try
			{
				JSONObject jsonObject = JSON.parseObject(json);
				if (jsonObject != null)
				{
					Set<String> keySet = jsonObject.keySet();

					for (String key : keySet)
					{
						configMap.put(key, jsonObject.getString(key));
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return configMap;
	}

	private Map<String, String> readRemoteImageConfig(String jsonPath)
	{
		String json = readJsonFile(jsonPath);
		Map<String, String> configMap = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(json))
		{
			try
			{
				JSONObject jsonObject = JSON.parseObject(json);
				Set<String> keySet = jsonObject.keySet();

				for (String key : keySet)
				{
					if (key.equals("version"))
						configMap.put(key, jsonObject.getString(key));
					else
					{
						JSONArray jsonArray = jsonObject.getJSONArray(key);
						if (jsonArray != null)
						{
							for (int i = 0; i < jsonArray.size(); i++)
							{
								configMap.put(jsonArray.getString(i), key);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		return configMap;
	}

	public String readJsonFile(String path)
	{
		BufferedReader reader = null;
		StringBuffer jsonStrBuffer = new StringBuffer();
		File file = new File(path);
		try
		{
			if (!file.exists())
				file.createNewFile();
			FileReader fileRead = new FileReader(file);
			reader = new BufferedReader(fileRead);
			String tempString = null;
			while ((tempString = reader.readLine()) != null)
			{
				jsonStrBuffer.append(tempString);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return jsonStrBuffer.toString();
	}
	
	private void writeConfig(String json,String savePath)
	{
		if (StringUtils.isEmpty(json) || StringUtils.isEmpty(savePath))
			return;
		File file = new File(savePath);
		FileWriter fileWriter = null;
		BufferedWriter bufferWriter = null;
		try
		{

			if (!file.exists())
				file.createNewFile();

			fileWriter = new FileWriter(file);
			bufferWriter = new BufferedWriter(fileWriter);
			bufferWriter.write(json);
			bufferWriter.flush();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bufferWriter != null)
					bufferWriter.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void writeDynamicImageConfig(Map<String, String> map)
	{
		if (map == null)
			return;
		String mergeJson = JSON.toJSONString(map);
		String localJsonPath = getLocalDynamicConfigPath() + "image_config_local.json";
		writeConfig(mergeJson, localJsonPath);
	}
}
