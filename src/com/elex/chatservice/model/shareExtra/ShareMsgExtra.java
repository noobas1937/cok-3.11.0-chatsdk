package com.elex.chatservice.model.shareExtra;

import java.util.List;

public class ShareMsgExtra
{
	private String icon;
	private ShareContent title;
	private ShareContent description;
	private MediaContent mediaContent;
	private List<String> actionArgs;
	
	public String getIcon()
	{
		return icon;
	}
	public void setIcon(String icon)
	{
		this.icon = icon;
	}
	public ShareContent getTitle()
	{
		return title;
	}
	public void setTitle(ShareContent title)
	{
		this.title = title;
	}
	public ShareContent getDescription()
	{
		return description;
	}
	public void setDescription(ShareContent description)
	{
		this.description = description;
	}
	public MediaContent getMediaContent()
	{
		return mediaContent;
	}
	public void setMediaContent(MediaContent mediaContent)
	{
		this.mediaContent = mediaContent;
	}
	public List<String> getActionArgs() {
		return actionArgs;
	}
	public void setActionArgs(List<String> actionArgs) {
		this.actionArgs = actionArgs;
	}
}
