package com.elex.im.core.model.db;

import android.provider.BaseColumns;

public class DBDefinition
{
	public static final String	TABLE_SQLITE_MASTER			= "sqlite_master";
	/** 表版本号 */
	public static final String	COLUMN_TABLE_VER			= "TableVer";

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

	public static String chatTableId2Name(String md5TableId)
	{
		return DBDefinition.TABEL_CHAT + "_" + md5TableId;
	}

	public static String mailTableId2Name(String md5TableId)
	{
		return DBDefinition.TABEL_MAIL + "_" + md5TableId;
	}

	/**
	 * User表
	 */
	public static final String	TABEL_USER						= "User";
	/** uid，唯一 */
	public static final String	USER_COLUMN_USER_ID				= "uid";
	/** 用户名（群名） */
	public static final String	USER_COLUMN_NICK_NAME			= "NickName";
	/** 联盟ID */
	public static final String	USER_COLUMN_ALLIANCE_ID			= "AllianceId";
	/** 联盟名称 */
	public static final String	USER_COLUMN_ALLIANCE_NAME		= "AllianceName";
	/** 联盟等级，只有自己有 */
	public static final String	USER_COLUMN_ALLIANCE_RANK		= "AllianceRank";
	/** 服务器ID */
	public static final String	USER_COLUMN_SERVER_ID			= "ServerId";
	/** 跨服战时的原服id，若为-1表示没有跨服 */
	public static final String	USER_CROSS_FIGHT_SRC_SERVER_ID	= "CrossFightSrcServerId";
	/** 用户类型 1:自己 2: */
	public static final String	USER_COLUMN_TYPE				= "Type";
	/** 系统自带头像 */
	public static final String	USER_COLUMN_HEAD_PIC			= "HeadPic";
	/** 自定义头像 */
	public static final String	USER_COLUMN_CUSTOM_HEAD_PIC		= "CustomHeadPic";
	/** 特殊用户 2:mod 4:tmod 5:smod 3:gm */
	public static final String	USER_COLUMN_PRIVILEGE			= "Privilege";
	/** vip等级 */
	public static final String	USER_COLUMN_VIP_LEVEL			= "VipLevel";
	public static final String	USER_COLUMN_SVIP_LEVEL			= "SvipLevel";
	/** vip结束时间，结束后vip信息显示为空 */
	public static final String	USER_COLUMN_VIP_END_TIME		= "VipEndTime";
	/** 上次更新时间 */
	public static final String	USER_COLUMN_LAST_UPDATE_TIME	= "LastUpdateTime";
	/** 上次聊天时间 */
	public static final String	USER_COLUMN_LAST_CHAT_TIME		= "LastChatTime";
	/** 0:未定义 1:男 2:女 */
	public static final String	USER_COLUMN_SEX					= "Sex";
	/** 玩家语言 */
	public static final String	USER_COLUMN_LANG				= "Lang";

	public static final String	CREATE_TABEL_USER				= "CREATE TABLE IF NOT EXISTS " + TABEL_USER + "(" + BaseColumns._ID
																		+ " INTEGER PRIMARY KEY AUTOINCREMENT , " + COLUMN_TABLE_VER
																		+ " INT4 DEFAULT " + DBHelper.CURRENT_DATABASE_VERSION + ", "
																		+ USER_COLUMN_USER_ID
																		+ " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE , "
																		+ USER_COLUMN_NICK_NAME + " TEXT , " + USER_COLUMN_ALLIANCE_ID
																		+ " TEXT , " + USER_COLUMN_ALLIANCE_NAME + " TEXT , "
																		+ USER_COLUMN_ALLIANCE_RANK + " INTEGER DEFAULT -1 , "
																		+ USER_COLUMN_SERVER_ID + " INTEGER DEFAULT -1 , "
																		+ USER_CROSS_FIGHT_SRC_SERVER_ID + " INTEGER DEFAULT -2 , "
																		+ USER_COLUMN_TYPE + " INTEGER DEFAULT 0 , " + USER_COLUMN_HEAD_PIC
																		+ " TEXT , " + USER_COLUMN_CUSTOM_HEAD_PIC
																		+ " INTEGER DEFAULT -1 , " + USER_COLUMN_PRIVILEGE
																		+ " INTEGER DEFAULT -1 , " + USER_COLUMN_VIP_LEVEL
																		+ " INTEGER DEFAULT -1 , " + USER_COLUMN_SVIP_LEVEL
																		+ " INTEGER DEFAULT -1 , " + USER_COLUMN_VIP_END_TIME
																		+ " INTEGER DEFAULT 0 , " + USER_COLUMN_LAST_UPDATE_TIME
																		+ " INTEGER DEFAULT 0 , " + USER_COLUMN_LAST_CHAT_TIME
																		+ " INTEGER DEFAULT 0 , " + USER_COLUMN_LANG + " TEXT )";

