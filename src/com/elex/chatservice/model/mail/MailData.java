package com.elex.chatservice.model.mail;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MailIconName;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.TranslateManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.LogUtil;

public class MailData extends ChannelListItem
{
	private int						type						= -1;
	private long					createTime;
	/** 是否未读，大于1则是 */
	private int						status;
	private int						reply;
	private int						rewardStatus;
	/** 需要读语言文件 */
	private int						itemIdFlag;
	/** 0未保存,1保存,2删除保存过 */
	private int						save;
	private String					uid							= "";
	private String					title						= "";
	private String					contents					= "";
	private String					fromName					= "";
	private String					fromUser						= "";
	private String					rewardId					= "";
	private int						mbLevel						= 0;
	private long					recycleTime					= -1;
	private List<String>			contentsArr;

	private transient String		version						= "";
	public transient String			pic							= "";

	// 运行时属性
	public transient int			tabType						= -1;
	public transient boolean		isAtk;
	public transient String			language					= "";
	public transient int			parseVersion				= -1;
	public transient boolean		needParseByForce			= false;
	/** 邮件是否打开过，对于复杂邮件，如果打开过就不再进行json解析 */
	public transient boolean		hasMailOpend				= false;
	public transient boolean		isKnightMail				= false;
	public transient boolean		isDragonTowerMail			= false;
	public transient boolean		isKnightActivityFinishMail	= false;
	public transient boolean		hasParseForKnight			= false;
	private transient String		recycleReableTime			= "";

	// 显示属性
	public String			nameText					= "";
	public String			contentText					= "";
	public String			mailIcon					= "";
	private int 			channelListItemType			=	-1;
	public transient String			timeText					= "";
	public transient boolean		usePersonalPic				= false;
	public transient ChatChannel	channel						= null;
	public transient String			channelId					= "";

	private static final int		PARSE_VERSION_BASIS			= 1;
	private static final int		CURRENT_PARSE_VERSION		= PARSE_VERSION_BASIS;

	public MailData()
	{
	}

	public List<String> getContentsArr()
	{
		return contentsArr;
	}

	public void setContentsArr(List<String> contentsArr)
	{
		this.contentsArr = contentsArr;
	}


	@JSONField(serialize = false)
	public boolean isParseVersionOld()
	{
		return parseVersion < CURRENT_PARSE_VERSION;
	}
	
	

	@JSONField(serialize = false)
	public boolean isHasParseForKnight()
	{
		return hasParseForKnight;
	}

	@JSONField(serialize = false)
	public void setHasParseForKnight(boolean hasParseForKnight)
	{
		this.hasParseForKnight = hasParseForKnight;
	}

	public void setNeedParseByForce(boolean needParseByForce)
	{
		this.needParseByForce = needParseByForce;
	}

	public void updateParseVersion()
	{
		parseVersion = CURRENT_PARSE_VERSION;
	}

	public void setMailData(MailData mailData)
	{
		type = mailData.type;
		createTime = mailData.createTime;
		status = mailData.status;
		reply = mailData.reply;
		rewardStatus = mailData.rewardStatus;
		itemIdFlag = mailData.itemIdFlag;
		save = mailData.save;
		mbLevel = mailData.mbLevel;
		uid = mailData.uid;
		title = mailData.title;
		contents = mailData.contents;
		fromName = mailData.fromName;
		fromUser = mailData.fromUser;
		rewardId = mailData.rewardId;
		recycleTime = mailData.recycleTime;
		version = mailData.version;
		channelId = mailData.channelId;
		parseVersion = mailData.parseVersion;
		language = mailData.language;
		needParseByForce = mailData.needParseByForce;
		hasMailOpend = mailData.hasMailOpend;
		isKnightMail = mailData.isKnightMail;
		isDragonTowerMail = mailData.isDragonTowerMail;
		isKnightActivityFinishMail = mailData.isKnightActivityFinishMail;
		hasParseForKnight = mailData.hasParseForKnight;
		channelListItemType = mailData.channelListItemType;
	}

	public void setMailDealStatus()
	{
		if(type == MailManager.MAIL_ALLIANCE_OFFICER && StringUtils.isNotEmpty(contents) && contents.contains("\"deal\":0"))
		{
			contents = contents.replace("\"deal\":0", "\"deal\":1");
		}
	}

