package com.elex.im;

import java.util.ArrayList;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.GSController;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.model.CustomChannelData;
import com.elex.im.ui.model.LatestChannelChatInfo;
import com.elex.im.ui.model.LatestChatInfo;

public class CokChannelDef
{
	public static String	switch_chat_k10	= "cn_uc,cn1,cn_mihy,cn_wdj,cn_ewan,cn_anzhi";
	public static String	switch_chat_k11	= "5|6";

	public static String getChatRoomFounderByKey(String groupId)
	{
		Channel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId);
		return channel.getRoomOwner();
	}

	public static boolean getIsMemberFlag(String groupId)
	{
		Channel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId);
		if (channel != null)
		{
			return channel.isMember;
		}
		return false;
	}

	public static boolean isInChatRoom()
	{
		return isInChatRoom(GSController.getCurrentChannelType());
	}

	public static boolean isInChatRoom(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	// 无引用
	public static boolean isInUserMail()
	{
		return isInUserMail(GSController.getCurrentChannelType());
	}
	
	public static boolean isInUserMail(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER;
	}
	
	public static boolean isInCustomChat()
	{
		return isInCustomChat(GSController.getCurrentChannelType());
	}
	
	public static boolean isInCustomChat(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
	}
	
	private static boolean isInAlliance()
	{
		return isInAlliance(GSController.getCurrentChannelType());
	}

	private static boolean isInAlliance(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}
	
	public static String getAllianceLabel(Msg msg)
	{
		return !isInAlliance() ? msg.getAllianceLabel() : "";
	}

	private static boolean isInCountry()
	{
		return isInCountry(GSController.getCurrentChannelType());
	}

	private static boolean isInCountry(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY;
	}
	
	public static boolean canHandleJoinAnnounceInvitation(int channelType)
	{
		return isInCountry(channelType);
	}

	public static boolean isInBattleField()
	{
		return isInBattleField(GSController.getCurrentChannelType());
	}

	public static boolean isInBattleField(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
	}

	public static boolean isInBasicChat()
	{
		return isInBasicChat(GSController.getCurrentChannelType());
	}
	
	public static boolean isInBasicChat(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}

	public static boolean isInChat()
	{
		return isInChat(GSController.getCurrentChannelType());
	}
	
	public static boolean isInChat(int channelType)
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
				|| channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
				|| channelType == DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
	}

	public static boolean isInMailDialog()
	{
		return (GSController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER || GSController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_CHATROOM);
	}
	
	public static boolean isInSystemMail()
	{
		return GSController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_OFFICIAL;
	}

	/**
	 * 是否对系统消息显示图标
	 * 一般来说只要msgType符合都可以显示，但现在多了一个联盟系统消息channel，里面的消息都是系统消息，不显示图标
	 */
	public static boolean canShowSysIcon()
	{
		return CokChannelDef.isInCountry() || CokChannelDef.isInAlliance();
	}
	
	public static boolean canSendHorn(int channelType)
	{
		if(getInstance().getChannelDef(channelType) == null)
			return false;
		return getInstance().getChannelDef(channelType).canSendHorn;
	}
	
	public static ArrayList<Integer> getHornChannelTypes()
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < allChannelTypes.length; i++)
		{
			if(canSendHorn(allChannelTypes[i]))
				result.add(allChannelTypes[i]);
		}
		return result;
	}
	
	private static int[]	allChannelTypes	= new int[] {
			DBDefinition.CHANNEL_TYPE_COUNTRY,
			DBDefinition.CHANNEL_TYPE_ALLIANCE,
			DBDefinition.CHANNEL_TYPE_USER,
			DBDefinition.CHANNEL_TYPE_CHATROOM,
			DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT };
	
	public static boolean canSendAudio(int channelType)
	{
		if(getInstance().getChannelDef(channelType) == null)
			return false;
		return getInstance().getChannelDef(channelType).canSendAudio;
	}

	public static boolean isChatRestrict()
	{
		boolean result = false;
		if (GSController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			User user = UserManager.getInstance().getCurrentUser();
			String uid = user.uid;
			if (StringUtils.isNotEmpty(uid) && uid.length() >= 3)
			{
				String uidPostfix = uid.substring(uid.length() - 3, uid.length());
				if (StringUtils.isNumeric(uidPostfix))
				{
					int serverId = Integer.parseInt(uidPostfix);
					uidPostfix = "" + serverId;
					if (user != null && StringUtils.isNotEmpty(user.userName))
					{
						if (user.userName.startsWith("Empire") && user.userName.endsWith(uidPostfix))
							return true;
						else
							return false;
					}
				}

			}
		}
		return result;
	}

	private static boolean isChatRestrictForLevel()
	{
		return isChatRestrictForLevel(GSController.getCurrentChannelType());
	}

	public static boolean isChatRestrictForLevel(int channelType)
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			return GSController.currentLevel < getChatRestrictLevel();
		return false;
	}

	public static int getChatRestrictLevel()
	{
		int level = 5;
		String channelName = MailManager.getInstance().getPublishChannelName();
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,
		// "channelName", channelName);
		if (StringUtils.isEmpty(channelName) || StringUtils.isEmpty(switch_chat_k11))
			return level;
		String[] switchArr = switch_chat_k11.split("\\|");
		if (switchArr.length != 2)
			return level;
		if (switch_chat_k10.contains(channelName))
		{
			if (StringUtils.isNumeric(switchArr[1]))
				level = Integer.parseInt(switchArr[1]);
		}
		else
		{
			if (StringUtils.isNumeric(switchArr[0]))
				level = Integer.parseInt(switchArr[0]);
		}
		// LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,
		// "level", level);
		return level;
	}

	public static void sendChatLatestMessage(Channel channel)
	{
		if (channel == null
				|| (channel.getChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && !CokConfig.needCrossServerBattleChat()))
			return;
		Msg latestMsgItem = channel.getLatestMsgInDB();
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "latestMsgItem", latestMsgItem);
		if (latestMsgItem != null)
		{
			postLatestChatMessage(latestMsgItem);
		}
	}

	public static void postLatestChatMessage(Msg msgItem)
	{
		if (msgItem == null)
			return;
		LatestChannelChatInfo chatInfo = new LatestChannelChatInfo();
		LatestChatInfo latestChatInfo = new LatestChatInfo();
		latestChatInfo.setMsgInfo(msgItem);
		if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			chatInfo.setLatestCountryChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			chatInfo.setLatestAllianceChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			chatInfo.setLatestBattleChatInfo(latestChatInfo);
		else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_USER || msgItem.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
			chatInfo.setLatestCustomChatInfo(latestChatInfo);

		try
		{
			String lateChatMessage = JSON.toJSONString(chatInfo);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "lateChatMessage", lateChatMessage);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] { lateChatMessage });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void postLatestCustomChatMessage(Msg msgItem)
	{
		LatestChannelChatInfo chatInfo = new LatestChannelChatInfo();
		LatestChatInfo latestChatInfo = new LatestChatInfo();
		if (msgItem != null)
			latestChatInfo.setMsgInfo(msgItem);
		chatInfo.setLatestCustomChatInfo(latestChatInfo);

		try
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "chatInfo", chatInfo);
			String lateChatMessage = JSON.toJSONString(chatInfo);
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "lateChatMessage", lateChatMessage);
			JniController.getInstance().excuteJNIVoidMethod("postChatLatestInfo", new Object[] { lateChatMessage });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void trackActivityPageView(int channelType, boolean rememberPosition)
	{
		ChannelDef def = getInstance().getChannelDef(channelType);
		if(def != null)
		{
			LogUtil.trackPageView(!rememberPosition ? def.pageName : def.pageNameReturn);
		}
	}
	
	public static void needShowNoContentUI(int channelType)
	{
		
	}

	public static void getNoContentUITextAndVisibility(int channelType)
	{
	}
	
	public static void onNoContentButtonClick(int channelType)
	{
		if(getInstance().getChannelDef(channelType) != null)
			getInstance().getChannelDef(channelType).noContentButtonHandler();
	}
	
	public static String getCustomChannelName(Channel channel)
	{
		if(isInUserMail(channel.getChannelType()))
		{
			String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
			if(StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
			{
				UserManager.checkUser(fromUid, "", 0);
				User userInfo = UserManager.getInstance().getUser(fromUid);
				String nameText = fromUid;
				if (userInfo != null)
					nameText = userInfo.userName;
				else
					nameText = channel.getCustomName();
				if(StringUtils.isNotEmpty(nameText) && channel.getChannelID().endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
					nameText+="(MOD)";
				return nameText;
			}
			else
			{
				return StringUtils.isNotEmpty(channel.getCustomName()) ? channel.getCustomName() : channel.getChannelID();
			}
			
		}
		else if(isInChatRoom(channel.getChannelType()))
		{
			return StringUtils.isNotEmpty(channel.getCustomName()) ? channel.getCustomName() : channel.getChannelID();
		}
		return "";
	}
	
	public static String getChannelNamePostfix(String channelId)
	{
		return channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD) ? "(MOD)" : "";
	}

	private ArrayList<ChannelDef> channelDefs;
	private static CokChannelDef	instance;

	public static CokChannelDef getInstance()
	{
		if (instance == null)
		{
			instance = new CokChannelDef();
		}
		return instance;
	}

	private CokChannelDef()
	{
		channelDefs = new ArrayList<ChannelDef>();
		channelDefs.add(new CountryChannelDef());
		channelDefs.add(new AllianceChannelDef());
		channelDefs.add(new UserMailChannelDef());
		channelDefs.add(new ChatroomChannelDef());
		channelDefs.add(new BattleFieldChannelDef());
		channelDefs.add(new CustomChannelDef());
	}
	
	public ChannelDef getChannelDef(int channelType)
	{
		for (ChannelDef channelDef : channelDefs)
		{
			if(channelDef.channelType == channelType)
				return channelDef;
		}
		return null;
	}
	
	public abstract class ChannelDef
	{
		public int channelType;
		public boolean canSendHorn = false;
		public boolean canSendAudio = false;
		public String pageName;
		public String pageNameReturn;
		public boolean noContentTipVisible = true;
		protected String noContentLabel;
		public String noContentButtonLabel;
		public boolean hasUserHeadPic = false;
		public boolean useMultiUserHeadPic = false;
		public boolean canShowNoContentUI(){
			return false;
		}
		public boolean canShowNoContentButton(){
			return true;
		}
		public String getNoContentLabel(){
			return noContentLabel;
		}
		public void noContentButtonHandler(){
			
		}
		public boolean canShowJoinAlliancePopup()
		{
			return false;
		}
		public String getPageViewTrackName()
		{
			return "click_chat_tab" + channelType;
		}
	}
	
	public class CountryChannelDef extends ChannelDef
	{
		public CountryChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_COUNTRY;
			canSendHorn = true;
			pageName = "ShowCountry";
			pageNameReturn = "ShowCountryReturn";
			noContentTipVisible = false;
		}

		public boolean canShowNoContentUI(){
			return !CokConfig.isBattleChatEnable && CokConfig.isInDragonSencen();
		}
		
		public boolean canShowNoContentButton(){
			return false;
		}
		
		public String getNoContentLabel(){
			return LanguageManager.getLangByKey(LanguageKeys.TIP_DRAGON_CHAT);
		}
	}
	
	public class BattleFieldChannelDef extends ChannelDef
	{
		public BattleFieldChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_COUNTRY;
			canSendHorn = true;
			pageName = "ShowBattleField";
			pageNameReturn = "ShowBattleFieldReturn";
			noContentLabel = LanguageManager.getLangByKey(LanguageKeys.BTN_BATTLE_FIELD_ADD_TIP);
			noContentButtonLabel = LanguageManager.getLangByKey(LanguageKeys.BTN_GO_TO_SEE);
		}

		public boolean canShowNoContentUI(){
			return CokConfig.needShowBattleTipLayout();
		}
		
		public void noContentButtonHandler()
		{
			LogUtil.trackAction("click_to_battle_from_chat");
			GSController.doHostAction("showBattleActivity", "", "", "", false);
		}
	}
	
	public class AllianceChannelDef extends ChannelDef
	{
		public AllianceChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_ALLIANCE;
			canSendAudio = true;
			pageName = "ShowAlliance";
			pageNameReturn = "ShowAllianceReturn";
			noContentLabel = LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE);
			noContentButtonLabel = LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN);
		}
		
		public boolean canShowNoContentUI(){
			return !UserManager.getInstance().isCurrentUserInAlliance();
		}
		
		public void noContentButtonHandler()
		{
			GSController.doHostAction("joinAllianceBtnClick", "", "", "", true);
		}
		
		public boolean canShowJoinAlliancePopup()
		{
			return !UserManager.getInstance().isCurrentUserInAlliance() && ConfigManager.getInstance().isFirstJoinAlliance;
		}
	}
	
	public class UserMailChannelDef extends ChannelDef
	{
		public UserMailChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_USER;
			canSendAudio = true;
			pageName = "ShowMail";
			pageNameReturn = "ShowMailReturn";
			noContentTipVisible = false;
			hasUserHeadPic = true;
		}
		
	}
	
	public class ChatroomChannelDef extends ChannelDef
	{
		public ChatroomChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_CHATROOM;
			canSendAudio = true;
			pageName = "ShowChatroom";
			pageNameReturn = "ShowChatroomReturn";
			noContentTipVisible = false;
			hasUserHeadPic = true;
			useMultiUserHeadPic = true;
		}
	}
	
	public class CustomChannelDef extends ChannelDef
	{
		public CustomChannelDef()
		{
			channelType = DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
			pageName = "ShowCustomChannel";
			pageNameReturn = "ShowCustomChannelReturn";
			noContentLabel = LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP);
			noContentButtonLabel = LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD);
		}

		public boolean canShowNoContentUI()
		{
			return !CustomChannelData.getInstance().hasCustomChannel() || !CustomChannelData.getInstance().hasCustomChannelData();
		}
		
		public boolean canShowNoContentButton(){
			return CustomChannelData.getInstance().hasCustomChannelData();
		}
		
		public String getNoContentLabel(){
			if(CustomChannelData.getInstance().hasCustomChannelData())
			{
				return LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP);
			}
			else
			{
				return LanguageManager.getLangByKey(LanguageKeys.CUSTOM_CHAT_ADD_TIP);
			}
		}
		
		public void noContentButtonHandler()
		{
			UIManager.getChatFragment().showCustomChannelSetting();
			LogUtil.trackAction("click_add_custom_btn");
		}
		
		public String getPageViewTrackName()
		{
			if(StringUtils.isNotEmpty(CustomChannelData.getInstance().customChannelId))
			{
				return "click_custom_channel_exist_true";
			}
			else
			{
				return "click_custom_channel_exist_false";
			}
		}
	}
}
