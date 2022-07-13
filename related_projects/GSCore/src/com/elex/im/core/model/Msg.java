package com.elex.im.core.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.im.core.model.db.DBManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.StickManager;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.core.util.TranslateManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Msg")
public class Msg
{
	@DatabaseField(generatedId = true)
	public int 	_id;
	@DatabaseField
	public int				sequenceId;
	/** uid，群聊时才会存数据库 */
	@DatabaseField
	public String			uid								= "";
	/** 频道类型 */
	@DatabaseField
	public int				channelType						= -1;
	@DatabaseField
	public String			channelID						= "";
	/** 收到的消息会在C++中初始化此字段，对应后台传回来的createTime */
	@DatabaseField
	public int				createTime						= 0;
	/** 数据库中名为type：是否为系统信息，“0”表示不是，非“0”表示是 */
	@DatabaseField
	public int				post							= -1;
	/** 消息体 */
	@DatabaseField
	public String			msg								= "";
	/** 翻译信息 */
	@DatabaseField
	public String			translateMsg					= "";
	/** 源语言 */
	@DatabaseField
	public String			originalLang					= "";
	/** 翻译后的语言 */
	@DatabaseField
	public String			translatedLang					= "";
	/**
	 * 对于自己发的消息,发送状态，0正在发送，1发送失败，2发送成功 红包消息时，表示红包的领取状态,1未领取，0领取过,2被抢光了,3到期了
	 * */
	@DatabaseField
	public int				sendState						= -1;
	@DatabaseField
	public int				readStateBefore					= -1;
	/** 战报UID，侦察战报UID,装备ID等 */
	@DatabaseField
	public String			attachmentId					= "";
	@DatabaseField
	public String			media							= "";
	/** JSON额外信息 */
	@DatabaseField
	public String			extra							= "";
	
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
	private Channel			channel							= null;
	/** 是否强制翻译，点击翻译菜单后置为true，点击原文置为false */
	public boolean			isTranslatedByForce				= false;
	/** 是否做过强制翻译，点击翻译菜单后置为true */
	public boolean			hasTranslatedByForce			= false;
	// 是否被强制显示原文
	public boolean			isOriginalLangByForce			= false;
	public boolean			isAudioDownloading				= false;

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

	// 消息类型
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
	/** 增加post时要变更这个值 */
	public final static int	MSG_TYPE_MAX_VALUE				= MSG_TYPE_SHOT;

	public final static int	MSG_TYPE_CHATROOM_TIP			= 100;
	public final static int	MSG_TYPE_MOD					= 200;
	public final static int	MSG_TYPE_MOD_AUDIO				= 201;

	public final static int	HANDLED							= 0;
	public final static int	UNHANDLE						= 1;
	public final static int	NONE_MONEY						= 2;
	public final static int	FINISH							= 3;

	public final static int	VOICE_UNREAD					= 0;
	public final static int	VOICE_READ						= 1;

	public Msg()
	{
	}
	
	/**
	 * 用于发送消息
	 */
	public Msg(String uidStr, boolean isNewMsg, boolean isSelf, int channelType, int post, String msgStr, int sendLocalTime)
	{
		this.uid = uidStr;
		this.isNewMsg = isNewMsg;
		this.isSelfMsg = isSelf && (post != 100);
		this.channelType = channelType;
		this.post = post;
		this.msg = msgStr;
		if (TranslateManager.getInstance().hasTranslated(this))
			this.hasTranslated = true;
		else
			this.hasTranslated = false;
		this.sendLocalTime = sendLocalTime;
	}