	/**
	 * Chat表
	 */
	public static final String	TABEL_CHAT						= "Chat";
	/** 消息的序号 */
	public static final String	CHAT_COLUMN_SEQUENCE_ID			= "SequenceID";
	/** 私信的uid */
	public static final String	CHAT_COLUMN_MAIL_ID				= "MailID";
	/** uid，群聊中才有 */
	public static final String	CHAT_COLUMN_USER_ID				= "UserID";
	/** 消息发送的时间，由后台决定 */
	public static final String	CHAT_COLUMN_CREATE_TIME			= "createTime";
	/** 消息发送的本地时间，用于第一次返回时判断是否是同一条 */
	public static final String	CHAT_COLUMN_LOCAL_SEND_TIME		= "LocalSendTime";
	/** post：是否为系统信息，“0”表示不是，非“0”表示是 */
	public static final String	CHAT_COLUMN_TYPE				= "Type";
	/** 5种对话type */
	public static final String	CHAT_COLUMN_CHANNEL_TYPE		= "channelType";
	/** 消息内容 */
	public static final String	CHAT_COLUMN_MSG					= "Msg";
	/** 翻译后的消息内容 */
	public static final String	CHAT_COLUMN_TRANSLATION			= "Translation";
	/** 源语言 */
	public static final String	CHAT_COLUMN_ORIGINAL_LANGUAGE	= "OriginalLanguage";
	/** 翻译后的语言 */
	public static final String	CHAT_COLUMN_TRANSLATED_LANGUAGE	= "TranslatedLanguage";
	/** 消息状态 SENDING=0; SEND_FAILED=1; SEND_SUCCESS=2 */
	public static final String	CHAT_COLUMN_STATUS				= "Status";
	/** reportUid:战报UID detectReportUid:侦察战报UID */
	public static final String	CHAT_COLUMN_ATTACHMENT_ID		= "AttachmentId";
	/** 媒体资源 */
	public static final String	CHAT_COLUMN_MEDIA				= "Media";

	public static final String	CHAT_TABLE_NAME_PLACEHOLDER		= TABEL_CHAT + "_" + "%chat_id%";
	public static final String	CREATE_TABLE_CHAT				= "CREATE TABLE IF NOT EXISTS " + CHAT_TABLE_NAME_PLACEHOLDER + "("
																		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
																		+ COLUMN_TABLE_VER + " INT4 DEFAULT "
																		+ DBHelper.CURRENT_DATABASE_VERSION + ", "
																		+ CHAT_COLUMN_SEQUENCE_ID + " INTEGER DEFAULT -1 , "
																		+ CHAT_COLUMN_MAIL_ID + " TEXT , " + CHAT_COLUMN_USER_ID
																		+ " TEXT NOT NULL , " + CHAT_COLUMN_CHANNEL_TYPE
																		+ " INTEGER CHECK(" + CHAT_COLUMN_CHANNEL_TYPE + " >= 0 ) , "
																		+ CHAT_COLUMN_CREATE_TIME + " INTEGER DEFAULT 0 , "
																		+ CHAT_COLUMN_LOCAL_SEND_TIME + " INTEGER DEFAULT 0 , "
																		+ CHAT_COLUMN_TYPE + " INTEGER DEFAULT -1 , " + CHAT_COLUMN_MSG
																		+ " TEXT , " + CHAT_COLUMN_TRANSLATION + " TEXT , "
																		+ CHAT_COLUMN_ORIGINAL_LANGUAGE + " TEXT , "
																		+ CHAT_COLUMN_TRANSLATED_LANGUAGE + " TEXT , " + CHAT_COLUMN_STATUS
																		+ " INTEGER DEFAULT -1 , " + CHAT_COLUMN_ATTACHMENT_ID + " TEXT , "
																		+ CHAT_COLUMN_MEDIA + " TEXT )";

