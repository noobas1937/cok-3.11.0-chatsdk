package com.elex.chatservice.model.mail.detectreport;

import java.util.List;

public class AggregationUserParams
{
	private String name;
	private String abbr;
	private int level;
	private int total;
	private String	pic;
	private int		picVer;
	private List<ReinAboutDetailParams> armys;
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getAbbr()
	{
		return abbr;
	}
	public void setAbbr(String abbr)
	{
		this.abbr = abbr;
	}
	public int getLevel()
	{
		return level;
	}
	public void setLevel(int level)
	{
		this.level = level;
	}
	public int getTotal()
	{
		return total;
	}
	public void setTotal(int total)
	{
		this.total = total;
	}
	public List<ReinAboutDetailParams> getArmys()
	{
		return armys;
	}
	public void setArmys(List<ReinAboutDetailParams> armys)
	{
		this.armys = armys;
	}
	public String getPic()
	{
		return pic;
	}
	public void setPic(String pic)
	{
		this.pic = pic;
	}
	public int getPicVer()
	{
		return picVer;
	}
	public void setPicVer(int picVer)
	{
		this.picVer = picVer;
	}
	
	
}
