package com.elex.chatservice.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.view.ChannelListFragment;

public class ChannelMainListInfo implements Comparable<ChannelMainListInfo> {

	private int channelItemType;
	private int unreadCount;
	private int hasReward ;
	private int canShow = 1;
	private int currentLoadCount;
	private int totalCount;



	public int getTotalCount() {
		return totalCount;
	}


	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}



	public int getChannelItemType() {
		return channelItemType;
	}



	public void setChannelItemType(int channelItemType) {
		this.channelItemType = channelItemType;
	}



	public int getUnreadCount() {
		return unreadCount;
	}



	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}




	public int getHasReward() {
		return hasReward;
	}



	public void setHasReward(int hasReward) {
		this.hasReward = hasReward;
	}



	public int getCanShow() {
		return canShow;
	}



	public void setCanShow(int canShow) {
		this.canShow = canShow;
	}



	public int getCurrentLoadCount() {
		return currentLoadCount;
	}



	public void setCurrentLoadCount(int currentLoadCount) {
		this.currentLoadCount = currentLoadCount;
	}



	@Override
	@JSONField(serialize = false)
	public int compareTo(ChannelMainListInfo another) {
		if(another.channelItemType < channelItemType)
			return 1;
		else if(another.channelItemType > channelItemType)
			return -1;
		else
			return 0;
	}
	
	@JSONField(serialize = false)
	public void setItemInfo(ChatChannel channel)
	{
		if(channel == null || StringUtils.isEmpty(channel.channelID))
			return;
		channelItemType = getChannelTypeById(channel.channelID);
		unreadCount = channel.unreadCount;
		hasReward = channel.isRewardAllChannel() && channel.hasMailDataInDBByType(DBManager.CONFIG_TYPE_REWARD) ? 1 : 0;
		
		if(channel.isMainMsgChannel())
		{
			List<ChatChannel> list = ChannelManager.getInstance().getAllMsgChannelById(channel.channelID);
			if(list!=null)
				totalCount = list.size();
		}
		else
		{
			totalCount = channel.getSysMailCountInDB();
		}
		
		
		if(channel.channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			canShow = MailManager.isDriftingBottleEnable ? 1 : 0;
		else if(channel.channelID.equals(MailManager.CHANNELID_NEAR_BY))
			canShow = 0;
		else if(channel.isDialogChannel() || channel.channelID.equals(MailManager.CHANNELID_MOD))
		{
			if(totalCount>0)
				canShow = 1;
			else
				canShow = 0;
		}
		else if(channel.channelID.equals(MailManager.CHANNELID_DRAGON_TOWER))
		{
			canShow = MailManager.dragonTowerMailEnable ? 1 : 0;
		}
		else
			canShow = 1;
		
		currentLoadCount = 0;
		if(channel.isMainMsgChannel())
		{
			List<ChatChannel> messageChannelArr = ChannelManager.getInstance().getAllMsgChannelById(channel.channelID);
			if(messageChannelArr!=null)
				currentLoadCount = messageChannelArr.size();
		}
		else
		{
			if(channel.mailUidList!=null)
				currentLoadCount = channel.mailUidList.size();
		}
		
//		if(channel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
//		{
//			List<MailData> mailList = DBManager.getInstance().getSysMailFromDB(channel.channelID, -1);
//			if(mailList!=null)
//			{
//				for (int i = 0; i < mailList.size(); i++)
//				{
//					MailData mailData = (MailData) mailList.get(i);
//					if (mailData != null)
//					{
//						if (mailData.canDelete())
//							allMailUids = ChannelManager.appendStr(allMailUids, mailData.getUid());
//					}
//				}
//			}
//		}
		
	}
	
	@JSONField(serialize = false)
	private int getChannelTypeById(String channelId)
	{
		if(StringUtils.isEmpty(channelId))
			return -1;
		int type = -1;
		if(channelId.equals(MailManager.CHANNELID_MESSAGE))
			type = 0;
		else if(channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			type = 1;
		else if(channelId.equals(MailManager.CHANNELID_NEAR_BY))
			type = 2;
		else if(channelId.equals(MailManager.CHANNELID_ALLIANCE))
			type = 3;
		else if(channelId.equals(MailManager.CHANNELID_FIGHT))
			type = 4;
		else if(channelId.equals(MailManager.CHANNELID_EVENT))
			type = 5;
		else if(channelId.equals(MailManager.CHANNELID_STUDIO))
			type = 6;
		else if(channelId.equals(MailManager.CHANNELID_SYSTEM))
			type = 7;
		else if(channelId.equals(MailManager.CHANNELID_DRAGON_TOWER))
			type = 8;
		else if(channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
			type = 9;
		else if(channelId.equals(MailManager.CHANNELID_MOD))
			type = 10;
		else if(channelId.equals(MailManager.CHANNELID_RESOURCE))
			type = 11;
		else if(channelId.equals(MailManager.CHANNELID_MONSTER))
			type = 12;
		else if(channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			type = 13;
		else if(channelId.equals(MailManager.CHANNELID_KNIGHT))
			type = 14;
		return type;
	}

}
