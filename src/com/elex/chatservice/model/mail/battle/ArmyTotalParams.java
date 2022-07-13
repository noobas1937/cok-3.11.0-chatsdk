package com.elex.chatservice.model.mail.battle;

public class ArmyTotalParams
{
	private int	num;
	private int	hurt;
	private int	kill;
	private int	dead;
	private int heal;
	
	public int getHeal() {
		return heal;
	}

	public void setHeal(int heal) {
		this.heal = heal;
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

}
