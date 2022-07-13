package com.elex.im.cok;

import android.util.Log;

import com.elex.im.CokConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.SharePreferenceUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.ui.GSController;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.view.autoscroll.ScrollTextManager;

public class ServiceInterface
{
	/**
	 * 若isAccountChanged，则强制重新初始化db
	 */
	public static void initDatabase(boolean isAccountChanged, boolean isNewUser)
	{
		DBManager.initDatabase(isAccountChanged, isNewUser);
	}

	public static void setCurrentUserId(String uidStr)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uidStr);
		UserManager.getInstance().setCurrentUserId(uidStr);
	}

	private static User	currentUserClone;

	/**
	 * 初始登录、重新登录、切服时会调用
	 * 
	 * @param worldTime
	 *            utc时间，单位为s
	 */
	public static void setPlayerInfo(int country, int worldTime,int timeZone, int gmod, int headPicVer, String name, String uidStr, String picStr,
			int vipLevel, int svipLevel, int vipEndTime, int lastUpdateTime, int crossFightSrcServerId,int chatBgId,int chatBgEndTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uidStr, "name", name, "country", country,"timeZone",timeZone,
				"crossFightSrcServerId", crossFightSrcServerId);

		TimeManager.getInstance().setServerBaseTime(worldTime,timeZone);
		currentUserClone = (User) UserManager.getInstance().getCurrentUser().clone();

		boolean countryChange = false;
		// 如果crossFightSrcServerId变了，serverId肯定会变，所以只判断serverId即可
		if (ConfigManager.useWebSocketServer && UserManager.getInstance().getCurrentUser().serverId>0 && country>0 && country!=UserManager.getInstance().getCurrentUser().serverId)
		{
			countryChange = true;
		}
		
		UserManager.getInstance().getCurrentUser().serverId = country;
		GSController.serverId = country;

		UserManager.getInstance().getCurrentUser().headPicVer = headPicVer;
		UserManager.getInstance().getCurrentUser().mGmod = gmod;
		UserManager.getInstance().getCurrentUser().userName = name;
		UserManager.getInstance().getCurrentUser().headPic = picStr;
		UserManager.getInstance().getCurrentUser().vipLevel = vipLevel;
		UserManager.getInstance().getCurrentUser().svipLevel = svipLevel;
		UserManager.getInstance().getCurrentUser().vipEndTime = vipEndTime;
		UserManager.getInstance().getCurrentUser().lastUpdateTime = lastUpdateTime;
		UserManager.getInstance().getCurrentUser().chatBgId = chatBgId;
		UserManager.getInstance().getCurrentUser().chatBgEndTime = chatBgEndTime;

