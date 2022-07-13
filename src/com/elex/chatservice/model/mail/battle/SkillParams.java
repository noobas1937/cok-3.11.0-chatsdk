package com.elex.chatservice.model.mail.battle;

public class SkillParams
{
	private long	startTime;
	private String	ownerId;
	private String	uuid;
	private String	skillId;
	private long	endTime;
	private int		stat;
	private long	actTime;
	private int		param1;
	private int		param2;

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public String getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(String ownerId)
	{
		this.ownerId = ownerId;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getSkillId()
	{
		return skillId;
	}

	public void setSkillId(String skillId)
	{
		this.skillId = skillId;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public int getStat()
	{
		return stat;
	}

	public void setStat(int stat)
	{
		this.stat = stat;
	}

	public long getActTime()
	{
		return actTime;
	}

	public void setActTime(long actTime)
	{
		this.actTime = actTime;
	}

	public int getParam1() {
		return param1;
	}

	public void setParam1(int param1) {
		this.param1 = param1;
	}

	public int getParam2() {
		return param2;
	}

	public void setParam2(int param2) {
		this.param2 = param2;
	}

}
