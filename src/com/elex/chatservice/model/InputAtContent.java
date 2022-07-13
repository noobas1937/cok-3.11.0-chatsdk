package com.elex.chatservice.model;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;

public class InputAtContent
{
	private String atNameText;
	private String atUidText;
	private String uid;
	private String lang;
	private int startPos;
	
	public String getAtNameText()
	{
		return atNameText;
	}
	public void setAtNameText(String atNameText)
	{
		this.atNameText = atNameText;
	}
	public String getAtUidText()
	{
		return atUidText;
	}
	public void setAtUidText(String atUidText)
	{
		this.atUidText = atUidText;
	}
	public String getUid()
	{
		return uid;
	}
	public void setUid(String uid)
	{
		this.uid = uid;
	}
	public int getStartPos()
	{
		return startPos;
	}
	public void setStartPos(int startPos)
	{
		this.startPos = startPos;
	}
	
	public String getLang()
	{
		return lang;
	}
	public void setLang(String lang)
	{
		this.lang = lang;
	}
	@JSONField(serialize = false)
	public boolean isNpcAt()
	{
		return StringUtils.isNotEmpty(uid) && (uid.equals("3000001") || uid.equals("3000002"));
	}
	
}
