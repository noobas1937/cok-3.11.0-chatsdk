package com.elex.chatservice.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.mail.allianceapply.AllianceApplyMailData;
import com.elex.chatservice.model.mail.allianceinvite.AllianceInviteMailData;
import com.elex.chatservice.model.mail.alliancekickout.AllianceKickOutMailData;
import com.elex.chatservice.model.mail.battle.BattleMailContents;
import com.elex.chatservice.model.mail.battle.BattleMailData;
import com.elex.chatservice.model.mail.detectreport.DetectReportMailData;
import com.elex.chatservice.model.mail.inviteteleport.InviteTeleportMailData;
import com.elex.chatservice.model.mail.monster.MonsterMailContents;
import com.elex.chatservice.model.mail.monster.MonsterMailData;
import com.elex.chatservice.model.mail.newworldboss.NewWorldBossMailContents;
import com.elex.chatservice.model.mail.newworldboss.NewWorldBossMailData;
import com.elex.chatservice.model.mail.ocupy.OcupyMailData;
import com.elex.chatservice.model.mail.refuseallreply.RefuseAllReplyMailData;
import com.elex.chatservice.model.mail.resouce.ResourceMailContents;
import com.elex.chatservice.model.mail.resouce.ResourceMailData;
import com.elex.chatservice.model.mail.resourcehelp.ResourceHelpMailData;
import com.elex.chatservice.model.mail.worldboss.WorldBossMailData;
import com.elex.chatservice.model.mail.worldexplore.WorldExploreMailData;
import com.elex.chatservice.util.IAnalyticTracker;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.view.ChannelListFragment;

public class MailManager
{
	public static final int		MAILTAB_USER				= 0;				// ????????????????????????????????????????????????????????????
	public static final int		MAILTAB_SYSTEM				= 1;				// ??????????????????????????????"per_sys"
	public static final int		MAILTAB_NOTICE				= 2;				// ????????????????????????????????????
	public static final int		MAILTAB_STUDIO				= 3;				// COK???????????????
	public static final int		MAILTAB_FIGHT				= 4;				// ????????????
	public static final int		MAILTAB_MOD					= 5;				// mod??????

	public static final String	CHANNELID_SYSTEM			= "system";
	public static final String	CHANNELID_NOTICE			= "notice";
	public static final String	CHANNELID_STUDIO			= "studio";
	public static final String	CHANNELID_FIGHT				= "fight";
	public static final String	CHANNELID_MOD				= "mod";
	public static final String	CHANNELID_RESOURCE			= "resource";
	public static final String	CHANNELID_MONSTER			= "monster";
	public static final String	CHANNELID_RESOURCE_HELP		= "resourcehelp";
	public static final String	CHANNELID_ALLIANCE			= "alliance";
	public static final String	CHANNELID_MESSAGE			= "message";
	public static final String	CHANNELID_EVENT				= "event";
	public static final String	CHANNELID_KNIGHT			= "knight";
	public static final String	CHANNELID_RECYCLE_BIN		= "recyclebin";
	public static final String	CHANNELID_NEW_WORLD_BOSS	= "newworldboss";
	public static final String	CHANNELID_DRIFTING_BOTTLE	= "driftingbottle";
	public static final String	CHANNELID_DRAGON_TOWER		= "dragonTower";
	public static final String	CHANNELID_NEAR_BY			= "nearby";

	public static final int		ITEM_BG_COLOR_MESSAGE		= 0xFF2E3D59;
	public static final int		ITEM_BG_COLOR_ALLIANCE		= 0xFF38693F;
	public static final int		ITEM_BG_COLOR_BATTLE		= 0xFF852828;
	public static final int		ITEM_BG_COLOR_STUDIO		= 0xFF3F4145;
	public static final int		ITEM_BG_COLOR_SYSTEM		= 0xFF7F5C13;

