package com.elex.chatservice.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.BaseColumns;

import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.HeadPicUtil;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.LogUtil;

public class UserInfo implements Cloneable
{
	public int		_id;
	public int		tableVer;
	public String	uid						= "";
	public String	userName				= "";
	public String	allianceId				= "";
	public String	asn						= "";
	public int		allianceRank			= -1;
	/** 国家号，只有本玩家有 */
	public int		serverId				= -1;
	/** 跨服战时的原服id，若为-1表示没有跨服 */
	public int		crossFightSrcServerId	= -2;
	/** 玩家类型，尚未使用；会被C++初始化且不赋初值 */
	public int		type					= 0;
	public String	headPic					= "";
	/** 头像id */
	public int		headPicVer				= -1;
	/** gm和mod信息，如果为"2"、"4"、"5"表示为mod，如果为“3”表示为gm */
	public int		mGmod					= -1;
	/** vip等级，至少为1，由vip points决定，只升不降 */
	public int		vipLevel				= -1;
	public int		svipLevel				= -1;
	/** vip时间，单位为s，有时区，过期则vip暂时失效（等级保留 */
	public int		vipEndTime				= 0;
	/** 上次更新时间 */
	public int		lastUpdateTime			= 0;
	/** 上次聊天时间 */
	public int		lastChatTime			= 0;
	public String	lang					= "";
	public int		chatBgId				= 0;
	public int		chatBgEndTime			= 0;
	public String	chatBgTextColor			= "";
	public String 	officer					= "";
	public boolean	isPositiveOfficer		= true;
	public String	flagCountry				= "";
	public int		showBanner				= 0;
	
	private int	chatBgTextColorValue		= -1; 

	// 运行时
	public boolean	isSelected				= false;
	public boolean	isDummy					= false;
	
	//聊天室设置使用
	public int  btnType					= 0;

	/**
	 * 用于初始化时创建当前玩家，以及代表国家、联盟的UserInfo
	 */
	public UserInfo()
	{

	}

