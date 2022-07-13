package com.elex.im.core.model.db;

import com.elex.im.core.util.MathUtil;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.model.db.DBManager;

public class ChatTable
{
	public String	channelID;
	public int		channelType;

	public ChatTable(String channelID, int type)
	{
		this.channelID = channelID;
		this.channelType = type;
	}

	public String getChannelName()
	{
		return channelID + DBDefinition.getPostfixForType(channelType);
	}

	public String getTableNameAndCreate()
	{
//		DBManager.getInstance().prepareChatTable(this);

		return getTableName();
	}

	public boolean isChannelType()
	{
//		if (StringUtils.isNotEmpty(channelID)
//				&& (channelID.equals(MailManager.CHANNELID_RESOURCE) || channelID.equals(MailManager.CHANNELID_MONSTER) || channelID
//						.equals(MailManager.CHANNELID_RESOURCE_HELP)))
//		{
//			return true;
//		}
		return false;
	}

	public int getMailTypeByChannelId()
	{
		int type = -1;
//		if (StringUtils.isNotEmpty(channelID))
//		{
//			if (channelID.equals(MailManager.CHANNELID_RESOURCE))
//			{
//				type = MailManager.MAIL_RESOURCE;
//			}
//			if (channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
//			{
//				type = MailManager.MAIL_RESOURCE_HELP;
//			}
//			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
//			{
//				type = MailManager.MAIL_ATTACKMONSTER;
//			}
//		}
		return type;
	}

	public String getTableName()
	{
		String channelName = getChannelName();
		String md5TableId = MathUtil.md5(channelName);
		String tableName = "";
		tableName = DBDefinition.chatTableId2Name(md5TableId);

		return tableName;
	}

	public static ChatTable createChatTable(final int channelType, final String channelID)
	{
		return new ChatTable(channelID, channelType);
	}
}
