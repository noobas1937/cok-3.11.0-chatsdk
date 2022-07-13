package com.elex.im.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.SparseArray;

import com.elex.im.CokChannelDef;
import com.elex.im.CokConfig;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LocalConfig;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.GSController;

public class CustomChannelData
{
	public int							customChannelType		= -1;
	public String						customChannelId;
	public boolean						isSettingCustomChannel	= false;
	public boolean						customChannelChange		= false;
	private List<Channel>				friendList				= null;
	private List<Channel>				chatroomChannelList		= null;

	private static CustomChannelData	instance;

	public static CustomChannelData getInstance()
	{
		if (instance == null)
		{
			instance = new CustomChannelData();
		}
		return instance;
	}

	private CustomChannelData()
	{

	}

	public boolean hasCustomChannel()
	{
		return customChannelType != -1 && StringUtils.isNotEmpty(customChannelId);
	}

	public Channel getCustomChannel()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if (config != null)
		{
			customChannelType = config.getCustomChannelType();
			customChannelId = config.getCustomChannelId();
		}
		else
		{
			config = new LocalConfig();
			ConfigManager.getInstance().setLocalConfig(config);
		}
		
		Channel channel = null;

		if ((CokChannelDef.isInUserMail(customChannelType) || CokChannelDef.isInChatRoom(customChannelType))
				&& StringUtils.isNotEmpty(customChannelId))
			channel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);

		if (channel != null)
		{
			int mailType = CokConfig.getInstance().isModChannel(channel) ? MailManager.MAIL_MOD_PERSONAL
					: MailManager.MAIL_USER;
			GSController.setMailInfo(channel.getChannelID(), channel.latestId, channel.getCustomName(), mailType);
		}
		
		return channel;
	}
	
	public boolean hasCustomChannelData()
	{
		return (friendList != null && friendList.size() > 0) || (chatroomChannelList != null && chatroomChannelList.size() > 0);
	}

	public SparseArray<List<Channel>> prepareCustomChannelData()
	{
		SparseArray<List<Channel>> channelMap = new SparseArray<List<Channel>>();
		friendList = new ArrayList<Channel>();
		chatroomChannelList = new ArrayList<Channel>();
		List<String> friendChannelIdList = new ArrayList<String>();
		List<String> chatRoomChannelIdList = new ArrayList<String>();
		
		List<Channel> msgChannelList = ChannelManager.getInstance().getAllMessageChannel();
		List<Channel> modChannelList = ChannelManager.getInstance().getAllModChannel();
		
		if(modChannelList!=null)
		{
			for(Channel channel :modChannelList)
			{
				if(channel!=null)
				{
					if(CokChannelDef.isInChatRoom(channel.getChannelType()) && !chatRoomChannelIdList.contains(channel.getChannelID()))
					{
						chatRoomChannelIdList.add(channel.getChannelID());
						chatroomChannelList.add(channel);
					}
					else if(!CokConfig.getInstance().isAllAllianceMailChannel(channel) && CokChannelDef.isInUserMail(channel.getChannelType())
							&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD) && !channel.getChannelID().equals(MailManager.CHANNELID_MESSAGE) && !friendChannelIdList.contains(channel.getChannelID()))
					{
						friendChannelIdList.add(channel.getChannelID());
						friendList.add(channel);
					}
				}
			}
		}
		
		if(msgChannelList!=null)
		{
			for(Channel channel :msgChannelList)
			{
				if(channel!=null)
				{
					if(CokChannelDef.isInChatRoom(channel.getChannelType()) && !chatRoomChannelIdList.contains(channel.getChannelID()))
					{
						chatRoomChannelIdList.add(channel.getChannelID());
						chatroomChannelList.add(channel);
					}
					else if(!CokConfig.getInstance().isAllAllianceMailChannel(channel) && CokChannelDef.isInUserMail(channel.getChannelType()) 
							&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD) && !channel.getChannelID().equals(MailManager.CHANNELID_MESSAGE) && !friendChannelIdList.contains(channel.getChannelID()))
					{
						friendChannelIdList.add(channel.getChannelID());
						friendList.add(channel);
					}
				}
			}
		}
		
		Collections.sort(friendList);
		Collections.sort(chatroomChannelList);
		channelMap.put(0, friendList);
		channelMap.put(1, chatroomChannelList);
		
		return channelMap;
	}

	public void resetState()
	{
		if(customChannelChange)
		{
			ConfigManager.getInstance().saveLocalConfig();
			customChannelChange = false;
		}
	}
}