	public UserInfo(Cursor c)
	{
		try
		{
			_id = c.getInt(c.getColumnIndex(BaseColumns._ID));
			tableVer = c.getInt(c.getColumnIndex(DBDefinition.COLUMN_TABLE_VER));
			uid = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_USER_ID));
			userName = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_NICK_NAME));
			allianceId = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_ID));
			asn = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_NAME));
			allianceRank = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_ALLIANCE_RANK));
			serverId = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_SERVER_ID));
			crossFightSrcServerId = c.getInt(c.getColumnIndex(DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID));
			type = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_TYPE));
			headPic = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_HEAD_PIC));
			headPicVer = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_CUSTOM_HEAD_PIC));
			mGmod = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_PRIVILEGE));
			vipLevel = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_VIP_LEVEL));
			svipLevel = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_SVIP_LEVEL));
			vipEndTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_VIP_END_TIME));
			lastUpdateTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_LAST_UPDATE_TIME));
			lastChatTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_LAST_CHAT_TIME));
			chatBgId = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_CHAT_BG_ID));
			chatBgEndTime = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_CHAT_BG_END_TIME));
			chatBgTextColor = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_CHAT_BG_TEXT_COLOR));
			lang = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_LANG));
			officer = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_OFFICER));
			flagCountry = c.getString(c.getColumnIndex(DBDefinition.USER_COLUMN_FLAGCOUNTRY));
			showBanner = c.getInt(c.getColumnIndex(DBDefinition.USER_COLUMN_SHOWBANNER));
			initNullField();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	public String getOfficerName()
	{
		if(StringUtils.isNotEmpty(officer))
			return UserManager.getInstance().getOfficerName(officer);
		return "";
	}
	
	private boolean officerSeted = false;
	public void setOfficerProperty()
	{
		if(StringUtils.isNotEmpty(officer) && StringUtils.isNumeric(officer))
		{
			if(!officerSeted)
			{
				int officerId = Integer.parseInt(officer);
				if(officerId > 216008)
					isPositiveOfficer = false;
				else
					isPositiveOfficer = true;
				officerSeted = true;
			}
		}
	}

	public ContentValues getContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefinition.COLUMN_TABLE_VER, DBHelper.CURRENT_DATABASE_VERSION);
		cv.put(DBDefinition.USER_COLUMN_USER_ID, uid);
		cv.put(DBDefinition.USER_COLUMN_NICK_NAME, userName);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_ID, allianceId);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_NAME, asn);
		cv.put(DBDefinition.USER_COLUMN_ALLIANCE_RANK, allianceRank);
		cv.put(DBDefinition.USER_COLUMN_SERVER_ID, serverId);
		cv.put(DBDefinition.USER_CROSS_FIGHT_SRC_SERVER_ID, crossFightSrcServerId);
		cv.put(DBDefinition.USER_COLUMN_TYPE, type);
		cv.put(DBDefinition.USER_COLUMN_HEAD_PIC, headPic);
		cv.put(DBDefinition.USER_COLUMN_CUSTOM_HEAD_PIC, headPicVer);
		cv.put(DBDefinition.USER_COLUMN_PRIVILEGE, mGmod);
		cv.put(DBDefinition.USER_COLUMN_VIP_LEVEL, vipLevel);
		cv.put(DBDefinition.USER_COLUMN_SVIP_LEVEL, svipLevel);
		cv.put(DBDefinition.USER_COLUMN_VIP_END_TIME, vipEndTime);
		cv.put(DBDefinition.USER_COLUMN_LAST_UPDATE_TIME, lastUpdateTime);
		cv.put(DBDefinition.USER_COLUMN_LAST_CHAT_TIME, lastChatTime);
		cv.put(DBDefinition.USER_COLUMN_CHAT_BG_ID, chatBgId);
		cv.put(DBDefinition.USER_COLUMN_CHAT_BG_END_TIME, chatBgEndTime);
		cv.put(DBDefinition.USER_COLUMN_CHAT_BG_TEXT_COLOR, chatBgTextColor);
		cv.put(DBDefinition.USER_COLUMN_LANG, lang);
		cv.put(DBDefinition.USER_COLUMN_OFFICER, officer);
		cv.put(DBDefinition.USER_COLUMN_FLAGCOUNTRY, flagCountry);
		cv.put(DBDefinition.USER_COLUMN_SHOWBANNER, showBanner);
		return cv;
	}

	/**
	 * 用于wrapper假消息
	 */
	public UserInfo(int gmod, int allianceRank, int headPicVer, int vipLv, String uidStr, String nameStr, String asnStr, String headPicStr,
			int lastUpdateTime,int chatBgId,int chatBgEndTime)
	{
		this.vipLevel = vipLv;
		this.vipEndTime = TimeManager.getInstance().getCurrentTime() + 60;
		this.userName = nameStr;
		this.headPic = headPicStr;
		this.uid = uidStr;
		this.asn = asnStr;
		this.mGmod = gmod;
		this.allianceRank = allianceRank;
		this.headPicVer = headPicVer;
		this.lastUpdateTime = lastUpdateTime;
		this.chatBgId = chatBgId;
		this.chatBgEndTime = chatBgEndTime;
	}

	/**
	 * 用于接受到的消息时，在本地找不到用户信息，临时创建dummy UserInfo
	 */
	public UserInfo(String uidStr)
	{
		uid = uidStr;
		if(StringUtils.isNotEmpty(uid) && uid.equals("3000002"))
		{
			headPic = "guide_player_icon";
			userName = LanguageManager.getNPCName();
		}
		else
		{
			headPic = "g044";
			userName = uid;
		}
		isDummy = true;
	}

	public String getLowcaseName()
	{
		if(StringUtils.isNotEmpty(userName))
			return userName.toLowerCase();
		else
			return "";
	}
	
	public boolean isNpc()
	{
		return StringUtils.isNotEmpty(uid) && (uid.equals("3000001") || uid.equals("3000002"));
	}
	
	public String getUppercaseName()
	{
		if(StringUtils.isNotEmpty(userName))
			return userName.toUpperCase();
		else
			return "";
	}
	
	public com.alibaba.fastjson.JSONObject getUserJson()
	{
		com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();

		ArrayList<Field> fields = getDatabaseFields();
		for (Field f : fields)
		{
			String filedName = f.getName();
			try
			{
				params.put(filedName, f.get(this));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return params;
	}
	
	
	public JSONObject getJson()
	{
		JSONObject params = new JSONObject();

		ArrayList<Field> fields = getDatabaseFields();
		for (Field f : fields)
		{
			String filedName = f.getName();
			try
			{
				params.put(filedName, f.get(this));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return params;
	}
	
	private static ArrayList<Field> databaseFields;
	private ArrayList<Field> getDatabaseFields()
	{
		if(databaseFields != null)
		{
			return databaseFields;
		}
		
		databaseFields = new ArrayList<Field>();
		Field[] fields = UserInfo.class.getDeclaredFields();
		for (Field f : fields)
		{
//			Annotation annotation = f.getAnnotation(OldDatabaseField.class);
//			if (annotation != null)
//			{
//				OldDatabaseField databaseField = (OldDatabaseField) annotation;
//				if (!f.getName().equals("_id") && !f.getName().equals("tableVer"))
//				{
//					databaseFields.add(f);
//				}
//			}
			if(isDBFields(f.getName()) && !Modifier.isStatic(f.getModifiers()))
			{
				databaseFields.add(f);
			}
		}
		return databaseFields;
	}

	private static String[] noneDBFields = {"_id", "tableVer", "chatBgTextColorValue", "isSelected", "isDummy", "btnType"};
	private boolean isDBFields(String field)
	{
		for (int i = 0; i < noneDBFields.length; i++)
		{
			if(noneDBFields[i].equals(field))
				return false;
		}
		return true;
	}
	
	public void setFromJson(JSONObject json)
	{
		if(json == null)
			return;
		
		ArrayList<Field> fields = getDatabaseFields();
		for (Field f : fields)
		{
			String filedName = f.getName();
			String type = f.getGenericType().toString();
			try
			{
				Object value = getValue(type, filedName, json);
				if(value != null)
					f.set(this, getValue(type, filedName, json));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private Object getValue(String type, String filedName, JSONObject json)
	{
		if (type.equals("class java.lang.String"))
		{
			return json.optString(filedName);
		}
		if (type.equals("int") || type.equals("class java.lang.Integer"))
		{
			return json.optInt(filedName);
		}
		return null;
	}

	public boolean equalsLogically(Object o)
	{
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}

		UserInfo user = (UserInfo) o;
		boolean result;
		result = uid.equals(user.uid) && userName.equals(user.userName) && allianceId.equals(user.allianceId) && asn.equals(user.asn)
				&& allianceRank == user.allianceRank && serverId == user.serverId && crossFightSrcServerId == user.crossFightSrcServerId
				&& type == user.type && headPic.equals(user.headPic) && headPicVer == user.headPicVer && mGmod == user.mGmod
				&& vipLevel == user.vipLevel && svipLevel == user.svipLevel && vipEndTime == user.vipEndTime
				&& lastUpdateTime == user.lastUpdateTime && lastChatTime == user.lastChatTime
				&& chatBgId == user.chatBgId && chatBgEndTime == user.chatBgEndTime
				&& (chatBgTextColor!=null && user.chatBgTextColor!=null &&chatBgTextColor.equals(user.chatBgTextColor))
				&& (lang != null && user.lang != null && lang.equals(user.lang)) 
				&& (officer!=null && user.officer!=null && officer.equals(user.officer));

		return result;
	}

	@Override
	public Object clone()
	{
		UserInfo o = null;
		try
		{
			o = (UserInfo) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return o;
	}

	public String getVipInfo()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		String vipInfo = "";
		if (dtime > 0)
		{
			if (svipLevel > 0)
				vipInfo = LanguageManager.getLangByKey(LanguageKeys.SVIP_INFO, String.valueOf(svipLevel));
			else if (vipLevel > 0)
				vipInfo = LanguageManager.getLangByKey(LanguageKeys.VIP_INFO, String.valueOf(vipLevel));
		}
		return vipInfo;
	}
	
	public boolean isSVIP()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		if (dtime > 0 && svipLevel > 0)
			return true;
		return false;
	}
	
	public boolean isVIP()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		if (dtime > 0 && vipLevel > 0)
			return true;
		return false;
	}

	public int getVipLevel()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		int vipInfo = (vipLevel > 0 && dtime > 0) ? vipLevel : 0;
		return vipInfo;
	}
	
	public int getSVipLevel()
	{
		int dtime = vipEndTime - TimeManager.getInstance().getCurrentTime();
		int vipInfo = (svipLevel > 0 && dtime > 0) ? svipLevel : 0;
		return vipInfo;
	}

	public boolean isCustomHeadImage()
	{
		if (isDummy)
			return false;
		return headPicVer > 0 && headPicVer < 1000000;
	}

	/**
	 * 自定义头像网络URL
	 */
	public String getCustomHeadPicUrl()
	{
		String result = HeadPicUtil.getCustomPicUrl(uid, headPicVer);

		return result;
	}

	/**
	 * 自定义头像本地路径
	 */
	public String getCustomHeadPic()
	{
		String result = HeadPicUtil.getCustomPic(getCustomHeadPicUrl());

		return result;
	}

	public int getHeadPicVer()
	{
		return headPicVer;
	}

	/**
	 * 自定义头像是否存在
	 */
	public boolean isCustomHeadPicExist()
	{
		String path = getCustomHeadPic();
		return ImageUtil.isPicExist(path);
	}

	/**
	 * C++创建的对象可能没有默认值赋值，需要补上
	 */
	public void initNullField()
	{
		if (userName == null)
		{
			userName = "";
		}
		if (allianceId == null)
		{
			allianceId = "";
		}
		if (asn == null)
		{
			asn = "";
		}
		if (headPic == null)
		{
			headPic = "";
		}
	}

	/**
	 * 判断是否是dummy user，特征为：
	 * 不是频道user，且privilege（或AllianceRank、ServerId、CustomHeadPic、VipLevel）为-1
	 * dummy user有两种来源： 1.故意创建的临时dummy user 2.以前曾将dummy user存入了db
	 */
	public boolean isValid()
	{
		boolean result = isChannelUser() || this.mGmod != -1;
		return result;
	}

	public boolean isChannelUser()
	{
		return this.uid.contains("@");
	}
	
	public int getCustomChatBg()
	{
		int dtime = chatBgEndTime - TimeManager.getInstance().getCurrentTime();
		return (chatBgId>0 && dtime > 0) ? chatBgId : 0;
	}
	
	public int getCustomChatBgTextColor()
	{
		if(chatBgTextColorValue!=-1 && chatBgTextColorValue!=-2)
			return chatBgTextColorValue;
		if(StringUtils.isEmpty(chatBgTextColor) || chatBgTextColorValue == -2)
			return -1;
		String[] rgb = chatBgTextColor.split(":");
		if(rgb.length!=3)
		{
			chatBgTextColorValue = -2;
			return -1;
		}
		if(StringUtils.isNumeric(rgb[0]) && StringUtils.isNumeric(rgb[1]) && StringUtils.isNumeric(rgb[2]))
		{
			int r = Integer.parseInt(rgb[0]);
			int g = Integer.parseInt(rgb[1]);
			int b = Integer.parseInt(rgb[2]);
			if(r>=0 && r<=255 && g>=0 && g<=255 && b>=0 && b<=255)
			{
				chatBgTextColorValue = Color.rgb(r, g, b);
			}
			else
				chatBgTextColorValue = -2;
		}
		else
			chatBgTextColorValue = -2;
		return chatBgTextColorValue;
	}
	
	public boolean isInAlliance()
	{
		return StringUtils.isNotEmpty(allianceId);
	}
}
