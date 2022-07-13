package com.elex.chatservice.view;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.UserManager;

import android.app.Activity;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

public class ChatQuickActionFactory
{
	public static final int	ID_JOIN_ALLIANCE			= 1;
	public static final int	ID_COPY						= 2;
	public static final int	ID_SEND_MAIL				= 3;
	public static final int	ID_VIEW_PROFILE				= 4;
	public static final int	ID_BAN						= 5;
	public static final int	ID_UNBAN					= 8;
	public static final int	ID_TRANSLATE				= 6;
	public static final int	ID_ORIGINAL_LANGUAGE		= 7;
	public static final int	ID_VIEW_BATTLE_REPORT		= 9;
	public static final int	ID_INVITE					= 10;
	public static final int	ID_BLOCK					= 11;
	public static final int	ID_UNBLOCK					= 12;
	public static final int	ID_VIEW_DETECT_REPORT		= 13;
	public static final int	ID_REPORT_HEAD_IMG			= 14;
	public static final int	ID_VIEW_EQUIPMENT			= 15;
	public static final int	ID_REPORT_PLAYER_CHAT		= 16;
	public static final int	ID_TRANSLATE_NOT_UNDERSTAND	= 17;
	public static final int	ID_SAY_HELLO				= 18;
	public static final int	ID_VIEW_RALLY_INFO			= 19;
	public static final int	ID_VIEW_LOTTERY_SHARE		= 20;
	public static final int	ID_VIEW_ALLIANCETASK_SHARE	= 21;
	public static final int	ID_VIEW_RED_PACKAGE			= 22;
	public static final int	ID_VIEW_ALLIANCE_TREASURE	= 23;
	public static final int	ID_SWITCH_TO_RECEIVER		= 24;
	public static final int	ID_SWITCH_TO_SPEAKER		= 25;
	public static final int	ID_VIEW_ALLIANCEHELP		= 26;
	public static final int	ID_VIEW_FIRST_KILL			= 27;
	public static final int	ID_VIEW_BUY_MSG_BG			= 28;
	public static final int	ID_VIEW_VIP_LOTTERY			= 29;
	public static final int	ID_VIEW_KING_INFO			= 30;
	public static final int	ID_VIEW_CANNON_INFO			= 31;
	public static final int	ID_VIEW_RANKING_LIST		= 32;
	public static final int	ID_VIEW_ACTIVITY			= 33;
	public static final int	ID_VIEW_DRAGON_SHARE		= 34;
	public static final int	ID_VIEW_NEW_RALLY			= 35;
	public static final int	ID_VIEW_COMMON_SHARED		= 36;
	public static final int	ID_VIEW_GOLDEN_BOX			= 37;
	
	public static QuickAction createQuickAction(final Activity activity, MsgItem item)
	{
		QuickAction quickAction = actuallyCreateQuickAction(activity, item, QuickAction.HORIZONTAL, 0);

		if (quickAction.isWiderThanScreen())
		{
			quickAction = ChatQuickActionFactory.actuallyCreateQuickAction(activity, item, QuickAction.VERTICAL, quickAction.getMaxItemWidth());
		}

		return quickAction;
	}

	private static QuickAction actuallyCreateQuickAction(final Activity activity, MsgItem item, int orientation, int maxItemWidth)
	{
		// create QuickAction. Use QuickAction.VERTICAL or
		// QuickAction.HORIZONTAL param to define layout orientation
		final QuickAction quickAction = new QuickAction(activity, orientation);

		if (orientation == QuickAction.VERTICAL && maxItemWidth > 0)
		{
			quickAction.maxItemWidth = maxItemWidth;
		}

		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			quickAction.scaleRatio = ConfigManager.scaleRatio;
		}

