package com.elex.im.core.model;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.elex.im.core.util.IAnalyticTracker;
import com.elex.im.core.util.LogUtil;

public class MailManager
{
	public static final int		MAILTAB_USER			= 0;				// 玩家邮件，包括个人邮件、聊天室、联盟邮件
	public static final int		MAILTAB_SYSTEM			= 1;				// 系统邮件，对应后台的"per_sys"
	public static final int		MAILTAB_NOTICE			= 2;				// 公告邮件，包括更新、公告
	public static final int		MAILTAB_STUDIO			= 3;				// COK工作室邮件
	public static final int		MAILTAB_FIGHT			= 4;				// 战斗邮件
	public static final int		MAILTAB_MOD				= 5;				// mod邮件

	public static final String	CHANNELID_SYSTEM		= "system";
	public static final String	CHANNELID_NOTICE		= "notice";
	public static final String	CHANNELID_STUDIO		= "studio";
	public static final String	CHANNELID_FIGHT			= "fight";
	public static final String	CHANNELID_MOD			= "mod";
	public static final String	CHANNELID_RESOURCE		= "resource";
	public static final String	CHANNELID_MONSTER		= "monster";
	public static final String	CHANNELID_RESOURCE_HELP	= "resourcehelp";
	public static final String	CHANNELID_ALLIANCE		= "alliance";
	public static final String	CHANNELID_MESSAGE		= "message";
	public static final String	CHANNELID_EVENT			= "event";
	public static final String	CHANNELID_KNIGHT		= "knight";
	public static final String	CHANNELID_RECYCLE_BIN	= "recyclebin";

	public static final int		ITEM_BG_COLOR_MESSAGE	= 0xFF2E3D59;
	public static final int		ITEM_BG_COLOR_ALLIANCE	= 0xFF38693F;
	public static final int		ITEM_BG_COLOR_BATTLE	= 0xFF852828;
	public static final int		ITEM_BG_COLOR_STUDIO	= 0xFF3F4145;
	public static final int		ITEM_BG_COLOR_SYSTEM	= 0xFF7F5C13;

	public static final int		SERVER_BATTLE_FIELD		= 2;

	public static int getColorByChannelId(String channelId)
	{
		if (channelId.equals(CHANNELID_MESSAGE))
		{
			return ITEM_BG_COLOR_MESSAGE;
		}
		else if (channelId.equals(CHANNELID_ALLIANCE))
		{
			return ITEM_BG_COLOR_ALLIANCE;
		}
		else if (channelId.equals(CHANNELID_FIGHT))
		{
			return ITEM_BG_COLOR_BATTLE;
		}
		else if (channelId.equals(CHANNELID_STUDIO))
		{
			return ITEM_BG_COLOR_STUDIO;
		}
		else if (channelId.equals(CHANNELID_SYSTEM))
		{
			return ITEM_BG_COLOR_SYSTEM;
		}
		else if (channelId.equals(CHANNELID_MOD))
		{
			return ITEM_BG_COLOR_MESSAGE;
		}
		return ITEM_BG_COLOR_SYSTEM;
	}