	/**
	 * 用于wrapper假消息
	 */
	public Msg(int seqId, boolean isNewMsg, boolean isSelf, int channelType, int post, String uidStr, String msgStr,
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

	public void setExternalInfo()
	{
		if (TranslateManager.getInstance().hasTranslated(this))
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

	public String getHeadPic()
	{
		return getUser().headPic;
	}

	public int getGmod()
	{
		return getUser().mGmod;
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
//		String fromUid = "";//ChannelManager.getInstance().getModChannelFromUid(mailOpponentUid);
		// mailOpponentName应该是为了用户信息不存在时设置邮件标题用的，可能不必要
		UserManager.checkUser(uid, mailOpponentName, lastUpdateTime);
	}

	public void initUserForSendedMsg()
	{
		UserManager.getInstance().getCurrentUser();
	}
	
	public boolean hasTranslation()
	{
		return StringUtils.isNotEmpty(translateMsg) && !translateMsg.startsWith("{\"code\":{");
	}

	public boolean canShowTranslateMsg()
	{
		return StringUtils.isNotEmpty(msg) && !StringUtils.isNumeric(msg) && TranslateManager.getInstance().isTranslateMsgValid(this)
				&& (!isTranlateDisable() || isTranslatedByForce) && !isOriginalSameAsTargetLang() && !isOriginalLangByForce;
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

	public boolean isInAlliance()
	{
		return !getASN().equals("");
	}

	public boolean isSystemHornMsg()
	{
		return (isHornMessage() && uid.equals("3000002"));
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
	
	public boolean isSelfMsg()
	{
		isSelfMsg = StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().uid)
				&& uid.equals(UserManager.getInstance().getCurrentUser().uid) && post != MSG_TYPE_CHATROOM_TIP;
		return isSelfMsg;
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

	public String getASN()
	{
		return getUser().asn;
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

	public User getUser()
	{
		UserManager.checkUser(uid, "", 0);
		User user = UserManager.getInstance().getUser(uid);
		return user;
	}

	public String getName()
	{
		return getUser().userName;
	}

	public static String getTableName(){
		return null;
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
		return post == MSG_TYPE_CHATROOM_TIP;
	}

	public boolean isModMsg()
	{
		return post == MSG_TYPE_MOD;
	}

	public String getAllianceLabel()
	{
		if (isInAlliance())
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

	public boolean isBattleReport()
	{
		return post == 4;
	}

	public boolean isDetectReport()
	{
		return post == 5;
	}

	public boolean isAnnounceInvite()
	{
		return post == 3;
	}

	public boolean isHornMessage()
	{
		return post == 6;
	}

	public boolean isEquipMessage()
	{
		return post == 7;
	}

	public boolean isAllianceJoinMessage()
	{
		return post == MSG_TYPE_ALLIANCE_JOIN;
	}

	public boolean isRallyMessage()
	{
		return post == MSG_TYPE_ALLIANCE_RALLY;
	}

	public boolean isLotteryMessage()
	{
		return post == MSG_TYPE_LOTTERY_SHARE;
	}

	public boolean isCordinateShareMessage()
	{
		return post == MSG_TYPE_COR_SHARE;
	}

	public boolean isAllianceTreasureMessage()
	{
		return post == MSG_TYPE_ALLIANCE_TREASURE;
	}

	public boolean isAllianceHelpMessage()
	{
		return post == MSG_TYPE_ALLIANCEHELP;
	}
	
	public boolean isAllianceOfficerMessage()
	{
		return post == MSG_TYPE_ALLIANCE_OFFICER;
	}
	
	public boolean isAllianceSkillMessage()
	{
		return post == MSG_TYPE_ALLIANCE_SKILL;
	}
	
	public boolean isStealFailedMessage()
	{
		return post == MSG_TYPE_STEAL_FAILED;
	}
	
	public boolean isShotMessage()
	{
		return post == MSG_TYPE_SHOT;
	}

	public boolean isAudioMessage()
	{
		return post == MSG_TYPE_AUDIO || post == MSG_TYPE_MOD_AUDIO;
	}

	/**
	 * 判断是否是系统消息
	 */
	public boolean isSystemMessage()
	{
		return post > 0 && !isTipMsg() && !isModMsg() && !isAudioMessage();
	}

	public boolean isAllianceTaskMessage()
	{
		return post == MSG_TYPE_ALLIANCETASK_SHARE;
	}

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

	public boolean isVersionInvalid()
	{
		if (post > MSG_TYPE_MAX_VALUE && !isTipMsg() && !isModMsg() && post != MailManager.MAIL_MOD_PERSONAL)
			return true;
		return false;
	}
	
	public boolean isInCrossFightServer()
	{
		return getUser() != null && getUser().crossFightSrcServerId > 0;
	}

	public int getMsgItemType(Context context)
	{
		if (firstNewMsgState == 1 || firstNewMsgState == 2)
		{
			return MSGITEM_TYPE_NEW_MESSAGE_TIP;
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
				else
					return getMessageType();
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
				|| ((isHornMessage() || isStealFailedMessage()) && !UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_NOTICE_LIST));
	}

	public boolean isInRestrictList()
	{
		return (!isSystemMessage() && UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_LIST))
				|| ((isHornMessage() || isStealFailedMessage()) && UserManager.getInstance().isInRestrictList(uid, UserManager.BAN_NOTICE_LIST));
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
	
	public static String parseStealFailedMsg(Msg msgItem)
	{
		if(msgItem!=null && msgItem.isStealFailedMessage() && StringUtils.isNotEmpty(msgItem.attachmentId))
		{
			try
			{
				JSONObject extraJson = JSON.parseObject(msgItem.attachmentId);
				if (extraJson != null && extraJson.containsKey("dialog") && extraJson.containsKey("msgarr"))
				{
					String dialog = extraJson.getString("dialog");
					JSONArray dialogMsgArr = extraJson.getJSONArray("msgarr");
					if (dialogMsgArr != null && dialogMsgArr.size() == 2)
					{
						return LanguageManager.getLangByKey(dialog, dialogMsgArr.getString(0), dialogMsgArr.getString(1));
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
	
	public static String parseShotMsg(Msg item)
	{
		if (item.isShotMessage())
		{
			if(StringUtils.isNotEmpty(item.attachmentId))
			{
				try
				{
					JSONObject jsonObj = JSON.parseObject(item.attachmentId);
					if (jsonObj != null && jsonObj.containsKey("point") && jsonObj.containsKey("dialog"))
					{
						return LanguageManager.getLangByKey(LanguageKeys.TIP_SHOT, jsonObj.getString("point"),
								LanguageManager.getLangByKey(jsonObj.getString("dialog")));
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
	
	public boolean canEnterScrollTextQueue()
	{
		return (isHornMessage() && (StringUtils.isNotEmpty(msg) 
				|| StringUtils.isNotEmpty(translateMsg))) || 
				(isStealFailedMessage() && StringUtils.isNotEmpty(attachmentId));
	}
	
	public void updateDB()
	{
		DBManager.getInstance().updateMsg(this);
	}

	public Channel getChannel()
	{
		return channel;
	}

	public void setChannel(Channel chatChannel)
	{
		this.channel = chatChannel;
	}
}
