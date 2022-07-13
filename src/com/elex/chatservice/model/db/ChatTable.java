package com.elex.chatservice.model.db;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.util.MathUtil;

public class ChatTable
{
	public String	channelID;
	public int		channelType;

	public ChatTable(String channelID, int type)
	{
		this.channelID = channelID;
		this.channelType = type;
	}
	
	public boolean needNotSaveDB()
	{
		return channelType != DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
	}
	
	public boolean isNotMailChannelTable()
	{
		return channelType != DBDefinition.CHANNEL_TYPE_COUNTRY 
				&& channelType != DBDefinition.CHANNEL_TYPE_ALLIANCE
				&& channelType != DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				&& channelType != DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
				&& channelType != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				&& needNotSaveDB();
	}

	public String getChannelName()
	{
		return channelID + DBDefinition.getPostfixForType(channelType);
	}

	public String getTableNameAndCreate()
	{
		DBManager.getInstance().prepareChatTable(this);

		return getTableName();
	}

	public boolean isChannelType()
	{
		if (StringUtils.isNotEmpty(channelID)
				&& (channelID.equals(MailManager.CHANNELID_RESOURCE) || channelID.equals(MailManager.CHANNELID_MONSTER) 
						|| channelID.equals(MailManager.CHANNELID_RESOURCE_HELP) || channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)))
		{
			return true;
		}
		return false;
	}

	public int getMailTypeByChannelId()
	{
		int type = -1;
		if (StringUtils.isNotEmpty(channelID))
		{
			if (channelID.equals(MailManager.CHANNELID_RESOURCE))
			{
				type = MailManager.MAIL_RESOURCE;
			}
			if (channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
			{
				type = MailManager.MAIL_RESOURCE_HELP;
			}
			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
			{
				type = MailManager.MAIL_ATTACKMONSTER;
			}
			else if (channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			{
				type = MailManager.MAIL_NEW_WORLD_BOSS;
			}
		}
		return type;
	}

	public String getTableName()
	{
		String channelName = getChannelName();
		if(channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
			channelName = channelID + DBDefinition.getPostfixForType(DBDefinition.CHANNEL_TYPE_ALLIANCE);
		else if(channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			channelName = channelID + DBDefinition.getPostfixForType(DBDefinition.CHANNEL_TYPE_COUNTRY);
		String md5TableId = MathUtil.md5(channelName);
		String tableName = "";
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			tableName = DBDefinition.chatTableId2Name(md5TableId);
		}
		else
		{
			tableName = DBDefinition.TABEL_MAIL;
		}

		return tableName;
	}

	public static ChatTable createChatTable(final int channelType, final String channelID)
	{
		return new ChatTable(channelID, channelType);
	}
	
	public boolean isSysAllianceChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS;
	}
	
	public boolean isSysCountryChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}
	
	public boolean isNormalAllianceChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}
	
	public boolean isNormalCountryChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY;
	}
}