	/**
	 * Channel表(聊天频道属性)，条目与User表一一对应
	 */
	public static final String	TABEL_CHANNEL					= "Channel";
	/** 与User的uid字段内容一样 */
	public static final String	CHANNEL_CHANNEL_ID				= "channelID";
	/** 用于频道最新消息序号 */
	public static final String	CHANNEL_MIN_SEQUENCE_ID			= "MinSequenceID";
	/** 用于频道最新消息序号 */
	public static final String	CHANNEL_MAX_SEQUENCE_ID			= "MaxSequenceID";
	/** 5种对话type */
	public static final String	CHANNEL_TYPE					= "ChannelType";
	/** 用于聊天室成员列表 */
	public static final String	CHANNEL_CHATROOM_MEMBERS		= "ChatRoomMembers";
	/** 聊天室房主 */
	public static final String	CHANNEL_CHATROOM_OWNER			= "ChatRoomOwner";
	/** 当前玩家是否属于聊天室成员 */
	public static final String	CHANNEL_IS_MEMBER				= "IsMember";
	/** 聊天室自定义名称 */
	public static final String	CHANNEL_CUSTOM_NAME				= "CustomName";
	/** 未读数目 */
	public static final String	CHANNEL_UNREAD_COUNT			= "UnreadCount";
	/** 最近邮件的ID（邮件专用） */
	public static final String	CHANNEL_LATEST_ID				= "LatestId";
	/** 最近消息时间 */
	public static final String	CHANNEL_LATEST_TIME				= "LatestTime";
	/** 最近修改时间，仅针对系统邮件 */
	public static final String	CHANNEL_LATEST_MODIFY_TIME		= "LatestModifyTime";
	/** 置顶、免打扰、保存到通讯录、聊天背景、是否显示昵称等开关型属性，key1:0|key2:1形式（无顺序，没有就是默认值），或者用0|1|0形式 */
	public static final String	CHANNEL_SETTINGS				= "Settings";
	public static final String	CHANNEL_CHAT_DRAFT				= "Draft";
	public static final String	CHANNEL_CHAT_DRAFT_TIME			= "DraftTime";

	public static final String	CREATE_TABEL_CHANNEL			= "CREATE TABLE IF NOT EXISTS " + TABEL_CHANNEL + "(" + BaseColumns._ID
																		+ " INTEGER PRIMARY KEY AUTOINCREMENT , " + COLUMN_TABLE_VER
																		+ " INT4 DEFAULT " + DBHelper.CURRENT_DATABASE_VERSION + ", "
																		+ CHANNEL_TYPE + " INTEGER CHECK(" + CHANNEL_TYPE + " >= 0) , "
																		+ CHANNEL_CHANNEL_ID
																		+ " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE , "
																		+ CHANNEL_MIN_SEQUENCE_ID + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_MAX_SEQUENCE_ID + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_CHATROOM_MEMBERS + " TEXT , " + CHANNEL_CHATROOM_OWNER
																		+ " TEXT , " + CHANNEL_IS_MEMBER + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_CUSTOM_NAME + " TEXT , " + CHANNEL_UNREAD_COUNT
																		+ " INTEGER DEFAULT 0 , " + CHANNEL_LATEST_ID + " TEXT , "
																		+ CHANNEL_LATEST_TIME + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_LATEST_MODIFY_TIME + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_CHAT_DRAFT + " TEXT , "
																		+ CHANNEL_CHAT_DRAFT_TIME + " INTEGER DEFAULT -1 , "
																		+ CHANNEL_SETTINGS + " TEXT ) ";

	/**
	 * Mail表，存储系统邮件，都是发给当前玩家的
	 */
	public static final String	TABEL_MAIL						= "Mail";
	/** 邮件唯一ID */
	public static final String	MAIL_ID							= "ID";
	/** 邮件的channelId */
	public static final String	MAIL_CHANNEL_ID					= "ChannelID";
	public static final String	MAIL_FROM_USER_ID				= "FromUser";
	public static final String	MAIL_FROM_NAME					= "FromName";
	public static final String	MAIL_TITLE						= "Title";
	public static final String	MAIL_CONTENTS					= "Contents";
	public static final String	MAIL_REWARD_ID					= "RewardId";
	public static final String	MAIL_ITEM_ID_FLAG				= "ItemIdFlag";
	public static final String	MAIL_STATUS						= "Status";
	public static final String	MAIL_TYPE						= "Type";
	public static final String	MAIL_REWARD_LEVEL				= "MailRewardLevel";
	public static final String	MAIL_REWARD_STATUS				= "RewardStatus";
	public static final String	MAIL_SAVE_FLAG					= "SaveFlag";
	public static final String	MAIL_CREATE_TIME				= "CreateTime";
	public static final String	MAIL_REPLY						= "Reply";
	public static final String	MAIL_TRANSLATION				= "Translation";
	public static final String	MAIL_TITLE_TEXT					= "TitleText";
	public static final String	MAIL_SUMMARY					= "Summary";
	public static final String	MAIL_LANGUAGE					= "Language";
	public static final String	PARSE_VERSION					= "ParseVersion";
	public static final String	RECYCLE_TIME					= "RecycleTime";

