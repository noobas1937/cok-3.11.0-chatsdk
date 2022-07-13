package com.elex.im.core.model;

import com.elex.im.core.util.FileUtil;
import com.elex.im.core.util.HeadPicUtil;
import com.elex.im.core.util.TimeManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "User")
public class User {
	
	@DatabaseField(generatedId = true)
	public int 	_id;
	@DatabaseField
	public String uid = "";
	@DatabaseField
	public String userName = "";

	/** TODO 应该放入json，用于setDeviceInfo() */
	/** 国家号，只有本玩家有 */
	@DatabaseField
	public int	serverId;
	@DatabaseField
	public String	allianceId				= "";
	@DatabaseField
	public String	asn						= "";
	@DatabaseField
	public int		allianceRank			= -1;
	/** 跨服战时的原服id，若为-1表示没有跨服 */
	@DatabaseField
	public int		crossFightSrcServerId	= -2;
	
	@DatabaseField
	public String	lang					= "";
	@DatabaseField
	public String	headPic					= "";
	/** 上次更新时间 */
	@DatabaseField
	public int lastUpdateTime = 0;

	/** 头像id */
	@DatabaseField
	public int		headPicVer				= -1;
	
	/** gm和mod信息，如果为"2"、"4"、"5"表示为mod，如果为“3”表示为gm */
	@DatabaseField
	public int		mGmod					= -1;
	/** vip等级，至少为1，由vip points决定，只升不降 */
	@DatabaseField
	public int		vipLevel				= -1;
	@DatabaseField
	public int		svipLevel				= -1;
	/** vip时间，单位为s，有时区，过期则vip暂时失效（等级保留 */
	@DatabaseField
	public int		vipEndTime				= 0;

	/** 玩家类型，尚未使用；会被C++初始化且不赋初值 */
	@DatabaseField
	public int		type					= 0;
	/** 上次聊天时间 */
	@DatabaseField
	public int		lastChatTime			= 0;
	@DatabaseField
	public int		chatBgId				= 0;
	@DatabaseField
	public int		chatBgEndTime			= 0;

	// 运行时
	public boolean	isSelected				= false;
	public boolean	isDummy					= false;
	
	//聊天室设置使用
	public int  btnType					= 0;

	public User()
	{
	}
	
	/**
	 * 用于接受到的消息时，在本地找不到用户信息，临时创建dummy UserInfo
	 */
	public User(String uidStr)
	{
		uid = uidStr;
		headPic = "g044";
		userName = uid;
		isDummy = true;
	}

	/**
	 * 用于wrapper假消息
	 */
	public User(int gmod, int allianceRank, int headPicVer, int vipLv, String uidStr, String nameStr, String asnStr, String headPicStr,
			int lastUpdateTime)
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
		return FileUtil.isPicExist(path);
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

		User user = (User) o;
		boolean result;
		result = uid.equals(user.uid) && userName.equals(user.userName) && allianceId.equals(user.allianceId) && asn.equals(user.asn)
				&& allianceRank == user.allianceRank && serverId == user.serverId && crossFightSrcServerId == user.crossFightSrcServerId
				&& type == user.type && headPic.equals(user.headPic) && headPicVer == user.headPicVer && mGmod == user.mGmod
				&& vipLevel == user.vipLevel && svipLevel == user.svipLevel && vipEndTime == user.vipEndTime
				&& lastUpdateTime == user.lastUpdateTime && lastChatTime == user.lastChatTime
				&& chatBgId == user.chatBgId && chatBgEndTime == user.chatBgEndTime
				&& (lang != null && user.lang != null && lang.equals(user.lang));

		return result;
	}

	@Override
	public Object clone()
	{
		User o = null;
		try
		{
			o = (User) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return o;
	}
}
