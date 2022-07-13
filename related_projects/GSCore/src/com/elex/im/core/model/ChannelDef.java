package com.elex.im.core.model;

import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.util.MathUtil;

public class ChannelDef
{
	public String				channelId;
	public int					channelType;

	public String				roomId;
	public String				groupId;

	public int					tabIndex;
	
	public static final String	CHANNEL_ID_POSTFIX_COUNTRY	= "@country";
	public static final String	CHANNEL_ID_POSTFIX_ALLIANCE	= "@alliance";
	public static final String	CHANNEL_ID_POSTFIX_BATTLE	= "@battle";
	public static final String	CHANNEL_ID_POSTFIX_USER		= "";
	public static final String	CHANNEL_ID_POSTFIX_CHATROOM	= "@chatroom";
	public static final String	CHANNEL_ID_POSTFIX_OFFICIAL	= "@official";
	public static final String	CHANNEL_ID_POSTFIX_MOD		= "@mod";
	public static final int		CHANNEL_TYPE_COUNTRY		= 0;
	public static final int		CHANNEL_TYPE_ALLIANCE		= 1;
	public static final int		CHANNEL_TYPE_USER			= 2;
	public static final int		CHANNEL_TYPE_CHATROOM		= 3;
	public static final int		CHANNEL_TYPE_OFFICIAL		= 4;
	public static final int		CHANNEL_TYPE_ALLIANCE_SHARE	= 5;
	public static final int		CHANNEL_TYPE_CUSTOM_CHAT	= 6;
	public static final int		CHANNEL_TYPE_BATTLE_FIELD	= 7;

	public static String getPostfixForType(int type)
	{
		switch (type)
		{
			case CHANNEL_TYPE_USER:
				return CHANNEL_ID_POSTFIX_USER;
			case CHANNEL_TYPE_COUNTRY:
				return CHANNEL_ID_POSTFIX_COUNTRY;
			case CHANNEL_TYPE_ALLIANCE:
				return CHANNEL_ID_POSTFIX_ALLIANCE;
			case CHANNEL_TYPE_BATTLE_FIELD:
				return CHANNEL_ID_POSTFIX_BATTLE;
			case CHANNEL_TYPE_CHATROOM:
				return CHANNEL_ID_POSTFIX_CHATROOM;
			case CHANNEL_TYPE_OFFICIAL:
				return CHANNEL_ID_POSTFIX_OFFICIAL;
			default:
				return null;
		}
	}

	public String getChannelName()
	{
		return channelId + getPostfixForType(channelType);
	}

	public String getChatTableName()
	{
		return DBDefinition.TABEL_CHAT + "_" + MathUtil.md5(getChannelName());
	}
}
