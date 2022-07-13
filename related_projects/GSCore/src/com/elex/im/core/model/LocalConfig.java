package com.elex.im.core.model;

public class LocalConfig
{
	private int customChannelType;
	private String customChannelId;
	private boolean	audioUsed = false;
	private boolean	battleChannelShowed		= false;
	
	public int getCustomChannelType()
	{
		return customChannelType;
	}
	public void setCustomChannelType(int customChannelType)
	{
		this.customChannelType = customChannelType;
	}
	public String getCustomChannelId()
	{
		return customChannelId;
	}
	public void setCustomChannelId(String customChannelId)
	{
		this.customChannelId = customChannelId;
	}
	public boolean isAudioUsed()
	{
		return audioUsed;
	}
	public void setAudioUsed(boolean audioUsed)
	{
		this.audioUsed = audioUsed;
	}

	public boolean isBattleChannelShowed()
	{
		return battleChannelShowed;
	}

	public void setBattleChannelShowed(boolean battleChannelShowed)
	{
		this.battleChannelShowed = battleChannelShowed;
	}
}
