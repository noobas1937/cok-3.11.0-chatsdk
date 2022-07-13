package com.elex.chatservice.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ResUtil;
import com.elex.chatservice.util.RoomUtils;
import com.elex.chatservice.view.emoj.EmojIcon;

public final class MsgItem
{
	public final static int	SENDING							= 0;
	public final static int	SEND_FAILED						= 1;
	public final static int	SEND_SUCCESS					= 2;

	// 界面类型
	public final static int	MSGITEM_TYPE_MESSAGE			= 0;
	public final static int	MSGITEM_TYPE_GIF				= 1;
	public final static int	MSGITEM_TYPE_PIC				= 2;
	public final static int	MSGITEM_TYPE_REDPACKAGE			= 3;
	public final static int	MSGITEM_TYPE_CHATROM_TIP		= 4;
	public final static int	MSGITEM_TYPE_NEW_MESSAGE_TIP	= 5;
	public final static int	MSGITEM_TYPE_AUDIO				= 6;
	public final static int	MSGITEM_TYPE_SHARE				= 7;

	// 消息类型
	public final static int	MSG_TYPE_SELF					= 1;
	public final static int	MSG_TYPE_ALLIANCE				= 2;
	public final static int	MSG_TYPE_ALLIANCE_INVITE		= 3;
	public final static int	MSG_TYPE_BATTLE_REPORT			= 4;
	public final static int	MSG_TYPE_DETECT_REPORT			= 5;
	public final static int	MSG_TYPE_HORN					= 6;
	public final static int	MSG_TYPE_EQUIP_SHARE			= 7;
	public final static int	MSG_TYPE_ALLIANCE_JOIN			= 8;
	public final static int	MSG_TYPE_ALLIANCE_RALLY			= 9;
	public final static int	MSG_TYPE_LOTTERY_SHARE			= 10;
	public final static int	MSG_TYPE_ALLIANCETASK_SHARE		= 11;
	public final static int	MSG_TYPE_RED_PACKAGE			= 12;
	public final static int	MSG_TYPE_COR_SHARE				= 13;
	public final static int	MSG_TYPE_ALLIANCE_TREASURE		= 14;
	public final static int	MSG_TYPE_AUDIO					= 15;
	public final static int	MSG_TYPE_ALLIANCEHELP			= 16;
	public final static int	MSG_TYPE_ALLIANCE_OFFICER		= 17;
	public final static int	MSG_TYPE_ALLIANCE_SKILL			= 18;
	public final static int	MSG_TYPE_STEAL_FAILED			= 19;
	public final static int	MSG_TYPE_SHOT					= 20;
	public final static int	MSG_TYPE_MONSTER_FIRST_KILL		= 21;
	public final static int MSG_TYPE_NEW_BATTLE_REPORT		= 22;
	public final static int MSG_TYPE_VIP_LOTTERY_SHARE		= 23;
	public final static int MSG_TYPE_MID_SYSTEM_TIP			= 24;
	public final static int MSG_TYPE_KING					= 25;
	public final static int MSG_TYPE_CANNON					= 26;
	public final static int MSG_TYPE_NPC_TIP				= 27;
	public final static int MSG_TYPE_RANKING_LIST			= 28;
	public final static int MSG_TYPE_ACTIVITY				= 29;
	public final static int MSG_TYPE_DRAGON_SHARE			= 30;
	public final static int MSG_TYPE_NEW_RALLY				= 31;
	public final static int MSG_TYPE_COMMON_SHARED			= 32;
	public final static int MSG_TYPE_GOLDEN_BOX				= 33;
	/** 增加post时要变更这个值 */
	public final static int	MSG_TYPE_MAX_VALUE				= MSG_TYPE_GOLDEN_BOX;

	public final static int	MSG_TYPE_CHATROOM_TIP			= 100;
	public final static int	MSG_TYPE_MOD					= 200;
	public final static int	MSG_TYPE_MOD_AUDIO				= 201;
	public final static int	MSG_TYPE_DRIFTING_BOTTLE		= 300;
	public final static int	MSG_TYPE_DRIFTING_BOTTLE_AUDIO	= 301;
	public final static int	MSG_TYPE_NEARBY_LIKE			= 302;
	public final static int	MSG_TYPE_EMOJ					= 303;

	public final static int	HANDLED							= 0;
	public final static int	UNHANDLE						= 1;
	public final static int	NONE_MONEY						= 2;
	public final static int	FINISH							= 3;
	public final static int	HANDLEDISABLE					= 4;

	public final static int	VOICE_UNREAD					= 0;
	public final static int	VOICE_READ						= 1;
	
	public static final String CHAT_CUSTOM_BACKGROUND_LEFT_NORMAL	= "chat_background_left_normal_";
	public static final String CHAT_CUSTOM_BACKGROUND_RIGHT_NORMAL  = "chat_background_right_normal_";
	public static final String CHAT_CUSTOM_BACKGROUND_LEFT_PRESSED	= "chat_background_left_pressed_";
	public static final String CHAT_CUSTOM_BACKGROUND_RIGHT_PRESSED  = "chat_background_right_pressed_";

	/** 数据库使用的id */
	public int				_id;
	public int				tableVer;
	public int				sequenceId;
	/** 用来标识邮件的id */
	public String			mailId;
	/** uid，群聊时才会存数据库 */
	public String			uid								= "";
	/** 频道类型 */
	public int				channelType						= -1;
	/** 收到的消息会在C++中初始化此字段，对应后台传回来的createTime */
	public int				createTime						= 0;
	/** 数据库中名为type：是否为系统信息，“0”表示不是，非“0”表示是 */
	public int				post							= -1;
	/** 消息体 */
	public String			msg								= "";
	/** 翻译信息 */
	public String			translateMsg					= "";
	/** 源语言 */
	public String			originalLang					= "";
	/** 翻译后的语言 */
	public String			translatedLang					= "";
	/**
	 * 对于自己发的消息,发送状态，0正在发送，1发送失败，2发送成功 红包消息时，表示红包的领取状态,1未领取，0领取过,2被抢光了,3到期了
	 * 对于别人发的国家系统的消息，0未读 1已读
	 * */
	public int				sendState						= -1;
	public int				readStateBefore					= -1;
	/** 战报UID，侦察战报UID,装备ID等 */
	public String			attachmentId					= "";

	public String			media							= "";
	public List<InputAtContent> inputAtList					= null;
	public String 			atUids							= "";
	
	/*
	 * 消息已读未读状态，0表示未读，1表示已读
	 */
	public int				readStatus					= 0;
	public boolean			isAtMeMsg						= false;

	// 运行时属性
	/** 是否是自己的信息 */
	public boolean			isSelfMsg;
	/** 是否是新消息 */
	public boolean			isNewMsg;
	public String			currentText						= "";
	/** 是否被翻译过 */
	public boolean			hasTranslated;
	public boolean			isSendDataShowed				= false;
	public int				lastUpdateTime					= 0;
	/** 本地发送时间戳 */
	public int				sendLocalTime					= 0;
	public boolean			isTranslateByGoogle				= false;
	public boolean			isFirstNewMsg					= false;
	/**
	 * 0:不是第一条 1:第一条且新消息数小于等于200条 2:第一条且新消息数超过200条
	 * */
	public int				firstNewMsgState				= 0;
	/** msgItem所属的Channel */
	public ChatChannel		chatChannel						= null;
	/** 是否强制翻译，点击翻译菜单后置为true，点击原文置为false */
	public boolean			isTranslatedByForce				= false;
	/** 是否做过强制翻译，点击翻译菜单后置为true */
	public boolean			hasTranslatedByForce			= false;
	// 是否被强制显示原文
	public boolean			isOriginalLangByForce			= false;
	public boolean			isAudioDownloading				= false;
	public boolean			isTranslating					= false;
	private int				canNotShowTranslateMenuState 		= -1;
	private int					canNotShowTranslateQuickActionState		= -1;
	private List<String>		replaceEmojArray						= null;
	private String				replaceTranslateMsg						= "";
	public boolean				isFirstBottleMsg						= false;