	public static final int		SHOW_ITEM_NONE				= 0;
	public static final int		SHOW_ITEM_DETECT_REPORT		= 1;
	public static final int		SHOW_ITEM_ACTIVITY			= 2;

	public static final int		SERVER_BATTLE_FIELD			= 2;

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
		else if (channelId.equals(CHANNELID_DRAGON_TOWER))
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
		else if (channelId.equals(CHANNELID_MOD) || channelId.equals(CHANNELID_DRIFTING_BOTTLE) || channelId.equals(CHANNELID_NEAR_BY))
		{
			return ITEM_BG_COLOR_MESSAGE;
		}
		return ITEM_BG_COLOR_SYSTEM;
	}

	// ????????????
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
	public static final int			MAIL_AUDIO_SELF_SEND			= 37;								// ????????????????????????
	public static final int			MAIL_AUDIO_OTHER_SEND			= 38;								// ????????????????????????
	public static final int			MAIL_MOD_AUDIO_SELF_SEND		= 39;								// ????????????MOD????????????
	public static final int			MAIL_MOD_AUDIO_OTHER_SEND		= 40;								// mod??????MOD????????????
	public static final int			CHAT_ROOM_AUDIO					= 41;								// ???????????????
	public static final int			MAIL_NEW_WORLD_BOSS				= 42;
	public static final int			MAIL_DRIFTING_BOTTLE_SELF_SEND	= 43;
	public static final int			MAIL_DRIFTING_BOTTLE_OTHER_SEND	= 44;
	public static final int			MAIL_EXPRESSION_USER_SELF_SEND	= 45;
	public static final int			MAIL_EXPRESSION_USER_OTHER_SEND	= 46;
	public static final int			CHATROOM_EXPRESSION				= 47;
	public static final int         MAIL_ALLIANCE_BOSS              = 48;
	/** ??????????????????,????????????????????? */
	public static final int			MAIL_TYPE_COUNT					= MAIL_ALLIANCE_BOSS;

	// ????????????
	public static final int			OriginTile						= 0;
	public static final int			CityTile						= 1;
	public static final int			CampTile						= 2;								// ?????????
	public static final int			ResourceTile					= 3;								// ??????
	public static final int			KingTile						= 4;								// ??????
	public static final int			BattleTile						= 5;								// ???
	public static final int			MonsterTile						= 6;								// ??????
	public static final int			MonsterRange					= 7;
	public static final int			CityRange						= 8;								// ????????????
	public static final int			FieldMonster					= 9;								// ??????
	public static final int			Throne							= 10;								// ??????
	public static final int			ThroneRange						= 11;								// ????????????
	public static final int			Trebuchet						= 12;								// ?????????
	public static final int			TrebuchetRange					= 13;								// ???????????????
	public static final int			Tile_allianceArea				= 14;
	public static final int			ActBossTile						= 15;								// ????????????boss
	public static final int			Tile_allianceRange				= 16;								// ????????????16
	public static final int			ActBossTileRange				= 17;
	public static final int			tile_superMine					= 18;
	public static final int			tile_superMineRange				= 19;
	public static final int			tile_tower						= 20;
	public static final int			tile_wareHouse					= 21;
	public static final int			tile_wareHouseRange				= 22;
	public static final int			tile_banner						= 23;								// ????????????
	public static final int			Crystal							= 24;								// ??????
	public static final int			Crystal_Range					= 25;								// ????????????
	public static final int			Armory							= 26;								// ?????????
	public static final int			Armory_Range					= 27;								// ???????????????
	public static final int			TrainingField					= 28;								// ?????????
	public static final int			TrainingField_Range				= 29;								// ???????????????
	public static final int			SupplyPoint						= 30;								// ?????????
	public static final int			BessingTower					= 31;								// ?????????
	public static final int			MedicalTower					= 32;								// ?????????
	public static final int			DragonTower						= 33;								// ??????
	public static final int			Barracks						= 34;								// ??????,????????????
	public static final int			Barracks_Range					= 35;								// ????????????
	public static final int			TransferPoint					= 36;								// ?????????
	public static final int			Resource_new					= 37;								// ???????????????
	public static final int			Resource_newRang				= 38;								// range
	public static final int			Little_Crystal					= 41;								// ????????????
	public static final int			Alliance_Tree					= 42;								// ?????????
	public static final int			Alliance_TreeRange				= 43;								// ???????????????
	public static final int			Barbarian						= 44;								// ???????????????
	public static final int			Barbarian_Range					= 45;								// ?????????????????????
	public static final int			tile_flagsbuilding				= 46;								// ???????????????
	public static final int			tile_flagsbuildingRange			= 47;								// ?????????????????????

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
	private ExecutorService			executorService					= null;
	public String					needDelayShowActivityMailUid	= "";
	public static boolean			isDriftingBottleEnable			= false;
	public static boolean			dragonTowerMailEnable			= false;
	public static boolean			cocosMailListEnable				= false;
	public static boolean			nearbyEnable					= false;
	public static boolean			nearbyLikeEnable				= false;

	private MailManager()
	{
		transportedMailUidList = new ArrayList<String>();
		executorService = Executors.newFixedThreadPool(4);
	}

	public void setDelayShowActivityMailUid(String mailUid)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid);
		needDelayShowActivityMailUid = mailUid;
	}

	public String getDelayShowActivityMailUid()
	{
		return needDelayShowActivityMailUid;
	}

	public void runOnExecutorService(Runnable runnable)
	{
		if (runnable != null)
			executorService.execute(runnable);
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

	public void transportMailInfo(String jsonStr, int showMailType)
	{

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "jsonStr", jsonStr);
		if (tracker != null)
		{
			tracker.transportMail(jsonStr, showMailType);
		}
	}

	public void transportMailArray(String jsonArratStr)
	{

		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "jsonArratStr", jsonArratStr);
		if (tracker != null)
		{
			tracker.transportMailArray(jsonArratStr);
		}
	}

	public String getPublishChannelName()
	{
		if (tracker != null)
			return tracker.getPublishChannelName();
		return "";
	}

	public String getDeviceId()
	{
		if (tracker != null)
			return tracker.getDeviceId();
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
		else if (channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
		{
			typeArray.add(MailManager.MAIL_NEW_WORLD_BOSS);
		}
		else if (channelId.equals(MailManager.CHANNELID_EVENT))
		{
			typeArray.add(MailManager.MAIL_ALLIANCE_BOSS);
			typeArray.add(MailManager.MAIL_WORLD_BOSS);
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		else if (channelId.equals(MailManager.CHANNELID_DRAGON_TOWER))
		{
			typeArray.add(MailManager.MAIL_BATTLE_REPORT);
		}
		return typeArray;
	}

	public MailData parseMailDataContent(MailData mailData)
	{
		if (mailData == null)
			return null;
		MailData mail = null;
		switch (mailData.getType())
		{
			case MailManager.MAIL_BATTLE_REPORT:
				mail = new BattleMailData();
				if (mailData instanceof BattleMailData)
				{
					BattleMailData battle = (BattleMailData) mailData;
					((BattleMailData) mail).setShareExtra(battle.getShareExtra());
				}
				break;
			case MailManager.MAIL_RESOURCE:
				mail = new ResourceMailData();
				break;
			case MailManager.MAIL_DETECT_REPORT:
				mail = new DetectReportMailData();
				break;
			case MailManager.MAIL_ENCAMP:
				mail = new OcupyMailData();
				break;
			case MailManager.WORLD_NEW_EXPLORE:
				mail = new WorldExploreMailData();
				break;
			case MailManager.MAIL_ALLIANCEINVITE:
				mail = new AllianceInviteMailData();
				break;
			case MailManager.MAIL_ALLIANCEAPPLY:
				mail = new AllianceApplyMailData();
				break;
			case MailManager.MAIL_ATTACKMONSTER:
				mail = new MonsterMailData();
				break;
			case MailManager.MAIL_RESOURCE_HELP:
				mail = new ResourceHelpMailData();
				break;
			case MailManager.MAIL_INVITE_TELEPORT:
				mail = new InviteTeleportMailData();
				break;
			case MailManager.MAIL_ALLIANCE_KICKOUT:
				mail = new AllianceKickOutMailData();
				break;
			case MailManager.MAIL_WORLD_BOSS:
				if (mailData.isWorldBossKillRewardMail())
					mail = new MailData();
				else
					mail = new WorldBossMailData();
				break;
			case MailManager.MAIL_ALLIANCE_BOSS:
				if (mailData.isAllianceBossKillRewardMail())
					mail = new MailData();
				else
					mail = new WorldBossMailData();
				break;
			case MailManager.MAIL_REFUSE_ALL_APPLY:
				mail = new RefuseAllReplyMailData();
				break;
			case MailManager.MAIL_NEW_WORLD_BOSS:
				mail = new NewWorldBossMailData();
				break;
			default:
				mail = new MailData();
				break;
		}
		mail.setMailData(mailData);
		mail.parseContents();
		mail.setNeedParseByForce(false);
		return mail;
	}

	public String getMailIconByName(String name)
	{
		if (ChatServiceController.isNewMailUIEnable)
			return MailNewUI.getInstance().getIconByName(name);
		else
			return MailOldUI.getInstance().getIconByName(name);
	}

	public String transportNeiberMailData(MailData mailData, boolean needEarly, boolean needNext)
	{
		ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
		if (channel == null || channel.mailDataList == null || channel.mailDataList.size() <= 0 || !(needEarly || needNext))
			return "";

		for (int i = 0; i < channel.mailDataList.size(); i++)
		{
			MailData mail = channel.mailDataList.get(i);
			if (mail != null && mail.getUid().equals(mailData.getUid()))
			{
				String uid = "";
				if (needEarly && i - 1 >= 0)
				{
					MailData earilyMail = channel.mailDataList.get(i - 1);
					if (earilyMail != null)
					{
						transportMailData(earilyMail);
						uid = earilyMail.getUid();
					}
				}
				if (needNext && i + 1 < channel.mailDataList.size())
				{
					MailData nextMail = channel.mailDataList.get(i + 1);
					if (nextMail != null)
					{
						transportMailData(nextMail);
						uid = nextMail.getUid();
					}
				}
				if (StringUtils.isNotEmpty(uid))
					return uid;
				break;
			}
		}
		return "";
	}

	public void transportMailDataForGame(MailData mailData)
	{
		if (mailData != null)
		{
			try
			{
				String jsonStr = JSON.toJSONString(mailData);
				MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_NONE);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void transportMailData(MailData mailData)
	{
		if (mailData != null)
		{
			if (!MailManager.getInstance().isInTransportedMailList(mailData.getUid()))
			{
				try
				{
					String jsonStr = "";
					if (!mailData.isChannelMail()
							&& ((mailData.isComplexMail() && !mailData.hasMailOpend) || (mailData.getType() == MailManager.MAIL_BATTLE_REPORT && !mailData
									.getChannelId().equals(MailManager.CHANNELID_KNIGHT))))
					{
						mailData.setNeedParseByForce(true);
						MailData mail = MailManager.getInstance().parseMailDataContent(mailData);
						if (mailData.getType() == MailManager.MAIL_BATTLE_REPORT)
							mail.setContents("");
						jsonStr = JSON.toJSONString(mail);
					}
					else
					{
						jsonStr = JSON.toJSONString(mailData);
					}
					MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_NONE);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public MailData createDialogSystemMail(ChatChannel channel, List<MailData> mailDataList, boolean isFromDB)
	{
		if (channel != null && StringUtils.isNotEmpty(channel.channelID) && mailDataList != null)
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;
			String channelID = channel.channelID;

			int unreadCount = 0;
			boolean isLock = false;
			List mailList = null;
			if (channelID.equals(MailManager.CHANNELID_RESOURCE))
				mailList = new ArrayList<ResourceMailContents>();
			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
				mailList = new ArrayList<MonsterMailContents>();
			else if (channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
				mailList = new ArrayList<NewWorldBossMailContents>();
			else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
				mailList = new ArrayList<BattleMailContents>();

			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				channel.addInLoadMailUidList(mailData.getUid());

				if (!isLock && channelID.equals(MailManager.CHANNELID_KNIGHT) && mailData.isLock())
					isLock = true;

				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (!isFromDB && !(StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(mailData.getChannelId())))
					mailData = ServiceInterface.handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData, isFromDB, false);
				if (mailData instanceof ResourceMailData)
				{
					ResourceMailData resourceMail = (ResourceMailData) mailData;
					if (resourceMail.isUnread())
						unreadCount++;

					if (resourceMail.getCollect() == null || resourceMail.getCollect().size() <= 0)
						continue;
					ResourceMailContents resource = resourceMail.getCollect().get(0);
					if (resource != null && !mailList.contains(resource))
						mailList.add(resource);
				}
				else if (mailData instanceof MonsterMailData)
				{
					MonsterMailData monsterMail = (MonsterMailData) mailData;
					if (monsterMail.isUnread())
						unreadCount++;

					if (monsterMail.getMonster() == null || monsterMail.getMonster().size() <= 0)
						continue;
					MonsterMailContents monster = monsterMail.getMonster().get(0);
					if (monster != null && !mailList.contains(monster))
						mailList.add(monster);
				}
				else if (mailData instanceof NewWorldBossMailData)
				{
					NewWorldBossMailData newWorldBossMail = (NewWorldBossMailData) mailData;
					if (newWorldBossMail.isUnread())
						unreadCount++;

					if (newWorldBossMail.getWorldbosslist() == null || newWorldBossMail.getWorldbosslist().size() <= 0)
						continue;
					NewWorldBossMailContents worldBoss = newWorldBossMail.getWorldbosslist().get(0);
					if (worldBoss != null && !mailList.contains(worldBoss))
						mailList.add(worldBoss);
				}
				else if (mailData instanceof BattleMailData)
				{
					BattleMailData knightMail = (BattleMailData) mailData;
					if (knightMail.isUnread())
						unreadCount++;

					if (knightMail.getKnight() == null || knightMail.getKnight().size() <= 0)
						continue;
					BattleMailContents knight = knightMail.getKnight().get(0);
					if (knight != null && !mailList.contains(knight))
						mailList.add(knight);
				}
			}

			if (channelID.equals(MailManager.CHANNELID_RESOURCE))
			{
				ResourceMailData newMail = new ResourceMailData();
				newMail.setMailData(mail);
				newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
				newMail.setUnread(unreadCount);
				newMail.setCollect(mailList);
				return newMail;
			}
			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
			{
				MonsterMailData newMail = new MonsterMailData();
				newMail.setMailData(mail);
				newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
				newMail.setUnread(unreadCount);
				newMail.setMonster(mailList);
				return newMail;
			}
			else if (channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			{
				NewWorldBossMailData newMail = new NewWorldBossMailData();
				newMail.setMailData(mail);
				newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
				newMail.setUnread(unreadCount);
				newMail.setWorldbosslist(mailList);
				return newMail;
			}
			else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
			{
				BattleMailData newMail = new BattleMailData();
				newMail.setMailData(mail);
				newMail.setIsKnightMail(true);
				newMail.setHasParseForKnight(true);
				newMail.setSave(isLock ? 1 : 0);
				newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
				newMail.setUnread(unreadCount);
				newMail.setKnight(mailList);
				newMail.setContents("");
				newMail.setDetail(null);
				newMail.getChannelListItemType();
				return newMail;
			}
		}
		return null;
	}

	public void saveNewMailDataFromServerForAndroidUI(List<MailData> mailDataList)
	{
		DBManager.getInstance().insertGetNewMailData(mailDataList);

		if (mailDataList != null)
		{
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
				if (channel != null && channel.hasSysMailInList())
				{
					MailData mail = ServiceInterface.parseMailData(mailData, false);
					if (mail != null)
						channel.addNewMailData(mail);
				}
			}
		}

		prepareSystemMailDataInGetNew();
		if (ChatServiceController.getChannelListFragment() == null)
			ChannelListFragment.onMailAdded();
	}

	private void prepareSystemMailDataInGetNew()
	{
		if (MailManager.hasMoreNewMailToGet)
		{
			if (StringUtils.isNotEmpty(MailManager.latestMailUidFromGetNew))
			{
				if (!MailManager.latestMailUidFromGetNew.equals("0"))
				{
					MailData mail = DBManager.getInstance().getSysMailByID(MailManager.latestMailUidFromGetNew);
					if (mail != null)
					{
						long time = mail.getCreateTime();
						int createTime = TimeManager.getTimeInS(time);
						JniController.getInstance().excuteJNIVoidMethod("getNewMailFromServer",
								new Object[] { MailManager.latestMailUidFromGetNew, String.valueOf(createTime), 80 });
					}
				}
				else
				{
					MailManager.hasMoreNewMailToGet = false;
					handleGetNewNoMore();
				}

			}
		}
		else
		{
			handleGetNewNoMore();
		}
	}

	private void handleGetNewNoMore()
	{
		if (cocosMailListEnable)
		{
			ChannelManager.getInstance().preLoadSystemMailForGame();
		}
		else
		{
			ChannelManager.getInstance().prepareSystemMailChannel();
			if (ChatServiceController.getChannelListFragment() != null)
				ChatServiceController.getChannelListFragment().refreshTitle();
		}
		ServiceInterface.getNewMailExistStatus();

	}

	public void saveNewMailDataFromServer(List<MailData> mailDataList)
	{
		if (cocosMailListEnable)
			saveNewMailDataFromServerForCocosUI(mailDataList);
		else
			saveNewMailDataFromServerForAndroidUI(mailDataList);
	}

	public void saveNewMailDataFromServerForCocosUI(List<MailData> mailDataList)
	{
		DBManager.getInstance().insertGetNewMailData(mailDataList);
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "insertDB end");
		prepareSystemMailDataInGetNew();
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "transport initMail to Games end");
	}

	public void receiveNewMailDataFromServer(List<MailData> mailDataList)
	{
		if (mailDataList == null || mailDataList.size() <= 0)
			return;

		boolean hasDetectMail = false;
		String channelId = "";
		boolean hasOldNewBattleMail = false;
		boolean hasOldNewDetectMail = false;
		List<MailData> list = new ArrayList<MailData>();

		MailData tempMail = mailDataList.get(0);
		if (tempMail != null)
		{
			MailData firstMail = MailManager.getInstance().parseMailDataContent(tempMail);
			channelId = firstMail.getChannelId();
		}

		ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
		if (channel == null)
			return;

		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelID", channel.channelID);

		for (int i = 0; i < mailDataList.size(); i++)
		{
			MailData mailData = mailDataList.get(i);
			if (mailData == null)
				continue;
			if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
				hasDetectMail = true;
			if (mailData.isUnread())
			{
				if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 1)
					hasOldNewBattleMail = true;
				else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 2)
					hasOldNewDetectMail = true;
			}

			if (cocosMailListEnable)
			{
				if (!channel.isDialogChannel())
				{
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
					mailData = ServiceInterface.handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData, false, false);
				}
				channel.addInLoadMailUidList(mailData.getUid());
				list.add(mailData);
			}
			else
			{
				MailData mail = ServiceInterface.parseMailData(mailData, false);
				if (mail != null)
					channel.addNewMailData(mail);
			}
		}

		if (cocosMailListEnable)
		{
			if (list != null && list.size() > 0)
			{
				if (channel.isDialogChannel())
				{
					MailData mail = MailManager.getInstance().createDialogSystemMail(channel, list, false);
					MailManager.getInstance().transportMailDataForGame(mail);
				}
				else
				{
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

				List<ChatChannel> channelList = new ArrayList<ChatChannel>();
				if (!channelList.contains(channel))
					channelList.add(channel);
				ChannelManager.getInstance().parseChannelList(channelList);
			}
		}
		else
		{
			// ??????C++?????????list????????????????????????????????????????????????????????????getNew????????????????????????
			if (StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(channelId))
				ChannelManager.getInstance().postNotifyPopup(channelId);
		}

		if (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_MONSTER))
		{
			int createTime = DBManager.getInstance().getEarlistDeleteableMailCreateTime(channel.getSysMailCountInDB(),
					channel.unreadCount, channelId);
			if (createTime != -1)
				ChannelManager.getInstance().deleteSysMailFromCreateTime(channel, createTime);
		}

		ChannelManager.getInstance().calulateAllChannelUnreadNum();
		if (hasOldNewBattleMail)
			DBManager.getInstance().getLatestUnReadReportByType(1);
		if (hasOldNewDetectMail)
			DBManager.getInstance().getLatestUnReadReportByType(2);
		if (hasDetectMail)
			DBManager.getInstance().getDetectMailInfo();
		if (ChatServiceController.getChannelListFragment() == null)
			ChannelListFragment.onMailAdded();
	}

	public void onGetMutiMailData(boolean hasMore, String latestMailUid, final List<MailData> mailDataList)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasMore", hasMore, "latestMailUid", latestMailUid);
		ServiceInterface.postMoreMailInfo(hasMore, latestMailUid);
		if (mailDataList == null)
			return;
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "size", mailDataList.size());
		Runnable runable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (cocosMailListEnable)
						saveNewMailDataFromServerForCocosUI(mailDataList);
					else
						saveNewMailDataFromServerForAndroidUI(mailDataList);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		};
		runOnExecutorService(runable);
	}

	public void openMail(MailData mailData)
	{
		if (mailData != null)
		{
			LogUtil.trackPageView("ShowSysMail-" + mailData.channelId);
			transportAndShowMailData(mailData);
		}
	}

	private void transportAndShowMailData(MailData mailData)
	{
		if (ChatServiceController.getInstance().isUsingDummyHost())
			return;
		if (mailData != null)
		{
			if (isInTransportedMailList(mailData.getUid()))
			{
				setShowingMailUid("");
				if (mailData.hasReward())
				{
					LocalConfig config = ConfigManager.getInstance().getLocalConfig();
					boolean hasShowed = (config != null && config.isFirstRewardTipShowed());
					JniController.getInstance().excuteJNIVoidMethod("postFirstRewardAnimationShowed", new Object[] { Boolean.valueOf(hasShowed) });
				}
				ChatServiceController.doHostAction("showMailPopup", mailData.getUid(), "", "", true, true);
			}
			else
			{
				System.out.println("transportAndShowMailData not isInTransportedMailList:" + mailData.getUid());
				MailManager.getInstance().setShowingMailUid(mailData.getUid());
				MailManager.getInstance().transportMailData(mailData);
			}

			if (!mailData.getChannelId().equals(MailManager.CHANNELID_RESOURCE)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_KNIGHT)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_MONSTER)
					&& !mailData.getChannelId().equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			{
				MailManager.getInstance().transportNeiberMailData(mailData, true, true);
			}

		}
	}
}
