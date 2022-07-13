package com.elex.chatservice.view.allianceshare.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;

public class AllianceShareInfo implements Comparable<AllianceShareInfo>
{
	private String							at;
	private AllianceShareAuthority			authority;
	private List<AllianceShareComment>		comment;
	private List<AllianceShareComment>		like;
	private List<AllianceShareImageData>	data;
	private String							fid;
	private String							id;
	private String							msg;
	private String							sender;
	private int								status;
	private long							time;
	private int								type;
	private int								newComment	= 0;
	private transient String				readableTime = "";

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

	public List<AllianceShareImageData> getData()
	{
		return data;
	}

	public void setData(List<AllianceShareImageData> data)
	{
		this.data = data;
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
	
	@JSONField(serialize = false)
	public String getReadableTime()
	{
		if(StringUtils.isEmpty(readableTime))
			readableTime = TimeManager.getReadableTime(time);
		else
		{
			int dt = TimeManager.getInstance().getCurrentTime() - TimeManager.getTimeInS(time);
			if (dt < 60 * 60)
				readableTime = TimeManager.getReadableTime(time);
		}
		return readableTime;
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

	public List<AllianceShareComment> getComment()
	{
		return comment;
	}

	public void setComment(List<AllianceShareComment> comment)
	{
		this.comment = comment;
	}

	public List<AllianceShareComment> getLike()
	{
		return like;
	}

	public void setLike(List<AllianceShareComment> like)
	{
		this.like = like;
	}

	public int getNewComment()
	{
		return newComment;
	}

	public void setNewComment(int newComment)
	{
		this.newComment = newComment;
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

	public UserInfo getUser()
	{
		UserManager.checkUser(sender, "", 0);
		UserInfo user = UserManager.getInstance().getUser(sender);
		return user;
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

	public String getName()
	{
		if (getUser() != null)
			return getUser().userName;
		return sender;
	}

	public String getHeadPic()
	{
		if (getUser() != null)
			return getUser().headPic;
		return "";
	}

	public boolean isKingInfo()
	{
		if (StringUtils.isNotEmpty(sender) && StringUtils.isNotEmpty(ChatServiceController.kingUid)
				&& ChatServiceController.kingUid.equals(sender))
			return true;
		return false;
	}

	public boolean isSelfAllianceShare()
	{
		if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()) || StringUtils.isEmpty(sender))
			return false;
		return UserManager.getInstance().getCurrentUserId().equals(sender);
	}
	
	public boolean hasLiked()
	{
		if(like!=null && like.size()>0)
		{
			for(AllianceShareComment likeItem : like)
			{
				if(likeItem!=null && likeItem.isSelfAllianceShareComment())
					return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(AllianceShareInfo another)
	{
		if(another != null)
		{
			long offset = another.getTime()-this.time;
			if(offset>0)
				return 1;
			else if(offset<0)
				return -1;
		}
		return 0;
	}
	
	public String getSelfLikeId()
	{
		if(like!=null && like.size()>0)
		{
			for(AllianceShareComment likeItem : like)
			{
				if(likeItem!=null && likeItem.isSelfAllianceShareComment())
					return likeItem.getId();
			}
		}
		return "";
	}
}
