package com.elex.im.core.model.db;

import com.elex.im.core.util.MathUtil;

public class MsgTable
{
	public String	channelID;
	public int		channelType;

	public MsgTable(String channelID, int channelType)
	{
		super();
		this.channelID = channelID;
		this.channelType = channelType;
	}

	public String getChannelName()
	{
		return channelID + DBDefinition.getPostfixForType(channelType);
	}

	public String getTableNameAndCreate()
	{
		DBManager.getInstance().prepareMsgTable(this);

		return getTableName();
	}

	public String getTableName()
	{
		String channelName = getChannelName();
		String md5TableId = MathUtil.md5(channelName);
		String tableName = "";
		tableName = DBDefinition.chatTableId2Name(md5TableId);

		return tableName;
	}

	public static MsgTable createMsgTable(final String channelID, final int channelType)
	{
		return new MsgTable(channelID, channelType);
	}
}
