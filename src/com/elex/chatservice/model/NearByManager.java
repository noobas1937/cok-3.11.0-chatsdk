package com.elex.chatservice.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class NearByManager
{
	private static NearByManager		mInstance			= null;
	private Map<String, NearByUserInfo>	nearByUserMap		= null;
	private boolean						permissionGot		= false;
	private boolean						hasUploadLocation	= false;
	private boolean						hasSearchNearByUser	= false;
	private boolean						clearLocation		= false;
	private int							enter_list_type		= 1;
	public static int					todayLikeNumLimit	= 3;
	public List<String>					todayLikeUidList	= null;
	public static final int				NEARBY_LIKE			= 1;
	public static final int				NEARBY_LIKE_CANCEL	= 2;

	public int getTodayLikeNum()
	{
		if (todayLikeUidList != null)
			return todayLikeUidList.size() >= todayLikeNumLimit ? 0 : todayLikeNumLimit - todayLikeUidList.size();
		else
			return todayLikeNumLimit;
	}

	public int getEnter_list_type()
	{
		return enter_list_type;
	}

	public void setEnter_list_type(int enter_list_type)
	{
		this.enter_list_type = enter_list_type;
	}

	private NearByManager()
	{
		nearByUserMap = new HashMap<String, NearByUserInfo>();
		todayLikeUidList = new ArrayList<String>();
	}

	public List<String> getTodayLikeUidList()
	{
		return todayLikeUidList;
	}

	public void addTodayLikeUid(String uid)
	{
		if (todayLikeUidList != null && StringUtils.isNotEmpty(uid) && !todayLikeUidList.contains(uid))
		{
			todayLikeUidList.add(uid);
		}
	}

	public void removeTodayLikeUid(String uid)
	{
		if (todayLikeUidList != null && StringUtils.isNotEmpty(uid) && todayLikeUidList.contains(uid))
		{
			todayLikeUidList.remove(uid);
		}
	}

	public void initTodayLikeUid(List<String> uidList)
	{
		if (uidList == null || uidList.size() == 0)
			return;
		if (todayLikeUidList != null)
			todayLikeUidList.clear();
		for (String uid : uidList)
			todayLikeUidList.add(uid);
	}

	public boolean isHasUploadLocation()
	{
		return hasUploadLocation;
	}

	public boolean isClearLocation()
	{
		return clearLocation;
	}

	public void setClearLocation(boolean clearLocation)
	{
		this.clearLocation = clearLocation;
	}

	public void setHasUploadLocation(boolean hasUploadLocation)
	{
		this.hasUploadLocation = hasUploadLocation;
	}

	public boolean isHasSearchNearByUser()
	{
		return hasSearchNearByUser;
	}

	public void setHasSearchNearByUser(boolean hasSearchNearByUser)
	{
		this.hasSearchNearByUser = hasSearchNearByUser;
	}

	public static NearByManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (NearByManager.class)
			{
				if (mInstance == null)
					mInstance = new NearByManager();
			}
		}
		return mInstance;
	}

	public List<NearByUserInfo> getNearByUserArray()
	{
		List<NearByUserInfo> userArray = new ArrayList<NearByUserInfo>();
		if (nearByUserMap != null && nearByUserMap.values() != null)
		{
			for (NearByUserInfo user : nearByUserMap.values())
				userArray.add(user);
		}
		Collections.sort(userArray);
		return userArray;
	}

	public void addNearByUser(NearByUserInfo userInfo)
	{
		if (userInfo == null || StringUtils.isEmpty(userInfo.getUid()))
			return;
		nearByUserMap.put(userInfo.getUid(), userInfo);
	}

	public void resetData()
	{
		if (nearByUserMap != null)
			nearByUserMap.clear();
	}

	public void onPermissionGot()
	{
		permissionGot = true;
		if (ChatServiceController.getNearByListActivity() != null)
		{
			ChatServiceController.getNearByListActivity().runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					ChatServiceController.getNearByListActivity().uploadLocation();
				}
			});
		}
	}

	public boolean isPermissionGot()
	{
		return permissionGot;
	}

	/**
	 * 跳转定位服务界面
	 *
	 * @param context
	 *            全局信息接口
	 */
	public void gotoLocServiceSettings(Context context)
	{
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 跳转WIFI服务界面
	 *
	 * @param context
	 *            全局信息接口
	 */
	public static void gotoWifiServiceSettings(Context context)
	{
		final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public void likeNearbyUser(String uid)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "uid", uid);
		if (!SwitchUtils.mqttEnable)
			WebSocketManager.getInstance().likeNearbyUser(uid);
		else
			MqttManager.getInstance().likeNearbyUser(uid);
	}

	public void cancelLikeNearbyUser(String uid)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "uid", uid);
		if (!SwitchUtils.mqttEnable)
			WebSocketManager.getInstance().cancelLikeNearbyUser(uid);
		else
			MqttManager.getInstance().cancelLikeNearbyUser(uid);
	}

	public void updateNearbyLikeData(String uid, int type)
	{
		if (StringUtils.isNotEmpty(uid))
		{
			if (type == NEARBY_LIKE)
				addTodayLikeUid(uid);
			else if (type == NEARBY_LIKE_CANCEL)
				removeTodayLikeUid(uid);

			if (nearByUserMap.containsKey(uid))
			{
				NearByUserInfo userInfo = nearByUserMap.get(uid);
				if (userInfo != null)
				{
					int likeNum = userInfo.getLikeNum();
					if (type == NEARBY_LIKE)
						userInfo.setLikeNum(likeNum + 1);
					else if (type == NEARBY_LIKE_CANCEL)
					{
						likeNum -= 1;
						if (likeNum < 0)
							likeNum = 0;
						userInfo.setLikeNum(likeNum);
					}
				}
			}

			if (ChatServiceController.getNearByListActivity() != null)
			{
				ChatServiceController.getNearByListActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						if (ChatServiceController.getNearByListActivity() != null)
							ChatServiceController.getNearByListActivity().refreshNearbyData();
					}
				});
			}
		}
	}

}
