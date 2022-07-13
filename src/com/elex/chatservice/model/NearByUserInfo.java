package com.elex.chatservice.model;

import java.util.List;

public class NearByUserInfo implements Comparable<NearByUserInfo>
{
	private String	uid;
	private double	distance;
	private long	lastLoginTime;
	private String  freshNewsText;
	private List<ColorFragment>	freshNewsColorFragmentList;	
	private int 	likeNum;
	
	
	

	public int getLikeNum()
	{
		return likeNum;
	}

	public void setLikeNum(int likeNum)
	{
		this.likeNum = likeNum;
	}

	public NearByUserInfo(String uid, double distance, long lastLoginTime)
	{
		this.uid = uid;
		this.distance = distance;
		this.lastLoginTime = lastLoginTime;
	}
	
	public NearByUserInfo(String uid, double distance, long lastLoginTime,int likeNum)
	{
		this( uid,  distance,  lastLoginTime);
		this.likeNum = likeNum;
	}
	
	public String getFreshNewsText()
	{
		return freshNewsText;
	}

	public void setFreshNewsText(String freshNewsText)
	{
		this.freshNewsText = freshNewsText;
	}

	public List<ColorFragment> getFreshNewsColorFragmentList()
	{
		return freshNewsColorFragmentList;
	}

	public void setFreshNewsColorFragmentList(List<ColorFragment> freshNewsColorFragmentList)
	{
		this.freshNewsColorFragmentList = freshNewsColorFragmentList;
	}

	public NearByUserInfo(String uid, double distance, long lastLoginTime,String freshNews,List<ColorFragment> colorFragmentList)
	{
		this.uid = uid;
		this.distance = distance;
		this.lastLoginTime = lastLoginTime;
		this.freshNewsText = freshNews;
		this.freshNewsColorFragmentList = colorFragmentList;
	}
	
	public NearByUserInfo(String uid, double distance, long lastLoginTime,String freshNews,List<ColorFragment> colorFragmentList,int likes)
	{
		this(uid, distance, lastLoginTime, freshNews, colorFragmentList);
		this.likeNum = likes;
	}

	public String getUid()
	{
		return uid;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
	}

	public long getLastLoginTime()
	{
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime)
	{
		this.lastLoginTime = lastLoginTime;
	}

	@Override
	public int compareTo(NearByUserInfo another)
	{
		if (this.likeNum > another.likeNum)
		{
			return -1;
		}
		else if (this.likeNum < another.likeNum)
		{
			return 1;
		}
		else
		{
			if (this.distance > another.distance)
				return 1;
			else if (this.distance < another.distance)
				return -1;
			else
				return 0;
		}
		
	}

}