	// 邮件类型
	public static final int			MAIL_SELF_SEND					= 0;
	public static final int			MAIL_USER						= 1;
	public static final int			MAIL_SYSTEM						= 2;
	public static final int			MAIL_SERVICE					= 3;
	public static final int			MAIL_BATTLE_REPORT				= 4;
	public static final int			MAIL_RESOURCE					= 5;
	public static final int			MAIL_DETECT						= 6;
	public static final int			MAIL_GENERAL_TRAIN				= 7;
	public static final int			MAIL_DETECT_REPORT				= 8;
	public static final int			MAIL_ENCAMP						= 9;
	public static final int			MAIL_FRESHER					= 10;
	public static final int			MAIL_WOUNDED					= 11;
	public static final int			MAIL_DIGONG						= 12;
	public static final int			ALL_SERVICE						= 13;
	public static final int			WORLD_NEW_EXPLORE				= 14;
	public static final int			MAIL_SYSNOTICE					= 15;
	public static final int			MAIL_SYSUPDATE					= 16;
	public static final int			MAIL_ALLIANCEINVITE				= 17;
	public static final int			MAIL_ATTACKMONSTER				= 18;
	public static final int			WORLD_MONSTER_SPECIAL			= 19;
	public static final int			MAIL_Alliance_ALL				= 20;
	public static final int			MAIL_RESOURCE_HELP				= 21;
	public static final int			MAIL_PERSONAL					= 22;
	public static final int			MAIL_MOD_PERSONAL				= 23;
	public static final int			MAIL_MOD_SEND					= 24;
	public static final int			MAIL_ALLIANCEAPPLY				= 25;
	public static final int			MAIL_INVITE_TELEPORT			= 26;
	public static final int			MAIL_ALLIANCE_KICKOUT			= 27;
	public static final int			MAIL_GIFT						= 28;
	public static final int			MAIL_DONATE						= 29;
	public static final int			MAIL_WORLD_BOSS					= 30;
	public static final int			CHAT_ROOM						= 31;
	public static final int			MAIL_ACTIVITY					= 32;
	public static final int			MAIL_REFUSE_ALL_APPLY			= 33;
	public static final int			MAIL_ALLIANCE_PACKAGE			= 34;
	public static final int			MAIL_ALLIANCE_RANKCHANGE		= 35;
	public static final int			MAIL_ALLIANCE_OFFICER			= 36;
	public static final int			MAIL_AUDIO_SELF_SEND			= 37;				// 自己发的聊天语音
	public static final int			MAIL_AUDIO_OTHER_SEND			= 38;				// 别人发的聊天语音
	public static final int			MAIL_MOD_AUDIO_SELF_SEND		= 39;				// 自己发的MOD聊天语音
	public static final int			MAIL_MOD_AUDIO_OTHER_SEND		= 40;				// mod发的MOD聊天语音
	public static final int			CHAT_ROOM_AUDIO					= 41;				// 聊天室语音
	public static final int			MAIL_ALLIANCE_BOSS				= 48;
	/** 邮件类型总数,增加邮件需修改 */
	public static final int			MAIL_TYPE_COUNT					= MAIL_ALLIANCE_BOSS;

	// 世界建筑
	public static final int			OriginTile						= 0;
	public static final int			CityTile						= 1;
	public static final int			CampTile						= 2;				// 扎营地
	public static final int			ResourceTile					= 3;				// 资源
	public static final int			KingTile						= 4;				// 遗迹
	public static final int			BattleTile						= 5;				// 塔
	public static final int			MonsterTile						= 6;				// 地宫
	public static final int			MonsterRange					= 7;
	public static final int			CityRange						= 8;				// 玩家周边
	public static final int			FieldMonster					= 9;				// 野怪
	public static final int			Throne							= 10;				// 王座
	public static final int			ThroneRange						= 11;				// 王座周边
	public static final int			Trebuchet						= 12;				// 投石机
	public static final int			TrebuchetRange					= 13;				// 投石机周边
	public static final int			Tile_allianceArea				= 14;
	public static final int			ActBossTile						= 15;				// 活动怪物boss
	public static final int			Tile_allianceRange				= 16;				// 领地周边16
	public static final int			ActBossTileRange				= 17;
	public static final int			tile_superMine					= 18;
	public static final int			tile_superMineRange				= 19;
	public static final int			tile_tower						= 20;
	public static final int			tile_wareHouse					= 21;
	public static final int			tile_wareHouseRange				= 22;
	public static final int			tile_banner						= 23;				// 联盟国旗
	public static final int			Crystal							= 24;				// 水晶
	public static final int			Crystal_Range					= 25;				// 水晶周边
	public static final int			Armory							= 26;				// 军械库
	public static final int			Armory_Range					= 27;				// 军械库周边
	public static final int			TrainingField					= 28;				// 训练场
	public static final int			TrainingField_Range				= 29;				// 训练场周边
	public static final int			SupplyPoint						= 30;				// 供给点
	public static final int			BessingTower					= 31;				// 祝福塔
	public static final int			MedicalTower					= 32;				// 医疗塔
	public static final int			DragonTower						= 33;				// 龙塔
	public static final int			Barracks						= 34;				// 兵营,骑士大厅
	public static final int			Barracks_Range					= 35;				// 兵营周边
	public static final int			TransferPoint					= 36;				// 传送点
	public static final int			Resource_new					= 37;				// 活动资源点
	public static final int			Resource_newRang				= 38;				// range
	public static final int			Little_Crystal					= 41;				// 小水晶点
	public static final int			Alliance_Tree					= 42;				// 联盟树
	public static final int			Alliance_TreeRange				= 43;				// 联盟树周边
	public static final int			Barbarian						= 44;				// 野蛮人城堡
	public static final int			Barbarian_Range					= 45;				// 野蛮人城堡周边
	public static final int			tile_flagsbuilding				= 46;				// 旗帜建筑物
	public static final int			tile_flagsbuildingRange			= 47;				// 旗帜建筑物周边

