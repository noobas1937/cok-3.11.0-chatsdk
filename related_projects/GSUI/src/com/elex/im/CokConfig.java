package com.elex.im;

import java.util.ArrayList;

import com.elex.im.core.IAppConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.GSController;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.model.ChannelView;
import com.elex.im.ui.model.CustomChannelData;

public class CokConfig implements IAppConfig
{
	private String				appId			= "100001";
	private static final String	GROUP_COUNTRY	= "country";
	private static final String	GROUP_ALLIANCE	= "alliance";
	private static final String	GROUP_ORIGINAL	= "original";

	private static CokConfig	instance;

	public static CokConfig getInstance()
	{
		if (instance == null)
		{
			instance = new CokConfig();
		}
		return instance;
	}

	private CokConfig()
	{
	}
	
	public String getAppId()
	{
		return appId;
	}

	public boolean isDefaultTranslateEnable()
	{
		return GSController.isDefaultTranslateEnable;
	}

	public String roomId2channelId(String roomId)
	{
		return roomId.substring(roomId.lastIndexOf("_") + 1);
	}
	
	public int group2channelType(String group)
	{
		if(StringUtils.isNotEmpty(group))
		{
			if(group.equals(GROUP_ORIGINAL))
				return DBDefinition.CHANNEL_TYPE_COUNTRY;
			else if(group.equals(GROUP_ALLIANCE))
				return DBDefinition.CHANNEL_TYPE_ALLIANCE;
			else if(group.equals(GROUP_COUNTRY))
			{
				if(isBattleChatEnable)
					return DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
				else
					return DBDefinition.CHANNEL_TYPE_COUNTRY;
			}
		}
		return -1;
	}