	private boolean 			attachmentParsed						= false;
	private String				attachmentParseLang						= "";
	public String				icon									= "";
	public String				title									= "";
	public String				description								= "";
	public List<ColorFragment>	descriptionColorFragmentList			= null;		
	public int					mediaType								= -1;
	public int					mediaTime								= 0;
	public String				sourceName								= "";
	public String				sourceLogo								= "";
	public String				actionArgs								= "";
	
	public String				sendErrorCode							= "";
	public long					sendErrorBanTime						= 0;
	private String				expressionName							= "";
	
	// 被C++使用
	/** 发送者名称 */
	public String			name;
	/** 联盟简称 */
	public String			asn;
	/** vip信息 */
	public String			vip;
	/** 系统头像 */
	public String			headPic;
	public int				gmod;
	/** 自定义头像 */
	public int				headPicVer;

	/**
	 * C++创建的对象可能没有默认值赋值，需要补上
	 */
	public void initNullField()
	{
		if (currentText == null)
		{
			currentText = "";
		}
	}

	public MsgItem()
	{

	}

	public UserInfo getUser()
	{
		UserManager.checkUser(uid, "", 0);
		UserInfo user = UserManager.getInstance().getUser(uid);
		return user;
	}

	public String getName()
	{
		return getUser().userName;
	}
	