		boolean canTranlate = !item.canNotShowTranslateQuickActionMenu();
		boolean canViewEquip = item.isEquipMessage();
		boolean hasTranslated = item.canShowTranslateMsg() || (item.isTranslatedByForce && !item.isOriginalLangByForce);
		boolean canJoinAlliance = item.isAllianceInviteMessage() && item.isInAlliance() && !item.isSelfMsg() &&
				UserManager.getInstance().getCurrentUser()!=null && !UserManager.getInstance().getCurrentUser().isInAlliance()
				&& !ChatServiceController.isInMailDialog();
		boolean canBlock = !item.isNpcMessage() && !item.isSystemMessage() && !item.isSelfMsg() && !item.isTipMsg()
				&& !UserManager.getInstance().isInRestrictList(item.uid, UserManager.BLOCK_LIST);
		boolean canUnBlock = !item.isNpcMessage() && !item.isSystemMessage() && !item.isSelfMsg() && !item.isTipMsg()
				&& UserManager.getInstance().isInRestrictList(item.uid, UserManager.BLOCK_LIST);
		boolean canBan = !item.isNpcMessage() && !item.isSystemMessage() && item.isNotInRestrictList() && !item.isSelfMsg() && !item.isTipMsg()
				&& (UserManager.getInstance().getCurrentUser().mGmod >= 1 && UserManager.getInstance().getCurrentUser().mGmod != 11)
				&& !ChatServiceController.isInMailDialog();
		boolean canUnBan = !item.isNpcMessage() && !item.isSystemMessage() && item.isInRestrictList() && !item.isSelfMsg() && !item.isTipMsg()
				&& (UserManager.getInstance().getCurrentUser().mGmod == 3) && !ChatServiceController.isInMailDialog();
		boolean canViewBattleReport = item.isBattleReport() && UserManager.getInstance().getCurrentUser()!=null 
				&& UserManager.getInstance().getCurrentUser().isInAlliance() && !ChatServiceController.isInMailDialog();
		boolean canViewDetectReport = item.isDetectReport() && UserManager.getInstance().getCurrentUser()!=null 
				&& UserManager.getInstance().getCurrentUser().isInAlliance() && !ChatServiceController.isInMailDialog();
		boolean canInvite = !item.isNpcMessage() && !item.isInAlliance()
				&& UserManager.getInstance().getCurrentUser()!=null && UserManager.getInstance().getCurrentUser().isInAlliance()
				&& UserManager.getInstance().getCurrentUser().allianceRank >= 3 && !item.isSelfMsg() && !item.isTipMsg()
				&& !ChatServiceController.isInMailDialog();
		boolean canReportHeadImg = !item.isNpcMessage() && (item.isHornMessage() || !item.isSystemMessage()) && !item.isSelfMsg() && item.isCustomHeadImage();
		boolean canReportContent = !item.isNpcMessage() && (item.isHornMessage() || !item.isSystemMessage()) && !item.isSelfMsg();
		boolean canSayHello = item.isAllianceJoinMessage() && !item.isSelfMsg();
		boolean canViewRallyInfo = item.isRallyMessage();
		boolean canViewLotteryShare = item.isLotteryMessage();
		boolean canViewAllianceTaskShare = item.isAllianceTaskMessage() && StringUtils.isNotEmpty(item.msg) && item.msg.contains(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1);
		boolean canViewAllianceTreasure = item.isAllianceTreasureMessage() && !item.isSelfMsg();
		boolean canSwitchToReceiver = item.isAudioMessage() && ConfigManager.playAudioBySpeaker;
		boolean canSwitchToSpeaker = item.isAudioMessage() && !ConfigManager.playAudioBySpeaker;
		boolean canHelpAlliance = item.isAllianceHelpMessage() && !item.isSelfMsg();
		boolean canViewMonster = item.isMonsterFirstKillMessage();
		boolean canBuyMsgBg = ChatServiceController.currentMainCityLevel > 5 && !item.isSelfMsg() && item.getCustomChatBgId()>0 && UserManager.getInstance().getCurrentUser()!=null &&
				UserManager.getInstance().getCurrentUser().getCustomChatBg()<=0;
		boolean canViewVipGiftBox = item.isVipLotteryShare();
		boolean canViewKing	= item.isKingTip();
		boolean canViewCannon = item.isCannonTip() && UserManager.getInstance().getCurrentUser().isInAlliance();
		boolean canViewRankingList = item.isRankingListTip();
		boolean canViewActivityConcern = item.isActivityTip();
		boolean canViewDragonShare = item.isDragonShareMsg();
		boolean canViewNewRally = item.isNewRallyMsg();
		boolean canViewCommonShared = item.isCommonSharedMsg();
		boolean canViewGoldenBox = item.isGoldenBoxMsg();
		boolean canTranslateDevelop = ChatServiceController.translateDevelopEnable && !item.isSelfMsg() && hasTranslated && !item.isNpcMessage() && (item.isHornMessage() || !item.isSystemMessage()) && StringUtils.isNotEmpty(item.msg) && StringUtils.isNotEmpty(item.translateMsg) && StringUtils.isNotEmpty(item.originalLang);

