package com.elex.chatservice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.image.AsyncImageLoader;
import com.elex.chatservice.image.ImageLoaderListener;
import com.elex.chatservice.model.db.ChatTable;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.mail.battle.BattleMailContents;
import com.elex.chatservice.model.mail.battle.BattleMailData;
import com.elex.chatservice.model.mail.monster.MonsterMailContents;
import com.elex.chatservice.model.mail.monster.MonsterMailData;
import com.elex.chatservice.model.mail.newworldboss.NewWorldBossMailContents;
import com.elex.chatservice.model.mail.newworldboss.NewWorldBossMailData;
import com.elex.chatservice.model.mail.resouce.ResourceMailContents;
import com.elex.chatservice.model.mail.resouce.ResourceMailData;
import com.elex.chatservice.model.mail.resourcehelp.ResourceHelpMailContents;
import com.elex.chatservice.model.mail.resourcehelp.ResourceHelpMailData;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.BitmapUtil;
import com.elex.chatservice.util.CombineBitmapManager;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.SortUtil;
import com.elex.chatservice.view.ChannelListFragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

public class ChatChannel extends ChannelListItem implements Serializable, Comparable<ChatChannel>
{
	private static final long	serialVersionUID				= -4351092186878517042L;

	public int					channelType						= -1;
	public String				channelID;
	public int					dbMinSeqId						= -1;
	public int					dbMaxSeqId						= -1;
	/** 聊天室成员uid列表 */
	public ArrayList<String>	memberUidArray					= new ArrayList<String>();
	/** 聊天室房主 */
	public String				roomOwner;
	/** 是否是聊天室成员 */
	public boolean				isMember						= false;
	/** 聊天室自定义名称 */
	public String				customName						= "";
	/** 最近消息时间 */
	public long					latestTime						= -1;
	/** 最近修改时间，仅针对系统邮件 */
	public long					latestModifyTime				= -1;
	/** 最近消息的id（邮件专用） */
	public String				latestId						= "0";
	/** 聊天室设置 */
	public String				settings;
	/** 聊天草稿 */
	public InputDraft			inputDraft						= null;
	public String				draft							= "";
	public List<InputAtContent>	draftAt							= null;
	/** 聊天草稿时间 */
	public long					draftTime						= -1;

	// 运行时属性
	/** 消息对象List，保存所有消息 */
	public ArrayList<MsgItem>	msgList							= new ArrayList<MsgItem>();
	/** 正在发送的消息 */
	public ArrayList<MsgItem>	sendingMsgList					= new ArrayList<MsgItem>();
	/** 是否获取到消息过 */
	public boolean				hasRequestDataBefore			= false;
	/** 是否没有更多消息了 */
	public boolean				noMoreDataFlag					= false;
	private int					sysMailCountInDB				= 0;
	private int					sysUnreadMailCountInDB			= 0;

	public Point				lastPosition					= new Point(-1, -1);

	public int					serverMinSeqId;
	public int					serverMaxSeqId;

	public long					serverMaxTime;
	public long					serverMinTime;
	/** 连ws后台时，登陆后从history.roomsv2接口加载到的新消息数量 **/
	public int					wsNewMsgCount;

	/** 收取前db的最大id */
	public int					prevDBMaxSeqId;
	/** 是否正在批量加载新消息 */
	public boolean				isLoadingAllNew					= false;
	/** 是否已经批量加载过新消息 */
	public boolean				hasLoadingAllNew				= false;
	public int					firstNewMsgSeqId;
	/** 最近的一条邮件信息 */
	public MailData				latestMailData					= null;
	public boolean				isMemberUidChanged				= false;

	// 显示属性
	public String				nameText						= "";
	public String				contentText						= "";
	public String				channelIcon						= "";
	public UserInfo				channelShowUserInfo				= null;
	public String				timeText						= "";
	public boolean				usePersonalPic					= false;
	public MsgItem				showItem						= null;
	private ChannelView			channelView						= null;
	public CopyOnWriteArrayList<String>			mailUidList						= new CopyOnWriteArrayList<String>();

	/** 系统邮件的邮件对象 */
	public List<MailData>		mailDataList					= new ArrayList<MailData>();
	public long					latestLoadedMailCreateTime		= -1;
	public long					latestLoadedMailRecycleTime		= -1;
	private List<Integer>		msgTimeIndexArray				= null;

	private boolean				calculateSysMailCountInDB		= false;
	private boolean				calculateUnreadSysMailCountInDB	= false;
	
	public List<MsgItem>		hideMsgItemList					= new ArrayList<MsgItem>();
	public int 					randomChatMode					= 0;
	
	private List<String>		unreadMailUids					= null;
	private List<String>		rewardMailUids					= null;
	
	private int					hasMsgInDBStatus				= 0;
	private int					earliestAtMeMsgCreateTime		= -1;
	public int 					currentShowEarliestTime			= -1;
	public boolean				isLoadingEarliestAtMeMsg		= false;
	public CopyOnWriteArrayList<MsgItem>		atMeMsgList 					= new CopyOnWriteArrayList<MsgItem>();
	public CopyOnWriteArrayList<MsgItem>		unreadSystemMsgList 			= new CopyOnWriteArrayList<MsgItem>();
	

	public ChatChannel()
	{
	}

	public int getUnreadCountInMailList()
	{
		int unreadCount = 0;
		if (mailDataList != null && mailDataList.size() > 0)
		{
			for (MailData mail : mailDataList)
			{
				if (mail != null && mail.isUnread())
					unreadCount++;
			}
		}
		return unreadCount;
	}

	public void refreshRenderData()
	{
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY);

			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			timeText = TimeManager.getReadableTime(latestTime);

