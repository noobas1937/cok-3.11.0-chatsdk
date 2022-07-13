package com.elex.chatservice.view.allianceshare.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

public class AllianceShareImageData implements Serializable
{
	private static final long	serialVersionUID	= 7411537046112373381L;
	private String url;
	private String w;
	private String h;
	private transient boolean hasOpenDetail = false;
	
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getW()
	{
		return w;
	}
	public void setW(String w)
	{
		this.w = w;
	}
	public String getH()
	{
		return h;
	}
	public void setH(String h)
	{
		this.h = h;
	}
	
	@JSONField(serialize=false)
	public String getLocalPath()
	{
		if(StringUtils.isNotEmpty(url))
			return AllianceShareManager.getInstance().getLocalAllianceShareImagePath(url);
		return "";
	}
	
	@JSONField(serialize=false)
	public String getLocalThumbPath()
	{
		if(StringUtils.isNotEmpty(url))
			return AllianceShareManager.getInstance().getLocalAllianceShareImageThumbPath(url);
		return "";
	}
	
	@JSONField(serialize=false)
	public String getServerPath()
	{
		if(StringUtils.isNotEmpty(url))
			return AllianceShareManager.getInstance().getServerAllianceShareImagePath(url);
		return "";
	}
	
	@JSONField(serialize=false)
	public String getServerThumbPath()
	{
		if(StringUtils.isNotEmpty(url))
			return AllianceShareManager.getInstance().getServerAllianceShareImageThumbPath(url);
		return "";
	}
	
	@JSONField(serialize=false)
	public int getWidth()
	{
		if(StringUtils.isNumeric(w))
			return Integer.parseInt(w);
		return 0;
	}
	
	@JSONField(serialize=false)
	public int getHeight()
	{
		if(StringUtils.isNumeric(h))
			return Integer.parseInt(h);
		return 0;
	}
	
	@JSONField(serialize=false)
	public boolean hasOpenDetail()
	{
		return hasOpenDetail;
	}
	
	@JSONField(serialize=false)
	public void setOpenDetail(boolean hasOpenDetail)
	{
		this.hasOpenDetail = hasOpenDetail;
	}
}