	private static MailManager		_instance						= null;
	public static IAnalyticTracker	tracker							= null;

	public int						leastestUserMailCreateTime		= 0;
	public String					leastestUserMailUid				= "";
	public int						leastestSystemMailCreateTime	= 0;
	public String					leastestSystemMailUid			= "";
	private List<String>			transportedMailUidList			= null;
	private String					showingMailUid					= "";
	public static boolean			hasMoreNewMailToGet				= false;
	public static String			latestMailUidFromGetNew			= "";

	private MailManager()
	{
		transportedMailUidList = new ArrayList<String>();
	}

	public String getShowingMailUid()
	{
		return showingMailUid;
	}

	public void setShowingMailUid(String showingMailUid)
	{
		this.showingMailUid = showingMailUid;
	}

	public static MailManager getInstance()
	{
		if (_instance == null)
		{
			synchronized (MailManager.class)
			{
				if (_instance == null)
					_instance = new MailManager();
			}
		}
		return _instance;
	}

	public void clearMailInTransportList()
	{
		if (transportedMailUidList == null)
			return;
		transportedMailUidList.clear();
	}

	public void addMailInTransportedList(String mailUid)
	{
		if (transportedMailUidList == null)
			return;
		if (!isInTransportedMailList(mailUid))
			transportedMailUidList.add(mailUid);
	}

	public boolean isInTransportedMailList(String mailUid)
	{
		if (transportedMailUidList == null)
			return false;
		boolean ret = transportedMailUidList.contains(mailUid);
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "isInTranslatedMailList ret", ret);
		return ret;
	}

	public void transportMailInfo(String jsonStr, boolean isShowDetectMail)
	{
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "jsonStr", jsonStr);
//		if (tracker != null)
//		{
//			tracker.transportMail(jsonStr, isShowDetectMail);
//		}
	}

	public String getPublishChannelName()
	{
		if (tracker != null)
			return tracker.getPublishChannelName();
		return "";
	}

	public List<Integer> getChannelTypeArrayByChannel(String channelId)
	{
		List<Integer> typeArray = new ArrayList<Integer>();
		if (channelId.equals(MailManager.CHANNELID_FIGHT))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
			typeArray.add(MailManager.MAIL_DETECT);
			typeArray.add(MailManager.MAIL_DETECT_REPORT);
			typeArray.add(MailManager.MAIL_ENCAMP);
			typeArray.add(MailManager.WORLD_NEW_EXPLORE);
			typeArray.add(MailManager.WORLD_MONSTER_SPECIAL);
			typeArray.add(MailManager.MAIL_SYSTEM);
		}
		else if (channelId.equals(MailManager.CHANNELID_ALLIANCE))
		{
			typeArray.add(MailManager.MAIL_SYSTEM);
			typeArray.add(MailManager.MAIL_DONATE);
			typeArray.add(MailManager.MAIL_ALLIANCEINVITE);
			typeArray.add(MailManager.MAIL_Alliance_ALL);
			typeArray.add(MailManager.MAIL_ALLIANCEAPPLY);
			typeArray.add(MailManager.MAIL_ALLIANCE_KICKOUT);
			typeArray.add(MailManager.MAIL_INVITE_TELEPORT);
			typeArray.add(MailManager.MAIL_REFUSE_ALL_APPLY);
			typeArray.add(MailManager.MAIL_RESOURCE_HELP);
			typeArray.add(MailManager.MAIL_ALLIANCE_PACKAGE);
			typeArray.add(MailManager.MAIL_ALLIANCE_RANKCHANGE);
			typeArray.add(MailManager.MAIL_ALLIANCE_OFFICER);
		}
		else if (channelId.equals(MailManager.CHANNELID_STUDIO))
		{
			typeArray.add(MailManager.ALL_SERVICE);
			typeArray.add(MailManager.MAIL_SYSUPDATE);
		}
		else if (channelId.equals(MailManager.CHANNELID_SYSTEM))
		{
			typeArray.add(MailManager.MAIL_SYSNOTICE);
			typeArray.add(MailManager.MAIL_SYSTEM);
			typeArray.add(MailManager.MAIL_SERVICE);
			typeArray.add(MailManager.MAIL_FRESHER);
			typeArray.add(MailManager.MAIL_WOUNDED);
			typeArray.add(MailManager.MAIL_GIFT);
		}
		else if (channelId.equals(MailManager.CHANNELID_RESOURCE))
		{
			typeArray.add(MailManager.MAIL_RESOURCE);
		}
		else if (channelId.equals(MailManager.CHANNELID_KNIGHT))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_MONSTER))
		{
			typeArray.add(MailManager.MAIL_ATTACKMONSTER);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT))
		{
			typeArray.add(MailManager.MAIL_ALLIANCE_BOSS);
			typeArray.add(MailManager.MAIL_WORLD_BOSS);
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		return typeArray;
	}

