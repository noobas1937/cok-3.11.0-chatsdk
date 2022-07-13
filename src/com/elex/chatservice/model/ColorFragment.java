package com.elex.chatservice.model;

public class ColorFragment
{
	private String	dialogExtra	= "";
	private int		color		= 0;
	private boolean	needLink	= false;

	public String getDialogExtra()
	{
		return dialogExtra;
	}

	public void setDialogExtra(String dialogExtra)
	{
		this.dialogExtra = dialogExtra;
	}

	public int getColor()
	{
		return color;
	}

	public void setColor(int color)
	{
		this.color = color;
	}

	public boolean isNeedLink()
	{
		return needLink;
	}

	public void setNeedLink(boolean needLink)
	{
		this.needLink = needLink;
	}
}
