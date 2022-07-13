package com.elex.im.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import com.elex.im.core.IMCore;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.net.SyncController;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
public class ChannelManager
{
	public static final int						LOAD_MORE_COUNT			= 20;
	public static final int						LOAD_ALL_MORE_MIN_COUNT	= 20;
	public static final int						LOAD_ALL_MORE_MAX_COUNT	= 200;
	private static ChannelManager				instance;

	private ConcurrentHashMap<String, Channel>	channelMap;
	public Channel								countryChannel;
	public Channel								allianceChannel;
	private ArrayList<Channel>					modChannelList			= null;
	private ArrayList<Channel>					messageChannelList		= null;

	private ExecutorService						executorService			= null;
	public boolean								isGetingNewMsg			= false;
	
	private ChannelManager()
	{
		init();
	}

	public void reset()
	{
		init();
//		getLoadedModChannel().clear();
//		getLoadedMessageChannel().clear();
	}

	public static boolean isInited()
	{
		return instance != null;
	}

	private void init()
	{
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); 
		
		channelMap = new ConcurrentHashMap<String, Channel>();
		
		ArrayList<Channel> dbChannels = DBManager.getInstance().getAllChannels();
		for (int i = 0; i < dbChannels.size(); i++)
		{
			Channel dbChannel = dbChannels.get(i);
			if(channelMap.contains(dbChannel.getChannelName()))
				channelMap.remove(dbChannel.getChannelName());
			channelMap.put(dbChannel.getChannelName(), dbChannel);
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "    channelMap put key", dbChannel.getChannelName());
		}
	}

	public void loadInitMsgs()
	{
		if (channelMap != null)
		{
			Set<String> keySet = channelMap.keySet();
			if (keySet != null)
			{
				for (String key : keySet)
				{
					Channel channel = channelMap.get(key);
					// 初始化前20条Msg
					ArrayList<Msg> msgs = DBManager.getInstance().getMsgDAO().findMsgBySize(channel.getMsgTable(), 20);
					SyncController.handleMessage(msgs.toArray(new Msg[0]), channel.getChannelID(), "", false, false);
				}
			}
		}
	}

	public static ChannelManager getInstance()
	{
		if (instance == null)
		{
			instance = new ChannelManager();
		}
		return instance;
	}
	
	public Channel getChannel(int channelType)
	{
		return IMCore.getInstance().getAppConfig().getChannel(channelType);
	}

	/**
	 * channelId不一定存在
	 * channelId存在时，如果找不到会创建
	 * 
	 * @return null(如果channelType不为国家或联盟，且channelId为空时)
	 */
	public Channel getChannel(int channelType, String channelID)
	{
		if (StringUtils.isEmpty(channelID))
		{
			if (IMCore.getInstance().getAppConfig().isInBasicChat(channelType))
			{
				return getChannel(channelType);
			}
			else
			{
				LogUtil.trackMessage("Channel.getChannel return null, channelType=" + channelType + " channelID=" + channelID);
				return null;
			}
		}

		String channelName = Channel.getChannelName(channelType, channelID);
		Channel channel = channelMap.get(channelName);
		if (channel == null)
		{
			channel = DBManager.getInstance().getChannelDAO().getChannel(channelID, channelType);
			if (channel == null)
			{
				channel = initChannel(channelType, channelID);
//				ChannelListFragment.onChannelAdd();
				
				if (channel != null)
				{
					channel.setLatestModifyTime(TimeManager.getInstance().getCurrentTimeMS());
					DBManager.getInstance().getChannelDAO().update(channel);
				}
			}
			else
			{
//				if (ChatServiceController.isNewMailUIEnable
//						&& (channel.getChannelID().equals(MailManager.CHANNELID_NOTICE) || channel.getChannelID()
//								.equals(MailManager.CHANNELID_RESOURCE_HELP)))
//					return channel;
				if(channelMap.contains(channelName))
					channelMap.remove(channelName);
				channelMap.put(channelName, channel);
				// if (channel.isDialogChannel() ||
				// channel.channelID.equals(MailManager.CHANNELID_MOD))
				// parseFirstChannelID();
			}
		}
		
		return channel;
	}

	private Channel initChannel(int channelType, String channelID)
	{
		Channel channel = new Channel();
		channel.setChannelType(channelType);
		if (channelID != null)
		{
			channel.setChannelID(channelID);
		}
		DBManager.getInstance().getChannelDAO().add(channel);
		
		if (channel.getChannelID().equals(MailManager.CHANNELID_MOD) || channel.getChannelID().equals(MailManager.CHANNELID_MESSAGE)
				|| channel.getChannelID().equals(MailManager.CHANNELID_NOTICE)
				|| channel.getChannelID().equals(MailManager.CHANNELID_RESOURCE_HELP))
			return channel;
		if(channelMap.contains(channel.getChannelName()))
			channelMap.remove(channel.getChannelName());
		channelMap.put(channel.getChannelName(), channel);

		// if (channel.isDialogChannel())
		// parseFirstChannelID();
		return channel;
	}

	public String getModChannelFromUid(String channelId)
	{
		String fromUid = channelId;
		if (StringUtils.isNotEmpty(fromUid) && fromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
		{
			fromUid = fromUid.substring(0, fromUid.indexOf(DBDefinition.CHANNEL_ID_POSTFIX_MOD));
		}
		return fromUid;
	}

	public List<Channel> getAllModChannel()
	{
		return modChannelList;
	}

	public List<Channel> getAllMessageChannel()
	{
		return messageChannelList;
	}
	
	/**
	 * TODO channel与forceUseTime重复了，条件判断冗余，参数也有点过长。可以拆开，或者修改调用方式
	 */
	public void loadMoreMsgFromDB(final Channel channel,final int minSeqId,final int maxSeqId,final int minCreateTime,final boolean forceUseTime)
	{
		if (channel == null)
			return;

		Runnable runable = new Runnable()
		{
			
			@Override
			public void run()
			{
				// 删掉了使用seqId的逻辑
				if (forceUseTime)
				{
					List<Msg> dbItems = DBManager.getInstance().getMsgDAO().getMsgsByTime(channel, minCreateTime, LOAD_MORE_COUNT);
					if (dbItems != null)
					{
						Msg[] dbItemsArray = dbItems.toArray(new Msg[0]);
						if (dbItemsArray.length <= 0)
							return;

						SyncController.handleMessage(dbItemsArray, channel.getChannelID(), channel.getCustomName(), false, false);
					}
				}
			}
		};
		
		executorService.execute(runable);
	}
}