	public static final String	CREATE_TABEL_MAIL				= "CREATE TABLE IF NOT EXISTS " + TABEL_MAIL + "(" + BaseColumns._ID
																		+ " INTEGER PRIMARY KEY AUTOINCREMENT , " + COLUMN_TABLE_VER
																		+ " INT4 DEFAULT " + DBHelper.CURRENT_DATABASE_VERSION + ", "
																		+ MAIL_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE , "
																		+ MAIL_CHANNEL_ID + " TEXT NOT NULL, " + MAIL_FROM_USER_ID
																		+ " TEXT , " + MAIL_FROM_NAME + " TEXT NOT NULL , " + MAIL_TITLE
																		+ " TEXT NOT NULL , " + MAIL_CONTENTS + " TEXT NOT NULL , "
																		+ MAIL_REWARD_ID + " TEXT , " + MAIL_ITEM_ID_FLAG
																		+ " INTEGER DEFAULT -1 , " + MAIL_STATUS + " INTEGER DEFAULT -1 , "
																		+ MAIL_TYPE + " INTEGER DEFAULT -1 , " + MAIL_REWARD_LEVEL
																		+ " INTEGER DEFAULT 0 , " + MAIL_REWARD_STATUS
																		+ " INTEGER DEFAULT -1 , " + MAIL_SAVE_FLAG
																		+ " INTEGER DEFAULT -1 , " + MAIL_CREATE_TIME
																		+ " INTEGER DEFAULT -1 , " + MAIL_REPLY + " INTEGER DEFAULT -1 , "
																		+ MAIL_TRANSLATION + " TEXT , " + MAIL_TITLE_TEXT + " TEXT , "
																		+ MAIL_SUMMARY + " TEXT , " + MAIL_LANGUAGE + " TEXT , "
																		+ RECYCLE_TIME + " INTEGER DEFAULT -1 , "
																		+ PARSE_VERSION + " INTEGER DEFAULT -1 )";
	
	
	/**
	 * Msg表  原Chat表	表结构与Msg类结构完全相同
	 */
	
	public static final String	TABLE_MSG 				= "Msg"				;
	
	public static final String	MSG_SEQUENCE_ID 		=  "sequenceId";
	/** uid，群聊时才会存数据库 */
	public static final String	MSG_UID 				=  "uid";
	/** 频道类型 */
	public static final String  MSG_CHANNEL_TYPE 		=  "channelType";
	public static final String  MSG_CHANNEL_ID 		=  "channelID";
	/** 收到的消息会在C++中初始化此字段，对应后台传回来的createTime */
	public static final String	MSG_CREATE_TIME			=	"createTime";
	/** 数据库中名为type：是否为系统信息，“0”表示不是，非“0”表示是 */
	public static final String	MSG_POST				=	"post";
	/** 消息体 */
	public static final String	MSG_MSG					=	"msg";
	/** 翻译信息 */
	public static final String	MSG_TRANSLATE_MSG		=	"translateMsg";
	/** 源语言 */
	public static final String	MSG_ORIGINAL_LANG		=	"originalLang";
	/** 翻译后的语言 */
	public static final String	MSG_TRANSLATED_LANG		=	"translatedLang";
	/**
	 * 对于自己发的消息,发送状态，0正在发送，1发送失败，2发送成功 红包消息时，表示红包的领取状态,1未领取，0领取过,2被抢光了,3到期了
	 * */
	public static final String	MSG_SEND_STATE			=	"sendState";
	public static final String	MSG_READ_STATE_BEFORE	=	"readStateBefore";
	/** 战报UID，侦察战报UID,装备ID等 */
	public static final String	MSG_ATTACHEMENT_ID		=	"attachmentId";
	public static final String	MSG_MEDIA				=	"media";
	/** JSON额外信息 */
	public static final String	MSG_EXTRA				=	"extra";
	
	public static final String	CREATE_TABEL_MSG				= "CREATE TABLE IF NOT EXISTS " + TABLE_MSG + "(" 
																+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " 
																
//																+ COLUMN_TABLE_VER
//																+ " INT4 DEFAULT " + DBHelper.CURRENT_DATABASE_VERSION + ", "
																
																+ MSG_SEQUENCE_ID + " INTEGER DEFAULT -1 ,"
																+ MSG_UID + " TEXT NOT NULL, " 
																+ MSG_CHANNEL_TYPE + " INTEGER DEFAULT -1 ,"
																+ MSG_CREATE_TIME + " INTEGER DEFAULT 0 , " 
																+ MSG_POST + " INTEGER DEFAULT -1 , " 
																+ MSG_MSG + " TEXT NOT NULL , "	
																+ MSG_TRANSLATE_MSG + " TEXT , " 
																+ MSG_ORIGINAL_LANG + " TEXT , " 
																+ MSG_TRANSLATED_LANG + " TEXT , "
																+ MSG_SEND_STATE + " INTEGER DEFAULT -1 , " 
																+ MSG_READ_STATE_BEFORE + " INTEGER DEFAULT -1 , "
																+ MSG_ATTACHEMENT_ID + " TEXT , " 
																+ MSG_MEDIA + " TEXT , " 
																+ MSG_EXTRA + " TEXT)";
	
	
}
