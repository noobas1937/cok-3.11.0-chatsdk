package com.elex.chatservice.model.mail.detectreport;

public class ReinAboutDetailParams
{
	private String	armyId;
	private int		count;
	private boolean	about;
	private int star;

	public int getStar()
	{
		return star;
	}

	public void setStar(int star)
	{
		this.star = star;
	}

	public String getArmyId()
	{
		return armyId;
	}

	public void setArmyId(String armyId)
	{
		this.armyId = armyId;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public boolean isAbout()
	{
		return about;
	}

	public void setAbout(boolean about)
	{
		this.about = about;
	}

}