			if (msgList.size() > 0)
			{
				MsgItem msg = msgList.get(msgList.size() - 1);
				if (msg != null)
				{
					showItem = msg;
					if (!msg.translateMsg.equals(""))
						contentText = msg.translateMsg;
					else
						contentText = msg.msg;
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE);

			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			timeText = TimeManager.getReadableTime(latestTime);

			if (msgList.size() > 0)
			{
				MsgItem msg = msgList.get(msgList.size() - 1);
				if (msg != null)
				{
					showItem = msg;
					if (!msg.translateMsg.equals(""))
						contentText = msg.translateMsg;
					else
						contentText = msg.msg;
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (StringUtils.isEmpty(channelID))
				return;

			if (channelID.equals(MailManager.CHANNELID_MOD))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MOD);
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
				contentText = ChannelManager.getInstance().latestModChannelMsg;
				return;
			}
			else if (channelID.equals(MailManager.CHANNELID_MESSAGE))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MESSAGE);
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_MESSAGE);
				contentText = ChannelManager.getInstance().latestMessageChannelMsg;
				return;
			}
			else if (channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_DRIFTING_BOTTLE);
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_DRIFTING_BOTTLE);
				contentText = ChannelManager.getInstance().latestDrfitingBottleChannelMsg;
				return;
			}
			else if (channelID.equals(MailManager.CHANNELID_NEAR_BY))
			{
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_NEARBY);
				nameText = LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY_MSG);
				contentText = ChannelManager.getInstance().latestNearbyChannelMsg;
				return;
			}

			if (TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channelID);
			UserManager.checkUser(fromUid, "", 0);

			if (StringUtils.isNotEmpty(fromUid))
			{
				UserInfo fromUser = UserManager.getInstance().getUser(fromUid);
				if (fromUser != null)
				{
					channelIcon = fromUser.headPic;
					channelShowUserInfo = fromUser;
					nameText = "";
					if (StringUtils.isNotEmpty(fromUser.asn))
					{
						nameText = "(" + fromUser.asn + ")";
					}

					if (StringUtils.isNotEmpty(fromUser.userName))
					{
						nameText += fromUser.userName;
					}
					else if (StringUtils.isNotEmpty(customName))
					{
						nameText += customName;
					}
					else
					{
						nameText += fromUser.uid;
					}
				}
				if (fromUid.equals(UserManager.getInstance().getCurrentUserId()))
				{
					nameText = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
				}
			}
			else
			{
				nameText = channelID;
			}

			if (StringUtils.isNotEmpty(draft))
			{
				timeText = TimeManager.getReadableTime(draftTime);
				contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_DRAFT) + " " + draft;
			}
			else
			{
				MsgItem mail = getLatestUserMail();

				if (mail != null)
				{
					if (TimeManager.isInValidTime(latestTime))
					{
						latestTime = mail.createTime;
						timeText = TimeManager.getReadableTime(latestTime);
					}
					if(mail.isAudioMessage())
					{
						contentText = "["+LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO)+"]";
					}
					else if(mail.isNewEmojMsg())
					{
						contentText = mail.parseNewEmojName();
					}
					else
					{
						if (mail.canShowTranslateMsg())
						{
							contentText = mail.translateMsg;
						}
						else
						{
							contentText = mail.msg;
						}
					}
				}
			}

		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
			if (TimeManager.isInValidTime(latestTime))
			{
				latestTime = getLatestTime();
			}
			timeText = TimeManager.getReadableTime(latestTime);

			nameText = StringUtils.isNotEmpty(customName) ? customName : channelID;

			if (StringUtils.isNotEmpty(draft))
			{
				timeText = TimeManager.getReadableTime(draftTime);
				contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_DRAFT) + " " + draft;
			}
			else
			{
				MsgItem mail = getLatestUserMail();

				if (mail != null)
				{
					if (TimeManager.isInValidTime(latestTime))
					{
						latestTime = mail.createTime;
						timeText = TimeManager.getReadableTime(latestTime);
					}
					
					if(mail.isAudioMessage())
					{
						contentText = "["+LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO)+"]";
						if (mail.isSelfMsg())
							contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU) + ":" + contentText;
						else
							contentText = mail.getName() + ":"  + contentText;
					}
					else if(mail.isNewEmojMsg())
					{
						contentText = mail.parseNewEmojName();
					}
					else
					{
						if (mail.canShowTranslateMsg())
						{
							if (mail.isTipMsg())
								contentText = mail.translateMsg;
							else
							{
								if (mail.isSelfMsg())
									contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU) + ":" + mail.translateMsg;
								else
									contentText = mail.getName() + ":" + mail.translateMsg;
							}
						}
						else
						{
							if (mail.isTipMsg())
								contentText = mail.msg;
							else
							{
								if (mail.isSelfMsg())
									contentText = LanguageManager.getLangByKey(LanguageKeys.TIP_YOU) + ":" + mail.msg;
								else
									contentText = mail.getName() + ":" + mail.msg;
							}
						}
					}
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (ChatServiceController.isNewMailUIEnable)
			{
				if (channelID.equals(MailManager.CHANNELID_FIGHT))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_FIGHT);
				if (channelID.equals(MailManager.CHANNELID_DRAGON_TOWER))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_DRAGON_TOWER);
				else if (channelID.equals(MailManager.CHANNELID_ALLIANCE))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_ALLIANCE);
				else if (channelID.equals(MailManager.CHANNELID_MESSAGE))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MESSAGE);
				else if (channelID.equals(MailManager.CHANNELID_EVENT))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_EVENT);
				else if (channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RECYCLEBIN);
				else if (channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
					channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_NEW_WORLD_BOSS);
			}

			if (channelID.equals(MailManager.CHANNELID_STUDIO))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_STUDIO);
			else if (channelID.equals(MailManager.CHANNELID_SYSTEM))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_SYSTEM);
			else if (channelID.equals(MailManager.CHANNELID_RESOURCE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RESOURCE);
			else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_KNIGHT);
			else if (channelID.equals(MailManager.CHANNELID_MONSTER))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
			else if (channelID.equals(MailManager.CHANNELID_NOTICE))
				channelIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_ANNOUNCEMENT);

			nameText = getSystemChannelName();