	public void parseInputAtList()
	{
		if(StringUtils.isNotEmpty(atUids))
		{
			try
			{
				inputAtList = JSON.parseArray(atUids, InputAtContent.class);
				if(inputAtList!=null)
				{
					for(InputAtContent at : inputAtList)
					{
						if(at!=null && StringUtils.isNotEmpty(at.getUid()) && at.getUid().equals(UserManager.getInstance().getCurrentUserId()))
						{
							isAtMeMsg = true;
							return;
						}
					}
				}
				
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			inputAtList = null;
		}
		isAtMeMsg = false;
	}
	
	public int getCustomChatBgId()
	{
		return getUser().getCustomChatBg();
	}
	
	public int getCustomChatBgTextColor()
	{
		return getUser().getCustomChatBgTextColor();
	}

	public String getCustomChatBgNormalName()
	{
		int customChatBgId = getCustomChatBgId();
		if(customChatBgId<=0)
			return "";
		String customChatBgPrefix = "";
		if(!ConfigManager.getInstance().needRTL())
		{
			if(isSelfMsg())
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_RIGHT_NORMAL;
			else
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_LEFT_NORMAL;
		}
		else
		{
			if(isSelfMsg())
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_LEFT_NORMAL;
			else
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_RIGHT_NORMAL;
		}
		return customChatBgPrefix+customChatBgId;
	}
	
	public String getCustomChatBgPressedName()
	{
		int customChatBgId = getCustomChatBgId();
		if(customChatBgId<=0)
			return "";
		String customChatBgPrefix = "";
		if(!ConfigManager.getInstance().needRTL())
		{
			if(isSelfMsg())
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_RIGHT_PRESSED;
			else
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_LEFT_PRESSED;
		}
		else
		{
			if(isSelfMsg())
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_LEFT_PRESSED;
			else
				customChatBgPrefix = MsgItem.CHAT_CUSTOM_BACKGROUND_RIGHT_PRESSED;
		}
		return customChatBgPrefix+customChatBgId;
	}
	
	public String getLang()
	{
		String lang = originalLang;
		if (StringUtils.isEmpty(lang) && StringUtils.isNotEmpty(getUser().lang))
			lang = getUser().lang;
		return lang;
	}

	public int getSrcServerId()
	{
		return getUser().crossFightSrcServerId;
	}
	
	public int getServerId()
	{
		return getUser().serverId;
	}

	public String getASN()
	{
		return getUser().asn;
	}
	
	public String getAllianceId()
	{
		return getUser().allianceId;
	}

	public String getVip()
	{
		return getUser().getVipInfo();
	}

	public int getVipLevel()
	{
		return getUser().getVipLevel();
	}

	public int getSVipLevel()
	{
		return getUser().getSVipLevel();
	}

	public String getHeadPic()
	{
		return getUser().headPic;
	}

	public int getGmod()
	{
		return getUser().mGmod;
	}
	
	public String getOfficerName()
	{
		return getUser().getOfficerName();
	}
	
	public String getBannerIcon()
	{
		String iconName = "";
		UserInfo user = getUser();
		if(user!=null && user.showBanner == 1 && StringUtils.isNotEmpty(user.flagCountry))
			iconName = "fl_"+user.flagCountry.toLowerCase();
		return iconName;
	}
	
	public void initOfficer()
	{
		getUser().setOfficerProperty();
	}
	
	public boolean isPositiveOfficer()
	{
		return getUser().isPositiveOfficer;
	}

	public int getHeadPicVer()
	{
		return getUser().headPicVer;
	}

	public void initUserForReceivedMsg(String mailOpponentUid, String mailOpponentName)
	{
		if (lastUpdateTime > TimeManager.getInstance().getCurrentTime())
		{
			LogUtil.printVariables(Log.WARN, LogUtil.TAG_MSG, "invalid lastUpdateTime msg:\n" + LogUtil.typeToString(this));
		}
		String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(mailOpponentUid);
		// TODO 删除第一个分支，不管什么情况，this.uid总是对应this.lastUpdateTime
		// mailOpponentName应该是为了用户信息不存在时设置邮件标题用的，可能不必要
		if (channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(fromUid) && !fromUid.equals(uid) && !isSelfMsg())
		{
			UserManager.checkUser(fromUid, mailOpponentName, lastUpdateTime);
		}
		else
		{
			UserManager.checkUser(uid, "", lastUpdateTime);
		}
	}

	public void initUserForSendedMsg()
	{
		UserManager.getInstance().getCurrentUser();
	}

	/**
	 * 用于从数据库获取消息
	 */
	public MsgItem(Cursor c)
	{
		try
		{
			_id = c.getInt(c.getColumnIndex(BaseColumns._ID));
			tableVer = c.getInt(c.getColumnIndex(DBDefinition.COLUMN_TABLE_VER));
			sequenceId = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_SEQUENCE_ID));
			uid = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_USER_ID));
			mailId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MAIL_ID));
			createTime = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CREATE_TIME));
			sendLocalTime = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME));
			post = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TYPE));
			channelType = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_CHANNEL_TYPE));
			msg = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MSG));
			translateMsg = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TRANSLATION));
			originalLang = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_ORIGINAL_LANGUAGE));
			translatedLang = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_TRANSLATED_LANGUAGE));
			sendState = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_STATUS));
			if (sendState < 0)
			{
				if (isRedPackageMessage())
					sendState = UNHANDLE;
				else if (isAudioMessage())
					sendState = VOICE_UNREAD;
			}

			attachmentId = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_ATTACHMENT_ID));
			media = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_MEDIA));
			isSelfMsg = uid.equals(UserManager.getInstance().getCurrentUserId());
			atUids = c.getString(c.getColumnIndex(DBDefinition.CHAT_COLUMN_AT_UIDS));
			readStatus = c.getInt(c.getColumnIndex(DBDefinition.CHAT_COLUMN_AT_READ_STATUS));
			parseInputAtList();
			if(isSelfMsg)
				readStatus = 1;
			UserManager.getInstance().getUser(uid);
			
			isNewMsg = false;
			if (hasTranslated())
				this.hasTranslated = true;
			else
				this.hasTranslated = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.CHAT_COLUMN_SEQUENCE_ID, sequenceId);
		cv.put(DBDefinition.CHAT_COLUMN_USER_ID, uid);
		cv.put(DBDefinition.CHAT_COLUMN_MAIL_ID, mailId);
		cv.put(DBDefinition.CHAT_COLUMN_CREATE_TIME, createTime);
		cv.put(DBDefinition.CHAT_COLUMN_LOCAL_SEND_TIME, sendLocalTime);
		cv.put(DBDefinition.CHAT_COLUMN_TYPE, post);
		cv.put(DBDefinition.CHAT_COLUMN_CHANNEL_TYPE, channelType);
		cv.put(DBDefinition.CHAT_COLUMN_MSG, msg);
		cv.put(DBDefinition.CHAT_COLUMN_TRANSLATION, translateMsg);
		cv.put(DBDefinition.CHAT_COLUMN_ORIGINAL_LANGUAGE, originalLang);
		cv.put(DBDefinition.CHAT_COLUMN_TRANSLATED_LANGUAGE, translatedLang);
		if (isRedPackageMessage() && sendState < 0)
			sendState = UNHANDLE;
		cv.put(DBDefinition.CHAT_COLUMN_STATUS, sendState);
		cv.put(DBDefinition.CHAT_COLUMN_ATTACHMENT_ID, attachmentId);
		cv.put(DBDefinition.CHAT_COLUMN_MEDIA, media);
		cv.put(DBDefinition.CHAT_COLUMN_AT_UIDS, atUids);
		cv.put(DBDefinition.CHAT_COLUMN_AT_READ_STATUS, readStatus);
		return cv;
	}

	/**
	 * 用于发送消息
	 */
	public MsgItem(String uidStr, boolean isNewMsg, boolean isSelf, int channelType, int post, String msgStr, int sendLocalTime)
	{
		this.uid = uidStr;
		this.isNewMsg = isNewMsg;
		this.isSelfMsg = isSelf && (post != 100);
		this.channelType = channelType;
		this.post = post;
		this.msg = msgStr;
		if (hasTranslated())
			this.hasTranslated = true;
		else
			this.hasTranslated = false;
		this.sendLocalTime = sendLocalTime;
	}

	/**
	 * 用于wrapper假消息
	 */
	public MsgItem(int seqId, boolean isNewMsg, boolean isSelf, int channelType, int post, String uidStr, String msgStr,
			String translateMsgStr, String originalLangStr, int sendLocalTime)
	{
		this.sequenceId = seqId;
		this.isNewMsg = isNewMsg;
		this.isSelfMsg = isSelf && (post != 100);
		this.channelType = channelType;
		this.msg = msgStr;
		this.uid = uidStr;
		this.post = post;
		this.translateMsg = translateMsgStr;
		this.originalLang = originalLangStr;
		this.sendLocalTime = sendLocalTime;

		setExternalInfo();
	}

	public static MsgItem createMsgItem(RoomPushData roomGroupPushMsg, boolean isTipsMsg, String msg){
        MsgItem item = new MsgItem();
		item.channelType = WebSocketManager.group2channelType(roomGroupPushMsg.group);
        // 除了从db获取，都为true
        item.isNewMsg = true;
        item.sequenceId = -1;
        item.uid = roomGroupPushMsg.sender;
		if(msg != null){
			item.msg = msg;
		}else {
			item.msg = roomGroupPushMsg.msg;
		}
        item.isSelfMsg = item.isSelfMsg();
        item.sendLocalTime = RoomUtils.getSecond(roomGroupPushMsg.sendTime);
        item.createTime = RoomUtils.getSecond(roomGroupPushMsg.serverTime);
        item.originalLang = roomGroupPushMsg.originalLang;
//        item.translateMsg = roomGroupPushMsg.trans;
		if(isTipsMsg){
			item.post = MSG_TYPE_CHATROOM_TIP;
		}else {
			if(roomGroupPushMsg.extra!=null){
				item.post = roomGroupPushMsg.extra.post;
				item.media = roomGroupPushMsg.extra.media;
			}
		}
        item.mailId = "";
		if(!TextUtils.isEmpty(roomGroupPushMsg.seqId)){
			try {
				item.sequenceId = Integer.parseInt(roomGroupPushMsg.seqId);
			}catch (NumberFormatException e){
				e.printStackTrace();
			}
		}
        item.lastUpdateTime = RoomUtils.getSecond(roomGroupPushMsg.senderInfo.lastUpdateTime);
        if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
            item.sendState = MsgItem.SEND_SUCCESS;
        return item;
	}

	public static MsgItem createMsgItem(RoomResponseCommand roomGroupResponse, String msg,boolean isTipsMsg) {
		MsgItem item = new MsgItem();
		// 这里写死 自定义聊天channelType
		item.channelType = DBDefinition.CHANNEL_TYPE_CHATROOM;
		// 除了从db获取，都为true
		item.isNewMsg = true;
		item.sequenceId = -1;
		item.uid = roomGroupResponse.uid;
		item.msg = msg;
		item.isSelfMsg = item.isSelfMsg();
		item.sendLocalTime = RoomUtils.getSecond(roomGroupResponse.sendTime);
		item.createTime = RoomUtils.getSecond(roomGroupResponse.serverTime);
//        item.channelType = WebSocketManager.group2channelType(roomGroupPushMsg.group);
//		item.originalLang = roomGroupResponse.originalLang;
//        item.translateMsg = roomGroupPushMsg.trans;
		if(isTipsMsg){
			item.post = MSG_TYPE_CHATROOM_TIP;
		}else {
			item.post = 0;
		}
		item.mailId = "";
//		item.sequenceId = roomGroupResponse.seqId;
//		item.lastUpdateTime = RoomUtils.getSecond(roomGroupResponse.senderInfo.lastUpdateTime);
		if (!item.isRedPackageMessage() && !(!item.isSelfMsg() && item.isAudioMessage()))
			item.sendState = MsgItem.SEND_SUCCESS;
		return item;
	}
	
	public void setExternalInfo()
	{
		if (hasTranslated())
		{
			this.hasTranslated = true;
		}
		else
		{
			this.hasTranslated = false;
		}

		if (isSystemHornMsg())
		{
			this.headPic = "guide_player_icon";
		}
	}

	public boolean isEqualTo(MsgItem msgItem)
	{
		if (this.isSelfMsg == msgItem.isSelfMsg && this.msg.equals(msgItem.msg))
			return true;
		return false;
	}

	public boolean isSelfMsg()
	{
		isSelfMsg = StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())
				&& uid.equals(UserManager.getInstance().getCurrentUserId()) && post != MSG_TYPE_CHATROOM_TIP;
		return isSelfMsg;
	}

	public boolean isInAlliance()
	{
		return StringUtils.isNotEmpty(getAllianceId());
	}

	public boolean isSystemHornMsg()
	{
		return ((isHornMessage() || isStealFailedMessage()) && (uid.equals("3000001") || uid.equals("3000002")));
	}

	public boolean isTranlateDisable()
	{
		if (StringUtils.isNotEmpty(originalLang) && StringUtils.isNotEmpty(TranslateManager.getInstance().disableLang))
		{
			boolean isContainsOriginLang = false;
			if (originalLang.contains(","))
			{
				String langStr[] = originalLang.split(",");
				for (int i = 0; i < langStr.length; i++)
				{
					if (!langStr[i].equals("") && isContainsLang(TranslateManager.getInstance().disableLang, langStr[i]))
					{
						isContainsOriginLang = true;
						break;
					}
				}
			}
			else
			{
				isContainsOriginLang = isContainsLang(TranslateManager.getInstance().disableLang, originalLang);
			}

			if (isContainsOriginLang)
				return true;
		}
		return false;
	}

	private boolean isContainsLang(String disableLang, String lang)
	{
		boolean ret = false;
		if (StringUtils.isNotEmpty(disableLang) && StringUtils.isNotEmpty(originalLang))
		{
			if (disableLang.contains(lang))
				ret = true;
			else
			{
				if (((disableLang.contains("zh-CN") || disableLang.contains("zh_CN") || disableLang.contains("zh-Hans")) && TranslateManager
						.getInstance().isZh_CN(lang))
						|| ((disableLang.contains("zh-TW") || disableLang.contains("zh_TW") || disableLang.contains("zh-Hant")) && TranslateManager
								.getInstance().isZh_TW(lang)))
					ret = true;
			}
		}
		return ret;
	}

	public boolean isCustomHeadImage()
	{
		try
		{
			return getUser().getHeadPicVer() > 0 && getUser().getHeadPicVer() < 1000000 && !getUser().getCustomHeadPic().equals("");
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return false;
		}
	}

	/**
	 * 是否是聊天室的提示消息,显示在中间
	 */
	public boolean isTipMsg()
	{
		return post == MSG_TYPE_CHATROOM_TIP || isMidSystemTip();
	}

	public boolean isModMsg()
	{
		return post == MSG_TYPE_MOD;
	}
	
	public boolean isDriftingBottleMsg()
	{
		return post == MSG_TYPE_DRIFTING_BOTTLE || post == MSG_TYPE_DRIFTING_BOTTLE_AUDIO;
	}

	public String getAllianceLabel()
	{
		if (isInAlliance() && StringUtils.isNotEmpty(getASN()))
		{
			return "(" + getASN() + ") ";
		}
		else
		{
			return "";
		}
	}

	public String getVipLabel()
	{
		return getVip() + " ";
	}

	/**
	 * post == 4
	 * @return
	 */
	public boolean isBattleReport()
	{
		return post == MSG_TYPE_BATTLE_REPORT;
	}

	/**
	 * post == 5
	 * @return
	 */
	public boolean isDetectReport()
	{
		return post == MSG_TYPE_DETECT_REPORT;
	}
	
	/**
	 * post == 2
	 * @return
	 */
	public boolean isAllianceMessage()
	{
		return post == MSG_TYPE_ALLIANCE;
	}
	
	/**
	 * post == 1
	 * @return
	 */
	public boolean isCountrySystemMessage()
	{
		return post == MSG_TYPE_SELF;
	}

	/**
	 * post == 3
	 * @return
	 */
	public boolean isAllianceInviteMessage()
	{
		return post == MSG_TYPE_ALLIANCE_INVITE;
	}

	/**
	 * post == 6
	 * @return
	 */
	public boolean isHornMessage()
	{
		return post == MSG_TYPE_HORN;
	}

	/**
	 * post == 7
	 * @return
	 */
	public boolean isEquipMessage()
	{
		return post == MSG_TYPE_EQUIP_SHARE;
	}

	/**
	 * post == 8
	 * @return
	 */
	public boolean isAllianceJoinMessage()
	{
		return post == MSG_TYPE_ALLIANCE_JOIN;
	}

	/**
	 * post == 9
	 * @return
	 */
	public boolean isRallyMessage()
	{
		return post == MSG_TYPE_ALLIANCE_RALLY;
	}

	/**
	 * post == 10
	 * @return
	 */
	public boolean isLotteryMessage()
	{
		return post == MSG_TYPE_LOTTERY_SHARE;
	}

	/**
	 * post == 13
	 * @return
	 */
	public boolean isCordinateShareMessage()
	{
		return post == MSG_TYPE_COR_SHARE;
	}

	/**
	 * post == 14
	 * @return
	 */
	public boolean isAllianceTreasureMessage()
	{
		return post == MSG_TYPE_ALLIANCE_TREASURE;
	}

	/**
	 * post == 16
	 * @return
	 */
	public boolean isAllianceHelpMessage()
	{
		return post == MSG_TYPE_ALLIANCEHELP;
	}

	/**
	 * post == 17
	 * @return
	 */
	public boolean isAllianceOfficerMessage()
	{
		return post == MSG_TYPE_ALLIANCE_OFFICER;
	}

	/**
	 * post == 18
	 * @return
	 */
	public boolean isAllianceSkillMessage()
	{
		return post == MSG_TYPE_ALLIANCE_SKILL;
	}

	/**
	 * post == 19
	 * @return
	 */
	public boolean isStealFailedMessage()
	{
		return post == MSG_TYPE_STEAL_FAILED;
	}
	
	/**
	 * post == 21
	 * @return
	 */
	public boolean isMonsterFirstKillMessage()
	{
		return post == MSG_TYPE_MONSTER_FIRST_KILL;
	}
	
	/**
	 * post == 22
	 * @return
	 */
	public boolean isNewBattleReport()
	{
		return post == MSG_TYPE_NEW_BATTLE_REPORT;
	}
	
	/**
	 * post == 23
	 * @return
	 */
	public boolean isVipLotteryShare()
	{
		return post == MSG_TYPE_VIP_LOTTERY_SHARE;
	}
	
	/**
	 * post == 24
	 * @return
	 */
	public boolean isMidSystemTip()
	{
		return post == MSG_TYPE_MID_SYSTEM_TIP;
	}
	
	public boolean isNpcMessage()
	{
		return isStealFailedMessage() || isMonsterFirstKillMessage() || uid.equals("3000001") || uid.equals("3000002")
				|| isKingTip() || isCannonTip() || isNpcTip() || isActivityTip() || isRankingListTip();
	}
	
	/**
	 * post == 25
	 * @return
	 */
	public boolean isKingTip()
	{
		return post == MSG_TYPE_KING;
	}
	
	/**
	 * post ==26
	 * @return
	 */
	public boolean isCannonTip()
	{
		return post == MSG_TYPE_CANNON;
	}
	
	/**
	 * post == 27
	 * @return
	 */
	public boolean isNpcTip()
	{
		return post == MSG_TYPE_NPC_TIP;
	}
	
	/**
	 * post == 29
	 * @return
	 */
	public boolean isActivityTip()
	{
		return post == MSG_TYPE_ACTIVITY;
	}
	
	/**
	 * post == 30
	 * @return
	 */
	public boolean isDragonShareMsg()
	{
		return post == MSG_TYPE_DRAGON_SHARE;
	}
	
	/**
	 * post == 31
	 * @return
	 */
	public boolean isNewRallyMsg()
	{
		return post == MSG_TYPE_NEW_RALLY;
	}
	
	/**
	 * post == 32
	 * @return
	 */
	public boolean isCommonSharedMsg()
	{
		return post == MSG_TYPE_COMMON_SHARED;
	}
	
	/**
	 * post == 33
	 * @return
	 */
	public boolean isGoldenBoxMsg()
	{
		return post == MSG_TYPE_GOLDEN_BOX;
	}
	
	/**
	 * post == 28
	 * @return
	 */
	public boolean isRankingListTip()
	{
		return post == MSG_TYPE_RANKING_LIST;
	}
	
	public boolean isNeedParseDialog()
	{
		return isCountrySystemMessage() || isAllianceMessage() || isAllianceInviteMessage() || isBattleReport() || isDetectReport()
				|| isRallyMessage() || isStealFailedMessage() || isMonsterFirstKillMessage() || isNewBattleReport() || isVipLotteryShare()
				|| isMidSystemTip() || isKingTip() || isCannonTip() || isNpcTip() || isRankingListTip() || isActivityTip() 
				|| isNearbyLikeMsg() || isDragonShareMsg() || isNewRallyMsg() || isCommonSharedMsg() || isGoldenBoxMsg();
	}
	
	public boolean isOldDialogMessage()
	{
		return  isBattleReport() || isDetectReport() || isHornMessage() || isEquipMessage() || isAllianceJoinMessage() 
				|| isRallyMessage() || isLotteryMessage()
				|| isAllianceTaskMessage() || isRedPackageMessage() || isCordinateShareMessage()
				|| isAllianceTreasureMessage() || isAudioMessage() || isAllianceHelpMessage() || 
				isAllianceOfficerMessage()  || isAllianceSkillMessage() || isShotMessage() ;
	}

	/**
	 * post == 20
	 * @return
	 */
	public boolean isShotMessage()
	{
		return post == MSG_TYPE_SHOT;
	}

	public boolean isAudioMessage()
	{
		return post == MSG_TYPE_AUDIO || post == MSG_TYPE_MOD_AUDIO || post == MSG_TYPE_DRIFTING_BOTTLE_AUDIO;
	}
	
	public boolean isAllianceMemberChangedMsg()
	{
		return isAllianceJoinMessage() || isAllianceMessage();
	}

	/**
	 * 判断是否是系统消息
	 */
	public boolean isSystemMessage()
	{
		return post > 0 && !isTipMsg() && !isModMsg() && !isAudioMessage() && !isDriftingBottleMsg() && !isNearbyLikeMsg() && !isNewEmojMsg();
	}
	
	public boolean isNewAllianceSystem()
	{
		return post > 0
				&& post != MSG_TYPE_BATTLE_REPORT 
				&& post != MSG_TYPE_DETECT_REPORT 
				&& post != MSG_TYPE_RED_PACKAGE
				&& post != MSG_TYPE_ALLIANCE_RALLY
				&& post != MSG_TYPE_AUDIO 
				&& post != MSG_TYPE_CHATROOM_TIP 
				&& post != MSG_TYPE_MOD 
				&& post != MSG_TYPE_DRIFTING_BOTTLE
				&& post != MSG_TYPE_MOD_AUDIO 
				&& post != MSG_TYPE_DRIFTING_BOTTLE_AUDIO
				&& post != MSG_TYPE_NEW_BATTLE_REPORT
				&& post != MSG_TYPE_EMOJ;
	}
	
	public boolean isNewCountrySystem()
	{
		return post > 0
				&& post != MSG_TYPE_HORN
				&& post != MSG_TYPE_RED_PACKAGE
				&& post != MSG_TYPE_AUDIO 
				&& post != MSG_TYPE_STEAL_FAILED
				&& post != MSG_TYPE_NEW_BATTLE_REPORT
				&& post != MSG_TYPE_EMOJ;
	}

	/**
	 * post == 11
	 * @return
	 */
	public boolean isAllianceTaskMessage()
	{
		return post == MSG_TYPE_ALLIANCETASK_SHARE;
	}

	/**
	 * post == 12
	 * @return
	 */
	public boolean isRedPackageMessage()
	{
		return post == MSG_TYPE_RED_PACKAGE;
	}

	private Date getSendUtcDate()
	{
		int t = createTime > 0 ? createTime : sendLocalTime;
		Date date = new Date((long) t * 1000);
		return date;
	}

	public String getSendTime()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeYMD()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeHM()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
		return formatter.format(getSendUtcDate());
	}

	public String getSendTimeToShow()
	{
		if (TimeManager.getInstance().isToday(createTime))
		{
			return getSendTimeHM();
		}
		return getSendTime();
	}

	public boolean hasTranslation()
	{
		return StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{");
	}
	
	public boolean needTranslatePreProcess()
	{
		return StringUtils.isNotEmpty(replaceTranslateMsg) && replaceEmojArray!=null && replaceEmojArray.size()>0;
	}
	
	public String getProcessedMsg()
	{
		return replaceTranslateMsg;
	}
	
	public void postProcessTranslateMsg()
	{
		if(StringUtils.isNotEmpty(translateMsg) && replaceEmojArray!=null)
		{
			for(int i=0;i<replaceEmojArray.size();i++)
			{
				translateMsg = translateMsg.replace("| "+i+" |", replaceEmojArray.get(i));
				translateMsg = translateMsg.replace("|"+i+"|", replaceEmojArray.get(i));
				translateMsg = translateMsg.replace(i+" |"+" |", replaceEmojArray.get(i));
			}
		}
	}
	
	public boolean canShowTranslateMsg()
	{
		return StringUtils.isNotEmpty(msg) && !StringUtils.isNumeric(msg) && isTranslateMsgValid()
				&& (!isTranlateDisable() || isTranslatedByForce) && !isOriginalSameAsTargetLang() && !isOriginalLangByForce;
	}

	public boolean isOriginalLangInValid()
	{
		if (isOriginalSameAsTargetLang() && StringUtils.isEmpty(translateMsg))
			return true;
		return false;
	}

	public boolean isOriginalSameAsTargetLang()
	{
		boolean isSame = false;
		if (StringUtils.isNotEmpty(originalLang)
				&& StringUtils.isNotEmpty(ConfigManager.getInstance().gameLang)
				&& (ConfigManager.getInstance().gameLang.equals(originalLang) || TranslateManager.getInstance().isSameZhLang(originalLang,
						ConfigManager.getInstance().gameLang)))
			isSame = true;
		return isSame;
	}

	public boolean isTranslateMsgValid()
	{
		if (StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{") || StringUtils.isEmpty(translatedLang)
				|| !TranslateManager.isLangSameAsTargetLang(translatedLang))
			return false;
		return true;
	}
	
	public boolean isNearbyLikeMsg()
	{
		return post == MSG_TYPE_NEARBY_LIKE;
	}
	
	public boolean isNewEmojMsg()
	{
		return post == MSG_TYPE_EMOJ;
	}

	public boolean isVersionInvalid()
	{
		if (post > MSG_TYPE_MAX_VALUE && !isTipMsg() && !isModMsg() && !isNearbyLikeMsg() && !isNewEmojMsg() && !isDriftingBottleMsg() && post != MailManager.MAIL_MOD_PERSONAL)
			return true;
		return false;
	}

	public ChatChannel getChatChannel()
	{
		ChatChannel channel = null;
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
			channel = ChannelManager.getInstance().getCountryChannel();
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
			channel = ChannelManager.getInstance().getAllianceChannel();
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
			channel = ChannelManager.getInstance().getBattleFieldChannel();
		return channel;
	}

	public void handleRedPackageFinishState()
	{
		if (!isRedPackageMessage())
			return;
		if (sendState == UNHANDLE && isRedPackageFinish())
		{
			sendState = FINISH;
			ChatChannel channel = getChatChannel();
			if (channel != null)
				DBManager.getInstance().updateMessage(this, channel.getChatTable());
		}
	}

	public boolean isRedPackageFinish()
	{
		if (!isRedPackageMessage())
			return false;
		if (createTime + ChatServiceController.red_package_during_time * 60 * 60 < TimeManager.getInstance().getCurrentTime())
			return true;
		return false;
	}

	public boolean isKingMsg()
	{
		if (StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(ChatServiceController.kingUid)
				&& ChatServiceController.kingUid.equals(uid))
			return true;
		return false;
	}

	public boolean isBattleMsg()
	{
		if (ChatServiceController.getInstance().isInKingdomBattleField()
				&& ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && getUser() != null
				&& getUser().crossFightSrcServerId > 0)
			return true;
		return false;
	}

	public int getMsgItemType(Context context)
	{
		if (firstNewMsgState == 1 || firstNewMsgState == 2)
		{
			return MSGITEM_TYPE_NEW_MESSAGE_TIP;
		}
		else
		{
			if(isNewEmojMsg())
			{
				return MSGITEM_TYPE_PIC;
			}
			else
			{
				String replacedEmoj = StickManager.getPredefinedEmoj(msg);
				if (replacedEmoj != null)
				{
					return getPicType(context, replacedEmoj);
				}
				else
				{
					if (isRedPackageMessage())
						return MSGITEM_TYPE_REDPACKAGE;
					else if (isAudioMessage())
						return MSGITEM_TYPE_AUDIO;
					else if (isNewBattleReport())
						return MSGITEM_TYPE_SHARE;
					else
						return getMessageType();
				}
			}
			
		}
	}

	public int getMessageType()
	{
		if (isTipMsg())
			return MSGITEM_TYPE_CHATROM_TIP;
		else
			return MSGITEM_TYPE_MESSAGE;
	}

	public int getPicType(Context context, String fileName)
	{
		if (fileName == null)
			return -1;
		int id = ResUtil.getId(context, "drawable", fileName);
		if (id == 0)
			return -1;
		if (context.getString(id).endsWith(".gif"))
		{
			return MSGITEM_TYPE_GIF;
		}
		else
		{
			return MSGITEM_TYPE_PIC;
		}
	}

	public boolean isSVIPMsg()
	{
		if (getUser() != null)
			return getUser().isSVIP();
		return false;
	}

	public boolean isVIPMsg()
	{
		if (getUser() != null)
			return getUser().isVIP();
		return false;
	}

	public boolean isNotInRestrictList()
	{
		return (!isSystemMessage() && !UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_LIST))
				|| ((isHornMessage() || isStealFailedMessage()) && !UserManager.getInstance().isInRestrictList(uid,
						UserManager.BAN_NOTICE_LIST));
	}

	public boolean isInRestrictList()
	{
		return (!isSystemMessage() && UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_LIST))
				|| ((isHornMessage() || isStealFailedMessage()) && UserManager.getInstance().isInRestrictList(uid,
						UserManager.BAN_NOTICE_LIST));
	}

	public String getAllianceTreasureInfo(int index)
	{
		if (isAllianceTreasureMessage())
		{
			if (StringUtils.isNotEmpty(attachmentId))
			{
				String[] arr = attachmentId.split("\\|");
				if (arr.length == 3 && index < 3 && index >= 0)
				{
					return arr[index];
				}
			}
		}
		return "";
	}

	public void setVoiceRecordReadState()
	{
		if (isAudioMessage() && !isSelfMsg())
		{
			sendState = VOICE_READ;
			ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
			if (channel != null)
				DBManager.getInstance().updateMessage(this, channel.getChatTable());
		}

	}
	
	
	
	private ParseResult parseTextFromExtra(JSONObject json)
	{
		ParseResult result = null;
		try
		{
			if (json != null && json.containsKey("useDialog") && json.containsKey("text"))
			{
				String text = json.getString("text");
				if (json.getBooleanValue("useDialog"))
				{
					if (json.containsKey("dialogExtra"))
					{
						JSONArray dialogExtraArr = json.getJSONArray("dialogExtra");
						if (dialogExtraArr != null && dialogExtraArr.size() > 0)
						{
							List<ColorFragment> dialogArr = new ArrayList<ColorFragment>();
							for (int i = 0; i < dialogExtraArr.size(); i++)
							{
								JSONObject extra = dialogExtraArr.getJSONObject(i);
								if (extra != null)
								{
									String dialogExtra = "";
									if (extra.containsKey("type"))
									{
										int type = extra.getIntValue("type");
										if (type == 0 && extra.containsKey("text"))
										{
											dialogExtra = extra.getString("text");
										}
										else if (type == 1 && extra.containsKey("xmlId") && extra.containsKey("proName"))
										{
											dialogExtra = JniController.getInstance().excuteJNIMethod("getPropById",
													new Object[] { extra.getString("xmlId"), extra.getString("proName") });
										}
										else if (type == 2 && extra.containsKey("dialog"))
										{
											dialogExtra = LanguageManager.getLangByKey(extra.getString("dialog"));
										}
									}
									int color = 0;
									if(extra.containsKey("textColor"))
										color = ChatServiceController.parseColor(extra.getString("textColor"));
									boolean needLink = false;
									if(extra.containsKey("isLink"))
										needLink = extra.getBoolean("isLink");
									
									ColorFragment fragment = new ColorFragment();
									fragment.setDialogExtra(dialogExtra);
									fragment.setColor(color);
									fragment.setNeedLink(needLink);
									dialogArr.add(fragment);
								}
							}
							if (dialogArr.size() == 1)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);
								ColorFragment fragment = dialogArr.get(0);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra()));
							}
							else if (dialogArr.size() == 2)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);
								
								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(),fragment1.getDialogExtra()));
							}
							else if (dialogArr.size() == 3)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);
								
								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								ColorFragment fragment2 = dialogArr.get(2);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(),fragment1.getDialogExtra(),fragment2.getDialogExtra()));
							}
							else if (dialogArr.size() == 4)
							{
								result = new ParseResult();
								result.setColorExtraList(dialogArr);
								
								ColorFragment fragment = dialogArr.get(0);
								ColorFragment fragment1 = dialogArr.get(1);
								ColorFragment fragment2 = dialogArr.get(2);
								ColorFragment fragment3 = dialogArr.get(3);
								result.setText(LanguageManager.getLangByKey(text, fragment.getDialogExtra(),fragment1.getDialogExtra(),fragment2.getDialogExtra(),fragment3.getDialogExtra()));
							}
						}
						else
						{
							result = new ParseResult();
							result.setColorExtraList(null);
							result.setText(LanguageManager.getLangByKey(text));
						}

					}
					else
					{
						result = new ParseResult();
						result.setColorExtraList(null);
						result.setText(LanguageManager.getLangByKey(text));
					}

				}
				else
				{
					result = new ParseResult();
					result.setColorExtraList(null);
					result.setText(text);;
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private String parseNewPostMsg(JSONObject extraJson)
	{
		try
		{
			boolean parseLangNotSame = StringUtils.isNotEmpty(attachmentParseLang) && !attachmentParseLang.equals(ConfigManager.getInstance().gameLang);
			if(!attachmentParsed || parseLangNotSame)
			{
				if(!attachmentParsed)
				{
					if (extraJson.containsKey("icon"))
						icon = extraJson.getString("icon");
					if (extraJson.containsKey("source"))
					{
						JSONObject source = extraJson.getJSONObject("source");
						if (source != null)
						{
							if (source.containsKey("logo"))
								sourceLogo = source.getString("logo");
							if (source.containsKey("name"))
								sourceName = source.getString("name");
						}
					}
					
					if (extraJson.containsKey("mediaContent"))
					{
						JSONObject mediaContentJson = extraJson.getJSONObject("mediaContent");
						if (mediaContentJson.containsKey("type"))
							mediaType = mediaContentJson.getIntValue("type");
						if (mediaType > -1)
						{
							if (mediaContentJson.containsKey("url"))
								media = mediaContentJson.getString("url");
							if (mediaContentJson.containsKey("time"))
								mediaTime = mediaContentJson.getIntValue("time");
						}
					}
					if (extraJson.containsKey("actionArgs"))
						actionArgs = extraJson.getString("actionArgs");
				}
				
				if (extraJson.containsKey("title"))
				{
					JSONObject titleJson = extraJson.getJSONObject("title");
					ParseResult result = parseTextFromExtra(titleJson);
					if(result!=null)
						title = result.getText();
				}
				if (extraJson.containsKey("description"))
				{
					JSONObject descriptionJson = extraJson.getJSONObject("description");
					ParseResult result = parseTextFromExtra(descriptionJson);
					if(result!=null)
					{
						description = result.getText();
						descriptionColorFragmentList = result.getColorExtraList();
					}
				}
				
				attachmentParsed = true;
				attachmentParseLang = ConfigManager.getInstance().gameLang;
			}
			return description;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	private String parseOldPostMsg(JSONObject extraJson)
	{
		try
		{
			boolean parseLangNotSame = StringUtils.isNotEmpty(attachmentParseLang) && !attachmentParseLang.equals(ConfigManager.getInstance().gameLang);
			if(!attachmentParsed || parseLangNotSame)
			{
				
				if(extraJson.containsKey("dialog"))
				{
					String dialog = extraJson.getString("dialog");
					if(StringUtils.isNotEmpty(dialog))
					{
						if(extraJson.containsKey("msgarr"))
						{
							JSONArray dialogMsgArr = extraJson.getJSONArray("msgarr");
							if (dialogMsgArr != null && dialogMsgArr.size()>0)
							{
								if(dialogMsgArr.size() == 1)
									description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0));
								else if(dialogMsgArr.size() == 2)
								{
									if(isMonsterFirstKillMessage())
									{
										String monsterName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] {dialogMsgArr.getString(1) });
										description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), monsterName);
									}
									else
										description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), dialogMsgArr.getString(1));
								}
								else if(dialogMsgArr.size() == 3)
								{
									if(isMonsterFirstKillMessage())
									{
										String monsterName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] {dialogMsgArr.getString(1) });
										description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), monsterName,monsterName);
									}
									else
										description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), dialogMsgArr.getString(1),dialogMsgArr.getString(2));
								}
								else if(dialogMsgArr.size() == 4)
								{
									description = LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), dialogMsgArr.getString(1),dialogMsgArr.getString(2),dialogMsgArr.getString(3));
								}
							}
							else
							{
								description = LanguageManager.getLangByKey(dialog);
							}
						}
						else
						{
							description = LanguageManager.getLangByKey(dialog);
						}
					}
				}
				
				attachmentParsed = true;
				attachmentParseLang = ConfigManager.getInstance().gameLang;
			}
			return description;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	public String parseExtraDialogMsg()
	{
		if(StringUtils.isNotEmpty(attachmentId) && !isAudioMessage() && (attachmentId.contains("{") || attachmentId.contains("[")))
		{
			try
			{
				JSONObject extraJson = JSON.parseObject(attachmentId);
				if (extraJson != null)
				{
					if(isNewBattleReport())
					{
						if(extraJson.containsKey("shareExtra"))
						{
							JSONObject shareExtra = extraJson.getJSONObject("shareExtra");
							if(shareExtra!=null)
								return parseNewPostMsg(shareExtra);
						}
					}
					else if(extraJson.containsKey("description"))
					{
						return parseNewPostMsg(extraJson);
					}
					else
					{
						return parseOldPostMsg(extraJson);
					}
					
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}

	public boolean neednotTranslate()
	{
		return canNotShowTranslateMenu() || isTranslateMsgValid();
	}

	public boolean canNotShowTranslateMenu()
	{
		if(canNotShowTranslateMenuState == -1)
			canNotShowTranslateMenuState =  (StringUtils.isNotEmpty(msg) && isOriginalSameAsTargetLang()) || canNotShowTranslateQuickActionMenu()? 1 : 0;
		return canNotShowTranslateMenuState == 1 ? true : false;
	}
	
	public boolean canNotShowTranslateQuickActionMenu()
	{
		if(canNotShowTranslateQuickActionState == -1)
			canNotShowTranslateQuickActionState = StringUtils.isEmpty(msg) || isSelfMsg() || (isSystemMessage() && !isHornMessage()) || isNearbyLikeMsg() || isNewEmojMsg() || isAudioMessage() 
			|| !TranslateManager.isNeedTranslateChar(msg) || (StringUtils.isNotEmpty(translateMsg) && isOriginalSameAsTargetLang()) || isSpecialChar() || isCanNotTranslateMessage() ? 1 : 0;
		return canNotShowTranslateQuickActionState == 1 ? true : false;
	}

	public boolean hasTranslated()
	{
		return isTranslateMsgValid()
				&& !isTranlateDisable()
				&& !isOriginalSameAsTargetLang()
				&& (ChatServiceController.isDefaultTranslateEnable || (!ChatServiceController.isDefaultTranslateEnable && hasTranslatedByForce));
	}

	public boolean isCordinateMessage()
	{
		if (StringUtils.isNotEmpty(msg))
		{
			Pattern pattern = Pattern
					.compile("(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])(:|：|: |： )(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])");
			Matcher matcher = pattern.matcher(msg);
			if (matcher.matches())
				return true;
			else
				return false;
		}
		return false;
	}

	public static String toUnicodeString(String s)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c >= 0 && c <= 255)
			{
				sb.append(c);
			}
			else
			{
				sb.append("\\u" + Integer.toHexString(c));
			}
		}
		return sb.toString();
	}
	
	public static String stringtounicode(String s)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c >= 0 && c <= 255)
			{
				sb.append(c);
			}
			else
			{
				String uncodeStr = "\\u" + Integer.toHexString(c);
				Pattern pattern = Pattern.compile("\\\\ud83[c-d]{1}|\\\\ud[c-f]{1}[0-9|a-f]{2}");
				Matcher matcher = pattern.matcher(uncodeStr);
				if(matcher.find())
					sb.append(uncodeStr);
				else
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String unicode2string(String s) {
        List<String> list =new ArrayList<String>();
        String zz="\\\\u[0-9,a-z,A-Z]{4}";
        Pattern pattern = Pattern.compile(zz);
        Matcher m = pattern.matcher(s);
        while(m.find()){
            list.add(m.group());
        }
        for(int i=0,j=2;i<list.size();i++){
            String st = list.get(i).substring(j, j+4);
            char ch = (char) Integer.parseInt(st, 16);
            s = s.replace(list.get(i), String.valueOf(ch));
        }
        return s;
    }

	public boolean isEmojMessage()
	{
		if(StringUtils.isNotEmpty(replaceTranslateMsg) && replaceEmojArray!=null && replaceEmojArray.size()>0)
			return false;
		if (StringUtils.isNotEmpty(msg))
		{
			try
			{
				String unicode = stringtounicode(msg);
				Pattern pattern = Pattern.compile("\\\\ud83[c-d]{1}\\\\ud[c-f]{1}[0-9|a-f]{2}");
				Matcher matcher = pattern.matcher(unicode);
				if(matcher.find())
				{
					String tmp = unicode.replaceAll("\\\\ud83[c-d]{1}\\\\ud[c-f]{1}[0-9|a-f]{2}", "");
					String trimStr = tmp.trim();
					if (StringUtils.isNotEmpty(trimStr))
					{
						replaceEmojArray = new ArrayList<String>();
						
						int count = 0;
						String group  = matcher.group(0);
						if(unicode.contains(group))
						{
							replaceEmojArray.add(unicode2string(group));
							unicode = unicode.replace(group, toUnicodeString("|"+count+"|"));
							count++;
						}
						
						while(matcher.find())
						{
							group  = matcher.group(0);
							if(unicode.contains(group))
							{
								replaceEmojArray.add(unicode2string(group));
								unicode = unicode.replace(group, toUnicodeString("|"+count+"|"));
								count++;
							}
						}
						replaceTranslateMsg = unicode2string(unicode);
						return false;
					}
					else
						return true;
				}
				else
					return false;
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean isNumberMessage()
	{
		if (StringUtils.isNotEmpty(msg))
		{
			Pattern pattern = Pattern.compile("-?[0-9]+.*[0-9]*");
			Matcher matcher = pattern.matcher(msg);
			if (matcher.matches())
				return true;
			else
				return false;
		}
		return false;
	}
	
	public boolean isCanNotTranslateMessage()
	{
		if (StringUtils.isNotEmpty(msg))
		{
			try
			{
				String reg = "/\\/|\\~|\\!|\\@|\\#|\\$|\\%|\\￥|\\^|\\&|\\*|\\“|\\？|\\……|\\【|\\】|\\”|\\(|\\)|\\_|\\+|\\{|\\}|\\:|\\<|\\>|\\\"|\\?|\\[|\\]|\\,|\\.|\\/|\\;|\\'|\\`|\\-|\\=|\\\\|\\|";
				String reg2 = "[0-9]";
				String reg3 = "\\\\ud83[c-d]{1}\\\\ud[c-f]{1}[0-9|a-f]{2}";
				//System.out.println("isCanNotTranslateMessage msg:"+msg);
				String tmp = msg.replaceAll(reg, "");
				String tmp2 = tmp.replaceAll(reg2, "");
				String unicode = stringtounicode(tmp2);
				//System.out.println("isCanNotTranslateMessage unicode:"+unicode);
				String tmp3 = unicode.replaceAll(reg3, "");
				//System.out.println("isCanNotTranslateMessage tmp3:"+tmp3);
				String tmp4 = unicode2string(tmp3);
				//System.out.println("isCanNotTranslateMessage tmp4:"+tmp4);
				Pattern pattern = Pattern.compile("\\\\ud83[c-d]{1}\\\\ud[c-f]{1}[0-9|a-f]{2}");
				Matcher matcher = pattern.matcher(unicode);
				
				if (StringUtils.isNotEmpty(tmp4.trim()))
				{
					if(matcher.find())
					{
						replaceEmojArray = new ArrayList<String>();
						
						Pattern pattern2 = Pattern.compile("\\\\ud83[c-d]{1}\\\\ud[c-f]{1}[0-9|a-f]{2}");
						String msgUnicode = stringtounicode(msg);
						Matcher matcher2 = pattern2.matcher(msgUnicode);
						
						int count = 0;
						while(matcher2.find())
						{
							String group  = matcher2.group(0);
							//System.out.println("isCanNotTranslateMessage group:"+group);
							if(msgUnicode.contains(group))
							{
								replaceEmojArray.add(unicode2string(group));
								msgUnicode = msgUnicode.replace(group, toUnicodeString("|"+count+"|"));
								//System.out.println("isCanNotTranslateMessage msgUnicode:"+msgUnicode);
								count++;
							}
						}
						replaceTranslateMsg = unicode2string(msgUnicode);
						//System.out.println("isCanNotTranslateMessage replaceTranslateMsg:"+replaceTranslateMsg);
					}
					return false;
				}
				else
					return true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean isSpecialChar()
	{
		if (StringUtils.isNotEmpty(msg))
		{
			 try
			{
				String reg = "/\\/|\\~|\\!|\\@|\\#|\\$|\\%|\\￥|\\^|\\&|\\*|\\“|\\？|\\……|\\【|\\】|\\”|\\(|\\)|\\_|\\+|\\{|\\}|\\:|\\<|\\>|\\\"|\\?|\\[|\\]|\\,|\\.|\\/|\\;|\\'|\\`|\\-|\\=|\\\\|\\|";
				String tmp = msg.replaceAll(reg, "");
				if (StringUtils.isNotEmpty(tmp.trim()))
					return false;
				else
					return true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public String parseShotMsg()
	{
		if (isShotMessage())
		{
			if (StringUtils.isNotEmpty(attachmentId))
			{
				try
				{
					JSONObject jsonObj = JSON.parseObject(attachmentId);
					if (jsonObj != null && jsonObj.containsKey("point") && jsonObj.containsKey("dialog"))
					{
						if(jsonObj.containsKey("name"))
						{
							return LanguageManager.getLangByKey(LanguageKeys.TIP_SHOT_2, jsonObj.getString("point"),
									LanguageManager.getLangByKey(jsonObj.getString("dialog")),jsonObj.getString("name"));
						}
						else
						{
							return LanguageManager.getLangByKey(LanguageKeys.TIP_SHOT, jsonObj.getString("point"),
									LanguageManager.getLangByKey(jsonObj.getString("dialog")));
						}
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return "";
	}
	
	public String parseNewEmojName()
	{
		if(StringUtils.isEmpty(expressionName))
		{
			expressionName = "["+LanguageManager.getLangByKey(LanguageKeys.TITLE_EXEPRESSION)+"]";
			if (isNewEmojMsg())
			{
				if (StringUtils.isNotEmpty(msg))
				{
					String[] msgArr = msg.split("\\|");
					if(msgArr.length >= 2)
					{
						String name = "";
						if(msgArr.length == 4 && StringUtils.isNotEmpty(msgArr[3]))
							name = msgArr[3];
						if(StringUtils.isNotEmpty(name))
							expressionName = "["+LanguageManager.getLangByKey(name)+"]";
					}
				}
			}
		}
		return expressionName;
	}
	
	public boolean canEnterScrollTextQueue()
	{
		return (isHornMessage() && (StringUtils.isNotEmpty(msg) || StringUtils.isNotEmpty(translateMsg)))
				|| (isStealFailedMessage() && StringUtils.isNotEmpty(attachmentId));
	}
	
	public boolean isUserChatChannelMsg(){
		return ChannelManager.isUserChannelType(channelType);
	}
}