		boolean canSendMail = false;
		
		if (canHelpAlliance)
		{
			ActionItem nextItem = new ActionItem(ID_VIEW_ALLIANCEHELP, LanguageManager.getLangByKey(LanguageKeys.MENU_ALLIANCE_HELP));
			quickAction.addActionItem(nextItem);
		}
		
		if (canViewEquip)
		{
			ActionItem nextItem = new ActionItem(ID_VIEW_EQUIPMENT, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_EQUIPMENT));
			quickAction.addActionItem(nextItem);
		}

		if (canViewBattleReport)
		{
			ActionItem battleMsgItem = new ActionItem(ID_VIEW_BATTLE_REPORT, LanguageManager.getLangByKey(LanguageKeys.MENU_BATTLEREPORT));
			quickAction.addActionItem(battleMsgItem);
		}

		if (canViewDetectReport)
		{
			ActionItem detectMsgItem = new ActionItem(ID_VIEW_DETECT_REPORT, LanguageManager.getLangByKey(LanguageKeys.MENU_DETECTREPORT));
			quickAction.addActionItem(detectMsgItem);
		}

		if (canViewRallyInfo)
		{
			ActionItem rallyMsgItem = new ActionItem(ID_VIEW_RALLY_INFO, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_RALLY_INFO));
			quickAction.addActionItem(rallyMsgItem);
		}

		if (canViewLotteryShare)
		{
			ActionItem lotteryShareMsgItem = new ActionItem(ID_VIEW_LOTTERY_SHARE, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(lotteryShareMsgItem);
		}

		if (canViewAllianceTaskShare)
		{
			ActionItem allianceTaskShareMsgItem = new ActionItem(ID_VIEW_ALLIANCETASK_SHARE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_TASK));
			quickAction.addActionItem(allianceTaskShareMsgItem);
		}

		if (canTranlate)
		{
			if (hasTranslated)
			{
				ActionItem originalItem = new ActionItem(ID_ORIGINAL_LANGUAGE, LanguageManager.getLangByKey(LanguageKeys.MENU_ORIGINALLAN));
				quickAction.addActionItem(originalItem);
			}
			else
			{
				ActionItem translateItem = new ActionItem(ID_TRANSLATE, LanguageManager.getLangByKey(LanguageKeys.MENU_TRANSLATE));
				quickAction.addActionItem(translateItem);
			}
		}
		
		if(canBuyMsgBg)
		{
			ActionItem buyItem = new ActionItem(ID_VIEW_BUY_MSG_BG,
					LanguageManager.getLangByKey(LanguageKeys.MENU_BUY_MSG_BG));
			quickAction.addActionItem(buyItem);
		}
		
		if(canTranslateDevelop)
		{
			ActionItem nextItem = new ActionItem(ID_TRANSLATE_NOT_UNDERSTAND, LanguageManager.getLangByKey(LanguageKeys.MENU_REPORT_CHAT_TRANSLATION));
			quickAction.addActionItem(nextItem);
		}
		
		if(canViewKing)
		{
			ActionItem viewKingItem = new ActionItem(ID_VIEW_KING_INFO,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_KING));
			quickAction.addActionItem(viewKingItem);
		}
		
		if(canViewCannon)
		{
			ActionItem viewCannonItem = new ActionItem(ID_VIEW_CANNON_INFO,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_CANNON));
			quickAction.addActionItem(viewCannonItem);
		}
		
		if(canViewRankingList)
		{
			ActionItem viewRankingItem = new ActionItem(ID_VIEW_RANKING_LIST,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_RANKING_LIST));
			quickAction.addActionItem(viewRankingItem);
		}
		
		if(canViewDragonShare)
		{
			ActionItem viewDragonShareItem = new ActionItem(ID_VIEW_DRAGON_SHARE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(viewDragonShareItem);
		}
		
		if(canViewNewRally)
		{
			ActionItem viewNewRallyItem = new ActionItem(ID_VIEW_NEW_RALLY,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(viewNewRallyItem);
		}
		
		if(canViewCommonShared)
		{
			ActionItem viewItem = new ActionItem(ID_VIEW_COMMON_SHARED,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(viewItem);
		}
		
		if(canViewGoldenBox)
		{
			ActionItem viewItem = new ActionItem(ID_VIEW_GOLDEN_BOX,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(viewItem);
		}
		
		if(canViewActivityConcern)
		{
			ActionItem viewActivityItem = new ActionItem(ID_VIEW_ACTIVITY,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW_ACTIVITY));
			quickAction.addActionItem(viewActivityItem);
		}
		
		
		if(canViewVipGiftBox)
		{
			ActionItem viewItem = new ActionItem(ID_VIEW_VIP_LOTTERY,
					LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(viewItem);
		}

		if (canViewAllianceTreasure)
		{
			ActionItem viewAllianceTreasure = new ActionItem(ID_VIEW_ALLIANCE_TREASURE,
					LanguageManager.getLangByKey(LanguageKeys.MENU_ALLIANCE_TREASURE));
			quickAction.addActionItem(viewAllianceTreasure);
		}

		if(!item.isAudioMessage()){
			ActionItem copyItem = new ActionItem(ID_COPY, LanguageManager.getLangByKey(LanguageKeys.MENU_COPY));
			quickAction.addActionItem(copyItem);
		}

		if (canSendMail)
		{
			ActionItem searchItem = new ActionItem(ID_SEND_MAIL, LanguageManager.getLangByKey(LanguageKeys.MENU_SENDMSG));
			quickAction.addActionItem(searchItem);
		}

		if (canSayHello)
		{
			String content = LanguageManager.getLangByKey(LanguageKeys.MENU_SAY_HELLO);
			if (StringUtils.isEmpty(content) || content.equals(LanguageKeys.MENU_SAY_HELLO))
				content = "Say Hello";
			ActionItem sayHelloItem = new ActionItem(ID_SAY_HELLO, content);
			quickAction.addActionItem(sayHelloItem);
		}

		if (canBlock)
		{
			ActionItem blockItem = new ActionItem(ID_BLOCK, LanguageManager.getLangByKey(LanguageKeys.MENU_SHIELD));
			quickAction.addActionItem(blockItem);
		}
		if (canUnBlock)
		{
			ActionItem unBlockItem = new ActionItem(ID_UNBLOCK, LanguageManager.getLangByKey(LanguageKeys.MENU_UNSHIELD));
			quickAction.addActionItem(unBlockItem);
		}

		if (canBan)
		{
			ActionItem banItem = new ActionItem(ID_BAN, LanguageManager.getLangByKey(LanguageKeys.MENU_BAN));
			quickAction.addActionItem(banItem);
		}
		if (canUnBan)
		{
			ActionItem unBanItem = new ActionItem(ID_UNBAN, LanguageManager.getLangByKey(LanguageKeys.MENU_UNBAN));
			quickAction.addActionItem(unBanItem);
		}

		if (canInvite)
		{
			ActionItem actionItem = new ActionItem(ID_INVITE, LanguageManager.getLangByKey(LanguageKeys.MENU_INVITE));
			quickAction.addActionItem(actionItem);
		}

		if (canJoinAlliance)
		{
			ActionItem nextItem = new ActionItem(ID_JOIN_ALLIANCE, LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));
			quickAction.addActionItem(nextItem);
		}
		if(canViewMonster)
		{
			ActionItem nextItem = new ActionItem(ID_VIEW_FIRST_KILL, LanguageManager.getLangByKey(LanguageKeys.MENU_VIEW));
			quickAction.addActionItem(nextItem);
		}
		if (canReportHeadImg)
		{
			ActionItem nextItem = new ActionItem(ID_REPORT_HEAD_IMG, LanguageManager.getLangByKey(LanguageKeys.MENU_REPORT_HEADIMG));
			quickAction.addActionItem(nextItem);
		}
		if (canReportContent)
		{
			ActionItem nextItem = new ActionItem(ID_REPORT_PLAYER_CHAT, LanguageManager.getLangByKey(LanguageKeys.MENU_REPORT_PLAYER_CHAT));
			quickAction.addActionItem(nextItem);
		}
		
		
		
		if(item.isAudioMessage())
		{
			if(canSwitchToReceiver)
			{
				ActionItem nextItem = new ActionItem(ID_SWITCH_TO_RECEIVER, LanguageManager.getLangByKey(LanguageKeys.MENU_SWITCH_TO_RECEIVER));
				quickAction.addActionItem(nextItem);
			}
			if(canSwitchToSpeaker)
			{
				ActionItem nextItem = new ActionItem(ID_SWITCH_TO_SPEAKER, LanguageManager.getLangByKey(LanguageKeys.MENU_SWITCH_TO_SPEAKER));
				quickAction.addActionItem(nextItem);
			}
		}

		return quickAction;
	}
}
