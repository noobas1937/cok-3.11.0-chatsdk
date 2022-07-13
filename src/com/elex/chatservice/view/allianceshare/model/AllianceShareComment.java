package com.elex.chatservice.view.allianceshare.model;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

public class AllianceShareComment implements Comparable<AllianceShareComment>
{
	private String at;
	private AllianceShareAuthority authority;
	private String fid;
	private String id;
	private String msg;
	private String sender;
	private int status;
	private long time;
	private int type;
	public String getAt()
	{
		return at;
	}
	public void setAt(String at)
	{
		this.at = at;
	}
	public AllianceShareAuthority getAuthority()
	{
		return authority;
	}
	public void setAuthority(AllianceShareAuthority authority)
	{
		this.authority = authority;
	}
	public String getFid()
	{
		return fid;
	}
	public void setFid(String fid)
	{
		this.fid = fid;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getMsg()
	{
		return msg;
	}
	public void setMsg(String msg)
	{
		this.msg = msg;
	}
	public String getSender()
	{
		return sender;
	}
	public void setSender(String sender)
	{
		this.sender = sender;
	}
	public int getStatus()
	{
		return status;
	}
	public void setStatus(int status)
	{
		this.status = status;
	}
	public long getTime()
	{
		return time;
	}
	public void setTime(long time)
	{
		this.time = time;
	}
	public int getType()
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	
	public boolean isComment()
	{
		return type == AllianceShareManager.ALLIANCE_SHARE_COMMENT;
	}
	
	public boolean isLike()
	{
		return type == AllianceShareManager.ALLIANCE_SHARE_LIKE;
	}
	
	public UserInfo getUser()
	{
		UserManager.checkUser(sender, "", 0);
		UserInfo user = UserManager.getInstance().getUser(sender);
		return user;
	}
	
	public UserInfo getAtUser()
	{
		UserManager.checkUser(at, "", 0);
		UserInfo user = UserManager.getInstance().getUser(at);
		return user;
	}
	
	public String getName()
	{
		if(getUser()!=null)
			return getUser().userName;
		return sender;
	}
	
	public String getAtName()
	{
		if(getAtUser()!=null)
			return getAtUser().userName;
		return at;
	}
	
	public boolean isSelfAllianceShareComment()
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) || StringUtils.isEmpty(sender))
			return false;
		return UserManager.getInstance().getCurrentUserId().equals(sender);
	}
	
	public String getVipLabel()
	{
		return getVip() + " ";
	}

	public String getVip()
	{
		if (getUser() != null)
			return getUser().getVipInfo();
		return "";
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

	public boolean isInAlliance()
	{
		return !getASN().equals("");
	}

	public String getASN()
	{
		if (getUser() != null)
			return getUser().asn;
		return "";
	}
	
	public String getHeadPic()
	{
		if (getUser() != null)
			return getUser().headPic;
		return "";
	}
	@Override
	public int compareTo(AllianceShareComment another)
	{
		if(another != null)
		{
			long offset = this.time -another.getTime();
			if(offset>0)
				return 1;
			else if(offset<0)
				return -1;
		}
		return 0;
	}
}
