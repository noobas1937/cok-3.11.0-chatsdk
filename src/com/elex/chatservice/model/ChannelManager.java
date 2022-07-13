package com.elex.chatservice.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.db.ChatTable;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.mail.updatedata.MailUpdateData;
import com.elex.chatservice.model.mail.updatedata.UpdateParam;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.util.SharePreferenceUtil;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.ChannelListFragment;
import com.elex.chatservice.view.banner.BannerInfo;
import com.elex.chatservice.view.recyclerrefreshview.AbstractRecyclerActivity;


import static com.elex.chatservice.util.SharePreferenceUtil.MAIL_UPODATE_DATA;


public class ChannelManager implements Serializable
{
	private static final long						serialVersionUID				= 3664013024183969534L;

	public static final int							LOAD_MORE_COUNT					= 20;
	public static final int							LOAD_ALL_MORE_MIN_COUNT			= 20;
	public static final int							LOAD_ALL_MORE_MAX_COUNT			= 200;

	public static final int							OPERATION_DELETE_MUTI			= 1;
	public static final int							OPERATION_REWARD_MUTI			= 2;

	private static ChannelManager					instance;
	public boolean									isGetingNewMsg					= false;

	private ConcurrentHashMap<String, ChatChannel>	channelMap;
	public ChatChannel								countryChannel;
	public ChatChannel								countrySysChannel;
	public ChatChannel								allianceChannel;
	public ChatChannel								allianceSysChannel;

	public boolean									isFetching;
	public static int								totalUnreadCount				= 0;
	public Map<String, Integer>						parseFrom2dxMap					= new HashMap<String, Integer>();
	public Map<String, Boolean>						parseLocalFinishMap				= new HashMap<String, Boolean>();

	private CopyOnWriteArrayList<ChatChannel>		modChannelList					= null;
	private CopyOnWriteArrayList<ChatChannel>		messageChannelList				= null;
	private CopyOnWriteArrayList<ChatChannel>		driftingBottleChannelList		= null;
	private CopyOnWriteArrayList<ChatChannel>		nearbyChannelList				= null;
	private ArrayList<ChatChannel>					loadedModChannelList;
	private ArrayList<ChatChannel>					loadedMessageChannelList;
	private ArrayList<ChatChannel>					loadedDriftingBottleChannelList;
	private ArrayList<ChatChannel>					loadedNearbyChannelList;
	public String									latestModChannelMsg				= "";
	public String									latestMessageChannelMsg			= "";
	public String									latestDrfitingBottleChannelMsg	= "";
	public String									latestNearbyChannelMsg			= "";
	private ChatChannel								modChannel						= null;
	private ChatChannel								messageChannel					= null;
	private ChatChannel								driftingBottleChannel			= null;
	private ChatChannel								nearbyChannel					= null;
	public boolean									isInRootChannelList				= false;

	public List<String>								mailDeleteArray					= null;
	public Map<String, UpdateParam>					mailUpdateMap					= null;
	public static boolean							isHandlingChannelInfo			= false;
	private String									latestId_official				= "";
	private long									latestSysMailModifyTime			= 0;
	public static String							currentOpenedChannel			= "";
	private Map<String, MsgItem>					redPackageUnHandleMap			= null;

	public static String							mailUpdateData					= "";
	private String									firstChannelID					= null;
	private ExecutorService							executorService					= null;
	public List<String>								userChatChannelInDB				= null;

	public MailData									newestDetectMail				= null;
	public MailData									newestBattleMail				= null;
	public int										newestUserChatChannelType		= -1;
	public String									newestUserChatChannelId			= "";
	private List<BannerInfo>						bannerInfoList					= null;
	public static boolean							bannerEnable					= false;
	public boolean									hasPreLoadSystemMailForGame		= false;
	public ErrorUpdateMailInfo						errorUpdateMail					= null;

	public int hasNewestReport(String mailId)
	{
		if (StringUtils.isEmpty(mailId))
			return 0;
		if (newestBattleMail != null && StringUtils.isNotEmpty(newestBattleMail.getUid()) && newestBattleMail.getUid().equals(mailId))
		{
			return 1;
		}
		else if (newestDetectMail != null && StringUtils.isNotEmpty(newestDetectMail.getUid()) && newestDetectMail.getUid().equals(mailId))
		{
			return 2;
		}
		return 0;

	}

	public boolean hasNewestUserChat(String channelId)
	{
		if (StringUtils.isEmpty(channelId))
			return false;
		return newestUserChatChannelId.equals(channelId);
	}

	public static ChannelManager getInstance()
	{
		if (instance == null)
		{
			synchronized (ChannelManager.class)
			{
				if (instance == null)
				{
					instance = new ChannelManager();
					instance.init();
				}
			}
		}
		return instance;
	}

	public static boolean isInited()
	{
		return instance != null;
	}

	private ChannelManager()
	{
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		bannerInfoList = new ArrayList<BannerInfo>();
	}

