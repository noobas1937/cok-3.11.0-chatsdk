package com.elex.chatservice.model.mail.battle;

public class ArmyParams
{
	private int		exp;
	private String	armId;
	private int		num;
	private int		hurt;
	private int		kill;
	private int		dead;
	private int		star;
	private int 	heal;
	private String	skills;

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp = exp;
	}

	public String getArmId()
	{
		return armId;
	}

	public void setArmId(String armId)
	{
		this.armId = armId;
	}

	public int getNum()
	{
		return num;
	}

	public void setNum(int num)
	{
		this.num = num;
	}

	public int getHurt()
	{
		return hurt;
	}

	public void setHurt(int hurt)
	{
		this.hurt = hurt;
	}

	public int getKill()
	{
		return kill;
	}

	public void setKill(int kill)
	{
		this.kill = kill;
	}

	public int getDead()
	{
		return dead;
	}

	public void setDead(int dead)
	{
		this.dead = dead;
	}

	public int getStar()
	{
		return star;
	}

	public void setStar(int star)
	{
		this.star = star;
	}

	public int getHeal()
	{
		return heal;
	}

	public void setHeal(int heal)
	{
		this.heal = heal;
	}

	public String getSkills()
	{
		return skills;
	}

	public void setSkills(String skills)
	{
		this.skills = skills;
	}
}