//		if (ChatServiceController.crossFightSrcServerId != crossFightSrcServerId)
//		{
//			if (WebSocketManager.getInstance().isConnected())
//			{
//				WebSocketManager.getInstance().leaveCountryRoom();
//				WebSocketManager.getInstance().forceClose();
//			}
//		}
		UserManager.getInstance().getCurrentUser().crossFightSrcServerId = crossFightSrcServerId;
		CokConfig.crossFightSrcServerId = crossFightSrcServerId;

		UserManager.getInstance().updateUser(UserManager.getInstance().getCurrentUser());
		CokConfig.getInstance().getCountryChannel();
		
		ScrollTextManager.getInstance().clear(DBDefinition.CHANNEL_TYPE_COUNTRY);
		ScrollTextManager.getInstance().clear(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
		
		WebSocketManager.getInstance().sendDevice();

		if (countryChange)
		{
			if(!WebSocketManager.chatSessionEnable || SharePreferenceUtil.checkSession())
			{
				WebSocketManager.getInstance().joinRoom();
			}
		}
	}

	/**
	 * 初始登录时会调用 打开聊天时，会紧接着setPlayerInfo后面调
	 * 重新登录、切服等时候，会调C++的parseData()刷新联盟信息，也调用此函数
	 */
	public static void setPlayerAllianceInfo(String asnStr, String allianceIdStr, int alliancerank, boolean isFirstJoinAlliance)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "allianceIdStr", allianceIdStr, "current allianceId", UserManager
				.getInstance().getCurrentUser().allianceId);

		String previousAllianceId = UserManager.getInstance().getCurrentUser().allianceId;
		
		// 这里总是会leave，然后再判断是否join，是否应该改成判断前后allianceId是否相等
		resetPlayerIsInAlliance(false);
		
		// 变更联盟（退出联盟）
		if (UserManager.getInstance().isCurrentUserInAlliance()
				&& !UserManager.getInstance().getCurrentUser().allianceId.equals(allianceIdStr) && ChannelManager.isInited())
		{
			IMCore.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if(CokConfig.getInstance().getAllianceChannel()!=null)
						{
							CokConfig.getInstance().getAllianceChannel().resetMsgChannel();
							if (UIManager.getChatFragment() != null)
							{
								UIManager.getChatActivity().getFragment(CokConfig.getInstance().getAllianceChannel()).notifyDataSetChanged(true);
							}
						}
//						if(CokConfig.getInstance().getAllianceSysChannel()!=null)
//						{
//							CokConfig.getInstance().getAllianceSysChannel().resetMsgChannel();
//							if (UIManager.getChatFragment() != null)
//							{
//								UIManager.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS, "", true);
//							}
//						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}

		if (currentUserClone == null)
			currentUserClone = (User) UserManager.getInstance().getCurrentUser().clone();

		UserManager.getInstance().getCurrentUser().asn = asnStr;
		UserManager.getInstance().getCurrentUser().allianceId = allianceIdStr;
		UserManager.getInstance().getCurrentUser().allianceRank = alliancerank;
		ConfigManager.getInstance().isFirstJoinAlliance = isFirstJoinAlliance;
		// 使用旧后台、且db中没有联盟时，需要将allianceChannel加入channelMap，以免getChannelInfo中没有联盟
		CokConfig.getInstance().getAllianceChannel();

		if (ConfigManager.useWebSocketServer && !previousAllianceId.equals(allianceIdStr))
		{
			if(!WebSocketManager.chatSessionEnable || SharePreferenceUtil.checkSession())
			{
				// 可能在登陆时调用，此时ws未初始化，调用无效
				WebSocketManager.getInstance().joinRoom();
			}
		}

		if (!currentUserClone.equalsLogically(UserManager.getInstance().getCurrentUser()))
		{
			LogUtil.printVariables(
					Log.INFO,
					LogUtil.TAG_CORE,
					"current user updated:\n"
							+ LogUtil.compareObjects(new Object[] { UserManager.getInstance().getCurrentUser(), currentUserClone }));
			UserManager.getInstance().updateCurrentUser();
		}
		JniController.getInstance().excuteJNIVoidMethod("getLatestChatMessage", null);

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
		
		if(!(WebSocketManager.chatSessionEnable && SharePreferenceUtil.isCreatingSession))
		{
//			connect2WS();
		}
		// TODO 把init和connect分开，放入上面的条件中
		IMCore.getInstance().init(CokConfig.getInstance());

//		AllianceShareManager.getInstance().initFireBase();
	}

	/**
	 * 目的是清空联盟消息，以免切换账号或切换联盟之后还有旧消息在
	 * 退出联盟会调用，此时fromGame为true
	 * 一般的setPlayerAllianceInfo会调用
	 * 切换账号时的setPlayerAllianceInfo也会调用
	 * 
	 * @param fromGame 是否是游戏在退出联盟时直接调的
	 */
	public static void resetPlayerIsInAlliance(boolean fromGame)
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return;

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "fromGame", fromGame);

		UserManager.getInstance().clearAllianceMember();

		if (UserManager.getInstance().getCurrentUser().allianceId.equals(""))
			return;

		if (ChannelManager.isInited())
		{
			if (UserManager.getInstance().isCurrentUserInAlliance())
			{
				if(CokConfig.getInstance().getAllianceChannel() != null
						&& CokConfig.getInstance().getAllianceChannel().msgList != null)
				{
					// 有时候会发生nullPointer异常
					CokConfig.getInstance().getAllianceChannel().msgList.clear();
					if (UIManager.getChatFragment() != null)
						UIManager.getChatActivity().getFragment(CokConfig.getInstance().getAllianceChannel()).notifyDataSetChanged(true);
				}
				
//				if(ChannelManager.getInstance().getAllianceSysChannel() != null
//						&& ChannelManager.getInstance().getAllianceSysChannel().msgList != null)
//				{
//					// 有时候会发生nullPointer异常
//					ChannelManager.getInstance().getAllianceSysChannel().msgList.clear();
//					if (ChatServiceController.getChatFragment() != null)
//						ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS, "", true);
//				}
				
			}
//			ChannelManager.getInstance().setNoMoreDataFlag(1, false);
		}

		String previousAllianceId = UserManager.getInstance().getCurrentUser().allianceId;
		
		if(fromGame)
		{
			UserManager.getInstance().getCurrentUser().asn = "";
			UserManager.getInstance().getCurrentUser().allianceId = "";
			UserManager.getInstance().getCurrentUser().allianceRank = -1;
			UserManager.getInstance().updateCurrentUser();
		}
		
		if (fromGame && ConfigManager.useWebSocketServer && StringUtils.isNotEmpty(previousAllianceId))
		{
			if(!WebSocketManager.chatSessionEnable || SharePreferenceUtil.checkSession())
			{
				// 可能在登陆时调用，此时ws未初始化，调用无效
				WebSocketManager.getInstance().leaveRoom(CokConfig.getInstance().getAllianceRoomId());
			}
		}
	}
	
	public static void notifyUserInfo(int index)
	{
		UserManager.getInstance().onReceiveUserInfo(IMCore.getInstance().host.getUserInfoArray(index));
	}

	public static void notifySearchedUserInfo(int index)
	{
		UserManager.getInstance().onReceiveSearchUserInfo(IMCore.getInstance().host.getUserInfoArray(index));
	}

	public static void initXiaoMiSDK(String appId, String appKey, String pid, String pkey, String guid, String b2token)
	{
		// demo有，但似乎非必要
		// GlobalData.initialize(ChatServiceController.hostActivity,
		// Integer.parseInt(appId));

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "appId", appId, "appKey", appKey, "pid", pid, "pkey", pkey, "guid",
				guid, "b2token", b2token);
		XiaoMiToolManager.getInstance().initSDK(IMCore.hostActivity, appId, appKey, pid, pkey, guid, b2token);
	}

	public static void showChatActivityFrom2dx(int maxHornInputCount, final int chatType, int sendInterval, final boolean rememberPosition,
			boolean enableCustomHeadImg, boolean isNoticeItemUsed)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "chatType", chatType, "sendInterval", sendInterval,
				"rememberPosition", rememberPosition, "enableCustomHeadImg", enableCustomHeadImg, "isNoticeItemUsed", isNoticeItemUsed);

		ConfigManager.maxHornInputLength = maxHornInputCount;
		ConfigManager.enableCustomHeadImg = enableCustomHeadImg;
		GSController.isHornItemUsed = isNoticeItemUsed;
		ConfigManager.sendInterval = sendInterval * 1000;
		GSController.isCreateChatRoom = false;
		if (IMCore.hostActivity != null)
		{
			IMCore.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						UIManager.showChatActivity(IMCore.hostActivity, chatType, rememberPosition);
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}
	
	public static void setChatSessionEnable(boolean enable)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
		WebSocketManager.chatSessionEnable = enable;
	}
	
	public static void onCreateChatSession(String session, String expire)
	{
		SharePreferenceUtil.isCreatingSession = false;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_ALL, "session", session, "expire", expire);
		LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - SharePreferenceUtil.startAuthTime,
				LogUtil.CHAT_CONNECT_GET_SESSION);
		SharePreferenceUtil.setChatSession(session, Integer.parseInt(expire));
		WebSocketManager.getInstance().connect();
	}
}
