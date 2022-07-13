package com.elex.chatservice.model.mail.monster;

public class AttParams
{
	private int	total;
	private int	exp;
	private int generalExp;
	private int	hurt;
	private int	survived;
	private int	kill;
	private long	powerLost;
	private int	dead;
	private String rwdPro;

	public int getGeneralExp()
	{
		return generalExp;
	}
	public void setGeneralExp(int generalExp)
	{
		this.generalExp = generalExp;
	}
	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp = exp;
	}

	public int getHurt()
	{
		return hurt;
	}

	public void setHurt(int hurt)
	{
		this.hurt = hurt;
	}

	public int getSurvived()
	{
		return survived;
	}

	public void setSurvived(int survived)
	{
		this.survived = survived;
	}

	public int getKill()
	{
		return kill;
	}

	public void setKill(int kill)
	{
		this.kill = kill;
	}


	public long getPowerLost()
	{
		return powerLost;
	}

	public void setPowerLost(long powerLost)
	{
		this.powerLost = powerLost;
	}

	public int getDead()
	{
		return dead;
	}

	public void setDead(int dead)
	{
		this.dead = dead;
	}

	public String getRwdPro()
	{
		return rwdPro;
	}

	public void setRwdPro(String rwdPro)
	{
		this.rwdPro = rwdPro;
	}
}