//			if (channelID.equals(MailManager.CHANNELID_FIGHT) || channelID.equals(MailManager.CHANNELID_ALLIANCE)
//					|| channelID.equals(MailManager.CHANNELID_EVENT) || channelID.equals(MailManager.CHANNELID_STUDIO)
//					|| channelID.equals(MailManager.CHANNELID_SYSTEM) || channelID.equals(MailManager.CHANNELID_KNIGHT)
//					|| channelID.equals(MailManager.CHANNELID_MONSTER) || channelID.equals(MailManager.CHANNELID_RESOURCE)
//					|| channelID.equals(MailManager.CHANNELID_RECYCLE_BIN) || channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)
//					|| channelID.equals(MailManager.CHANNELID_DRAGON_TOWER))
//				return;
//
//			if (TimeManager.isInValidTime(latestTime))
//			{
//				latestTime = getLatestTime();
//			}
//			timeText = TimeManager.getReadableTime(latestTime);
//
//			if (mailDataList.size() > 0)
//			{
//				MailData mail = getLatestMailData();
//				if (mail != null)
//				{
//					if (TimeManager.isInValidTime(latestTime))
//					{
//						latestTime = mail.getCreateTime();
//						timeText = TimeManager.getReadableTime(latestTime);
//					}
//					if (StringUtils.isEmpty(nameText))
//						nameText = mail.nameText;
//					contentText = mail.contentText;
//					channelIcon = mail.mailIcon;
//				}
//			}
		}
	}

	public MsgItem getLatestUserMail()
	{
		MsgItem mail = null;
		if (msgList != null && msgList.size() > 0)
			mail = msgList.get(msgList.size() - 1);
		return mail;
	}

	public String getSystemChannelName()
	{
		String name = "";
		if (channelID.equals(MailManager.CHANNELID_SYSTEM))
			name = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM);
		else if (channelID.equals(MailManager.CHANNELID_STUDIO))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
		else if (channelID.equals(MailManager.CHANNELID_FIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
		else if (channelID.equals(MailManager.CHANNELID_DRAGON_TOWER))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_DRAGON_PVE);
		else if (channelID.equals(MailManager.CHANNELID_MOD))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
		else if (channelID.equals(MailManager.CHANNELID_ALLIANCE))
			name = LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE);
		else if (channelID.equals(MailManager.CHANNELID_NOTICE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
		else if (channelID.equals(MailManager.CHANNELID_RESOURCE))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_RESOURCE);
		else if (channelID.equals(MailManager.CHANNELID_KNIGHT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_ACTIVITYREPORT);
		else if (channelID.equals(MailManager.CHANNELID_RESOURCE_HELP))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_RESOURCEHELP);
		else if (channelID.equals(MailManager.CHANNELID_MONSTER))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103715);
		else if (channelID.equals(MailManager.CHANNELID_EVENT))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_EVENT);
		else if (channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
			name = LanguageManager.getLangByKey(LanguageKeys.CHANNEL_NAME_RECYCLEBIN);
		else if (channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
			name = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137451);
		return name;
	}

	public String getCustomName()
	{
		String name = "";
		if (StringUtils.isNotEmpty(customName))
		{
			name = customName;
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			UserInfo fromUser = UserManager.getInstance().getUser(channelID);
			if (fromUser != null)
			{
				if (StringUtils.isNotEmpty(fromUser.userName))
				{
					name = fromUser.userName;
					customName = name;
					DBManager.getInstance().updateChannel(this);
				}
				else
				{
					name = fromUser.uid;
				}
			}

		}
		return name;
	}

	public ChatChannel(Cursor c)
	{
		try
		{
			channelID = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHANNEL_ID));
			dbMinSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_MIN_SEQUENCE_ID));
			dbMaxSeqId = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_MAX_SEQUENCE_ID));
			channelType = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_TYPE));
			if (StringUtils.isNotEmpty(c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_MEMBERS))))
			{
				String[] members = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_MEMBERS)).split("\\|");
				for (int i = 0; i < members.length; i++)
				{
					memberUidArray.add(members[i]);
				}
			}

			roomOwner = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHATROOM_OWNER));
			isMember = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_IS_MEMBER)) == 1;
			customName = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CUSTOM_NAME));
			if (ChannelManager.getInstance().isNeedCalculateUnreadCount(channelID))
				unreadCount = 0;
			else
				unreadCount = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_UNREAD_COUNT));
			latestId = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_ID));
			latestTime = c.getInt(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_TIME));
			latestModifyTime = c.getLong(c.getColumnIndex(DBDefinition.CHANNEL_LATEST_MODIFY_TIME));
			String draftContent = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_CHAT_DRAFT));
			if(StringUtils.isNotEmpty(draftContent) && draftContent.contains("{") && draftContent.contains("}"))
			{
				inputDraft = JSON.parseObject(draftContent, InputDraft.class);
				if(inputDraft!=null)
				{
					draft = inputDraft.getDraft();
					draftAt = inputDraft.getInputAt();
				}
			}
			else
			{
				inputDraft = null;
				draft = draftContent;
				draftAt = null;
			}
			draftTime = c.getLong(c.getColumnIndex(DBDefinition.CHANNEL_CHAT_DRAFT_TIME));
			settings = c.getString(c.getColumnIndex(DBDefinition.CHANNEL_SETTINGS));
			initSeqId();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

	}

	private void initSeqId()
	{
		if (DBManager.getInstance().isTableExists(getChatTable().getTableName()) && hasSeqId())
		{
			getMaxAndMinSeqId();
		}
	}

	private boolean hasSeqId()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE 
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM
				|| channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}

	private void getMaxAndMinSeqId()
	{
		int maxSeqId = DBManager.getInstance().getMaxDBSeqId(getChatTable());
		int minSeqId = DBManager.getInstance().getMinDBSeqId(getChatTable());
		boolean hasChanged = false;
		if (maxSeqId != 0)
		{
			dbMaxSeqId = maxSeqId;
			hasChanged = true;
		}
		if (minSeqId != 0)
		{
			dbMinSeqId = minSeqId;
			hasChanged = true;
		}
		if (hasChanged)
			DBManager.getInstance().updateChannel(this);
		prevDBMaxSeqId = dbMaxSeqId;
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.CHANNEL_CHANNEL_ID, channelID);
		cv.put(DBDefinition.CHANNEL_MIN_SEQUENCE_ID, dbMinSeqId);
		cv.put(DBDefinition.CHANNEL_MAX_SEQUENCE_ID, dbMaxSeqId);
		cv.put(DBDefinition.CHANNEL_TYPE, channelType);
		cv.put(DBDefinition.CHANNEL_CHATROOM_MEMBERS, getMembersString(memberUidArray));
		cv.put(DBDefinition.CHANNEL_CHATROOM_OWNER, roomOwner);
		cv.put(DBDefinition.CHANNEL_IS_MEMBER, isMember ? 1 : 0);
		cv.put(DBDefinition.CHANNEL_CUSTOM_NAME, customName);
		cv.put(DBDefinition.CHANNEL_UNREAD_COUNT, unreadCount);
		cv.put(DBDefinition.CHANNEL_LATEST_ID, latestId);
		cv.put(DBDefinition.CHANNEL_LATEST_TIME, latestTime);
		cv.put(DBDefinition.CHANNEL_LATEST_MODIFY_TIME, latestModifyTime);
		if(inputDraft == null)
			cv.put(DBDefinition.CHANNEL_CHAT_DRAFT, draft);
		else
		{
			try
			{
				String draft = JSON.toJSONString(inputDraft);
				cv.put(DBDefinition.CHANNEL_CHAT_DRAFT, draft);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		cv.put(DBDefinition.CHANNEL_CHAT_DRAFT_TIME, draftTime);
		cv.put(DBDefinition.CHANNEL_SETTINGS, settings);
		return cv;
	}

	/**
	 * 显示着的最新sequenceId，与server、数据库的max一样
	 */
	public int getViewMaxId()
	{
		int result = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			result = msgList.get(i).sequenceId > result ? msgList.get(i).sequenceId : result;
		}
		return result;
	}

	/**
	 * 显示着的最老sequenceId
	 */
	public int getViewMinId()
	{
		int result = msgList.size() > 0 ? msgList.get(0).sequenceId : 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			result = msgList.get(i).sequenceId < result ? msgList.get(i).sequenceId : result;
		}
		return result;
	}

	/**
	 * DB中的最新sequenceId
	 */
	public int getDBMaxId()
	{
		return DBManager.getInstance().getMaxDBSeqId(getChatTable());
	}

	/**
	 * DB中的最新消息ID（邮件专用）
	 */
	public String getDBLatestId()
	{
		return DBManager.getInstance().getLatestId(getChatTable());
	}

	/**
	 * DB中的最新sequenceId
	 */
	public int getDBMinId()
	{
		return DBManager.getInstance().getMinDBSeqId(getChatTable());
	}

	/**
	 * 能否显示新消息数量提示
	 */
	public boolean canLoadAllNew()
	{
		return getNewMsgCount() > ChannelManager.LOAD_ALL_MORE_MIN_COUNT && getNewMsgActualCount() > 0 && !isNotInitedInDB()
				&& !isLoadingAllNew && !hasLoadingAllNew;
	}
	
	public boolean hasAtMeMsg()
	{
		if(isLoadingEarliestAtMeMsg)
			return false;
		else
			return hasAtMeMsgInChannel();
	}
	
	public boolean hasUnreadCountrySysMsg()
	{
		if(unreadSystemMsgList!=null && unreadSystemMsgList.size()>0)
			return true;
		else
		{
//			int minCreateTime = getMinCreateTime();
//			int earliestAtMeMsgTime =  getEarliestAtMeMsgCreateTime(minCreateTime);
//			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "maxCreateTime", minCreateTime, "earliestAtMeMsgTime", earliestAtMeMsgTime);
//			return earliestAtMeMsgTime > 0 && earliestAtMeMsgTime < minCreateTime;
			return false;
		}
	}
	
	public boolean hasAtMeMsgInChannel()
	{
		if(isCountryOrAllianceChannel())
		{
			if(atMeMsgList!=null && atMeMsgList.size()>0)
				return true;
			else
			{
				int minCreateTime = getMinCreateTime();
				int earliestAtMeMsgTime =  getEarliestAtMeMsgCreateTime(minCreateTime);
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "maxCreateTime", minCreateTime, "earliestAtMeMsgTime", earliestAtMeMsgTime);
				return earliestAtMeMsgTime > 0 && earliestAtMeMsgTime < minCreateTime;
			}
		}
		return false;
	}

	/**
	 * channel表的seqId字段尚未被初始化
	 */
	public boolean isNotInitedInDB()
	{
		return prevDBMaxSeqId <= 0;
	}

	/**
	 * 服务器有而本地没有的最早id
	 */
	public int getServerNewestId()
	{
		int dbMaxId = getDBMaxId();
		return Math.min(serverMinSeqId, dbMaxId);
	}

	//

	/**
	 * 未收取的新消息的最小id
	 */
	public int getNewMsgMinSeqId()
	{
		if (isNotInitedInDB())
			return serverMaxSeqId;

		return serverMinSeqId > prevDBMaxSeqId ? serverMinSeqId : (prevDBMaxSeqId + 1);
	}

	/**
	 * 未收取的新消息的最大id
	 */
	public int getNewMsgMaxSeqId()
	{
		if (getChannelView() != null)
		{
			return getChannelView().chatChannel.getMinSeqId() - 1;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * 收取前尚未加载的新消息数（可能会因为serverMaxSeqId而变化？）
	 */
	public int getNewMsgCount()
	{
		return serverMaxSeqId - getNewMsgMinSeqId() + 1;
	}

	/**
	 * 当前尚未加载的新消息数（除去已加载的）
	 */
	public int getNewMsgActualCount()
	{
		return getNewMsgMaxSeqId() - getNewMsgMinSeqId() + 1;
	}

	/**
	 * 找到指定section在服务器中的交集数量
	 */
	public int getServerSectionCount(int upperId, int lowerId)
	{
		if (serverMinSeqId == -1 && serverMaxSeqId == -1)
		{
			return 0;
		}
		int minId = Math.min(upperId, lowerId);
		int maxId = Math.max(upperId, lowerId);
		int upper = Math.min(maxId, serverMaxSeqId);
		int lower = Math.max(minId, serverMinSeqId);
		return upper - lower + 1;
	}

	public ChatTable getChatTable()
	{
		return ChatTable.createChatTable(channelType, channelID);
	}

	public void setChannelView(ChannelView v)
	{
		channelView = v;
	}

	public ChannelView getChannelView()
	{
		return channelView;
	}

	public static String getMembersString(ArrayList<String> members)
	{
		String uidsStr = "";
		if (members == null)
			return uidsStr;

		for (int i = 0; i < members.size(); i++)
		{
			try
			{
				uidsStr += (i > 0 ? "|" : "") + members.get(i);
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
		return uidsStr;
	}

	public void setMember(boolean isMember)
	{
		this.isMember = isMember;
	}

	public boolean isMember()
	{
		return isMember;
	}

	public boolean getNoMoreDataFlag(int index)
	{
		return serverMinSeqId <= getViewMinId();
	}

	public boolean containCurrentUser()
	{
		// 已经退出的国家
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
		{
			if(StringUtils.isNotEmpty(""+UserManager.getInstance().getCurrentUser().crossFightSrcServerId))
				return channelID.equals(""+UserManager.getInstance().getCurrentUser().crossFightSrcServerId);
			else
				return channelID.equals(""+UserManager.getInstance().getCurrentUser().serverId);
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && !channelID.equals(""+UserManager.getInstance().getCurrentUser().serverId))
		{
			return false;
		}
		// 已经退出的联盟
		else if ((channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS) && !channelID.equals(UserManager.getInstance().getCurrentUser().allianceId))
		{
			return false;
		}
		// 已经退出的聊天室
		else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && !isMember)
		{
			return false;
		}
		return true;
	}

	private boolean isInMailDataList(MailData mailData)
	{
		if (mailUidList != null && mailUidList.contains(mailData.getUid()))
			return true;
		return false;
	}
	
	public void addInLoadMailUidList(String mailUid)
	{
		if (mailUidList != null && StringUtils.isNotEmpty(mailUid) && !mailUidList.contains(mailUid))
			mailUidList.add(mailUid);
	}

	public void addNewMailData(final MailData mailData)
	{
		if (!mailData.isUserMail() && !isInMailDataList(mailData))
		{
			mailDataList.add(mailData);
			if (mailUidList != null)
				mailUidList.add(mailData.getUid());
			mailData.channel = this;
			SortUtil.getInstance().refreshNewMailListOrder(mailDataList);
			if(ChatServiceController.getCurrentActivity()!=null)
			{
				ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
				{
					
					@Override
					public void run()
					{
						ChannelListFragment.onMailDataAdded(mailData);
//						refreshRenderData();
					}
				});
			}
		}
	}

	private boolean isInMsgList(MsgItem msg)
	{
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).msg.equals(msg.msg) && msgList.get(i).createTime == msg.createTime)
			{
				return true;
			}
		}
		return false;
	}

	private boolean isInUserMailList(MsgItem msg)
	{
		if (msg != null && StringUtils.isNotEmpty(msg.mailId))
		{
			for (int i = 0; i < msgList.size(); i++)
			{
				if (StringUtils.isNotEmpty(msgList.get(i).mailId) && msgList.get(i).mailId.equals(msg.mailId))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean addHistoryMsg(MsgItem msg)
	{
		boolean channelTypeRestrict = ((channelType == DBDefinition.CHANNEL_TYPE_COUNTRY 
				|| channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				&& (msg.channelType == channelType) || (channelType != DBDefinition.CHANNEL_TYPE_COUNTRY && channelType != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)) ;
		if (channelTypeRestrict && !isMsgExist(msg) && !isMsgIgnored(msg) && !isMsgHide(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			if (msg.channelType != DBDefinition.CHANNEL_TYPE_USER && firstNewMsgSeqId > 0 && firstNewMsgSeqId == msg.sequenceId)
			{
				if (this.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT)
				{
					msg.firstNewMsgState = 1;
				}
				else
				{
					msg.firstNewMsgState = 2;
				}
			}
			
			addMsg(msg);
			return true;
		}
		else
			return false;
	}
	
	private boolean chatroomHeadImagesLoading	= false;
	public ConcurrentHashMap<String, Bitmap>	chatroomHeadImages;
	public int									customPicLoadingCnt;
	public Bitmap								chatRoomBitmap;
	private boolean 							chatRoomImageinited = false;
	
	public void initChatRoomImage()
	{
		if(chatRoomImageinited)
			return;
		refreshChatRoomChannelImage();
	}
	
	public void refreshChatRoomChannelImage()
	{
		if(channelType != DBDefinition.CHANNEL_TYPE_CHATROOM || chatroomHeadImagesLoading
				|| memberUidArray == null || memberUidArray.size() == 0)
			return;
		
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelID",channelID);
		
		if(chatroomHeadImages == null)
			chatroomHeadImages = new ConcurrentHashMap<String, Bitmap>();
		else
			chatroomHeadImages.clear();
		customPicLoadingCnt = 0;
		chatroomHeadImagesLoading = true;
		
		List<UserInfo> users = new ArrayList<UserInfo>();
		for (int i = 0; i < memberUidArray.size(); i++)
		{
			UserInfo user = UserManager.getInstance().getUser(memberUidArray.get(i));
			if (user != null)
				users.add(user);
			if (users.size() >= 9)
				break;
		}
		
		for (int i = 0; i < users.size(); i++)
		{
			final UserInfo user = users.get(i);

			Bitmap predefinedHeadImage = BitmapFactory.decodeResource(ChatServiceController.hostActivity.getResources(),
					ImageUtil.getHeadResId(ChatServiceController.hostActivity, user.headPic));
			
			if (predefinedHeadImage != null)
				chatroomHeadImages.put(user.uid, predefinedHeadImage);

			if (user.isCustomHeadImage())
			{
				customPicLoadingCnt++;
				if(ChatServiceController.hostActivity!=null)
				{
					ChatServiceController.hostActivity.runOnUiThread(new Runnable()
					{
						
						@Override
						public void run()
						{
							ImageUtil.getDynamicPic(user.getCustomHeadPicUrl(), user.getCustomHeadPic(), new ImageLoaderListener()
							{
								@Override
								public void onImageLoaded(final Bitmap bitmap)
								{
									onCustomImageLoaded(user.uid, bitmap);
								}
							});
						}
					});
				}
				
			}
		}
		if (customPicLoadingCnt == 0)
			generateCombinePic();
	}
	
	private synchronized void onCustomImageLoaded(String uid, final Bitmap bitmap)
	{
		if (bitmap != null)
			chatroomHeadImages.put(uid, bitmap);
		customPicLoadingCnt--;
		if (customPicLoadingCnt == 0)
			generateCombinePic();
	}
	
	private void generateCombinePic()
	{
		chatroomHeadImagesLoading = false;

		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		Set<String> keySet = chatroomHeadImages.keySet();
		for (String key : keySet)
		{
			if (StringUtils.isNotEmpty(key) && chatroomHeadImages.get(key) != null)
				bitmaps.add(chatroomHeadImages.get(key));
		}
		
		try
		{
			chatRoomBitmap = CombineBitmapManager.getInstance().getCombinedBitmap(bitmaps);
			if (chatRoomBitmap != null && StringUtils.isNotEmpty(channelID) && getChatroomHeadPicPath() != null)
			{
				BitmapUtil.saveMyBitmap(chatRoomBitmap, getChatroomHeadPicPath(), getChatroomHeadPicFile(channelID));

				if (isMemberUidChanged)
				{
					isMemberUidChanged = false;
					String fileName = getChatroomHeadPicPath() + getChatroomHeadPicFile(channelID);
					AsyncImageLoader.removeMemoryCache(fileName);
				}
			}
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public String getChatroomHeadPicPath()
	{
		return DBHelper.getHeadDirectoryPath(ChatServiceController.hostActivity) + "chatroom/";
	}

	public String getChatroomHeadPicFile(String channelId)
	{
		return channelId;
	}

	private boolean isMsgExist(MsgItem msg)
	{
		if (msg.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			return isInUserMailList(msg);
		}
		else
		{
			return isInMsgList(msg);
		}
	}

	private boolean isMsgIgnored(MsgItem msg)
	{
		if (msg.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			return false;
		}
		else
		{
			return !WebSocketManager.isWebSocketEnabled() && msg.sequenceId == -1;
		}
	}
	
	private boolean isMsgHide(MsgItem msg)
	{
		return ChatServiceController.isAllianceTreasureHiden() && msg.isAllianceTreasureMessage() && !msg.isNewMsg;
	}

	public void addMsg(MsgItem msg)
	{
		msg.initNullField();
		addMsgAndSort(msg);
		if(msg!=null && !msg.isSelfMsg())
		{
			if(isAllianceOrAllianceSysChannel())
				UserManager.getInstance().addAllianceLoadUid(msg.uid);
			else if(isCountryOrCountrySysChannel())
				UserManager.getInstance().addCountryLoadUid(msg.uid);
		}
		initMsg(msg);
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则可能新发的消息会插到前面
	 */
	public void addDummyMsg(MsgItem msg)
	{
		msgList.add(msg);
		initMsg(msg);
	}

	/**
	 * 由于后台返回的createTime与前台不一样（通常慢几秒），不能按时间排序插入，否则会错乱
	 */
	public void replaceDummyMsg(MsgItem msg, int index)
	{
		if(index >= 0 && index < msgList.size())
			msgList.set(index, msg);
		initMsg(msg);
	}

	private void initMsg(MsgItem msg)
	{
		msg.chatChannel = this;
		refreshRenderData();
	}

	private void addMsgAndSort(MsgItem msg)
	{
		int pos = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msg.createTime > msgList.get(i).createTime
					|| (msg.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && msg.createTime == msgList.get(i).createTime && msg.sequenceId > msgList
							.get(i).sequenceId))
			{
				pos = i + 1;
			}
			else
			{
				break;
			}
		}
		msgList.add(pos, msg);
		if(!msg.isSelfMsg() && (msg.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE 
				|| msg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) 
				&& msg.isAtMeMsg && msg.readStatus !=1 && !atMeMsgList.contains(msg))
			atMeMsgList.add(msg);
		if(unreadSystemMsgList!=null)
		{
			int size = unreadSystemMsgList.size();
			if(!msg.isSelfMsg() && ChatServiceController.countrySysChannelEnable && 
					(msg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
					|| msg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) 
					&& msg.isNewCountrySystem() && msg.readStatus != 1 
					&& !unreadSystemMsgList.contains(msg))
			{
				unreadSystemMsgList.add(msg);
				if(size == 0 && ChatServiceController.getChatFragment()!=null && ChatServiceController.isInNormalOrSysCountryChannel())
					ChatServiceController.getChatFragment().showNewSystemAnimation();
			}
		}
		
		
		hasMsgInDBStatus = 1;
	}

	public boolean addNewMsg(MsgItem msg)
	{
		boolean channelTypeRestrict = ((channelType == DBDefinition.CHANNEL_TYPE_COUNTRY 
				|| channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)
				&& (msg.channelType == channelType) || (channelType != DBDefinition.CHANNEL_TYPE_COUNTRY && channelType != DBDefinition.CHANNEL_TYPE_BATTLE_FIELD)) ;
		if (channelTypeRestrict && !isMsgExist(msg) && !isMsgIgnored(msg) && !UserManager.getInstance().isInRestrictList(msg.uid, UserManager.BLOCK_LIST))
		{
			addMsg(msg);
			if (isModChannel())
			{
				ChannelManager.getInstance().latestModChannelMsg = msg.msg;
				ChatChannel modChannel = ChannelManager.getInstance().getModChannel();
				if (modChannel != null && unreadCount == 1)
					modChannel.unreadCount++;
			}
			else if (isMessageChannel())
			{
				ChannelManager.getInstance().latestMessageChannelMsg = msg.msg;
				ChatChannel messageChannel = ChannelManager.getInstance().getMessageChannel();
				if (messageChannel != null && unreadCount == 1)
					messageChannel.unreadCount++;
			}
			else if(isDriftingBottleChannel())
			{
				ChannelManager.getInstance().latestDrfitingBottleChannelMsg = msg.msg;
				ChatChannel driftingBottleChannel = ChannelManager.getInstance().getDriftingBottleChannel();
				if (driftingBottleChannel != null && unreadCount == 1)
					driftingBottleChannel.unreadCount++;
			}
			else if(isNearbyChannel())
			{
				ChannelManager.getInstance().latestNearbyChannelMsg = msg.msg;
				ChatChannel nearbyChannel = ChannelManager.getInstance().getNearbyChannel();
				if (nearbyChannel != null && unreadCount == 1)
					nearbyChannel.unreadCount++;
			}
			return true;
		}
		return false;
	}

	public void clearFirstNewMsg()
	{
		if (msgList == null || (WebSocketManager.isRecieveFromWebSocket(channelType) || (WebSocketManager.isWebSocketEnabled() && isNearbyChannel()))  && wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
		{
			return;
		}

		firstNewMsgSeqId = 0;
		for (int i = 0; i < msgList.size(); i++)
		{
			MsgItem mi = msgList.get(i);
			if (mi != null)
			{
				mi.firstNewMsgState = 0;
			}  
		}
		
	}

	public boolean hasReward()
	{
		for (Iterator<MailData> iterator = mailDataList.iterator(); iterator.hasNext();)
		{
			MailData mailData = (MailData) iterator.next();
			if (mailData.hasReward())
				return true;
		}
		return false;
	}

	public List<String> getChannelRewardUidArray()
	{
		List<String> rewardUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.hasReward() && StringUtils.isNotEmpty(mailData.getUid()) && !rewardUidArray.contains(mailData.getUid()))
					{
						rewardUidArray.add(mailData.getUid());
					}
				}
			}
		}
		return rewardUidArray;
	}

	public List<String> getChannelUnreadUidArray()
	{
		List<String> unReadUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.isUnread() && StringUtils.isNotEmpty(mailData.getUid()) && !unReadUidArray.contains(mailData.getUid()))
					{
						unReadUidArray.add(mailData.getUid());
					}
				}
			}
		}
		return unReadUidArray;
	}

	public List<String> getChannelDeleteUidArray()
	{
		List<String> deleteUidArray = new ArrayList<String>();
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE  || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				  || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			return deleteUidArray;
		if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			if (isMainMsgChannel() && StringUtils.isNotEmpty(latestId))
			{
				deleteUidArray.add(latestId);
			}
			else
			{
				if (msgList != null && msgList.size() > 0)
				{
					MsgItem lastItem = msgList.get(0);
					for (int i = 1; i < msgList.size(); i++)
					{
						MsgItem item = msgList.get(i);
						if (item.createTime > lastItem.createTime)
							lastItem = item;
					}
					deleteUidArray.add(lastItem.mailId);
				}
			}
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.canDelete() && !mailData.getUid().equals("") && !deleteUidArray.contains(mailData.getUid()))
					{
						deleteUidArray.add(mailData.getUid());
					}
				}
			}
		}
		return deleteUidArray;
	}

	public boolean cannotOperatedForMuti(int type)
	{
		boolean ret = false;
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if ((type == ChannelManager.OPERATION_DELETE_MUTI && !mailData.canDelete())
							|| (type == ChannelManager.OPERATION_REWARD_MUTI && mailData.hasReward()))
					{
						ret = true;
						break;
					}
				}
			}
		}
		if (type == ChannelManager.OPERATION_REWARD_MUTI)
			ret = !ret;
		return ret;
	}

	public String getChannelRewardTypes()
	{
		String types = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.hasReward() && mailData.getType() > 0 && !types.contains("" + mailData.getType()))
					{
						if (types.equals(""))
							types += mailData.getType();
						else
							types += ("," + mailData.getType());
					}
				}
			}
		}
		return types;
	}

	public String getChannelDeleteTypes()
	{
		String types = "";
		if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE || channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				 || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
			return types;
		if (channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			types = "0";
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
		{
			if (hasSysMailInList())
			{
				for (int i = 0; i < mailDataList.size(); i++)
				{
					MailData mailData = mailDataList.get(i);
					if (mailData.canDelete() && mailData.getType() > 0 && !types.contains("" + mailData.getType()))
					{
						if (types.equals(""))
							types += mailData.getType();
						else
							types += ("," + mailData.getType());
					}
				}
			}
		}
		return types;
	}

	public int getMinCreateTime()
	{
		return getEdgeCreateTime(true);
	}

	public int getMaxCreateTime()
	{
		return getEdgeCreateTime(false);
	}

	public int getIndexByCreateTime(int time)
	{
		if (msgList == null)
			return -1;

		for (int i = 0; i < msgList.size(); i++)
		{
			if (time == msgList.get(i).createTime)
			{
				return i;
			}
		}
		return -1;
	}

	private int getEdgeCreateTime(boolean isMin)
	{
		if (msgList == null || msgList.size() == 0)
			return 0;
		if(isMin)
			return msgList.get(0).createTime;
		else
			return msgList.get(msgList.size()-1).createTime;

//		int result = msgList.get(0).createTime;
//		for (int i = 0; i < msgList.size(); i++)
//		{
//			if (isMin ? (msgList.get(i).createTime < result) : (msgList.get(i).createTime > result))
//			{
//				result = msgList.get(i).createTime;
//			}
//		}
//		return result;
	}

	public int getMinSeqId()
	{
		if (msgList == null || msgList.size() == 0)
			return 0;

		int result = msgList.get(0).sequenceId;
		for (int i = 0; i < msgList.size(); i++)
		{
			if (msgList.get(i).sequenceId < result)
			{
				result = msgList.get(i).sequenceId;
			}
		}
		return result;
	}

	public boolean isUnread()
	{
		return unreadCount > 0;
	}

	public long getChannelTime()
	{
		return latestTime;
	}

	public void updateMailList(MailData mailData)
	{
		if (mailData == null || mailDataList == null)
			return;
		for (int i = 0; i < mailDataList.size(); i++)
		{
			MailData mail = mailDataList.get(i);
			if (mail != null && mail.getUid().equals(mailData.getUid()))
			{
				if (StringUtils.isNotEmpty(mailData.nameText))
				{
					mail.nameText = mailData.nameText;
				}
				if (StringUtils.isNotEmpty(mailData.contentText))
				{
					mail.contentText = mailData.contentText;
				}
				break;
			}
		}
	}

	public MailData getLatestMailData()
	{
		if (StringUtils.isEmpty(latestId))
		{
			String latestMailId = DBManager.getInstance().getSysMailChannelLatestId(channelID);
			if (StringUtils.isNotEmpty(latestMailId))
			{
				latestId = latestMailId;
			}
		}

		if (StringUtils.isNotEmpty(latestId))
		{
			MailData mail = DBManager.getInstance().getSysMailByID(latestId);
			if (mail != null)
			{
				return mail;
			}
		}
		else
		{
			if (hasSysMailInList())
			{
				MailData mail = mailDataList.get(mailDataList.size() - 1);
				if (mail != null)
				{
					return mail;
				}
			}
		}
		return null;
	}

	public void markAsRead()
	{
		if (unreadCount > 0)
		{
			unreadCount = 0;
			latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
			ChannelManager.getInstance().calulateAllChannelUnreadNum();
			DBManager.getInstance().updateChannel(this);
			if(channelType == DBDefinition.CHANNEL_TYPE_CHATROOM && ChannelManager.getInstance().hasNewestUserChat(channelID))
				ChannelManager.getInstance().getNewUserChatChannelId();
			ChannelManager.getInstance().postMainChannelChangedToGame(this);
			if(isNearbyChannel())
			{
				ChatChannel channel = ChannelManager.getInstance().getNearbyChannel();
				if(channel!=null)
				{
					channel.unreadCount -= 1;
					if(channel.unreadCount < 0)
						channel.unreadCount = 0;
				}
			}
		}
	}
	
	public void hideSpecialMsg()
	{
		if(ChatServiceController.isAllianceTreasureHiden() && msgList!=null && msgList.size() >0 && hideMsgItemList!=null && hideMsgItemList.size()>0)
		{
			for(int i=0;i<hideMsgItemList.size();i++)
			{
				MsgItem item = hideMsgItemList.get(i);
				if(item!=null && msgList.contains(item))
				{
					msgList.remove(item);
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"hide item:"+item.msg);
				}
			}
		}
	}

	public MailData getMonsterMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_MONSTER) && hasSysMailInList())
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<MonsterMailContents> monsterArray = new ArrayList<MonsterMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof MonsterMailData)
				{
					MonsterMailData monsterMail = (MonsterMailData) mailData;
					if (monsterMail.isUnread())
						unreadCount++;

					if (monsterMail.getMonster() == null || monsterMail.getMonster().size() <= 0)
						continue;
					MonsterMailContents monster = monsterMail.getMonster().get(0);
					if (monster != null && !monsterArray.contains(monster))
						monsterArray.add(monster);
				}
			}

			MonsterMailData newMail = new MonsterMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setMonster(monsterArray);
			return newMail;
		}
		return null;
	}
	
	public MailData getNewWorldBossMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS) && hasSysMailInList())
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<NewWorldBossMailContents> worldBossArray = new ArrayList<NewWorldBossMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof NewWorldBossMailData)
				{
					NewWorldBossMailData newWorldBossMail = (NewWorldBossMailData) mailData;
					if (newWorldBossMail.isUnread())
						unreadCount++;

					if (newWorldBossMail.getWorldbosslist() == null || newWorldBossMail.getWorldbosslist().size() <= 0)
						continue;
					NewWorldBossMailContents worldBoss = newWorldBossMail.getWorldbosslist().get(0);
					if (worldBoss != null && !worldBossArray.contains(worldBoss))
						worldBossArray.add(worldBoss);
				}
			}

			NewWorldBossMailData newMail = new NewWorldBossMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setWorldbosslist(worldBossArray);
			return newMail;
		}
		return null;
	}

	public MailData getResourceMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_RESOURCE) && hasSysMailInList())
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<ResourceMailContents> collectArray = new ArrayList<ResourceMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof ResourceMailData)
				{
					ResourceMailData resourceMail = (ResourceMailData) mailData;
					if (resourceMail.isUnread())
						unreadCount++;

					if (resourceMail.getCollect() == null || resourceMail.getCollect().size() <= 0)
						continue;
					ResourceMailContents resource = resourceMail.getCollect().get(0);
					if (resource != null && !collectArray.contains(resource))
						collectArray.add(resource);
				}
			}

			ResourceMailData newMail = new ResourceMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setCollect(collectArray);
			return newMail;
		}
		return null;
	}

	public MailData getKnightMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_KNIGHT) && hasSysMailInList())
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			boolean isLock = false;
			List<BattleMailContents> knightArray = new ArrayList<BattleMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;

				if (!isLock && mailData.isLock())
					isLock = true;

				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof BattleMailData)
				{
					BattleMailData knightMail = (BattleMailData) mailData;
					if (knightMail.isUnread())
						unreadCount++;

					if (knightMail.getKnight() == null || knightMail.getKnight().size() <= 0)
						continue;
					BattleMailContents knight = knightMail.getKnight().get(0);
					if (knight != null && !knightArray.contains(knight))
						knightArray.add(knight);
				}
			}

			BattleMailData newMail = new BattleMailData();
			newMail.setMailData(mail);
			newMail.setIsKnightMail(true);
			newMail.setHasParseForKnight(true);
			newMail.setSave(isLock ? 1 : 0);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setKnight(knightArray);
			newMail.setContents("");
			newMail.setDetail(null);
			return newMail;
		}
		return null;
	}

	public MailData getResourceHelpMailData()
	{
		if (StringUtils.isNotEmpty(channelID) && channelID.equals(MailManager.CHANNELID_RESOURCE_HELP) && hasSysMailInList())
		{
			MailData mail = mailDataList.get(0);
			if (mail == null)
				return null;

			int unreadCount = 0;
			List<ResourceHelpMailContents> collectArray = new ArrayList<ResourceHelpMailContents>();
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mailData = mailDataList.get(i);
				if (mailData == null)
					continue;
				if (!mailData.hasMailOpend)
				{
					mailData.setNeedParseByForce(true);
					mailData = MailManager.getInstance().parseMailDataContent(mailData);
				}
				if (mailData instanceof ResourceHelpMailData)
				{
					ResourceHelpMailData resourceHelpMail = (ResourceHelpMailData) mailData;
					if (resourceHelpMail.isUnread())
						unreadCount++;

					if (resourceHelpMail.getCollect() == null || resourceHelpMail.getCollect().size() <= 0)
						continue;
					ResourceHelpMailContents resourceHelp = resourceHelpMail.getCollect().get(0);
					if (resourceHelp != null && !collectArray.contains(resourceHelp))
						collectArray.add(resourceHelp);
				}
			}

			ResourceHelpMailData newMail = new ResourceHelpMailData();
			newMail.setMailData(mail);
			newMail.setTotalNum(DBManager.getInstance().getSysMailCountByTypeInDB(mail.getChannelId()));
			newMail.setUnread(unreadCount);
			newMail.setCollect(collectArray);
			return newMail;
		}
		return null;
	}

	public void setUnreadCount(int count)
	{
		unreadCount = count;
		DBManager.getInstance().updateChannel(this);
	}

	public boolean isCountryChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY;
	}
	
	public boolean isBattleChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
	}

	public boolean isAllianceChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE;
	}
	
	public boolean isAllianceOrAllianceSysChannel()
	{
		return isAllianceChannel() || isAllianceSysChannel();
	}
	
	public boolean isCountryOrCountrySysChannel()
	{
		return isCountryChannel() || isCountrySysChannel();
	}
	
	public boolean isCountryOrAllianceChannel()
	{
		return isCountryOrCountrySysChannel() || isAllianceOrAllianceSysChannel();
	}
	
	public boolean isInChatChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY 
				|| channelType==DBDefinition.CHANNEL_TYPE_RANDOM_CHAT
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE  
				|| channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS
				|| channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS
				|| channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
	}
	
	public boolean isAllianceSysChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS;
	}
	
	public boolean isCountrySysChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS;
	}
	
	public boolean isCustomChannelNeedSendLatestMsg()
	{
		LocalConfig localConfig = ConfigManager.getInstance().getLocalConfig();
		return isUserChannelType() && localConfig != null && channelType == localConfig.getCustomChannelType()
				&& StringUtils.isNotEmpty(localConfig.getCustomChannelId()) && channelID.equals(localConfig
				.getCustomChannelId());
	}

	public boolean isModChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID) 
				&& channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& !channelID.equals(MailManager.CHANNELID_MOD);
	}

	public boolean isMessageChannel()
	{
		return (channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID) && isNotMainMsgChannel()
				&& !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD) 
				&& !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE)
				&& !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
				|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}
	
	public boolean isDriftingBottleChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER  && StringUtils.isNotEmpty(channelID) 
				&& channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE)
				&& !channelID.equals(MailManager.CHANNELID_DRIFTING_BOTTLE);
	}
	
	public boolean canUserChannelShow()
	{
		return hasSwitchUserChannel() || isNotSwitchUserChat();
	}
	
	public boolean hasSwitchUserChannel()
	{
		return (isDriftingBottleChannel() && MailManager.isDriftingBottleEnable) 
				|| (isNearbyChannel() && MailManager.nearbyEnable);
	}
	
	public boolean isNotSwitchUserChat()
	{
		return !isDriftingBottleChannel() && !isNearbyChannel();
	}
	
	public boolean isNearbyChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER  && StringUtils.isNotEmpty(channelID) 
				&& channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY)
				&& !channelID.equals(MailManager.CHANNELID_NEAR_BY);
	}
	
	public boolean isTempDriftBottleChannel()
	{
		return StringUtils.isNotEmpty(settings) && settings.equals("0");
	}
	
	public boolean isUserChannelType()
	{
		return ChannelManager.isUserChannelType(channelType) && isNotMainMsgChannel();
	}

	public boolean isUserOrChatRoomChannel()
	{
		return (channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID) && isNotMainMsgChannel()) 
				|| channelType == DBDefinition.CHANNEL_TYPE_CHATROOM;
	}

	public boolean isUserMailChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && !channelID.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)
				&& DBManager.getInstance().isUserMailExistDifferentType(getChatTable());
	}
	
	public boolean isUserChatChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID) &&
				isNotMainMsgChannel();
	}

	public String getLatestId()
	{
		if (StringUtils.isNotEmpty(channelID))
			return DBManager.getInstance().getSysMailChannelLatestId(channelID);
		return "";
	}

	public long getLatestTime()
	{
		if (StringUtils.isNotEmpty(channelID))
		{
			if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL)
			{
				return DBManager.getInstance().getSysMailChannelLatestTime(channelID);
			}
			else
			{
				return DBManager.getInstance().getChatLatestTime(getChatTable());
			}
		}
		return 0;
	}

	public boolean hasNoItemInChannel()
	{
		if (ChatServiceController.isNewMailUIEnable
				&& ((channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && StringUtils.isNotEmpty(channelID) && !isDialogChannel())
				|| (channelType == DBDefinition.CHANNEL_TYPE_USER && isMainMsgChannel())))
			return false;
		boolean ret = false;
		if (ChannelManager.isUserChannelType(channelType) && !hasMsgItemInDB())
		{
			ret = true;
		}
		else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && !hasMailDataInDB())
		{
			ret = true;
		}
		return ret;
	}

	public boolean hasSysMailInList()
	{
		return mailDataList != null && mailDataList.size() > 0;
	}

	public void clearAllSysMail()
	{
		if (hasSysMailInList())
		{
			boolean hasDetectMail = false;
			for (int i = 0; i < mailDataList.size(); i++)
			{
				MailData mail = mailDataList.get(i);
				if (mail != null && StringUtils.isNotEmpty(mail.getUid()))
				{
					DBManager.getInstance().deleteSysMail(this, mail.getUid());
					if (!hasDetectMail && mail.getType() == MailManager.MAIL_DETECT_REPORT)
						hasDetectMail = true;
				}
			}
			if (hasDetectMail)
				DBManager.getInstance().getDetectMailInfo();
			mailDataList.clear();
			mailUidList.clear();
		}

		unreadCount = 0;
		ChannelListFragment.onChannelRefresh();
		latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
		calculateSysMailCountInDB = false;
		calculateUnreadSysMailCountInDB = false;
		ChannelManager.getInstance().deleteChannel(this);
	}

	private boolean		initLoaded	= false;

	public void resetMsgChannel()
	{
		initLoaded = false;
		if (msgList != null && msgList.size() > 0)
			msgList.clear();
		if(atMeMsgList!=null && atMeMsgList.size() > 0)
			atMeMsgList.clear();
		if(unreadSystemMsgList!=null && unreadSystemMsgList.size() > 0)
			unreadSystemMsgList.clear();
		if(isAllianceOrAllianceSysChannel())
			UserManager.getInstance().resetAllianceCurrentLoadUid();
		else if(isCountryOrCountrySysChannel())
			UserManager.getInstance().resetCountryCurrentLoadUid();
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "resetMsgChannel", initLoaded);
	}
	
	public void setAtMeMsgReaded(int minCreateTime)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "minCreateTime",minCreateTime);
		if(atMeMsgList!=null && atMeMsgList.size()>0)
		{
			for(MsgItem msg:atMeMsgList)
			{
				if(msg!=null && msg.createTime >= minCreateTime)
					msg.readStatus = 1;
			}
			atMeMsgList.clear();
		}
		DBManager.getInstance().setAtMeMsgReaded(getChatTable(), minCreateTime);
		if(ChatServiceController.getCurrentActivity()!=null)
		{
			ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if(ChatServiceController.getChatFragment()!=null)
						ChatServiceController.getChatFragment().refreshToolTip();
				}
			});
		}
	}
	
	public void removeFromAtMeList(MsgItem item)
	{
		if(atMeMsgList!=null && atMeMsgList.size()>0 && atMeMsgList.contains(item))
			atMeMsgList.remove(item);
	}
	
	public void removeFromCountrySysUnreadList(MsgItem item)
	{
		if(unreadSystemMsgList!=null && unreadSystemMsgList.size()>0 && unreadSystemMsgList.contains(item))
			unreadSystemMsgList.remove(item);
	}

	public void loadMoreMsg()
	{
		initLoaded = true;
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_CORE, "initLoaded", initLoaded);
		ChannelManager.getInstance().loadMoreMsgFromDB(this, -1, -1, getMinCreateTime(), true);
	}

	/**
	 * 聊天型channel已经完成初次加载（从网络或db加载历史消息，收到push消息）
	 */
	public boolean hasInitLoaded()
	{
		return initLoaded == true || msgList.size() > 0;
	}

	public boolean hasMsgItemInDB()
	{
		if(hasMsgInDBStatus == 0)
			hasMsgInDBStatus = DBManager.getInstance().hasMsgItemInTable(getChatTable()) ? 1 : 2;
		return hasMsgInDBStatus == 1;
	}
	
	public int getEarliestAtMeMsgCreateTime(int createTime)
	{
		earliestAtMeMsgCreateTime = DBManager.getInstance().getEarlistAtMeMsgCreateTime(getChatTable(),createTime);
		return earliestAtMeMsgCreateTime;
	}
	
	public void resetHasMsgItemInDB()
	{
		hasMsgInDBStatus = 0;
	}

	public boolean hasMailDataInDB()
	{
		return DBManager.getInstance().hasMailDataInDB(channelID);
	}
	
	public boolean hasMailDataInDBByType(int type)
	{
		return DBManager.getInstance().hasMailDataInDBByType(channelID,type);
	}

	public List<String> getMailUidArrayByConfigType(int configType)
	{
		List<String> uidArray = new ArrayList<String>();
		List<MailData> mailList = DBManager.getInstance().getSysMailFromDB(channelID, configType);
		if (mailList != null && mailList.size() > 0)
		{
			for (int i = 0; i < mailList.size(); i++)
			{
				MailData mailData = mailList.get(i);
				if (StringUtils.isNotEmpty(mailData.getUid()) && !uidArray.contains(mailData.getUid()))
				{
					uidArray.add(mailData.getUid());
				}
			}
		}
		return uidArray;
	}

	public String getMailUidsByConfigType(int configType)
	{
		String uids = "";
		List<String> mailUidArray = getMailUidArrayByConfigType(configType);
		if (mailUidArray != null && mailUidArray.size() > 0)
		{
			uids = ChannelListFragment.getUidsByArray(mailUidArray);
		}
		return uids;
	}
	
	public List<String> getUnreadMailUids()
	{
		if(unreadMailUids == null)
			unreadMailUids = getMailUidArrayByConfigType(DBManager.CONFIG_TYPE_READ);
		return unreadMailUids;
	}
	
	public List<String> getRewardMailUids()
	{
		if(rewardMailUids == null)
			rewardMailUids = getMailUidArrayByConfigType(DBManager.CONFIG_TYPE_REWARD);
		return rewardMailUids;
	}
	
	public void updateMailStatus(MailData mailData)
	{
		if(mailData == null || StringUtils.isEmpty(mailData.getUid()))
			return;
		if(unreadMailUids!=null && unreadMailUids.contains(mailData.getUid()) && !mailData.isUnread())
			unreadMailUids.remove(mailData.getUid());
		
		if(rewardMailUids!=null && rewardMailUids.contains(mailData.getUid()) && StringUtils.isNotEmpty(mailData.getRewardId()) && mailData.getRewardStatus() == 1)
			rewardMailUids.remove(mailData.getUid());
	}

	public void getTimeNeedShowMsgIndex()
	{
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL && msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
				msgTimeIndexArray = new ArrayList<Integer>();
			else
				msgTimeIndexArray.clear();
			int tempCreateTime = 0;
			for (int i = 0; i < msgList.size(); i++)
			{
				MsgItem msgItem = msgList.get(i);
				if (msgItem.createTime - tempCreateTime > 5 * 60)
				{
					tempCreateTime = msgItem.createTime;
					msgTimeIndexArray.add(Integer.valueOf(i));
				}
			}
		}
	}

	public void getLoadedTimeNeedShowMsgIndex(int loadCount)
	{
		if (channelType != DBDefinition.CHANNEL_TYPE_OFFICIAL && msgList != null && msgList.size() > 0)
		{
			if (msgTimeIndexArray == null)
			{
				getTimeNeedShowMsgIndex();
			}
			else
			{
				if (msgTimeIndexArray.size() > 0)
					msgTimeIndexArray.remove(Integer.valueOf(0));
				for (int i = 0; i < msgTimeIndexArray.size(); i++)
				{
					Integer indexInt = msgTimeIndexArray.get(i);
					if (indexInt != null)
					{
						msgTimeIndexArray.set(i, Integer.valueOf(indexInt.intValue() + loadCount));
					}
				}

				int tempCreateTime = 0;
				for (int i = 0; i < msgList.size() && i < loadCount + 1; i++)
				{
					MsgItem msgItem = msgList.get(i);
					if (msgItem.createTime - tempCreateTime > 5 * 60)
					{
						tempCreateTime = msgItem.createTime;
						msgTimeIndexArray.add(Integer.valueOf(i));
					}
				}
			}
		}
	}

	public List<Integer> getMsgIndexArrayForTimeShow()
	{
		return msgTimeIndexArray;
	}

	public void querySysMailCountFromDB()
	{
		sysMailCountInDB = ChannelManager.getInstance().getSysMailDBCount(this);
	}

	public void updateSysMailCountFromDB(int count)
	{
		if (sysMailCountInDB <= 0 || sysMailCountInDB + count < 0)
			querySysMailCountFromDB();
		else
			sysMailCountInDB += count;
	}

	public int getSysMailCountInDB()
	{
		if (!calculateSysMailCountInDB)
			querySysMailCountFromDB();
		return sysMailCountInDB;
	}

	public void queryUnreadSysMailCountFromDB()
	{
		sysUnreadMailCountInDB = ChannelManager.getInstance().getUnreadSysMailDBCount(this);
	}

	public void updateUnreadSysMailCountFromDB(int count)
	{
		if (sysUnreadMailCountInDB <= 0 || sysUnreadMailCountInDB + count < 0)
			queryUnreadSysMailCountFromDB();
		else
			sysUnreadMailCountInDB += count;
	}

	public int getUnreadSysMailCountInDB()
	{
		if (!calculateUnreadSysMailCountInDB)
			queryUnreadSysMailCountFromDB();
		return sysUnreadMailCountInDB;
	}

	public boolean isDialogChannel()
	{
		return StringUtils.isNotEmpty(channelID)
				&& (channelID.equals(MailManager.CHANNELID_RESOURCE) || channelID.equals(MailManager.CHANNELID_KNIGHT) 
						|| channelID.equals(MailManager.CHANNELID_MONSTER) || channelID.equals(MailManager.CHANNELID_NEW_WORLD_BOSS));
	}
	
	public boolean isMainMsgChannel()
	{
		return ChannelManager.isMainMsgChannel(channelID);
	}
	
	public boolean isNotMainMsgChannel()
	{
		return ChannelManager.isNotMsgChannel(channelID);
	}

	public boolean isRewardAllChannel()
	{
		return StringUtils.isNotEmpty(channelID)
				&& (channelID.equals(MailManager.CHANNELID_ALLIANCE) || channelID.equals(MailManager.CHANNELID_STUDIO)
						|| channelID.equals(MailManager.CHANNELID_SYSTEM) || channelID.equals(MailManager.CHANNELID_EVENT));
	}

	@Override
	public int compareTo(ChatChannel another)
	{
		if (another == null)
			return -1;
		else
		{
			if (isMessageChannel() && another.isMessageChannel())
			{
				long timeOffset = another.latestTime - latestTime;
				if (timeOffset > 0)
					return 1;
				if (timeOffset < 0)
					return -1;
				else
					return 0;
			}
			else
				return 0;
		}
	}
	
	public boolean isAllAllianceMailChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(channelID) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId()) && channelID.equals(UserManager.getInstance().getCurrentUserId());
	}
	
	public boolean isRandomChatRoomChannel()
	{
		return channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT;
	}
	
	public boolean needNotSaveDB()
	{
		return isRandomChatRoomChannel();
	}
	
	public MailData getSysMailByUid(String mailUid)
	{
		if(StringUtils.isNotEmpty(mailUid))
		{
			if(mailDataList!=null && mailDataList.size()>0)
			{
				Iterator<MailData> iterator = mailDataList.iterator();
				while(iterator.hasNext())
				{
					MailData mail = iterator.next();
					if(mail!=null && StringUtils.isNotEmpty(mail.getUid()) && mail.getUid().equals(mailUid))
						return mail;
				}
			}
			else
			{
				MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
				if(mail!=null)
				{
					mail = MailManager.getInstance().parseMailDataContent(mail);
					return mail;
				}
				
			}
		}
		return null;
	}

}