	public void reset()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE);
		init();
		getLoadedModChannel().clear();
		getLoadedMessageChannel().clear();
		getLoadedDriftringBottleChannel().clear();
		getLoadedNearbyChannel().clear();
	}
	
	public void prepareInitMsgChannel()
	{
		prepareLoadMsgChannel(MailManager.CHANNELID_MESSAGE);
		prepareLoadMsgChannel(MailManager.CHANNELID_MOD);
	}
	
	public void prepareLoadMsgChannel(String channelId)
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelId",channelId);
		
		List<ChatChannel> allMsgChannels = getAllMsgChannelById(channelId);
		Collections.sort(allMsgChannels);
		List<ChatChannel> loadedMsgChannels = getLoadedChannelListById(channelId);
		
		int actualLoadSize = allMsgChannels.size() < 10 ? allMsgChannels.size() : 10;

		if(loadedMsgChannels!=null && loadedMsgChannels.size() < actualLoadSize)
		{
			List<ChatChannel> subMsgChannels = allMsgChannels.subList(0, actualLoadSize);
			for (int i = 0; i < subMsgChannels.size(); i++)
			{
				ChatChannel chatChannel = subMsgChannels.get(i);
				if (chatChannel != null)
				{
					if(!isChannelInList(chatChannel, loadedMsgChannels))
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "prepareLoadMsgChannel content",chatChannel.contentText,"channelId",chatChannel.channelID,"tableName",chatChannel.getChatTable().getTableName());
						if(!chatChannel.hasInitLoaded())
							chatChannel.loadMoreMsg();
						if(chatChannel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
							chatChannel.initChatRoomImage();
						loadedMsgChannels.add(chatChannel);
					}
				}
			}
		}
	}

	/**
	 * 从数据库初始化频道，只从db读取一次，新增的需要插入进来
	 */
	private void init()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE);
		channelMap = new ConcurrentHashMap<String, ChatChannel>();
		modChannelList = new CopyOnWriteArrayList<ChatChannel>();
		userChatChannelInDB = new ArrayList<String>();
		messageChannelList = new CopyOnWriteArrayList<ChatChannel>();
		driftingBottleChannelList = new CopyOnWriteArrayList<ChatChannel>();
		nearbyChannelList = new CopyOnWriteArrayList<ChatChannel>();
		ArrayList<ChatChannel> dbChannels = DBManager.getInstance().getAllChannel();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "dbChannels.size()", dbChannels.size());
		for (int i = 0; i < dbChannels.size(); i++)
		{
			ChatChannel dbChannel = dbChannels.get(i);
			if (!ChatServiceController.isNewMailUIEnable
					|| (ChatServiceController.isNewMailUIEnable && dbChannel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL
							&& dbChannel.channelType != DBDefinition.CHANNEL_TYPE_RANDOM_CHAT
							&& dbChannel.isNotMainMsgChannel()))
			{
				if (dbChannel.channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isEmpty(dbChannel.latestId))
					continue;
				addChatChannelInMap(dbChannel);

			}
		}
		getCountryChannel();
		getAllianceChannel();
		getAllianceSysChannel();
		if (ChatServiceController.isNewMailUIEnable)
			getNewSystemMailChannel();
		// latestId_official = DBManager.getInstance().getSystemMailLatestId();
		latestSysMailModifyTime = DBManager.getInstance().getSystemMailLatestModifyTime();
		errorUpdateMail = DBManager.getInstance().getEarliestErrorUpdateMail();

		redPackageUnHandleMap = new HashMap<String, MsgItem>();
		getUnHandleRedPackage(countryChannel);
		getUnHandleRedPackage(allianceChannel);
		prepareInitMsgChannel();
	}

	public Map<String, MsgItem> getUnHandleRedPackageMap()
	{
		return redPackageUnHandleMap;
	}

	private void getUnHandleRedPackage(ChatChannel channel)
	{
		if (channel != null)
		{
			List<MsgItem> msgItemArray = DBManager.getInstance().getUnHandleRedPackage(channel.getChatTable());
			if (msgItemArray != null)
			{
				for (int i = 0; i < msgItemArray.size(); i++)
				{
					MsgItem msgItem = msgItemArray.get(i);
					if (msgItem == null)
						continue;
					if (msgItem.sendState < 0)
						msgItem.sendState = MsgItem.UNHANDLE;
					if (msgItem != null && StringUtils.isNotEmpty(msgItem.attachmentId)
							&& !redPackageUnHandleMap.containsKey(msgItem.attachmentId))
					{
						redPackageUnHandleMap.put(msgItem.attachmentId, msgItem);
					}
				}
			}
		}
	}

	public long getLatestSysMailModifyTime()
	{
		return latestSysMailModifyTime;
	}

	private void getNewSystemMailChannel()
	{
		String[] channelIdArray = {
				MailManager.CHANNELID_FIGHT,
				MailManager.CHANNELID_ALLIANCE,
				MailManager.CHANNELID_STUDIO,
				MailManager.CHANNELID_RESOURCE,
				MailManager.CHANNELID_MONSTER,
				MailManager.CHANNELID_NEW_WORLD_BOSS,
				MailManager.CHANNELID_KNIGHT,
				MailManager.CHANNELID_SYSTEM,
				MailManager.CHANNELID_EVENT,
				MailManager.CHANNELID_DRAGON_TOWER,
				MailManager.CHANNELID_RECYCLE_BIN };
		for (int i = 0; i < channelIdArray.length; i++)
		{
			if (StringUtils.isNotEmpty(channelIdArray[i]))
			{
				ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelIdArray[i]);
				getChannel(chatTable);
				// ChatChannel channel = getChannel(chatTable);
				// if (channel != null)
				// {
				// channel.latestId = channel.getLatestId();
				// channel.latestTime = channel.getLatestTime();
				// }
			}
		}
	}

	public void removeChannelFromMap(String key)
	{
		if (channelMap != null && channelMap.containsKey(key))
		{
			ChatChannel channel = channelMap.get(key);
			if (channel != null)
			{
				if (channel.isModChannel())
				{
					if (modChannelList != null && modChannelList.contains(channel))
						modChannelList.remove(channel);
				}
				else if (channel.isMessageChannel())
				{
					if (messageChannelList != null && messageChannelList.contains(channel))
						messageChannelList.remove(channel);
				}
				else if (MailManager.isDriftingBottleEnable && channel.isDriftingBottleChannel())
				{
					if (driftingBottleChannelList != null && driftingBottleChannelList.contains(channel))
						driftingBottleChannelList.remove(channel);
				}
				else if (MailManager.nearbyEnable && channel.isNearbyChannel())
				{
					if (nearbyChannelList != null && nearbyChannelList.contains(channel))
						nearbyChannelList.remove(channel);
				}
				channel.resetHasMsgItemInDB();
			}

			if (channelMap.containsKey(key))
				channelMap.remove(key);
		}
	}

	private void initMsgChannel(CopyOnWriteArrayList<ChatChannel> channelList, String channelId)
	{
		if (channelList != null)
			SortUtil.getInstance().refreshListOrder(channelList, ChatChannel.class);
		ChatChannel parentChannel = null;
		if (channelId.equals(MailManager.CHANNELID_MOD))
			parentChannel = modChannel;
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
			parentChannel = messageChannel;
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			parentChannel = driftingBottleChannel;
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			parentChannel = nearbyChannel;

		if (channelList != null && channelList.size() > 0)
		{
			if (parentChannel == null)
			{
				parentChannel = new ChatChannel();
				parentChannel.channelType = DBDefinition.CHANNEL_TYPE_USER;
				parentChannel.channelID = channelId;
			}
			long latestCreateTime = 0;
			String latestId = "";
			int unreadCount = 0;
			ChatChannel latestChannel = null;
			for (int i = 0; i < channelList.size(); i++)
			{
				ChatChannel channel = channelList.get(i);
				if (channel != null)
				{
					if (channel.unreadCount > 0)
						unreadCount++;
					if (channel.latestTime >= latestCreateTime)
					{
						latestCreateTime = channel.latestTime;
						latestId = channel.latestId;
						latestChannel = channel;
					}

				}
			}
			parentChannel.latestId = latestId;
			parentChannel.latestTime = latestCreateTime;

			parentChannel.unreadCount = unreadCount;
			if (latestChannel != null && latestChannel.msgList != null && latestChannel.msgList.size() > 0)
			{
				MsgItem mail = latestChannel.msgList.get(latestChannel.msgList.size() - 1);
				if (mail != null)
				{
					if (channelId.equals(MailManager.CHANNELID_MOD))
						latestModChannelMsg = mail.msg;
					else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
						latestMessageChannelMsg = mail.msg;
					else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
						latestDrfitingBottleChannelMsg = mail.msg;
					else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
						latestNearbyChannelMsg = mail.msg;
				}
			}
		}
		else
		{
			if (ChatServiceController.isNewMailUIEnable)
			{
				if (parentChannel == null)
					parentChannel = new ChatChannel();
				parentChannel.channelType = DBDefinition.CHANNEL_TYPE_USER;
				parentChannel.channelID = channelId;
				parentChannel.unreadCount = 0;
			}
		}

		if (channelId.equals(MailManager.CHANNELID_MOD))
			modChannel = parentChannel;
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
			messageChannel = parentChannel;
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			driftingBottleChannel = parentChannel;
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			nearbyChannel = parentChannel;
	}

	public synchronized ArrayList<ChannelListItem> getAllMailChannel()
	{
		ArrayList<ChannelListItem> channelList = new ArrayList<ChannelListItem>();
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{
				boolean modChannelInit = false;
				boolean messageChannelInit = false;
				boolean driftingBottleChannelInit = false;
				boolean nearbyChannelInit = false;

				for (String key : keySet)
				{
					ChatChannel chatChannel = channelMap.get(key);
					if (chatChannel != null && !chatChannel.isInChatChannel() && !chatChannel.hasNoItemInChannel())// &&
																													// !chatChannel.hasInitLoaded()
					{
						if (chatChannel.isNotMainMsgChannel()
								&& !chatChannel.isModChannel()
								&& !chatChannel.isMessageChannel()
								&& !chatChannel.isDriftingBottleChannel()
								&& !chatChannel.isNearbyChannel())
						{
							System.out.println("channelId:" + chatChannel.channelID + "  chatChannel.channelType:" + chatChannel.channelType);
							if (chatChannel.channelID.equals(MailManager.CHANNELID_DRAGON_TOWER))
							{
								if (MailManager.dragonTowerMailEnable)
									channelList.add(chatChannel);
							}
							else
							{
								channelList.add(chatChannel);
							}
						}
					}
				}
				initMsgChannel(modChannelList, MailManager.CHANNELID_MOD);
				if (modChannel != null && !modChannelInit && needModChannel())
				{
					channelList.add(modChannel);
					modChannelInit = true;
					if (!MailManager.cocosMailListEnable && ((firstChannelID != null && !firstChannelID.equals(MailManager.CHANNELID_MOD)) || firstChannelID == null))
						ChannelManager.getInstance().parseFirstChannelID();
				}
				else
				{
					if (!MailManager.cocosMailListEnable && firstChannelID != null && firstChannelID.equals(MailManager.CHANNELID_MOD))
						ChannelManager.getInstance().parseFirstChannelID();
				}

				if (ChatServiceController.isNewMailUIEnable)
				{
					initMsgChannel(messageChannelList, MailManager.CHANNELID_MESSAGE);
					if (messageChannel != null && !messageChannelInit)
					{
						channelList.add(messageChannel);
						messageChannelInit = true;
					}
				}

				if (MailManager.isDriftingBottleEnable)
				{
					initMsgChannel(driftingBottleChannelList, MailManager.CHANNELID_DRIFTING_BOTTLE);
					if (driftingBottleChannel != null && !driftingBottleChannelInit && driftingBottleChannelList.size() > 0)
					{
						channelList.add(driftingBottleChannel);
						driftingBottleChannelInit = true;
					}
				}

				if (MailManager.nearbyEnable)
				{
					initMsgChannel(nearbyChannelList, MailManager.CHANNELID_NEAR_BY);
					// if (nearbyChannel != null && !nearbyChannelInit)
					// {
					// channelList.add(nearbyChannel);
					// nearbyChannelInit = true;
					// }
				}
			}
		}
		return channelList;
	}

	public String getFirstChannelID()
	{
		if (firstChannelID == null)
			parseFirstChannelID();
		return firstChannelID;
	}

	public boolean needParseFirstChannel(String channelId)
	{
		if (StringUtils.isEmpty(channelId))
			return false;
		if (StringUtils.isEmpty(firstChannelID))
			return true;
		if ((firstChannelID.equals(MailManager.CHANNELID_KNIGHT)
				&& (channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS) || channelId.equals(MailManager.CHANNELID_MONSTER)
						|| channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_MOD)))
				|| (firstChannelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)
						&& (channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_RESOURCE)
								|| channelId.equals(MailManager.CHANNELID_MOD)))
				|| (firstChannelID.equals(MailManager.CHANNELID_MONSTER)
						&& (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_MOD)))
				|| (firstChannelID.equals(MailManager.CHANNELID_RESOURCE)
						&& channelId.equals(MailManager.CHANNELID_MOD)))
			return true;
		return false;
	}

	public void parseFirstChannelID()
	{
		if (MailManager.cocosMailListEnable)
			return;
		System.out.println("parseFirstChannelID");
		firstChannelID = "";
		if (needModChannel())
			firstChannelID = MailManager.CHANNELID_MOD;
		else
		{
			ChatChannel resourceChannel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_RESOURCE);
			if (resourceChannel != null && !resourceChannel.hasNoItemInChannel())
				firstChannelID = MailManager.CHANNELID_RESOURCE;
			else
			{
				ChatChannel monsterChannel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_MONSTER);
				if (monsterChannel != null && !monsterChannel.hasNoItemInChannel())
					firstChannelID = MailManager.CHANNELID_MONSTER;
				else
				{
					ChatChannel newWorldBossChannel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_NEW_WORLD_BOSS);
					if (newWorldBossChannel != null && !newWorldBossChannel.hasNoItemInChannel())
						firstChannelID = MailManager.CHANNELID_NEW_WORLD_BOSS;
					else
					{
						ChatChannel knightChannel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_KNIGHT);
						if (knightChannel != null && !knightChannel.hasNoItemInChannel())
							firstChannelID = MailManager.CHANNELID_KNIGHT;
					}
				}
			}

		}
	}

	public List<ChatChannel> getAllMsgChannelById(String channelId)
	{
		if (StringUtils.isEmpty(channelId))
			return new ArrayList<ChatChannel>();
		if (channelId.equals(MailManager.CHANNELID_MOD))
			return getAllModChannel();
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
			return getAllMessageChannel();
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			return getAllDriftingBottleChannel();
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			return getAllNearbyChannel();
		else
			return new ArrayList<ChatChannel>();
	}

	public List<ChatChannel> getAllModChannel()
	{
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		if (modChannelList != null && modChannelList.size() > 0)
		{
			Iterator<ChatChannel> it = modChannelList.iterator();
			while (it.hasNext())
			{
				ChatChannel channel = it.next();
				if (channel != null && channel.hasMsgItemInDB())
					channelList.add(channel);
			}
		}
		return channelList;
	}

	public List<ChatChannel> getAllMessageChannel()
	{
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		if (messageChannelList != null && messageChannelList.size() > 0)
		{
			Iterator<ChatChannel> it = messageChannelList.iterator();
			while (it.hasNext())
			{
				ChatChannel channel = it.next();
				if (channel != null && channel.hasMsgItemInDB())
					channelList.add(channel);
			}
		}
		return channelList;
	}

	public List<ChatChannel> getAllDriftingBottleChannel()
	{
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		if (driftingBottleChannelList != null && driftingBottleChannelList.size() > 0)
		{
			Iterator<ChatChannel> it = driftingBottleChannelList.iterator();
			while (it.hasNext())
			{
				ChatChannel channel = it.next();
				if (channel != null && channel.hasMsgItemInDB())
					channelList.add(channel);
			}
		}
		return channelList;
	}

	public List<ChatChannel> getAllNearbyChannel()
	{
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		if (nearbyChannelList != null && nearbyChannelList.size() > 0)
		{
			Iterator<ChatChannel> it = nearbyChannelList.iterator();
			while (it.hasNext())
			{
				ChatChannel channel = it.next();
				if (channel != null && channel.hasMsgItemInDB())
					channelList.add(channel);
			}
		}
		return channelList;
	}

	public List<ChatChannel> getLoadedModChannel()
	{
		if (loadedModChannelList == null)
		{
			loadedModChannelList = new ArrayList<ChatChannel>();
		}
		return loadedModChannelList;
	}

	public List<ChatChannel> getLoadedDriftringBottleChannel()
	{
		if (loadedDriftingBottleChannelList == null)
		{
			loadedDriftingBottleChannelList = new ArrayList<ChatChannel>();
		}
		return loadedDriftingBottleChannelList;
	}

	public List<ChatChannel> getLoadedNearbyChannel()
	{
		if (loadedNearbyChannelList == null)
		{
			loadedNearbyChannelList = new ArrayList<ChatChannel>();
		}
		return loadedNearbyChannelList;
	}

	public List<ChatChannel> getLoadedChannelListById(String channelId)
	{
		if (StringUtils.isEmpty(channelId))
			return new ArrayList<ChatChannel>();
		if (channelId.equals(MailManager.CHANNELID_MOD))
			return getLoadedModChannel();
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
			return getLoadedMessageChannel();
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			return getLoadedDriftringBottleChannel();
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			return getLoadedNearbyChannel();
		else
			return new ArrayList<ChatChannel>();
	}

	public List<ChatChannel> getLoadedMessageChannel()
	{
		if (loadedMessageChannelList == null)
		{
			loadedMessageChannelList = new ArrayList<ChatChannel>();
		}
		return loadedMessageChannelList;
	}

	private void removeFromLoadedChannel(ChatChannel channel)
	{
		if (getLoadedModChannel().indexOf(channel) >= 0)
		{
			getLoadedModChannel().remove(channel);
		}
		if (getLoadedMessageChannel().indexOf(channel) >= 0)
		{
			getLoadedMessageChannel().remove(channel);
		}
		if (getLoadedDriftringBottleChannel().indexOf(channel) >= 0)
		{
			getLoadedDriftringBottleChannel().remove(channel);
		}
		if (getLoadedNearbyChannel().indexOf(channel) >= 0)
		{
			getLoadedNearbyChannel().remove(channel);
		}
	}

	public void addToLoadedChannel(ChatChannel channel)
	{
		List<ChatChannel> loadedChannels = getLoadedMsgChannels(channel);
		if (loadedChannels != null)
		{
			if (!isChannelInList(channel, loadedChannels))
			{
				loadedChannels.add(channel);
				if (channel.isModChannel() && getModChannel() != null)
					getModChannel().refreshRenderData();
				else if (channel.isMessageChannel() && getMessageChannel() != null)
					getMessageChannel().refreshRenderData();
				else if (channel.isDriftingBottleChannel() && getDriftingBottleChannel() != null)
					getDriftingBottleChannel().refreshRenderData();
				else if (channel.isNearbyChannel() && getNearbyChannel() != null)
					getNearbyChannel().refreshRenderData();
			}
		}
	}

	public static <T> boolean isChannelInList(ChatChannel channel, List<T> channels)
	{
		if (channel == null || channels == null)
			return false;

		if (channels.contains(channel))
		{
			return true;
		}
		for (int i = 0; i < channels.size(); i++)
		{
			if (channels.get(i) != null && channels.get(i) instanceof ChatChannel
					&& ((ChatChannel) channels.get(i)).channelID.equals(channel.channelID))
			{
				return true;
			}
		}

		return false;
	}

	private List<ChatChannel> getLoadedMsgChannels(ChatChannel channel)
	{
		if (channel != null)
		{
			if (channel.isModChannel())
				return ChannelManager.getInstance().getLoadedModChannel();
			else if (channel.isMessageChannel())
				return ChannelManager.getInstance().getLoadedMessageChannel();
			else if (channel.isDriftingBottleChannel())
				return ChannelManager.getInstance().getLoadedDriftringBottleChannel();
			else if (channel.isNearbyChannel())
				return ChannelManager.getInstance().getLoadedNearbyChannel();
		}
		return null;
	}

	public ChatChannel getMainMsgChannelById(String channelId)
	{
		if (StringUtils.isEmpty(channelId))
			return null;
		if (channelId.equals(MailManager.CHANNELID_MOD))
			return getModChannel();
		else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
			return getMessageChannel();
		else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			return getDriftingBottleChannel();
		else if (channelId.equals(MailManager.CHANNELID_NEAR_BY))
			return getNearbyChannel();
		else
			return null;
	}

	public ChatChannel getMainChannel(ChatChannel channel)
	{
		if (channel.isMessageChannel())
			return getMessageChannel();
		else if (channel.isModChannel())
			return getModChannel();
		else if (channel.isDriftingBottleChannel())
			return getDriftingBottleChannel();
		else if (channel.isNearbyChannel())
			return getNearbyChannel();
		return null;
	}

	public ChatChannel getModChannel()
	{
		return modChannel;
	}

	public ChatChannel getMessageChannel()
	{
		return messageChannel;
	}

	public ChatChannel getDriftingBottleChannel()
	{
		return driftingBottleChannel;
	}

	public ChatChannel getNearbyChannel()
	{
		return nearbyChannel;
	}

	public Map<String, ChatChannel> getAllSysMailChannelMap()
	{
		Map<String, ChatChannel> systemMailMap = new HashMap<String, ChatChannel>();
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{
				for (String key : keySet)
				{
					ChatChannel chatChannel = channelMap.get(key);
					if (chatChannel != null && chatChannel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL
							&& chatChannel.mailDataList != null && chatChannel.mailDataList.size() > 0)
					{
						for (int i = 0; i < chatChannel.mailDataList.size(); i++)
						{
							MailData mail = chatChannel.mailDataList.get(i);
							if (mail != null)
							{
								systemMailMap.put(mail.getUid(), chatChannel);
							}
						}
					}
				}
			}
		}
		return systemMailMap;
	}

	public List<ChatChannel> getAllSysMailChannel()
	{
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{
				for (String key : keySet)
				{
					ChatChannel chatChannel = channelMap.get(key);
					if (chatChannel != null && chatChannel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
					{
						channelList.add(chatChannel);
					}
				}
			}
		}
		return channelList;
	}

	public ChatChannel getCountryChannel()
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return null;

		if (ChatServiceController.isBattleChatEnable && UserManager.getInstance().isInBattleField())
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_COUNTRY,
					Integer.toString(UserManager.getInstance().getCurrentUser().crossFightSrcServerId));
			countryChannel = getChannel(chatTable);
		}
		else
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_COUNTRY,
					Integer.toString(UserManager.getInstance().getCurrentUser().serverId));
			countryChannel = getChannel(chatTable);
		}
		return countryChannel;
	}

	public ChatChannel getCountrySysChannel()
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return null;

		if (ChatServiceController.isBattleChatEnable && UserManager.getInstance().isInBattleField())
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_COUNTRY_SYS,
					Integer.toString(UserManager.getInstance().getCurrentUser().crossFightSrcServerId));
			countrySysChannel = getChannel(chatTable);
		}
		else
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_COUNTRY_SYS,
					Integer.toString(UserManager.getInstance().getCurrentUser().serverId));
			countrySysChannel = getChannel(chatTable);
		}
		return countrySysChannel;
	}

	public ChatChannel getBattleFieldChannel()
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return null;
		ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD,
				Integer.toString(UserManager.getInstance().getCurrentUser().serverId));
		boolean isInDragonObserverRoom = SwitchUtils.mqttEnable ? MqttManager.isInDragonObserverRoom() : WebSocketManager.isInDragonObserverRoom();
		if (ChatServiceController.canJoinDragonRoom())
			chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD,
					ChatServiceController.dragonRoomId);
		else if (isInDragonObserverRoom)
			chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD,
					ChatServiceController.dragonObserverRoomId);
		else if (ChatServiceController.canEnterDragonObserverRoom() && !isInDragonObserverRoom)
			return null;
		return getChannel(chatTable);
	}

	/**
	 * 可能有多个联盟或国家频道，需要动态取当前对应的频道
	 */
	public ChatChannel getAllianceChannel()
	{
		if (UserManager.getInstance().isCurrentUserInAlliance())
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_ALLIANCE,
					UserManager.getInstance().getCurrentUser().allianceId);
			allianceChannel = this.getChannel(chatTable);
		}
		else
		{
			allianceChannel = null;
		}
		return allianceChannel;
	}

	public ChatChannel getAllianceSysChannel()
	{
		if (UserManager.getInstance().isCurrentUserInAlliance())
		{
			ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS,
					UserManager.getInstance().getCurrentUser().allianceId);
			allianceSysChannel = this.getChannel(chatTable);
		}
		else
		{
			allianceSysChannel = null;
		}
		return allianceSysChannel;
	}

	private ChatChannel initChannel(int channelType, String channelID)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID)
				&& ((channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE) && !MailManager.isDriftingBottleEnable)
						|| (channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY) && !MailManager.nearbyEnable)))
			return null;
		ChatChannel channel = new ChatChannel();
		channel.channelType = channelType;
		if (channelID != null)
		{
			channel.channelID = channelID;
		}
		if (ChatServiceController.isNewMailUIEnable
				&& (channel.isMainMsgChannel() || channel.channelID.equals(MailManager.CHANNELID_NOTICE) || channel.channelID
						.equals(MailManager.CHANNELID_RESOURCE_HELP)))
			return channel;
		addChatChannelInMap(channel);
		return channel;
	}

	private void addChatChannelInMap(ChatChannel channel)
	{
		if (channel.canUserChannelShow())
		{
			channelMap.put(channel.getChatTable().getChannelName(), channel);

			if (channel.isModChannel())
				modChannelList.add(channel);
			else if (channel.isMessageChannel())
				messageChannelList.add(channel);
			else if (MailManager.isDriftingBottleEnable && channel.isDriftingBottleChannel())
				driftingBottleChannelList.add(channel);
			else if (MailManager.nearbyEnable && channel.isNearbyChannel())
				nearbyChannelList.add(channel);

			LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "    channelMap put key", channel.getChatTable().getChannelName());
		}
	}

	public ChatChannel addDummyChannel(int channelType, String channelID)
	{
		return initChannel(channelType, channelID);
	}

	private void removeChannel(String key)
	{
		if (StringUtils.isNotEmpty(key) && channelMap.containsKey(key))
		{
			try
			{
				ChatChannel channel = channelMap.get(key);
				if (channel != null)
				{
					if (channel.isModChannel())
					{
						if (modChannelList != null && modChannelList.contains(channel))
							modChannelList.remove(channel);
					}
					else if (channel.isMessageChannel())
					{
						if (messageChannelList != null && messageChannelList.contains(channel))
							messageChannelList.remove(channel);
					}
					else if (MailManager.isDriftingBottleEnable && channel.isDriftingBottleChannel())
					{
						if (driftingBottleChannelList != null && driftingBottleChannelList.contains(channel))
							driftingBottleChannelList.remove(channel);
					}
					else if (MailManager.nearbyEnable && channel.isNearbyChannel())
					{
						if (nearbyChannelList != null && nearbyChannelList.contains(channel))
							nearbyChannelList.remove(channel);
					}
				}

				if (channelMap.containsKey(key))
					channelMap.remove(key);
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	public long getLatestUserChatTime()
	{
		long latestCreateTime_user = 0;
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			for (String key : keySet)
			{
				if (StringUtils.isNotEmpty(key))
				{
					ChatChannel channel = channelMap.get(key);
					if (channel != null && channel.isUserChatChannel() && channel.channelID.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG))
					{
						if (channel.latestTime > latestCreateTime_user)
						{
							latestCreateTime_user = channel.latestTime;
						}
					}
				}
			}
		}
		return latestCreateTime_user;
	}

	/**
	 * 收取消息接口需要的参数
	 * 格式："id|seqid|channelType,id|seqid|channelType,id|seqid|channelType"
	 */
	public String getChannelInfo()
	{
		String result = "";
		simulateReturnChannelInfo = "";
		newServerChannels = new ArrayList<ChatChannel>();
		long latestCreateTime_user = 0;
		String latestId_user = "";
		String latestChannelId_user = "";

		Iterator<String> it = channelMap.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			ChatChannel channel = channelMap.get(key);

			if (!channel.containCurrentUser() || channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			{
				continue;
			}
			if (channel.channelType != DBDefinition.CHANNEL_TYPE_USER && channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				if (!WebSocketManager.isRecieveFromWebSocket(channel.channelType))
				{
					result = addCommaToParam(result);
					simulateReturnChannelInfo = addCommaToParam(simulateReturnChannelInfo);
					result += channel.channelID + "|" + channel.dbMaxSeqId + "|" + channel.channelType;
					simulateReturnChannelInfo += channel.channelType + "|" + channel.channelID + "|" + (channel.dbMaxSeqId - 1) + "|"
							+ channel.dbMaxSeqId;
					channel.prevDBMaxSeqId = channel.dbMaxSeqId;
				}
				else
				{
					newServerChannels.add(channel);
				}
			}
			else if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER && channel.isNotMainMsgChannel() && !channel.isNearbyChannel())
			{
				if (!(ConfigManager.useWebSocketServer && ConfigManager.pm_standalone_read) && StringUtils.isNotEmpty(channel.latestId) && channel.latestTime > latestCreateTime_user)
				{
					latestCreateTime_user = channel.latestTime;
					latestId_user = channel.latestId;
					latestChannelId_user = getActualUidFromChannelId(channel.channelID);
				}
			}
		}

		if (ChatServiceController.isNewMailListEnable)
		{
			result = addCommaToParam(result);

			if (!(ConfigManager.useWebSocketServer && ConfigManager.pm_standalone_read))
			{
				if (latestCreateTime_user != 0 && StringUtils.isNotEmpty(latestId_user))
					result += latestChannelId_user + "|" + latestId_user + "|" + DBDefinition.CHANNEL_TYPE_USER;
				else
					result += "0|0|" + DBDefinition.CHANNEL_TYPE_USER;
			}

		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "result", result);
		return result;
	}

    public String getLatestSystemMailInfo()
    {
        // 第一次数据库中没有邮件时，默认还回的是 "0|0|80"，会从最开始拉取mail ，有的话返回的是 ：44546ad650bc462684c4b5956bc5c6bf|1510813001|80 ，uid/createtime/count ,会从这个时间点拉取数据
        String ret = "0|0|80";
        String updateret = SharePreferenceUtil.getSharePreferenceString(ChatServiceController.hostActivity,MAIL_UPODATE_DATA,"");
        if (!updateret.equals("")){
            return updateret;
        }else {
            return ret;
        }
        // 下面是原有的逻辑, 其中的 errorUpdateEarliestMail ，没有用处
        //		long errorTime = 0;
        //		MailData errorUpdateEarliestMail = null;
        //		if (errorUpdateMail != null)
        //		{
        //			errorTime = errorUpdateMail.getCreateTime();
        //			errorUpdateEarliestMail = errorUpdateMail.getMailData();
        //		}
        //		MailData mail = DBManager.getInstance().getLatestSystemMail();
        //		if (mail != null)
        //		{
        //			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mail.getUid()", mail.getUid(), "mail.getCreateTime()", mail.getCreateTime());
        //			long time = mail.getCreateTime();
        //			String mailUid = mail.getUid();
        //			if (errorTime > 0)
        //			{
        //				if (errorUpdateEarliestMail != null)
        //				{
        //					if (errorTime <= time)
        //					{
        //						time = errorUpdateEarliestMail.getCreateTime();
        //						mailUid = errorUpdateEarliestMail.getUid();
        //					}
        //				}
        //				else
        //				{
        //					ret = "0|0|80";
        //					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "ret1", ret);
        //					return ret;
        //				}
        //			}
        //			int creatTime = TimeManager.getTimeInS(time);
        //			ret = mailUid + "|" + String.valueOf(creatTime) + "|80";
        //			// if(MailManager.cocosMailListEnable)
        //			// ret = mail.getUid() + "|" + String.valueOf(time) + "|1000";
        //			// else
        //			// ret = mail.getUid() + "|" + String.valueOf(time) + "|20";
        //		}
        //		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "ret", ret);
        //		return ret;
    }

	private String addCommaToParam(String param)
	{
		if (StringUtils.isNotEmpty(param))
		{
			param += ",";
		}
		return param;
	}

	private String simulateReturnChannelInfo = "";

	public String getSimulateReturnChannelInfo()
	{
		ServiceInterface.getChannelInfo();
		ConfigManager.setMailPullState(false);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "simulateReturnChannelInfo", simulateReturnChannelInfo);

		return simulateReturnChannelInfo;
	}

	private ArrayList<ChatChannel> newServerChannels;

	public ArrayList<ChatChannel> getNewServerChannels()
	{
		return newServerChannels;
	}

	/**
	 * 收取消息的channel信息返回值处理 此前消息已收到，缓存和db中的channel都更新了 格式：
	 * "channelType|id|firstSeqId|lastSeqId,channelType|id|firstSeqId|lastSeqId"
	 */
	public void handleChannelInfo(String channelInfo)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "channelInfo", channelInfo, "channelMap.size", channelMap.size());

		if (StringUtils.isEmpty(channelInfo))
			return;
		isHandlingChannelInfo = true;
		String[] channels = channelInfo.split(",");
		for (int i = 0; i < channels.length; i++)
		{
			String[] fields = channels[i].split("\\|");
			if (fields.length <= 1)
				continue;
			String channelId = fields[1];
			int channelType = Integer.parseInt(fields[0]);
			if (channelId.equals("null"))
			{
				if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
				{
					channelId = UserManager.getInstance().getCurrentUser().serverId + "";
				}
				else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
				{
					channelId = UserManager.getInstance().getCurrentUser().allianceId;
				}
			}
			ChatTable chatTable = new ChatTable(channelId, channelType);
			ChatChannel channel = ChannelManager.getInstance().getChannel(chatTable);
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "channelName", chatTable.getChannelName(), "channel", channel);
			if (channel != null)
			{
				channel.hasLoadingAllNew = false;
				if (channelType != DBDefinition.CHANNEL_TYPE_USER && channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
				{
					if (!WebSocketManager.isRecieveFromWebSocket(channelType) && !channel.isNearbyChannel())
					{
						channel.serverMaxSeqId = Integer.parseInt(fields[3]);
						channel.serverMinSeqId = Integer.parseInt(fields[2]);
						LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "serverMaxSeqId", channel.serverMaxSeqId, "serverMinSeqId",
								channel.serverMinSeqId, "prevDBMaxSeqId", channel.prevDBMaxSeqId, "hasInitLoaded", channel.hasInitLoaded());
						if (channel.prevDBMaxSeqId > 0 && !channel.hasInitLoaded())
						{
							LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "loadMoreMsg");
							channel.loadMoreMsg();
							DBManager.getInstance().updateChannel(channel);
						}
					}
				}
				// else
				// {
				// preProcessSysMailChannel(channel);
				// }
				// channelIdArray.add(channel.getChatTable().getChannelName());
			}
		}

		// Set<String> keySet = channelMap.keySet();
		// for (String key : keySet)
		// {
		// if (!key.equals("") && !channelIdArray.contains(key) &&
		// channelMap.get(key) != null)
		// {
		// preProcessSysMailChannel(channelMap.get(key));
		// }
		// }

		if (ChatServiceController.getChatFragment() != null)
		{
			ChatServiceController.getChatFragment().refreshHasMoreData(ChatServiceController.getChatFragment().getCurrentChannel());
		}

		// isHandlingChannelInfo = false;
		// calulateAllChannelUnreadNum();
	}

	private void preProcessSysMailChannel(ChatChannel channel)
	{
		// System.out.println("channel channelId:"+channel.channelID);
		if (channel.channelID.equals(MailManager.CHANNELID_MONSTER) || channel.channelID.equals(MailManager.CHANNELID_RESOURCE)
				|| channel.channelID.equals(MailManager.CHANNELID_RESOURCE_HELP) || channel.channelID.equals(MailManager.CHANNELID_KNIGHT)
				|| channel.channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
		{
			loadMoreSysMailFromDB(channel, -1);
		}
		calcUnreadCount(channel);
	}

	/**
	 * TODO channel与forceUseTime重复了，条件判断冗余，参数也有点过长。可以拆开，或者修改调用方式
	 */
	public void loadMoreMsgFromDB(final ChatChannel channel, final int minSeqId, final int maxSeqId, final int minCreateTime, final boolean forceUseTime)
	{
		if (channel == null || channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			return;

		Runnable runable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (forceUseTime || channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
					{
						System.out.println("loadMoreMsgFromDB 1");
						List<MsgItem> dbItems = DBManager.getInstance().getMsgsByTime(channel.getChatTable(), minCreateTime, LOAD_MORE_COUNT);
						if (dbItems != null)
						{
							MsgItem[] dbItemsArray = (MsgItem[]) dbItems.toArray(new MsgItem[0]);
							if (dbItemsArray.length <= 0)
								return;

							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelID", channel.channelID, "dbItemsArray.length", dbItemsArray.length);
							ServiceInterface.handleMessage(dbItemsArray, channel.channelID, channel.customName, false, false);
						}
					}
					else if (channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
							|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM
							|| channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
					{
						System.out.println("loadMoreMsgFromDB 2");
						List<MsgItem> dbItems = DBManager.getInstance().getChatMsgBySection(channel.getChatTable(), maxSeqId, minSeqId);
						MsgItem[] dbItemsArray = (MsgItem[]) dbItems.toArray(new MsgItem[0]);
						if (dbItemsArray.length <= 0)
							return;
						ServiceInterface.handleMessage(dbItemsArray, channel.channelID, channel.customName, false, false);
					} 
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		executorService.execute(runable);

	}

	public void loadMoreMsgToCreateTimeFromDB(final ChatChannel channel, final int maxCreateTime, final int minCreateTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "minCreateTime", minCreateTime, "maxCreateTime", maxCreateTime);
		if (channel == null || channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
			return;

		Runnable runable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					List<MsgItem> dbItems = DBManager.getInstance().getMsgsByTimeRange(channel.getChatTable(), maxCreateTime, minCreateTime);
					if (dbItems != null)
					{
						MsgItem[] dbItemsArray = (MsgItem[]) dbItems.toArray(new MsgItem[0]);
						if (dbItemsArray.length <= 0)
							return;

						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelID", channel.channelID, "dbItemsArray.length", dbItemsArray.length);
						ServiceInterface.handleMessage(dbItemsArray, channel.channelID, channel.customName, false, false);
						channel.setAtMeMsgReaded(minCreateTime);
					} 
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		executorService.execute(runable);

	}

	public void setAtMeMsgReaded(final ChatChannel channel, final int minCreateTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "minCreateTime", minCreateTime);
		if (channel == null || !channel.isCountryOrAllianceChannel())
			return;

		Runnable runable = new Runnable()
		{

			@Override
			public void run()
			{

				try
				{
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "start setAtMeMsgReaded");
					channel.setAtMeMsgReaded(minCreateTime);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};

		executorService.execute(runable);

	}

	public int getSysMailDBCount(ChatChannel channel)
	{
		return DBManager.getInstance().getSysMailDBCountByTime(channel.getChatTable(), -1);
	}

	public int getUnreadSysMailDBCount(ChatChannel channel)
	{
		return DBManager.getInstance().getUnreadSysMailDBCountByTime(channel.getChatTable(), -1);
	}

	private void calcUnreadCount(ChatChannel channel)
	{
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
			return;
		if (isNeedCalculateUnreadCount(channel.channelID))
		{
			channel.setUnreadCount(DBManager.getInstance().getUnreadCountOfSysMail(channel.getChatTable()));
		}
	}

	public void loadMoreRecycleBinMailFromDBForGame(final ChatChannel channel)
	{
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelId", channel.channelID);

		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				int loadNum = 0;

				try
				{
					List<MailData> moreItems = DBManager.getInstance().getRecycleMailByTime(
							channel.getChatTable(), channel.latestLoadedMailRecycleTime, 20);

					loadNum = moreItems.size();

					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "loadNum", loadNum);

					if (loadNum <= 0)
					{
						JniController.getInstance().excuteJNIVoidMethod("postSysMailLoadComplete", new Object[] { Integer.valueOf(0) });
						return;
					}

					transportMailListToGame(channel, moreItems);

				}
				catch (OutOfMemoryError e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}

			}
		};

		executorService.execute(run);
		// Thread thread = new Thread(run);
		// thread.start();
	}

	public void loadMoreSysMailFromDBForGame(final ChatChannel channel, final long lastMailTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "lastMailTime", lastMailTime);
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL || channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelId", channel.channelID);

		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				int loadNum = 0;

				try
				{
					List<MailData> moreItems = DBManager.getInstance()
							.getSysMailByTime(channel.getChatTable(),
									lastMailTime, 20);

					loadNum = moreItems.size();

					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "loadNum", loadNum, "channel.channelId", channel.channelID);

					if (loadNum <= 0)
					{
						JniController.getInstance().excuteJNIVoidMethod("postSysMailLoadComplete", new Object[] { Integer.valueOf(0) });
						return;
					}

					transportMailListToGame(channel, moreItems);

				}
				catch (OutOfMemoryError e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}

			}
		};

		executorService.execute(run);
		// Thread thread = new Thread(run);
		// thread.start();
	}

	public void transportMailListToGame(ChatChannel channel, List<MailData> moreItems)
	{
		if (channel.isDialogChannel())
		{
			MailData mail = MailManager.getInstance().createDialogSystemMail(channel, moreItems, true);
			if (mail != null)
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelId", channel.channelID);
				MailManager.getInstance().transportMailDataForGame(mail);
			}
		}
		else
		{
			List<MailData> list = new ArrayList<MailData>();
			for (int i = 0; i < moreItems.size(); i++)
			{
				MailData mailData = moreItems.get(i);
				if (mailData != null)
				{
					mailData.setNeedParseByForce(false);
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailData.contents", mailData.getContents(),
							"CreateTime", mailData.getCreateTime());
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
					mailData = ServiceInterface.handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData, true, false);
					if (mailData.getType() == MailManager.MAIL_BATTLE_REPORT)
						mailData.setContents("");
					channel.addInLoadMailUidList(mailData.getUid());
					LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailData.getUid", mailData.getUid(), "channel.channelID", channel.channelID);
					list.add(mailData);
					// MailManager.getInstance().transportMailDataForGame(mailData);
				}
			}
			try
			{
				String jsonStr = JSON.toJSONString(list);
				MailManager.getInstance().transportMailArray(jsonStr);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void loadMoreSysMailFromDB(final ChatChannel channel, final long lastMailCreateTime)
	{
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			return;
		}

		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{

				final List<MailData> moreItems = DBManager.getInstance().getSysMailByTime(channel.getChatTable(), lastMailCreateTime, 20);
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "size", moreItems.size());
				if (moreItems.size() <= 0)
				{
					if (ChatServiceController.getSysMailListFragment() != null)
						ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
					if (ChatServiceController.getRecyclerSysMailActivity() != null)
						ChatServiceController.getRecyclerSysMailActivity().notifyLoadMoreCompleted();
					return;
				}

				if (moreItems.size() > 0)
				{
					final List<MailData> mailDataArr = new ArrayList<MailData>();
					for (int i = 0; i < moreItems.size(); i++)
					{
						MailData mailData = moreItems.get(i);
						if (mailData != null)
						{
							MailData mail = ServiceInterface.parseMailData(mailData, true);
							if (mail != null)
							{
								mailDataArr.add(mail);
							}
						}
					}

					if (mailDataArr.size() > 0)
					{
						try
						{
							ChatChannel channel = null;
							String channelId = "";
							for (int i = 0; i < mailDataArr.size(); i++)
							{
								MailData mail = mailDataArr.get(i);
								if (mail != null)
								{
									if (StringUtils.isEmpty(channelId))
										channelId = mail.getChannelId();
									channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
											mail.getChannelId());
									if (channel != null)
									{
										channel.addNewMailData(mail);
										channel.latestLoadedMailCreateTime = mail.getCreateTime();
									}
								}
							}

							if (channel != null && channel.mailDataList != null)
								SortUtil.getInstance().refreshListOrder(channel.mailDataList, MailData.class);

							if (StringUtils.isNotEmpty(currentOpenedChannel) && currentOpenedChannel.equals(channelId))
								postNotifyPopup(channelId);

							if (ChatServiceController.getSysMailListFragment() != null)
								ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
							if (ChatServiceController.getRecyclerSysMailActivity() != null)
								ChatServiceController.getRecyclerSysMailActivity().notifyLoadMoreCompleted();
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				}
			}
		};

		executorService.execute(run);
		// Thread thread = new Thread(run);
		// thread.start();
	}

	public void loadMoreRecycleBinMailFromDB(final ChatChannel channel, final long lastMailRecycleTime)
	{
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			return;
		}

		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{

				final List<MailData> moreItems = DBManager.getInstance().getRecycleMailByTime(channel.getChatTable(), lastMailRecycleTime, 20);
				if (moreItems.size() <= 0)
				{
					if (ChatServiceController.getSysMailListFragment() != null)
						ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
					if (ChatServiceController.getRecyclerSysMailActivity() != null)
						ChatServiceController.getRecyclerSysMailActivity().notifyLoadMoreCompleted();
					return;
				}

				if (moreItems.size() > 0)
				{
					final List<MailData> mailDataArr = new ArrayList<MailData>();
					for (int i = 0; i < moreItems.size(); i++)
					{
						MailData mailData = moreItems.get(i);
						if (mailData != null)
						{
							MailData mail = ServiceInterface.parseMailData(mailData, true);
							if (mail != null)
							{
								mailDataArr.add(mail);
							}
						}
					}

					if (mailDataArr.size() > 0)
					{
						try
						{
							ChatChannel channel = null;
							String channelId = "";
							for (int i = 0; i < mailDataArr.size(); i++)
							{
								MailData mail = mailDataArr.get(i);
								if (mail != null)
								{
									if (StringUtils.isEmpty(channelId))
										channelId = mail.getChannelId();
									channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
											mail.getChannelId());
									if (channel != null)
									{
										channel.addNewMailData(mail);
										if (channel.latestLoadedMailRecycleTime == -1 || channel.latestLoadedMailRecycleTime > mail.getRecycleTime())
											channel.latestLoadedMailRecycleTime = mail.getRecycleTime();
									}
								}
							}

							if (channel != null && channel.mailDataList != null)
								SortUtil.getInstance().refreshListOrder(channel.mailDataList, MailData.class);

							if (StringUtils.isNotEmpty(currentOpenedChannel) && currentOpenedChannel.equals(channelId))
								postNotifyPopup(channelId);

							if (ChatServiceController.getSysMailListFragment() != null)
								ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
							if (ChatServiceController.getRecyclerSysMailActivity() != null)
								ChatServiceController.getRecyclerSysMailActivity().notifyLoadMoreCompleted();
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				}
			}
		};

		executorService.execute(run);
		// Thread thread = new Thread(run);
		// thread.start();
	}

	public void loadMoreUnreadSysMailFromDB(ChatChannel channel, long lastMailCreateTime)
	{
		if (channel == null || channel.channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			return;
		}

		final List<MailData> moreItems = DBManager.getInstance().getUnreadSysMailByTime(channel.getChatTable(), lastMailCreateTime, 20);
		if (moreItems.size() <= 0)
		{
			// System.out.println("moreItems.size() <= 0 channelId:"+channel.channelID);
			if (ChatServiceController.getSysMailListFragment() != null)
			{
				ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
			}
			return;
		}

		Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				if (moreItems.size() > 0)
				{
					final List<MailData> mailDataArr = new ArrayList<MailData>();
					for (int i = 0; i < moreItems.size(); i++)
					{
						MailData mailData = moreItems.get(i);
						if (mailData != null)
						{
							MailData mail = ServiceInterface.parseMailData(mailData, true);
							if (mail != null)
							{
								mailDataArr.add(mail);
							}
						}
					}

					if (mailDataArr.size() > 0)
					{
						ChatServiceController.hostActivity.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									ChatChannel channel = null;
									String channelId = "";
									for (int i = 0; i < mailDataArr.size(); i++)
									{
										MailData mail = mailDataArr.get(i);
										if (mail != null)
										{
											if (StringUtils.isEmpty(channelId))
												channelId = mail.getChannelId();
											channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
													mail.getChannelId());
											if (channel != null)
											{
												channel.addNewMailData(mail);
											}
										}
									}

									if (channel != null && channel.mailDataList != null)
										SortUtil.getInstance().refreshListOrder(channel.mailDataList, MailData.class);

									if (StringUtils.isNotEmpty(currentOpenedChannel) && currentOpenedChannel.equals(channelId))
										postNotifyPopup(channelId);

									if (ChatServiceController.getSysMailListFragment() != null)
									{
										ChatServiceController.getSysMailListFragment().onLoadMoreComplete();
									}
								}
								catch (Exception e)
								{
									LogUtil.printException(e);
								}
							}
						});
					}
				}
			}
		};

		executorService.execute(run);
		// Thread thread = new Thread(run);
		// thread.start();
	}

	public void postNotifyPopup(String channelId)
	{
		if (channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_RESOURCE)
				|| channelId.equals(MailManager.CHANNELID_KNIGHT) || channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
		{
			ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
			MailData mail = null;
			if (channel.channelID.equals(MailManager.CHANNELID_MONSTER))
				mail = channel.getMonsterMailData();
			else if (channel.channelID.equals(MailManager.CHANNELID_RESOURCE))
				mail = channel.getResourceMailData();
			else if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT))
				mail = channel.getKnightMailData();
			else if (channel.channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
				mail = channel.getNewWorldBossMailData();

			if (mail != null)
			{
				try
				{
					String jsonStr = JSON.toJSONString(mail);
					MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_NONE);
					// JniController.getInstance().excuteJNIVoidMethod("postNotifyMailPopup",
					// null);

					if (channel.channelID.equals(MailManager.CHANNELID_KNIGHT))
					{
						String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
						if (StringUtils.isNotEmpty(uids))
						{
							JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { uids });
						}
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
								new Object[] { Integer.valueOf(mail.getType()), Boolean.valueOf(false), "" });
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 现在加载的maxId是视图中最小seqId，会被手动加载历史消息改变，而minId是maxId-200，
	 * 可能需要变为初始化时的serverMaxId
	 */
	public void loadAllNew(ChatChannel channel)
	{
		int minSeqId = channel.getNewMsgCount() < LOAD_ALL_MORE_MAX_COUNT ? channel.getNewMsgMinSeqId() : (channel.getNewMsgMaxSeqId()
				- LOAD_ALL_MORE_MAX_COUNT + 1);
		channel.firstNewMsgSeqId = minSeqId;
		JniController.getInstance().excuteJNIVoidMethod(
				"getMsgBySeqId",
				new Object[] {
						Integer.valueOf(minSeqId),
						Integer.valueOf(channel.getNewMsgMaxSeqId()),
						Integer.valueOf(channel.channelType),
						channel.channelID });
	}

	public int loadLatestAtMsg(ChatChannel channel)
	{
		if (channel != null)
		{
			int maxCreateTime = channel.getMaxCreateTime();
			int earliestAtMeMsgTime = channel.getEarliestAtMeMsgCreateTime(maxCreateTime);
			int minCreateTime = channel.getMinCreateTime();
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "maxCreateTime", maxCreateTime,
					"minCreateTime", minCreateTime, "earliestAtMeMsgTime", earliestAtMeMsgTime);

			if (earliestAtMeMsgTime < minCreateTime)
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_DEBUG, "加载历史消息");
				loadMoreMsgToCreateTimeFromDB(channel, minCreateTime, earliestAtMeMsgTime);
			}
			else
			{
				LogUtil.printVariables(Log.VERBOSE, LogUtil.TAG_DEBUG, "跳转定位:", channel.getIndexByCreateTime(earliestAtMeMsgTime));
				setAtMeMsgReaded(channel, earliestAtMeMsgTime);
				return channel.getIndexByCreateTime(earliestAtMeMsgTime);
			}

		}

		return -1;
	}

	public static int channelType2tab(int channelType)
	{
		if (channelType <= DBDefinition.CHANNEL_TYPE_ALLIANCE)
			return channelType;
		else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM || channelType == DBDefinition.CHANNEL_TYPE_USER)
			return 2;
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			return 4;
		else if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			return 4;
		return 0;
	}

	/**
	 * 仅当视图存在时才返回，找不到不会创建
	 */
	public ChatChannel getChannelByViewIndex(int index)
	{
		ChatChannel result = null;
		if (ChatServiceController.getChatFragment() != null && ChatServiceController.getChatFragment().getChannelView(index) != null)
		{
			result = ChatServiceController.getChatFragment().getChannelView(index).chatChannel;
		}
		return result;
	}

	public ChatChannel getChannelFromMemory(ChatTable chatTable)
	{
		ChatChannel channel = channelMap.get(chatTable.getChannelName());
		if (channel == null)
			channel = initChannel(chatTable.channelType, chatTable.channelID);
		return channel;
	}

	/**
	 * 用于精确知道channelType和channelId的情况 如果找不到会创建
	 */
	public ChatChannel getChannel(ChatTable chatTable)
	{
		ChatChannel channel = channelMap.get(chatTable.getChannelName());
		if (channel == null)
		{
			if (chatTable.needNotSaveDB())
				channel = DBManager.getInstance().getChannel(chatTable);
			if (channel == null)
			{
				channel = initChannel(chatTable.channelType, chatTable.channelID);
				if (channel != null)
				{
					channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
					DBManager.getInstance().insertChannel(channel);
				}
			}
			else
			{
				if (ChatServiceController.isNewMailUIEnable
						&& (channel.channelID.equals(MailManager.CHANNELID_NOTICE) || channel.channelID
								.equals(MailManager.CHANNELID_RESOURCE_HELP)))
					return channel;
				addChatChannelInMap(channel);
			}
		}
		return channel;
	}

	public ChatChannel getChannel(int channelType)
	{
		String channelId = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			return getCountryChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			return getAllianceChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			return getBattleFieldChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
		{
			return getAllianceSysChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
		{
			return getCountrySysChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
		{
			LocalConfig config = ConfigManager.getInstance().getLocalConfig();
			if (config != null)
			{
				if (StringUtils.isNotEmpty(config.randomChannelId))
				{
					channelType = DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
					channelId = config.randomChannelId;
				}
				else
				{
					channelType = config.getCustomChannelType();
					channelId = config.getCustomChannelId();
				}
			}
		}
		else
		{
			if (UserManager.getInstance().getCurrentMail() != null)
				channelId = UserManager.getInstance().getCurrentMail().opponentUid;
			if (ChatServiceController.isModContactMode() && !UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
			else if (ChatServiceController.isDriftingBottleContactMode() && !UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
				channelId += DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE;
			else if (ChatServiceController.isNearbyContactMode() && !UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
				channelId += DBDefinition.CHANNEL_ID_POSTFIX_NEARBY;
		}

		return ChannelManager.getInstance().getChannel(channelType, channelId);
	}

	/**
	 * 用于fromUid(channelId)不一定存在的情况
	 * 
	 * @return null(如果channelType不为国家或联盟，且channelId为空时)
	 */
	public ChatChannel getChannel(int channelType, String fromUid)
	{
		ChatChannel chatChannel = null;
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			chatChannel = getCountryChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
		{
			chatChannel = getBattleFieldChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			chatChannel = getAllianceChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
		{
			chatChannel = getAllianceSysChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
		{
			chatChannel = getCountrySysChannel();
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			chatChannel = getChannel(ChatTable.createChatTable(channelType, fromUid));
		}
		else if (!StringUtils.isEmpty(fromUid))
		{
			if (channelType == DBDefinition.CHANNEL_TYPE_USER && ConfigManager.useWebSocketServer
					&& (ConfigManager.pm_standalone_read
							|| (!ConfigManager.pm_standalone_read && fromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY)))
					&& isNotMsgChannel(fromUid)
					&& !fromUid.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG) && (userChatChannelInDB == null || (userChatChannelInDB != null && !userChatChannelInDB.contains(fromUid))))
				fromUid = DBDefinition.CHANNEL_ID_PREFIX_STANDALONG + fromUid;
			ChatTable chatTable = ChatTable.createChatTable(channelType, fromUid);
			chatChannel = getChannel(chatTable);
		}
		else
		{
			LogUtil.trackMessage("ChatChannel.getChannel return null, channelType=" + channelType + " fromUid=" + fromUid);
		}

		return chatChannel;
	}

	public void setIsMemberFlag(String groupId, boolean flag)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (channel != null)
		{
			channel.setMember(flag);
			DBManager.getInstance().updateChannel(channel);
		}
	}

	public boolean getIsMemberFlag(String groupId)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (channel != null)
		{
			return channel.isMember();
		}
		return false;
	}

	public String getChatRoomMemberStr(String groupId)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (channel != null)
		{
			return ChatChannel.getMembersString(channel.memberUidArray);
		}
		return "";
	}

	public void setChannelMemberArray(String groupId, String uidStr, String roomName)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (!uidStr.equals(""))
		{
			channel.memberUidArray.clear();
			String[] uidArr = uidStr.split("_");
			for (int i = 0; i < uidArr.length; i++)
			{
				if (!uidArr[i].equals(""))
					channel.memberUidArray.add(uidArr[i]);
			}
			channel.isMemberUidChanged = true;
			channel.refreshChatRoomChannelImage();
		}
		if (!roomName.equals(""))
			channel.customName = roomName;
		DBManager.getInstance().updateChannel(channel);
	}

	public void updateChannelMemberArray(String groupId, String uidStr, boolean isAdd)
	{
		if (uidStr.equals(""))
			return;
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (channel.memberUidArray == null)
		{
			if (isAdd)
				channel.memberUidArray = new ArrayList<String>();
			else
				return;
		}
		String[] uidArr = uidStr.split("_");
		for (int i = 0; i < uidArr.length; i++)
		{
			if (!uidArr[i].equals(""))
			{
				if (isAdd && !channel.memberUidArray.contains(uidArr[i]))
				{
					channel.memberUidArray.add(uidArr[i]);
					channel.isMemberUidChanged = true;
				}
				else if (!isAdd && channel.memberUidArray.contains(uidArr[i]))
				{
					channel.memberUidArray.remove(uidArr[i]);
					channel.isMemberUidChanged = true;
				}
			}
		}
		if(channel.isMemberUidChanged)
			channel.refreshChatRoomChannelImage();
			
		DBManager.getInstance().updateChannel(channel);
	}

	public void setChatRoomFounder(String groupId, String founderUid)
	{
		if (founderUid.equals(""))
			return;
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		if (channel != null)
			channel.roomOwner = founderUid;
		DBManager.getInstance().updateChannel(channel);
	}

	public ArrayList<String> getChatRoomMemberArrayByKey(String groupId)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		ArrayList<String> memberUidArray = channel.memberUidArray;
		if (memberUidArray != null && !UserManager.getInstance().getCurrentUser().uid.equals("")
				&& !memberUidArray.contains(UserManager.getInstance().getCurrentUser().uid))
		{
			memberUidArray.add(UserManager.getInstance().getCurrentUser().uid);
		}
		return memberUidArray;
	}

	public String getChatRoomFounderByKey(String groupId)
	{
		ChatChannel channel = getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
		return channel.roomOwner;
	}

	public void setNoMoreDataFlag(int index, boolean flag)
	{
		if (getChannelByViewIndex(index) != null)
		{
			getChannelByViewIndex(index).noMoreDataFlag = flag;
		}
	}

	public boolean getNoMoreDataFlag(int index)
	{
		if (getChannelByViewIndex(index) == null)
			return false;

		return getChannelByViewIndex(index).noMoreDataFlag;
	}

	public void removeAllMailMsgByUid(final String fromUid)
	{
		final ChatChannel channel = getChannel(DBDefinition.CHANNEL_TYPE_USER, fromUid);
		if (channel != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						channel.msgList.clear();
						channel.getChannelView().getMessagesAdapter().notifyDataSetChanged();
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}

	}

	public void deleteChatroomChannel(ChatTable chatTable)
	{
		removeChannel(chatTable.getChannelName());

		DBManager.getInstance().deleteChannel(chatTable);
	}

	public void deleteChannel(ChatChannel channel)
	{
		if (channel == null || channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			return;

		if (!ChatServiceController.isNewMailUIEnable
				|| (ChatServiceController.isNewMailUIEnable
						&& (channel.channelType == DBDefinition.CHANNEL_TYPE_USER
								&& StringUtils.isNotEmpty(channel.channelID) && !(channel.channelID.equals(MailManager.CHANNELID_MOD)
										|| channel.channelID.equals(MailManager.CHANNELID_MESSAGE)))
						|| channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM
						|| channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT
						|| (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && channel.isDialogChannel())))
		{
			removeChannelFromMap(channel.getChatTable().getChannelName());
		}

		if (!MailManager.cocosMailListEnable && (channel.isDialogChannel() || channel.channelID.equals(MailManager.CHANNELID_MOD)))
			parseFirstChannelID();

		if (channel.channelType == DBDefinition.CHANNEL_TYPE_USER || channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			if (channel.isMainMsgChannel())
			{
				List<ChatChannel> channelArr = getAllMsgChannelById(channel.channelID);
				if (channelArr != null && channelArr.size() > 0)
				{
					for (int i = 0; i < channelArr.size(); i++)
					{
						ChatChannel chatChannel = channelArr.get(i);
						if (chatChannel != null)
							DBManager.getInstance().deleteChannel(chatChannel.getChatTable());
					}
				}
			}
			else
			{
				DBManager.getInstance().deleteChannel(channel.getChatTable());
			}
		}
		else if (channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			channel.unreadCount = 0;
			if (channel.isDialogChannel())
			{
				DBManager.getInstance().deleteDialogMailChannel(channel.getChatTable());
				parseFirstChannelID();
			}
			else
				DBManager.getInstance().deleteSysMailChannel(channel.getChatTable());
		}

		System.out.println("removeFromLoadedChannel:" + channel.channelID);

		removeFromLoadedChannel(channel);

		calulateAllChannelUnreadNum();
	}

	public void deleteSysMailFromChannel(ChatChannel channel, MailData mail, boolean isDeleteMuti)
	{
		if (channel == null || mail == null)
			return;
		if (channel.mailDataList != null && channel.mailDataList.size() > 0)
		{
			for (int i = 0; i < channel.mailDataList.size(); i++)
			{
				MailData mailData = channel.mailDataList.get(i);
				if (mailData != null && mailData.getUid().equals(mail.getUid()))
				{
					channel.mailDataList.remove(mailData);
					channel.mailUidList.remove(mailData.getUid());
					if (channel.mailDataList.size() == 0 && channel.isDialogChannel())
						deleteChannel(channel);
					break;
				}
			}
		}

		if (mail.isUnread())
		{
			channel.unreadCount--;
			ChannelListFragment.onChannelRefresh();
		}
		DBManager.getInstance().deleteSysMail(channel, mail.getUid());

		channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
		DBManager.getInstance().updateChannel(channel);

		if (!isDeleteMuti)
		{
			calulateAllChannelUnreadNum();
			channel.querySysMailCountFromDB();
			channel.queryUnreadSysMailCountFromDB();
		}

		if (!MailManager.cocosMailListEnable && channel.mailDataList != null && channel.mailDataList.size() == 0 && DBManager.getInstance().hasMailDataInDB(channel.channelID))
		{
			if (channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
				loadMoreRecycleBinMailFromDB(channel, -1);
			else
				loadMoreSysMailFromDB(channel, -1);
		}
	}

	public void deleteSysMailFromCreateTime(ChatChannel channel, long createTime)
	{
		// System.out.println("deleteSysMailFromCreateTime:"+channel.channelID);
		if (channel == null || createTime == -1
				|| !(channel.channelID.equals(MailManager.CHANNELID_RESOURCE) || channel.channelID.equals(MailManager.CHANNELID_MONSTER)))
			return;
		if (channel.mailDataList != null && channel.mailDataList.size() > 0)
		{
			List<MailData> mailList = new ArrayList<MailData>();
			for (int i = 0; i < channel.mailDataList.size(); i++)
			{
				MailData mailData = channel.mailDataList.get(i);
				if (mailData != null && mailData.getCreateTime() <= createTime && mailData.isUnread())
					mailList.add(mailData);
			}
			channel.mailDataList.removeAll(mailList);
		}

		DBManager.getInstance().deleteSysMailByCreateTimeFromDB(channel.channelID, createTime);
	}

	public void updateSysMailFromChannel(ChatChannel channel, String mailId, UpdateParam updateParam)
	{
		if (channel == null)
			return;
		if (channel.mailDataList != null && channel.mailDataList.size() > 0)
		{
			for (int i = 0; i < channel.mailDataList.size(); i++)
			{
				MailData mailData = channel.mailDataList.get(i);
				if (mailData != null && mailData.getUid().equals(mailId))
				{
					if (mailData.getStatus() != updateParam.getStatus() || mailData.getSave() != updateParam.getSaveFlag()
							|| mailData.getRewardStatus() != updateParam.getRewardStatus())
					{
						if (mailData.getSave() != updateParam.getSaveFlag())
							mailData.setSave(updateParam.getSaveFlag());
						if (mailData.isUnread() && updateParam.getStatus() == 1)
						{
							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailData.isUnread()", mailData.isUnread());
							mailData.setStatus(updateParam.getStatus());
							channel.unreadCount--;
						}
						if (mailData.getRewardStatus() == 0 && updateParam.getRewardStatus() == 1)
							mailData.setRewardStatus(1);
						DBManager.getInstance().updateMail(mailData);
						channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
						DBManager.getInstance().updateChannel(channel);
					}
					break;
				}
			}
		}
	}

	public void dealMailFrom2dx(String mailUid)
	{
		if (StringUtils.isNotEmpty(mailUid))
		{
			Map<String, ChatChannel> mailChannelMap = getAllSysMailChannelMap();
			if (mailChannelMap != null && mailChannelMap.containsKey(mailUid))
			{
				ChatChannel channel = mailChannelMap.get(mailUid);
				if (channel != null && channel.mailDataList != null)
				{
					for (int i = 0; i < channel.mailDataList.size(); i++)
					{
						MailData mailData = channel.mailDataList.get(i);
						if (mailData != null && mailUid.equals(mailData.getUid()))
						{
							if (mailData.getType() == MailManager.MAIL_ALLIANCEAPPLY
									|| mailData.getType() == MailManager.MAIL_ALLIANCEINVITE
									|| mailData.getType() == MailManager.MAIL_INVITE_TELEPORT
									|| mailData.getType() == MailManager.MAIL_ALLIANCE_PACKAGE
									|| mailData.getType() == MailManager.MAIL_ALLIANCE_OFFICER)
							{
								mailData.setMailDealStatus();
								DBManager.getInstance().updateMail(mailData);
							}
							break;
						}
					}
				}
			}
		}
	}

	public void updateMailData(MailUpdateData mailUpdateData)
	{
		mailDeleteArray = mailUpdateData.getDelete();
		List<UpdateParam> updateMailArr = mailUpdateData.getUpdate();

		if (mailDeleteArray != null && mailDeleteArray.size() > 0)
		{
			for (int j = 0; j < mailDeleteArray.size(); j++)
			{
				String mailUid = mailDeleteArray.get(j);
				if (StringUtils.isEmpty(mailUid))
					continue;
				MailData mailData = DBManager.getInstance().getSysMailByID(mailUid);
				if (mailData != null)
				{
					DBManager.getInstance().deleteSysMailFromDB(mailUid);
					ChatChannel channel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
					if (channel != null)
					{
						if (StringUtils.isEmpty(channel.latestId) || channel.latestId.equals(mailUid))
						{
							channel.latestId = channel.getLatestId();
							channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
							DBManager.getInstance().updateChannel(channel);
						}
					}
				}
			}
		}

		if (updateMailArr != null && updateMailArr.size() > 0)
		{
			for (int j = 0; j < updateMailArr.size(); j++)
			{
				UpdateParam updateParam = updateMailArr.get(j);
				String mailUid = updateParam.getUid();
				if (StringUtils.isEmpty(mailUid))
					continue;
				MailData mailData = DBManager.getInstance().getSysMailByID(mailUid);
				if (mailData != null)
				{
					if (mailData.getStatus() != updateParam.getStatus() || mailData.getSave() != updateParam.getSaveFlag()
							|| mailData.getRewardStatus() != updateParam.getRewardStatus())
					{

						if (mailData.getSave() != updateParam.getSaveFlag())
							mailData.setSave(updateParam.getSaveFlag());
						if (mailData.isUnread() && updateParam.getStatus() == 1)
						{
							LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailItem.isUnread()", mailData.isUnread());
							mailData.setStatus(updateParam.getStatus());
						}
						if (mailData.getRewardStatus() == 0 && updateParam.getRewardStatus() == 1)
							mailData.setRewardStatus(1);
						DBManager.getInstance().updateMail(mailData);
						ChatChannel channel = getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
						if (channel != null)
						{
							channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
							DBManager.getInstance().updateChannel(channel);
						}

					}
				}
			}
			calulateAllChannelUnreadNum();
		}
	}

	int calulateAllChannelCount;

	public void calulateAllChannelUnreadNum()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "MailManager.hasMoreNewMailToGet", MailManager.hasMoreNewMailToGet);
		// if (isHandlingChannelInfo)
		// return;
		if (MailManager.hasMoreNewMailToGet)
			return;
		calulateAllChannelCount++;
		List<ChannelListItem> channelList = null;
		if (ChatServiceController.getMainListFragment() != null
				&& ChatServiceController.getMainListFragment().getMainChannelAdapter() != null
				&& ChatServiceController.getMainListFragment().getMainChannelAdapter().list != null)
		{
			channelList = ChatServiceController.getMainListFragment().getMainChannelAdapter().list;
		}
		else
		{
			channelList = getAllMailChannel();
		}

		int oldTotalUnreadCount = totalUnreadCount;
		totalUnreadCount = 0;
		for (int i = 0; i < channelList.size(); i++)
		{
			try
			{
				if (channelList.get(i) != null && channelList.get(i) instanceof ChatChannel)
				{
					ChatChannel channel = (ChatChannel) channelList.get(i);
					totalUnreadCount += channel.unreadCount;
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "totalUnreadCount", totalUnreadCount, "oldTotalUnreadCount",
				oldTotalUnreadCount);
		if (oldTotalUnreadCount != totalUnreadCount)
		{
			JniController.getInstance().excuteJNIVoidMethod("postUnreadMailNum", new Object[] { Integer.valueOf(totalUnreadCount) });
		}
	}

	public boolean isNeedCalculateUnreadCount(String channelId)
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			if (ChatServiceController.isNewMailUIEnable)
			{
				if (channelId.equals(MailManager.CHANNELID_FIGHT) || channelId.equals(MailManager.CHANNELID_ALLIANCE)
						|| channelId.equals(MailManager.CHANNELID_STUDIO) || channelId.equals(MailManager.CHANNELID_RESOURCE)
						|| channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_KNIGHT)
						|| channelId.equals(MailManager.CHANNELID_SYSTEM) || channelId.equals(MailManager.CHANNELID_EVENT)
						|| channelId.equals(MailManager.CHANNELID_RECYCLE_BIN) || channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)
						|| channelId.equals(MailManager.CHANNELID_DRAGON_TOWER))
					return true;
			}
			else
			{
				if (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_STUDIO)
						|| channelId.equals(MailManager.CHANNELID_RESOURCE_HELP) || channelId.equals(MailManager.CHANNELID_FIGHT)
						|| channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_KNIGHT)
						|| channelId.equals(MailManager.CHANNELID_SYSTEM) || channelId.equals(MailManager.CHANNELID_EVENT))
					return true;
			}
		}
		return false;
	}

	public String getActualUidFromChannelId(String channelId)
	{
		String fromUid = channelId;
		if (StringUtils.isNotEmpty(fromUid))
		{
			if (fromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				fromUid = fromUid.substring(0, fromUid.indexOf(DBDefinition.CHANNEL_ID_POSTFIX_MOD));
			else if (fromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
				fromUid = fromUid.substring(0, fromUid.indexOf(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE));
			else if (fromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
				fromUid = fromUid.substring(0, fromUid.indexOf(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY));
			if (fromUid.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG))
				fromUid = fromUid.substring((DBDefinition.CHANNEL_ID_PREFIX_STANDALONG).length());
		}
		return fromUid;
	}

	public void serialize()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSerializeDataPath()));
			oos.writeObject(this);
			oos.flush();
			oos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String getSerializeDataPath()
	{
		return DBHelper.getDBDirectoryPath(ChatServiceController.hostActivity, true) + "channelManager.dat";
	}

	public static void deserialize()
	{
		ObjectInputStream oin = null;
		try
		{
			oin = new ObjectInputStream(new FileInputStream(getSerializeDataPath()));
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		ChannelManager manager = null;
		try
		{
			manager = (ChannelManager) oin.readObject();
			instance = manager;
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean needModChannel()
	{
		return ((UserManager.getInstance().getCurrentUser() != null && (UserManager.getInstance().getCurrentUser().mGmod == 2 || UserManager
				.getInstance().getCurrentUser().mGmod == 5))) || (modChannelList != null && modChannelList.size() > 0);
	}

	public void prepareSystemMailChannel()
	{
		Set<String> keySet = channelMap.keySet();
		for (String key : keySet)
		{
			if (StringUtils.isNotEmpty(key) && channelMap.get(key) != null)
			{
				preProcessSysMailChannel(channelMap.get(key));
			}
		}
		calulateAllChannelUnreadNum();
	}

	public void preLoadSystemMailForGame()
	{
		System.out.println("preLoadSystemMailForGame");
		Set<String> keySet = channelMap.keySet();
		for (String key : keySet)
		{
			if (StringUtils.isNotEmpty(key))
			{
				ChatChannel channel = channelMap.get(key);
				if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && StringUtils.isNotEmpty(channel.channelID))
				{
					if (channel.isDialogChannel())
					{
						if (channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
							loadMoreRecycleBinMailFromDBForGame(channel);
						else
							loadMoreSysMailFromDBForGame(channel, -1);
					}
					calcUnreadCount(channel);
				}
			}
		}
		System.out.println("preLoadSystemMailForGame 2");
		calulateAllChannelUnreadNum();
		System.out.println("preLoadSystemMailForGame 3");
		ServiceInterface.getChannelListData();
		hasPreLoadSystemMailForGame = true;
	}

	public static String appendStr(String originStr, String appendStr)
	{
		String ret = originStr;
		if (StringUtils.isNotEmpty(appendStr) && !ret.contains(appendStr))
		{
			if (ret.equals(""))
				ret = appendStr;
			else
				ret += "," + appendStr;
		}
		return ret;
	}

	public static boolean isMainMsgChannel(String channelID)
	{
		return StringUtils.isNotEmpty(channelID) && (channelID.equals(MailManager.CHANNELID_MOD)
				|| channelID.equals(MailManager.CHANNELID_MESSAGE) || channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE)
				|| channelID.equals(MailManager.CHANNELID_NEAR_BY));
	}

	public static boolean isNotMsgChannel(String channelID)
	{
		return StringUtils.isNotEmpty(channelID) && !(channelID.equals(MailManager.CHANNELID_MOD)
				|| channelID.equals(MailManager.CHANNELID_MESSAGE) || channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE)
				|| channelID.equals(MailManager.CHANNELID_NEAR_BY));
	}

	public void postNewMailExist(int type, boolean isExist)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type, "isExist", isExist);
		JniController.getInstance().excuteJNIVoidMethod("postNewMailExist", new Object[] { Integer.valueOf(type), Boolean.valueOf(isExist) });
	}

	public void getNewUserChatChannelId()
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		long latestTime = 0;
		newestUserChatChannelType = -1;
		newestUserChatChannelId = "";
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{
				for (String key : keySet)
				{
					ChatChannel chatChannel = channelMap.get(key);
					if (chatChannel != null && (chatChannel.isModChannel() || chatChannel.isMessageChannel()) && chatChannel.isUnread() && !chatChannel.hasNoItemInChannel())
					{
						if (latestTime < chatChannel.latestTime)
						{
							latestTime = chatChannel.latestTime;
							newestUserChatChannelType = chatChannel.channelType;
							newestUserChatChannelId = chatChannel.channelID;
						}
					}
				}
			}
		}
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "newestUserChatChannelType", newestUserChatChannelType, "newestUserChatChannelId", newestUserChatChannelId);
		postNewMailExist(0, StringUtils.isNotEmpty(newestUserChatChannelId));
	}

	public void showNewMailFrom2dx(final int type)
	{
		if (ChatServiceController.hostActivity != null)
		{
			ChatServiceController.hostActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type);
						if (type == 0 && StringUtils.isNotEmpty(newestUserChatChannelId) && (newestUserChatChannelType == DBDefinition.CHANNEL_TYPE_USER
								|| newestUserChatChannelType == DBDefinition.CHANNEL_TYPE_CHATROOM))
						{
							ChatChannel channel = getChannel(newestUserChatChannelType, newestUserChatChannelId);
							if (channel != null)
							{
								if (!channel.hasInitLoaded())
									channel.loadMoreMsg();
								int mailType = MailManager.MAIL_USER;
								if (channel.isModChannel())
									mailType = MailManager.MAIL_MOD_PERSONAL;
								else if (channel.isDriftingBottleChannel())
									mailType = MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND;
								if (channel.isNearbyChannel())
								{
									ServiceInterface.setMailInfo(3, channel.channelID, channel.getCustomName());
								}
								else
								{
									ServiceInterface.setMailInfo(channel.channelID, channel.latestId, channel.getCustomName(), mailType);
								}

								ServiceInterface.showChatActivity(ChatServiceController.hostActivity, channel.channelType, false);
								if (channel.isNotMainMsgChannel())
								{
									if (ChatServiceController.isModContactMode())
									{
										String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
										JniController.getInstance()
												.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(1) });
									}
									else if (ChatServiceController.isDriftingBottleContactMode())
									{
										String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
										JniController.getInstance()
												.excuteJNIVoidMethod("readChatMail", new Object[] { fromUid, Integer.valueOf(2) });
									}
									else if (ChatServiceController.isNearbyContactMode())
									{
										String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
										if (!SwitchUtils.mqttEnable)
											WebSocketManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
										else
											MqttManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, fromUid);
									}
									else
									{
										JniController.getInstance().excuteJNIVoidMethod("readChatMail",
												new Object[] { channel.channelID, Integer.valueOf(0) });
									}
									channel.markAsRead();
									getNewUserChatChannelId();
									if (ChatServiceController.getChannelListFragment() != null)
										ChatServiceController.getChannelListFragment().notifyDataSetChanged();
								}
							}
						}
						else if ((type == 1 && newestBattleMail != null) || (type == 2 && newestDetectMail != null))
						{
							MailData mail = null;
							if (type == 1)
								mail = newestBattleMail;
							else if (type == 2)
								mail = newestDetectMail;
							if (mail != null)
							{
								mail.setNeedParseByForce(false);
								MailData mailData = MailManager.getInstance().parseMailDataContent(mail);
								try
								{
									String jsonStr = JSON.toJSONString(mailData);
									LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "jsonStr", jsonStr);
									MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_DETECT_REPORT);
									if (mailData.isUnread())
									{
										mailData.setStatus(1);
										DBManager.getInstance().updateMail(mailData);
										DBManager.getInstance().getLatestUnReadReportByType(type);
										ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
										if (channel != null)
										{
											if (channel.unreadCount > 0)
											{
												channel.unreadCount--;
												ChannelManager.getInstance().calulateAllChannelUnreadNum();
											}
											channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
											DBManager.getInstance().updateChannel(channel);
											if (channel.mailDataList != null && channel.mailDataList.size() > 0)
											{
												for (int i = 0; i < channel.mailDataList.size(); i++)
												{
													MailData mailItem = channel.mailDataList.get(i);
													if (mailItem != null && mailData.getUid().equals(mailItem.getUid()))
													{
														if (mailItem.isUnread())
														{
															LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailItem.isUnread()", mailItem.isUnread());
															mailItem.setStatus(1);
															if (ChatServiceController.getChannelListFragment() != null)
																ChatServiceController.getChannelListFragment().notifyDataSetChanged();
														}
														break;
													}
												}

											}
										}

										// 更新mail
										JniController.getInstance().excuteJNIVoidMethod("readMail",
												new Object[] { mailData.getUid(), Integer.valueOf(mailData.getType()) });
									}
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}

	}

	public void setNewMailReport(int type, MailData mail)
	{
		if (type == 1)
			newestBattleMail = mail;
		else if (type == 2)
			newestDetectMail = mail;
		if (mail != null)
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type, "newestDetectMail.uid", mail.getUid());
		postNewMailExist(type, mail != null);
	}

	public List<BannerInfo> getBannerInfoList()
	{
		return bannerInfoList;
	}

	public void setBannerInfoList(List<BannerInfo> bannerInfoList)
	{
		this.bannerInfoList = bannerInfoList;
	}

	public int getDriftingBottleNum()
	{
		int count = 0;
		if (channelMap != null && MailManager.isDriftingBottleEnable)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{

				for (String key : keySet)
				{
					ChatChannel chatChannel = channelMap.get(key);
					if (chatChannel != null && chatChannel.isDriftingBottleChannel() && !chatChannel.isTempDriftBottleChannel()
							&& !chatChannel.hasNoItemInChannel())
						count++;
				}
			}
		}
		return count;
	}

	public static boolean isUserChannelType(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM || channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
	}

	public static boolean isNotMailChannel(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}

	public void readAllMainMsgChannel(ChatChannel channel)
	{
		if (channel != null && StringUtils.isNotEmpty(channel.channelID))
		{
			LogUtil.trackAction(channel.channelID + "_all_read");
			if (channel.isMainMsgChannel())
			{
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelID", channel.channelID);
				List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllMsgChannelById(channel.channelID);
				if (messageChannelArr != null && messageChannelArr.size() > 0)
				{
					for (int i = 0; i < messageChannelArr.size(); i++)
					{
						ChatChannel messageChannel = messageChannelArr.get(i);
						if (messageChannel != null)
						{
							messageChannel.markAsRead();
						}
					}

				}

				ChatChannel msgChannel = ChannelManager.getInstance().getMainMsgChannelById(channel.channelID);
				if (msgChannel != null)
				{
					msgChannel.unreadCount = 0;
					msgChannel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
					DBManager.getInstance().updateChannel(msgChannel);
					ChannelManager.getInstance().calulateAllChannelUnreadNum();
				}
				if (channel.channelID.equals(MailManager.CHANNELID_MESSAGE))
					JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
							new Object[] { Integer.valueOf(1), Boolean.valueOf(false), "0,1,20,37,38,45,46" });
				else if (channel.channelID.equals(MailManager.CHANNELID_MOD))
					JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
							new Object[] { Integer.valueOf(1), Boolean.valueOf(false), "23,24,39,40" });
				else if (channel.channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
					JniController.getInstance().excuteJNIVoidMethod("readDialogMail",
							new Object[] { Integer.valueOf(1), Boolean.valueOf(false), "43,44" });
				else if (channel.channelID.equals(MailManager.CHANNELID_NEAR_BY))
				{
					if (!SwitchUtils.mqttEnable)
						WebSocketManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, "all");
					else
						MqttManager.getInstance().readUserChat(WebSocketManager.USER_CHAT_MODE_NEARBY, "all");
				}
			}
			else
			{
				String uids = channel.getMailUidsByConfigType(DBManager.CONFIG_TYPE_READ);
				if (StringUtils.isNotEmpty(uids))
				{
					JniController.getInstance().excuteJNIVoidMethod("readMutiMail", new Object[] { uids });
				}
			}
		}
	}

	public void postMainChannelChangedToGame(ChatChannel channel)
	{
		if (!MailManager.cocosMailListEnable || channel == null || !(channel.channelType == DBDefinition.CHANNEL_TYPE_USER
				|| channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM))
			return;
		List<ChatChannel> channelList = new ArrayList<ChatChannel>();
		ChatChannel changedChannel = null;
		if (!channel.isMainMsgChannel())
			changedChannel = ChannelManager.getInstance().getMainChannel(channel);
		else
			changedChannel = channel;

		if (changedChannel != null && !channelList.contains(changedChannel))
			channelList.add(changedChannel);
		parseChannelList(channelList);
	}

	public void parseChannelList(List<ChatChannel> channelList)
	{
		if (!MailManager.cocosMailListEnable)
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
		if (channelList != null && channelList.size() > 0)
		{
			ChatChannel recycleBinChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_RECYCLE_BIN);
			if (recycleBinChannel != null && !channelList.contains(recycleBinChannel))
				channelList.add(recycleBinChannel);
			List<ChannelMainListInfo> list = new ArrayList<ChannelMainListInfo>();
			Iterator<ChatChannel> iterator = channelList.iterator();
			while (iterator.hasNext())
			{
				ChatChannel channel = (ChatChannel) iterator.next();
				if (channel != null)
				{
					ChannelMainListInfo mainItem = new ChannelMainListInfo();
					mainItem.setItemInfo(channel);
					if (mainItem.getChannelItemType() >= 0)
						list.add(mainItem);
				}
			}
			try
			{
				Collections.sort(list);
				String ret = JSON.toJSONString(list);
				LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "ret", ret);
				JniController.getInstance().excuteJNIVoidMethod("postMailRootDataJson", new Object[] { ret });
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
}