//	public MailData parseMailDataContent(MailData mailData)
//	{
//		MailData mail = null;
//		switch (mailData.getType())
//		{
//			case MailManager.MAIL_BATTLE_REPORT:
//				mail = new BattleMailData();
//				break;
//			case MailManager.MAIL_RESOURCE:
//				mail = new ResourceMailData();
//				break;
//			case MailManager.MAIL_DETECT_REPORT:
//				mail = new DetectReportMailData();
//				break;
//			case MailManager.MAIL_ENCAMP:
//				mail = new OcupyMailData();
//				break;
//			case MailManager.WORLD_NEW_EXPLORE:
//				mail = new WorldExploreMailData();
//				break;
//			case MailManager.MAIL_ALLIANCEINVITE:
//				mail = new AllianceInviteMailData();
//				break;
//			case MailManager.MAIL_ALLIANCEAPPLY:
//				mail = new AllianceApplyMailData();
//				break;
//			case MailManager.MAIL_ATTACKMONSTER:
//				mail = new MonsterMailData();
//				break;
//			case MailManager.MAIL_RESOURCE_HELP:
//				mail = new ResourceHelpMailData();
//				break;
//			case MailManager.MAIL_INVITE_TELEPORT:
//				mail = new InviteTeleportMailData();
//				break;
//			case MailManager.MAIL_ALLIANCE_KICKOUT:
//				mail = new AllianceKickOutMailData();
//				break;
//			case MailManager.MAIL_WORLD_BOSS:
//				if (mailData.isWorldBossKillRewardMail())
//					mail = new MailData();
//				else
//					mail = new WorldBossMailData();
//				break;
//			case MailManager.MAIL_REFUSE_ALL_APPLY:
//				mail = new RefuseAllReplyMailData();
//				break;
//			default:
//				mail = new MailData();
//				break;
//		}
//		mail.setMailData(mailData);
//		mail.parseContents();
//		mail.setNeedParseByForce(false);
//		return mail;
//	}

	public String getMailIconByName(String name)
	{
//		if (ChatServiceController.isNewMailUIEnable)
//			return MailNewUI.getInstance().getIconByName(name);
//		else
//			return MailOldUI.getInstance().getIconByName(name);
		return "";
	}
//
//	public void transportMailData(MailData mailData)
//	{
//		if (mailData != null)
//		{
//			if (!MailManager.getInstance().isInTransportedMailList(mailData.getUid()))
//			{
//				try
//				{
//					String jsonStr = "";
//					if (!mailData.isChannelMail()
//							&& ((mailData.isComplexMail() && !mailData.hasMailOpend) || (mailData.getType() == MailManager.MAIL_BATTLE_REPORT && !mailData
//									.getChannelId().equals(MailManager.CHANNELID_KNIGHT))))
//					{
//						mailData.setNeedParseByForce(true);
//						MailData mail = MailManager.getInstance().parseMailDataContent(mailData);
//						if (mailData.getType() == MailManager.MAIL_BATTLE_REPORT)
//							mail.setContents("");
//						jsonStr = JSON.toJSONString(mail);
//					}
//					else
//					{
//						jsonStr = JSON.toJSONString(mailData);
//					}
//					MailManager.getInstance().transportMailInfo(jsonStr, false);
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}
//	}
}
