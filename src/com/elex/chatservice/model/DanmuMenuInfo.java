package com.elex.chatservice.model;

public class DanmuMenuInfo
{
	private int index = 0;
	private int backgroundColor = 0x00000000;
	private String text = "";
	
	public int getIndex()
	{
		return index;
	}
	public void setIndex(int index)
	{
		this.index = index;
	}
	public int getBackgroundColor()
	{
		return backgroundColor;
	}
	public void setBackgroundColor(int backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}
	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	
	
}