	public String getRoomId(Channel channel)
	{
		if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY){
			return getCountryRoomId();
		}else if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE){
			return getAllianceRoomId();
		}else if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD){
			return getBattleFieldRoomId();
		}
		
		return "";
	}
	
	public String getGroupId(Channel channel)
	{
		if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY){
			return isBattleChatEnable ? GROUP_ORIGINAL : GROUP_COUNTRY;
		}else if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE){
			return GROUP_ALLIANCE;
		}else if(channel.getChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD){
			return GROUP_COUNTRY;
		}
		
		return "";
	}
	
	/**
	 * id格式：<p>
	 * country_1<p>
	 * alliance_1_c79be2b653224cb4b1aeb5138ad15118<p>
	 * 
	 * test_country_1<p>
	 * test_alliance_1_c79be2b653224cb4b1aeb5138ad15118<p>
	 * 
	 * beta_country_107<p>
	 * beta_alliance_107_c79be2b653224cb4b1aeb5138ad15118<p>
	 */
	private String getCountryRoomId()
	{
		if(isBattleChatEnable)
			return getOriginalRoomIdPrefix() + GROUP_ORIGINAL+"_" + getOriginalCountryId();
		else
			return getOriginalRoomIdPrefix() + GROUP_COUNTRY+"_" + UserManager.getInstance().getCurrentUser().serverId;
	}

	public String getAllianceRoomId()
	{
		return GROUP_ALLIANCE+"_" + getOriginalCountryId() + "_" + UserManager.getInstance().getCurrentUser().allianceId;
	}
	
	private String getBattleFieldRoomId()
	{
//		if(ChatServiceController.canJoinDragonRoom())
//			return ChatServiceController.dragonRoomId;
//		else
			return getRoomIdPrefix() + GROUP_COUNTRY+"_" + UserManager.getInstance().getCurrentUser().serverId;
	}

	private int getOriginalCountryId()
	{
		return UserManager.getInstance().isInBattleField() ? UserManager.getInstance().getCurrentUser().crossFightSrcServerId
				: UserManager.getInstance().getCurrentUser().serverId;
	}
	
	private String getOriginalRoomIdPrefix()
	{
		if ((isInnerVersion() || IMCore.getInstance().host.isUsingDummyHost() || isBetaVersion()))
		{
			return "test_";
		}
		else
		{
			return "";
		}
	}

	public String getRoomIdPrefix()
	{
		if (isNotTestServer() && (isInnerVersion() || GSController.getInstance().isUsingDummyHost() || isBetaVersion()))
		{
			return "test_";
		}
		else
		{
			return "";
		}
	}
	
	public boolean isExtendedAudioMsg(Channel channel)
	{
		return channel.getChannelType() == DBDefinition.CHANNEL_TYPE_USER || channel.getChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}
	
	// TODO 下面的配置定义和使用方式还要修改

	/** crossFightSrcServerId = -1 表示没有跨服， >=0表示现在处于跨服状态 */
	public static int					crossFightSrcServerId;
	public static boolean				isKingdomBattleStart			= false;
	public static boolean				isAnicientBattleStart			= false;
	public static boolean				isDragonBattleStart				= false;
	public static boolean				isDragonPlayOffStart			= false;
	public static String				dragonRoomId					= "";
	public static boolean				isBattleChatEnable				= true;

	public static int					serverType						= -1;
	public static final int				NORMAL							= 0;											// 普通服
	public static final int				TEST							= 1;											// 外网测试服
	public static final int				ANCIENT_BATTLE_FIELD			= 2;											// 远古战场服务器
	public static final int				DRAGON_BATTLE					= 3;											// 巨龙战役服务器
	public static final int				ANCIENT_BATTLE_FIELD_TEST		= 4;											// 远古战场服务器测试
	public static final int				DRAGON_BATTLE_TEST				= 5;											// 巨龙战役服务器测试
	public static final int				INNER_TEST						= 6;											// 内网测试服
	public static final int				DRAGON_PLAYOFF					= 7;											// 巨龙季后赛服务器
	public static final int				DRAGON_PLAYOFF_TEST				= 8;
	
	public boolean isBetaVersion()
	{
		return IMCore.hostActivity.getPackageName().equals("com.hcg.cok.beta");
	}

	public boolean isInnerVersion()
	{
		return IMCore.hostActivity.getPackageName().equals("com.clash.of.kings.inner");
	}

	public static boolean isInKingdomBattleField()
	{
		return false;//isBattleChatEnable && (isKingdomBattleStart || UserManager.getInstance().isInBattleField())  && !isInAncientSencen() && !canJoinDragonRoom();
	}

	public static boolean isInCrossFightServer()
	{
		return crossFightSrcServerId > 0;
	}

	public static boolean needShowBattleFieldChannel()
	{
		return isBattleChatEnable
				&& (isKingdomBattleStart || isAnicientBattleStart || isDragonBattleStart || UserManager.getInstance().isInBattleField() || isInAncientSencen() || canJoinDragonRoom());
	}

	public static boolean needShowBattleTipLayout()
	{
		return isBattleChatEnable && !isKingdomBattleStart && !UserManager.getInstance().isInBattleField() && (isAnicientBattleStart || isDragonBattleStart || isDragonPlayOffStart) && !isInAncientSencen()
				&& !canJoinDragonRoom();
	}
	
	public static boolean needCrossServerBattleChat()
	{
		return isBattleChatEnable && (isKingdomBattleStart || UserManager.getInstance().isInBattleField() || isInAncientSencen()
				|| canJoinDragonRoom());
	}
	
	public static int getHornChannelType()
	{
		if(CokConfig.needCrossServerBattleChat()){
			return DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
		}else{
			return DBDefinition.CHANNEL_TYPE_COUNTRY;
		}
	}
	
	public static int getUserChannelType()
	{
		return DBDefinition.CHANNEL_TYPE_USER;
	}

	public static boolean canJoinDragonRoom()
	{
		return isInDragonSencen() && StringUtils.isNotEmpty(dragonRoomId);
	}

	public static boolean isInDragonSencen()
	{
		return serverType == DRAGON_BATTLE;
	}

	public static boolean isInAncientSencen()
	{
		return serverType == ANCIENT_BATTLE_FIELD;
	}
	
	public static boolean isNotTestServer()
	{
		return serverType != NORMAL && serverType != ANCIENT_BATTLE_FIELD && serverType != DRAGON_BATTLE && serverType != DRAGON_PLAYOFF;
	}

	public static boolean isInBattleField()
	{
		return isInKingdomBattleField() && GSController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
	}
	
	public Channel getCountryChannel()
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return null;
		
		String channelId = UserManager.getInstance().getCurrentUser().crossFightSrcServerId > 0 ? Integer.toString(UserManager.getInstance()
				.getCurrentUser().crossFightSrcServerId) : Integer.toString(UserManager.getInstance().getCurrentUser().serverId);
		
		return ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_COUNTRY, channelId);
	}
	
	public ChannelView getCountryChannelView()
	{
		return new ChannelView(getCountryChannel(), DBDefinition.CHANNEL_TYPE_COUNTRY);
	}
	
	// 同上
	public Channel getBattleFieldChannel()
	{
		if (UserManager.getInstance().getCurrentUser() == null)
			return null;
		
		return ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD,
				Integer.toString(UserManager.getInstance().getCurrentUser().serverId));
	}
	
	public ChannelView getBattleFieldChannelView()
	{
		return new ChannelView(getBattleFieldChannel(), DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
	}
	
	public ChannelView getCustomChannelView()
	{
		Channel channel = CustomChannelData.getInstance().getCustomChannel();

		ChannelView channelView = new ChannelView(channel, DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT);
		channelView.customChannelType = CustomChannelData.getInstance().customChannelType;
		channelView.customChannelId = CustomChannelData.getInstance().customChannelId;
		
		return channelView;
	}
	
	public ChannelView getMailChannelView()
	{
		if (CokChannelDef.isInUserMail() || CokChannelDef.isInChatRoom())
		{
			int currentChannelType = GSController.getCurrentChannelType();
			Channel chatChannel = ChannelManager.getInstance().getChannel(currentChannelType);
			return new ChannelView(chatChannel, currentChannelType);
		}
		
		return null;
	}

	/**
	 * 同上
	 * 可能有多个联盟或国家频道，需要动态取当前对应的频道
	 */
	public Channel getAllianceChannel()
	{
		if (!UserManager.getInstance().isCurrentUserInAlliance())
			return null;
		
		return ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_ALLIANCE,
				UserManager.getInstance().getCurrentUser().allianceId);
	}
	
	public ChannelView getAllianceChannelView()
	{
		return new ChannelView(getAllianceChannel(), DBDefinition.CHANNEL_TYPE_ALLIANCE);
	}
	
	public Channel getChatroomChannel()
	{
		return ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, UserManager.getInstance().getCurrentMail().opponentUid);
	}
	
	public Channel getChannel(int channelType)
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
		else if (channelType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT)
		{
			return CustomChannelData.getInstance().getCustomChannel();
		}
