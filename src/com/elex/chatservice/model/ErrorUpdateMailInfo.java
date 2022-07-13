package com.elex.chatservice.model;

import com.elex.chatservice.model.mail.MailData;

public class ErrorUpdateMailInfo
{
	private long createTime;
	private MailData mailData;
	
	public long getCreateTime()
	{
		return createTime;
	}
	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}
	public MailData getMailData()
	{
		return mailData;
	}
	public void setMailData(MailData mailData)
	{
		this.mailData = mailData;
	}
}
