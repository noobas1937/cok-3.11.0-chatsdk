package com.elex.chatservice.model;

public class DetectMailInfo
{
	private String	name		= "";
	private String	mailUid		= "";
	private long	createTime	= 0;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMailUid()
	{
		return mailUid;
	}

	public void setMailUid(String mailUid)
	{
		this.mailUid = mailUid;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

}