//		else
//		{
//			if (UserManager.getInstance().getCurrentMail() != null)
//				channelId = UserManager.getInstance().getCurrentMail().opponentUid;
//			if (ChatServiceController.isContactMod
//					&& !UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
//				channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
//			return ChannelManager.getInstance().getChannel(channelType, channelId);
//		}

		return null;
	}
	
	public ArrayList<Channel> getPredefinedChannels()
	{
		ArrayList<Channel> channels = new ArrayList<Channel>();
		if(getCountryChannel() != null) channels.add(getCountryChannel());
		if(getAllianceChannel() != null) channels.add(getAllianceChannel());
		if(getBattleFieldChannel() != null) channels.add(getBattleFieldChannel());
		
		return channels;
	}

//	// 无用
//	public boolean isCountryChannel(int channelType)
//	{
//		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY;
//	}
//
//	// 无用
//	public boolean isAllianceChannel(int channelType)
//	{
//		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
//	}
//
//	// 无用
//	public boolean isUserOrChatRoomChannel(int channelType, String channelID)
//	{
//		return (channelType == DBDefinition.CHANNEL_TYPE_USER && !channelID.equals(MailManager.CHANNELID_MOD) && !channelID
//				.equals(MailManager.CHANNELID_MESSAGE)) || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
//	}

	// Channel引用了1处
	public boolean isMessageChannel(Channel channel)
	{
		return (channel.getChannelType() == DBDefinition.CHANNEL_TYPE_USER
				&& !channel.getChannelID().endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD) && !channel.getChannelID().equals(
				MailManager.CHANNELID_MESSAGE))
				|| channel.getChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	// ui引用了2处
	public boolean isModChannel(Channel channel)
	{
		return channel.getChannelType() == DBDefinition.CHANNEL_TYPE_USER
				&& channel.getChannelID().endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD);
	}

	// ui引用了2处
	public boolean isAllAllianceMailChannel(Channel channel)
	{
		return channel.getChannelType() == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channel.getChannelID())
				&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().uid)
				&& channel.getChannelID().equals(UserManager.getInstance().getCurrentUser().uid);
	}
	
	public boolean isInBasicChat(int channelType)
	{
		return CokChannelDef.isInBasicChat(channelType);
	}

	public boolean canGetMultiUserInfo()
	{
		return GSController.getInstance().isUsingDummyHost();
	}

	public void excuteJNIVoidMethod(final String methodName, final Object[] params)
	{
		JniController.getInstance().excuteJNIVoidMethod(methodName, params);
	}
}