	/**
	 * 用于从数据库获取消息
	 */
	public MailData(Cursor c)
	{
		try
		{
			type = c.getInt(c.getColumnIndex(DBDefinition.MAIL_TYPE));
			createTime = c.getLong(c.getColumnIndex(DBDefinition.MAIL_CREATE_TIME));
			status = c.getInt(c.getColumnIndex(DBDefinition.MAIL_STATUS));
			reply = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REPLY));
			rewardStatus = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REWARD_STATUS));
			itemIdFlag = c.getInt(c.getColumnIndex(DBDefinition.MAIL_ITEM_ID_FLAG));
			save = c.getInt(c.getColumnIndex(DBDefinition.MAIL_SAVE_FLAG));
			uid = c.getString(c.getColumnIndex(DBDefinition.MAIL_ID));
			channelId = c.getString(c.getColumnIndex(DBDefinition.MAIL_CHANNEL_ID));
			title = c.getString(c.getColumnIndex(DBDefinition.MAIL_TITLE));
			contents = c.getString(c.getColumnIndex(DBDefinition.MAIL_CONTENTS));
			fromName = c.getString(c.getColumnIndex(DBDefinition.MAIL_FROM_NAME));
			fromUser = c.getString(c.getColumnIndex(DBDefinition.MAIL_FROM_USER_ID));
			rewardId = c.getString(c.getColumnIndex(DBDefinition.MAIL_REWARD_ID));
			if (type == MailManager.MAIL_SYSUPDATE)
				version = fromUser;
			nameText = c.getString(c.getColumnIndex(DBDefinition.MAIL_TITLE_TEXT));
			contentText = c.getString(c.getColumnIndex(DBDefinition.MAIL_SUMMARY));
			language = c.getString(c.getColumnIndex(DBDefinition.MAIL_LANGUAGE));
			recycleTime = c.getLong(c.getColumnIndex(DBDefinition.RECYCLE_TIME));
			parseVersion = c.getInt(c.getColumnIndex(DBDefinition.PARSE_VERSION));
			mbLevel = c.getInt(c.getColumnIndex(DBDefinition.MAIL_REWARD_LEVEL));
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	@JSONField(serialize = false)
	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.MAIL_TYPE, type);
		cv.put(DBDefinition.MAIL_CREATE_TIME, createTime);
		cv.put(DBDefinition.MAIL_STATUS, status);
		cv.put(DBDefinition.MAIL_REPLY, reply);
		cv.put(DBDefinition.MAIL_REWARD_STATUS, rewardStatus);
		cv.put(DBDefinition.MAIL_ITEM_ID_FLAG, itemIdFlag);
		cv.put(DBDefinition.MAIL_SAVE_FLAG, save);
		cv.put(DBDefinition.MAIL_ID, uid);
		cv.put(DBDefinition.MAIL_CHANNEL_ID, channelId);
		cv.put(DBDefinition.MAIL_TITLE, title);
		cv.put(DBDefinition.MAIL_CONTENTS, contents);
		cv.put(DBDefinition.MAIL_FROM_NAME, fromName);
		cv.put(DBDefinition.MAIL_FROM_USER_ID, fromUser);
		cv.put(DBDefinition.MAIL_REWARD_ID, rewardId);
		cv.put(DBDefinition.MAIL_TITLE_TEXT, nameText);
		cv.put(DBDefinition.MAIL_SUMMARY, contentText);
		cv.put(DBDefinition.MAIL_LANGUAGE, language);
		cv.put(DBDefinition.PARSE_VERSION, parseVersion);
		cv.put(DBDefinition.MAIL_REWARD_LEVEL, mbLevel);
		cv.put(DBDefinition.RECYCLE_TIME, recycleTime);
		

		return cv;
	}
	
	public String getReableRecycleTime()
	{
		return TimeManager.getRecycleTime(recycleTime);
	}
	
	public long getRecycleTime()
	{
		return recycleTime;
	}

	public void setRecycleTime(long recycleTime)
	{
		this.recycleTime = recycleTime;
	}

	public boolean canDelete()
	{
		boolean ret = true;
		if ((!rewardId.equals("") && rewardStatus == 0) || save == 1)
			ret = false;
		return ret;
	}

	public void parseContents()
	{
		if (type == MailManager.MAIL_SYSUPDATE)
			version = fromUser;
		parseMailCellIcon();
		timeText = TimeManager.getReadableTime(createTime);

		if (needParseContent())
		{
			parseMailTypeTab();
			parseMailName();
			parseContentText();
			language = ConfigManager.getInstance().gameLang;
			updateParseVersion();
		}
	}

	@JSONField(serialize = false)
	public boolean isUserMail()
	{
		if (type <= MailManager.MAIL_USER || type == MailManager.MAIL_Alliance_ALL || type == MailManager.CHAT_ROOM
				|| type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
			return true;
		return false;
	}

	public void parseMailTypeTab()
	{
		if (type >= 0 && type < MailManager.MAIL_TYPE_COUNT)
		{
			if (type == MailManager.MAIL_BATTLE_REPORT || type == MailManager.MAIL_DETECT || type == MailManager.MAIL_DETECT_REPORT
					|| type == MailManager.MAIL_ENCAMP || type == MailManager.MAIL_WORLD_BOSS || type == MailManager.MAIL_ALLIANCE_BOSS)
			{
				tabType = MailManager.MAILTAB_FIGHT;
			}
			else if (type == MailManager.ALL_SERVICE)
			{
				tabType = MailManager.MAILTAB_STUDIO;
			}
			else if (type == MailManager.MAIL_FRESHER || type == MailManager.MAIL_SYSNOTICE || type == MailManager.MAIL_SYSUPDATE)
			{
				tabType = MailManager.MAILTAB_NOTICE;
			}
			else if (type <= MailManager.MAIL_USER || type == MailManager.MAIL_Alliance_ALL || type == MailManager.CHAT_ROOM)
			{
				tabType = MailManager.MAILTAB_USER;
			}
			else if (type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
			{
				if (UserManager.getInstance().getCurrentUser().mGmod == 2 || UserManager.getInstance().getCurrentUser().mGmod == 5)
				{
					tabType = MailManager.MAILTAB_MOD;
				}
				else
				{
					tabType = MailManager.MAILTAB_USER;
				}
			}
			else
			{
				tabType = MailManager.MAILTAB_SYSTEM;
			}
		}
	}

	private void parseMailCellIcon()
	{
		switch (type)
		{
			case MailManager.MAIL_SYSTEM:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_BATTLE_REPORT:
			{
				if (isAtk)
				{
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_VICTORY);
				}
				else
				{
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CITY_DEFENT_VICTORY);
				}
				break;
			}
			case MailManager.MAIL_RESOURCE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_RESOURCE);
				break;
			case MailManager.MAIL_DETECT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_DETECT);
				break;
			case MailManager.MAIL_DETECT_REPORT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_DETECT);
				break;
			case MailManager.MAIL_ENCAMP:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_CAMP);
				break;
			case MailManager.MAIL_FRESHER:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_WOUNDED:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_DIGONG:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.ALL_SERVICE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.WORLD_NEW_EXPLORE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_EXPLORE);
				break;
			case MailManager.MAIL_SYSNOTICE:
				if (StringUtils.isNotEmpty(title) && title.equals("114020"))
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM_VIP);
				else
					mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
				break;
			case MailManager.MAIL_SYSUPDATE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_ANNOUNCEMENT);
				break;
			case MailManager.MAIL_ALLIANCEINVITE:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_JOIN_ALLIANCE);
				break;
			case MailManager.MAIL_ATTACKMONSTER:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_MONSTER);
				break;
			case MailManager.MAIL_NEW_WORLD_BOSS:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.CHANNEL_ICON_NEW_WORLD_BOSS);
				break;
			case MailManager.WORLD_MONSTER_SPECIAL:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_Alliance_ALL:
				mailIcon = pic;
				usePersonalPic = true;
				break;
			case MailManager.MAIL_INVITE_TELEPORT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_MOVE);
				break;
			case MailManager.MAIL_ALLIANCE_KICKOUT:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_KICKEDOUT);
				break;
			case MailManager.MAIL_WORLD_BOSS:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_REPORT);
				break;
			case MailManager.MAIL_ALLIANCE_BOSS:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_REPORT);
				break;
			case MailManager.CHAT_ROOM:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_CHAT_ROOM);
				break;
			case MailManager.MAIL_REFUSE_ALL_APPLY:
				mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_INVITE_REJECTED);
				break;

			default:
				break;
		}

		if (isWorldBossKillRewardMail() || isAllianceBossKillRewardMail())
			mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_BATTLE_REPORT);
		if (mailIcon == null || mailIcon.equals(""))
		{
			mailIcon = MailManager.getInstance().getMailIconByName(MailIconName.MAIL_ICON_SYSTEM);
		}
	}

	public void parseMailName()
	{

		if (type == MailManager.MAIL_RESOURCE_HELP)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_RESOURCEHELP);
		}
		else if (type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_PERSONAL)
		{
			nameText = "[MOD]" + fromName;
		}
		else if (type == MailManager.MAIL_ALLIANCEINVITE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_INVITE);
		}
		else if (type == MailManager.MAIL_ALLIANCE_KICKOUT)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_QUITALLIANCE);
		}
		else if (type == MailManager.MAIL_GIFT)
		{
			if (UserManager.getInstance().getUser(fromUser) != null && !UserManager.getInstance().getUser(fromUser).asn.equals(""))
			{
				nameText = "(" + UserManager.getInstance().getUser(fromUser).asn + ")" + fromName;
			}
			else
			{
				nameText = fromName;
			}
		}
		else if (type == MailManager.MAIL_WORLD_BOSS || isWorldBossKillRewardMail())
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_WORLDBOSS);
		}
		else if (type == MailManager.MAIL_ALLIANCE_BOSS || isAllianceBossKillRewardMail())
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_ALLIANCEBOSS);
		}
		else if (type == MailManager.MAIL_RESOURCE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_RESOURCE);
		}
		else if (type == MailManager.MAIL_FRESHER || type == MailManager.MAIL_SYSTEM)
		{
			if (StringUtils.isNotEmpty(title) && title.equals("115429"))
			{
				if (StringUtils.isNotEmpty(fromName))
					nameText = fromName;
				else
					nameText = LanguageManager.getNPCName();
			}
		}
		else if (type == MailManager.MAIL_REFUSE_ALL_APPLY)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_115464);
		}
		else if (type == MailManager.MAIL_ATTACKMONSTER)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_103715);
		}
		else if (type == MailManager.MAIL_NEW_WORLD_BOSS)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137451);
		}
		else if (type == MailManager.MAIL_DETECT)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MENU_DETECTREPORT);
			if (itemIdFlag == 0 && StringUtils.isNotEmpty(title))
			{
				if (title.equals("1"))
				{
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105523);
				}
				else
				{
					nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105567);
				}
			}
			if(StringUtils.isNotEmpty(title) && title.equals("164181"))
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_164182);
			if(StringUtils.isNotEmpty(title) && title.equals("164550"))
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_164542);
		}
		else if (type == MailManager.ALL_SERVICE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
		}
		else if (type == MailManager.MAIL_SYSUPDATE)
		{
			nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
		}
		else if (type == MailManager.MAIL_ALLIANCE_OFFICER)
		{
			if(StringUtils.isNotEmpty(title))
				nameText = LanguageManager.getLangByKey(title);
		}
		
		if(StringUtils.isNotEmpty(fromName) && StringUtils.isNumeric(fromName) && !fromName.contains("."))
		{
			nameText = LanguageManager.getLangByKey(fromName);
		}

		if (StringUtils.isEmpty(nameText))
		{
			if (tabType == MailManager.MAILTAB_FIGHT)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_FIGHT);
			}
			else if (tabType == MailManager.MAILTAB_MOD)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
			}
			else if (tabType == MailManager.MAILTAB_STUDIO)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_STUDIO);
			}
			else if (tabType == MailManager.MAILTAB_SYSTEM)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.TIP_SYSTEM);
			}
			else if (tabType == MailManager.MAILTAB_NOTICE)
			{
				nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_NOTICE);
			}
		}
		
		if (fromName.equals("3000001") || fromName.equals("3000002"))
			nameText = LanguageManager.getNPCName();

	}

	public void parseContentText()
	{
		if (StringUtils.isNotEmpty(title))
		{
			contentText = title.replaceAll("\n", " ");
			if (title.equals("105734"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_CASTLEMOVE);
			else if (title.equals("138067"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_BATTLE_TITLE);
			else if (title.equals("114010"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NEWPLAYER_MOVECASTAL);
			else if (title.equals("115295"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_INVITE_MOVECASTAL);
			else if (title.equals("114012"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_APPLY);
			else if (title.equals("105718"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ENEMY_KILL);
			else if (title.equals("133017"))
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_REWARD);
			else if (StringUtils.isNumeric(title))
			{
				contentText = LanguageManager.getLangByKey(title);
			}
		}

		if (itemIdFlag == 1)
		{
			String contentArr[] = contents.split("\\|");
			int num = contentArr.length;

			switch (num)
			{
				case 2:
					if (contentArr[0].equals("170147") && title.equals("170146"))
					{
						String name = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { contentArr[1] });
						contentText = LanguageManager.getLangByKey("170146", name);
					}
					else if (contentArr[0].equals("170156") && title.equals("170155"))
					{
						String name = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { contentArr[1] });
						contentText = LanguageManager.getLangByKey("170155", name);
					}
					break;
				case 5:
					if (contentArr[0].equals("115336"))
					{
						String name = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, contentArr[4]);
						contentText = LanguageManager.getLangByKey(contentArr[3], name);
					}
					break;
				case 6:
				{
					String name = "";
					if (contentArr[1].equals("") || contentArr[2].equals(""))
					{
						name = LanguageManager.getLangByKey(LanguageKeys.MAIL_ALLIANCE_CASTLE, contentArr[5]);
						contentText = LanguageManager.getLangByKey(contentArr[4], name);
					}
					// else
					// {
					// 	name = contentArr[1];
					// }
					// contentText = LanguageManager.getLangByKey(contentArr[4], name);
				}
					break;
				default:
					break;
			}
		}

		if (type == MailManager.MAIL_DONATE)
		{
			String str[] = contents.split("\\|");
			if (str.length > 4)
			{
				String userName = str[1];
				if (!str[2].equals(""))
				{
					userName = "(" + str[2] + ")" + userName;
				}
				contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_GIFT, userName);
			}
		}
		else if (type == MailManager.MAIL_DIGONG || type == MailManager.WORLD_NEW_EXPLORE)
		{
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_WORLDEXPLORE);
		}
		else if (type == MailManager.MAIL_WOUNDED)
		{
			contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_WOUNDED);
		}
		else if (type == MailManager.MAIL_DETECT)
		{
			if (itemIdFlag == 1)
			{
				if (!contents.equals("") && contents.length() > 6)
				{
					String content[] = contents.split("\\|");
					if(content.length>=2)
					{
						if (content[0].equals("105554"))
						{
							contentText = LanguageManager.getLangByKey("105554",content[1]);
						}
					}
				}

			}
			else
			{
				if (title.equals("1"))
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105523);
				}
				else
				{
					contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105567);
				}

				String content[] = contents.split("\\|");
				int num = content.length;
				switch (num)
				{
					case 1:
						contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, contents);
						break;
					case 3:
					{
						String name = content[0];
						String type = content[1];
						if (type.equals("1"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137429, name);
						}
						else if (type.equals("2"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137431, name);
						}
						else if (type.equals("3"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_137430, name);
						}
						else if (type.equals("12"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140183, name,
									LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110081));
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140181,
									LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110081));
						}
						else if (type.equals("10"))
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140183, name,
									LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110172));
							nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_140181,
									LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_110172));
						}
						else
						{
							contentText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TITLE_105524, name);
						}
						break;
					}
					default:
						break;
				}

			}
		}
		else if (type == MailManager.MAIL_GIFT)
		{
			contentText = contents;
		}
		else if (type == MailManager.MAIL_ALLIANCE_OFFICER)
		{
			if(StringUtils.isNotEmpty(contents))
			{
				try
				{
					AllianceOfficerMailContents officerInfo = JSON.parseObject(contents, AllianceOfficerMailContents.class);
					if (officerInfo != null && StringUtils.isNotEmpty(officerInfo.getDialogId()))
					{
						String officer = officerInfo.getOfficer();

						if (StringUtils.isNotEmpty(officerInfo.getOperator()))
						{
							if (StringUtils.isNotEmpty(officer))
							{
								String[] officerDialogArr = officer.split("__");
								if (officerDialogArr.length == 1)
									contentText = LanguageManager.getLangByKey(officerInfo.getDialogId(), officerInfo.getOperator(),
											LanguageManager.getLangByKey(officerDialogArr[0]));
								else if (officerDialogArr.length == 2)
									contentText = LanguageManager.getLangByKey(officerInfo.getDialogId(), officerInfo.getOperator(),
											LanguageManager.getLangByKey(officerDialogArr[0]), LanguageManager.getLangByKey(officerDialogArr[1]));
							}
							else
							{
								contentText = LanguageManager.getLangByKey(officerInfo.getDialogId(), officerInfo.getOperator());
							}
						}
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (StringUtils.isNotEmpty(contentText) && contentText.length() > 50)
		{
			contentText = contentText.substring(0, 50);
			contentText = contentText + "...";
		}

	}

	@JSONField(serialize = false)
	public boolean isComplexMail()
	{
		if (type == MailManager.MAIL_BATTLE_REPORT || type == MailManager.MAIL_RESOURCE || type == MailManager.MAIL_DETECT_REPORT
				|| type == MailManager.MAIL_ENCAMP || type == MailManager.WORLD_NEW_EXPLORE || type == MailManager.MAIL_ALLIANCEINVITE
				|| type == MailManager.MAIL_ALLIANCEAPPLY || type == MailManager.MAIL_ATTACKMONSTER || type == MailManager.MAIL_NEW_WORLD_BOSS
				|| type == MailManager.MAIL_RESOURCE_HELP || type == MailManager.MAIL_INVITE_TELEPORT
				|| type == MailManager.MAIL_ALLIANCE_KICKOUT || type == MailManager.MAIL_WORLD_BOSS || type == MailManager.MAIL_ALLIANCE_BOSS
				|| type == MailManager.MAIL_REFUSE_ALL_APPLY)
		{
			return true;
		}
		return false;
	}

	public boolean needParseContent()
	{
		if (StringUtils.isNotEmpty(nameText) && StringUtils.isNotEmpty(contentText) && !isParseVersionOld()
				&& !StringUtils.isNumeric(nameText) && !StringUtils.isNumeric(contentText) && StringUtils.isNotEmpty(language)
				&& TranslateManager.isLangSameAsTargetLang(language))
		{
			return false;
		}
		return true;
	}

	@JSONField(serialize = false)
	public String getChannelId()
	{
		if (ChatServiceController.isNewMailUIEnable)
			return getNewChannelId();
		else
			return getOldChannelId();
	}

	@JSONField(serialize = false)
	public String getOldChannelId()
	{
		String channelId = "";
		if (tabType < 0)
			parseMailTypeTab();
		switch (tabType)
		{
			case MailManager.MAILTAB_SYSTEM:
				channelId = MailManager.CHANNELID_SYSTEM;
				break;
			case MailManager.MAILTAB_NOTICE:
				channelId = MailManager.CHANNELID_NOTICE;
				break;
			case MailManager.MAILTAB_STUDIO:
				channelId = MailManager.CHANNELID_STUDIO;
				break;
			case MailManager.MAILTAB_FIGHT:
				channelId = MailManager.CHANNELID_FIGHT;
				break;
			case MailManager.MAILTAB_MOD:
				channelId = MailManager.CHANNELID_MOD;
				break;
			default:
				break;
		}

		if (type == MailManager.MAIL_RESOURCE)
			channelId = MailManager.CHANNELID_RESOURCE;
		else if (type == MailManager.MAIL_ATTACKMONSTER)
			channelId = MailManager.CHANNELID_MONSTER;
		else if (type == MailManager.MAIL_RESOURCE_HELP)
			channelId = MailManager.CHANNELID_RESOURCE_HELP;
		return channelId;
	}

	@JSONField(serialize = false)
	public boolean isKnightMail()
	{
		return isKnightMail;
	}

	@JSONField(serialize = false)
	public void setIsKnightMail(boolean flag)
	{
		isKnightMail = flag;
	}

	@JSONField(serialize = false)
	public boolean isKnightActivityFinish()
	{
		return isKnightActivityFinishMail;
	}

	@JSONField(serialize = false)
	public String getNewChannelId()
	{
		String channelId = "";
		if(StringUtils.isNotEmpty(rewardId) && rewardStatus == 1 && !isLock() && !isUnread())
		{
			channelId = MailManager.CHANNELID_RECYCLE_BIN;
		}
		else
		{
			if (type == MailManager.MAIL_SYSTEM)
			{
				if (title.equals("114111") || title.equals("105726") || title.equals("105727") || title.equals("105728")
						|| title.equals("105729") || title.equals("105730") || title.equals("115429")
						|| title.equals("9100164") || title.equals("9100165") || title.equals("9100166") 
						|| title.equals("9100167") || title.equals("9100168"))
				{
					channelId = MailManager.CHANNELID_ALLIANCE;
				}
				else if (isWorldBossKillRewardMail() || isAllianceBossKillRewardMail() || isKnightActivityStartMail() || title.equals("150335") || title.equals("150717"))
				{
					channelId = MailManager.CHANNELID_EVENT;
				}
				else if (title.equals("105742"))
				{
					channelId = MailManager.CHANNELID_FIGHT;
				}
				else
				{
					channelId = MailManager.CHANNELID_SYSTEM;
				}
			}
			else if (type == MailManager.MAIL_BATTLE_REPORT)
			{
				if (!hasParseForKnight)
				{
					needParseByForce = true;
					MailData mail = MailManager.getInstance().parseMailDataContent(this);
					isKnightMail = mail.isKnightMail;
					isDragonTowerMail = mail.isDragonTowerMail;
				}
				if (isKnightMail())
				{
					channelId = MailManager.CHANNELID_KNIGHT;
				}
				else if(isDragonTowerMail && MailManager.dragonTowerMailEnable)
				{
					channelId = MailManager.CHANNELID_DRAGON_TOWER;
				}
				else if (isKnightActivityFinish())
				{
					channelId = MailManager.CHANNELID_EVENT;
				}
				else
				{
					channelId = MailManager.CHANNELID_FIGHT;
				}
			}
			else
			{
				switch (type)
				{
					case MailManager.MAIL_DETECT:
					case MailManager.MAIL_DETECT_REPORT:
					case MailManager.MAIL_ENCAMP:
					case MailManager.WORLD_NEW_EXPLORE:
					case MailManager.WORLD_MONSTER_SPECIAL:
						channelId = MailManager.CHANNELID_FIGHT;
						break;
					case MailManager.MAIL_ALLIANCEINVITE:
					case MailManager.MAIL_Alliance_ALL:
					case MailManager.MAIL_ALLIANCEAPPLY:
					case MailManager.MAIL_ALLIANCE_KICKOUT:
					case MailManager.MAIL_INVITE_TELEPORT:
					case MailManager.MAIL_REFUSE_ALL_APPLY:
					case MailManager.MAIL_RESOURCE_HELP:
					case MailManager.MAIL_DONATE:
					case MailManager.MAIL_ALLIANCE_PACKAGE:
					case MailManager.MAIL_ALLIANCE_RANKCHANGE:
					case MailManager.MAIL_ALLIANCE_OFFICER:
						channelId = MailManager.CHANNELID_ALLIANCE;
						break;
					case MailManager.ALL_SERVICE:
					case MailManager.MAIL_SYSUPDATE:
						channelId = MailManager.CHANNELID_STUDIO;
						break;
					case MailManager.MAIL_GIFT:
					case MailManager.MAIL_SYSNOTICE:
					case MailManager.MAIL_SERVICE:
					case MailManager.MAIL_FRESHER:
					case MailManager.MAIL_WOUNDED:
						channelId = MailManager.CHANNELID_SYSTEM;
						break;
					case MailManager.MAIL_ATTACKMONSTER:
						channelId = MailManager.CHANNELID_MONSTER;
						break;
					case MailManager.MAIL_NEW_WORLD_BOSS:
						channelId = MailManager.CHANNELID_NEW_WORLD_BOSS;
						break;
					case MailManager.MAIL_ALLIANCE_BOSS:
						channelId = MailManager.CHANNELID_EVENT;
						break;
					case MailManager.MAIL_RESOURCE:
						channelId = MailManager.CHANNELID_RESOURCE;
						break;
					case MailManager.MAIL_WORLD_BOSS:
						channelId = MailManager.CHANNELID_EVENT;
						break;
				}
			}
		}
		return channelId;
	}

	@Override
	public String toString()
	{
		return "[MailData]: uid = " + uid + " type = " + type + " title:" + title;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	@JSONField(serialize = false)
	public boolean isUnread()
	{
		return status == 0;
	}

	public int getReply()
	{
		return reply;
	}

	public void setReply(int reply)
	{
		this.reply = reply;
	}

	public boolean hasReward()
	{
		return StringUtils.isNotEmpty(rewardId) && rewardStatus == 0;
	}

	@JSONField(serialize = false)
	public boolean isLock()
	{
		return getSave() == 1;
	}

	public int getRewardStatus()
	{
		return rewardStatus;
	}

	public void setRewardStatus(int rewardStatus)
	{
		this.rewardStatus = rewardStatus;
	}

	public int getItemIdFlag()
	{
		return itemIdFlag;
	}

	public void setItemIdFlag(int itemIdFlag)
	{
		this.itemIdFlag = itemIdFlag;
	}

	public int getSave()
	{
		return save;
	}

	public void setSave(int save)
	{
		this.save = save;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public String getFromName()
	{
		return fromName;
	}

	public void setFromName(String fromName)
	{
		this.fromName = fromName;
	}
	
	

	public String getFromUser()
	{
		return fromUser;
	}

	public void setFromUser(String fromUser)
	{
		this.fromUser = fromUser;
	}

	public String getRewardId()
	{
		return rewardId;
	}

	public void setRewardId(String rewardId)
	{
		this.rewardId = rewardId;
	}

	@JSONField(serialize = false)
	public String getVersion()
	{
		return version;
	}

	@JSONField(serialize = false)
	public void setVersion(String version)
	{
		this.version = version;
	}

	@JSONField(serialize = false)
	public long getChannelTime()
	{
		return getCreateTime();
	}

	@JSONField(serialize = false)
	public boolean isWorldBossKillRewardMail()
	{
		if (itemIdFlag == 1 && StringUtils.isNotEmpty(title) && title.equals("137460"))
			return true;
		return false;
	}

	@JSONField(serialize = false)
	public boolean isAllianceBossKillRewardMail()
	{
		if (itemIdFlag == 1 && StringUtils.isNotEmpty(title) && title.equals("2910539"))
			return true;
		return false;
	}

	@JSONField(serialize = false)
	public boolean isKnightActivityStartMail()
	{
		if (StringUtils.isNotEmpty(title) && title.equals("133270"))
			return true;
		return false;
	}

	public int getMbLevel()
	{
		return mbLevel;
	}

	public void setMbLevel(int mbLevel)
	{
		this.mbLevel = mbLevel;
	}

	public boolean isChannelMail()
	{
		String channelId = getChannelId();
		if (StringUtils.isNotEmpty(channelId)
				&& (channelId.equals(MailManager.CHANNELID_KNIGHT) || channelId.equals(MailManager.CHANNELID_RESOURCE) 
						|| channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)))
			return true;
		return false;
	}

	public boolean isSpecialActivityMail()
	{
		return StringUtils.isNotEmpty(title) && (((title.contains("回归专享") && title.contains("官方助力") && title.contains("直升16堡")) || title.equals("【回归专享】 官方助力  直升16堡"))
				|| ((title.contains("Exclusive rewards for coming back") && title.contains("Official assistant") && title.contains("upgrade to Lv.16 directly")) || title.equals("[Exclusive rewards for coming back]Official assistant, upgrade to Lv.16 directly"))
						|| ((title.contains("복귀전용") && title.contains("공식") && title.contains("16캐슬 직행 지원")) || title.equals("[복귀전용] 공식 16캐슬 직행 지원")));
	}

	public String getNameText() {
		return nameText;
	}

	public void setNameText(String nameText) {
		this.nameText = nameText;
	}

	public String getContentText() {
		return contentText;
	}

	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	public String getMailIcon() {
		return mailIcon;
	}

	public void setMailIcon(String mailIcon) {
		this.mailIcon = mailIcon;
	}

	public int getChannelListItemType() {
		if(channelListItemType == -1)
		{
			String channelId = getChannelId();
			if(channelId.equals(MailManager.CHANNELID_ALLIANCE))
				channelListItemType = 3;
			else if(channelId.equals(MailManager.CHANNELID_FIGHT))
				channelListItemType = 4;
			else if(channelId.equals(MailManager.CHANNELID_EVENT))
				channelListItemType = 5;
			else if(channelId.equals(MailManager.CHANNELID_STUDIO))
				channelListItemType = 6;
			else if(channelId.equals(MailManager.CHANNELID_SYSTEM))
				channelListItemType = 7;
			else if(channelId.equals(MailManager.CHANNELID_DRAGON_TOWER))
				channelListItemType = 8;
			else if(channelId.equals(MailManager.CHANNELID_RECYCLE_BIN))
				channelListItemType = 9;
			else if(channelId.equals(MailManager.CHANNELID_RESOURCE))
				channelListItemType = 11;
			else if(channelId.equals(MailManager.CHANNELID_MONSTER))
				channelListItemType = 12;
			else if(channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS))
				channelListItemType = 13;
			else if(channelId.equals(MailManager.CHANNELID_KNIGHT))
				channelListItemType = 14;
		}
		return channelListItemType;
	}

	public void setChannelListItemType(int channelListItemType) {
		this.channelListItemType = channelListItemType;
	}
	
	public void setContentFromContentArr()
	{
		if(contentsArr == null || contentsArr.size() == 0)
			return;
		StringBuilder builder = new StringBuilder();
		for(int i = 0 ; i < contentsArr.size() ;i++)
			builder.append(contentsArr.get(i));
		contents = builder.toString();
		contentsArr = null;
	}
	
}
