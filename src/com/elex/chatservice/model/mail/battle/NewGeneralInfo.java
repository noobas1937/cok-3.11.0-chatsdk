package com.elex.chatservice.model.mail.battle;

import java.util.List;

public class NewGeneralInfo
{
	private String userId;
	private String generalId;
    private int level;
    private List<String>		skillList;
	
	public String getUserId()
	{
		return userId;
	}
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	public String getGeneralId()
	{
		return generalId;
	}
	public void setGeneralId(String generalId)
	{
		this.generalId = generalId;
	}
	public int getLevel()
	{
		return level;
	}
	public void setLevel(int level)
	{
		this.level = level;
	}
    
    public void setSkillList(List<String> skillList)
    {
        this.skillList = skillList;
    }
    
    public List<String> getSkillList()
    {
        return skillList;
    }
}
