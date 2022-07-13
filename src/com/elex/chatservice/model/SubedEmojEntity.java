package com.elex.chatservice.model;

public class SubedEmojEntity
{
	private String groupId;
	private long endTime;
	
	public String getGroupId()
	{
		return groupId;
	}
	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}
	public long getEndTime()
	{
		return endTime;
	}
	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}
	
	public SubedEmojEntity()
	{
		
	}
	
	public SubedEmojEntity(String groupId,long endTime)
	{
		this.groupId = groupId;
		this.endTime = endTime;
	}
}
