package com.elex.chatservice.model;

public class LocalConfig
{
	private int		customChannelType		= -1;
	private String	customChannelId			= "";
	private boolean	audioUsed				= false;
	private boolean	battleChannelShowed		= false;
	private boolean	firstRewardTipShowed	= false;
	private boolean rewardMenuAnimationShowed = false;
	private boolean autoTranslateSettingShowed = false;
	private boolean	randomChatTipShowed		= false;
	private boolean activityMailShowed 		= false;
	public transient String randomChannelId	= "";
	public transient int randomChatMode		= 0;
	private boolean translateDevelopChecked = false;

	public boolean isTranslateDevelopChecked()
	{
		return translateDevelopChecked;
	}

	public void setTranslateDevelopChecked(boolean translateDevelopChecked)
	{
		this.translateDevelopChecked = translateDevelopChecked;
	}

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

	public boolean isFirstRewardTipShowed()
	{
		return firstRewardTipShowed;
	}

	public void setFirstRewardTipShowed(boolean firstRewardTipShowed)
	{
		this.firstRewardTipShowed = firstRewardTipShowed;
	}

	public boolean isRewardMenuAnimationShowed()
	{
		return rewardMenuAnimationShowed;
	}

	public void setRewardMenuAnimationShowed(boolean rewardMenuAnimationShowed)
	{
		this.rewardMenuAnimationShowed = rewardMenuAnimationShowed;
	}

	public boolean isAutoTranslateSettingShowed()
	{
		return autoTranslateSettingShowed;
	}

	public void setAutoTranslateSettingShowed(boolean autoTranslateSettingShowed)
	{
		this.autoTranslateSettingShowed = autoTranslateSettingShowed;
	}

	public boolean isRandomChatTipShowed() {
		return randomChatTipShowed;
	}

	public void setRandomChatTipShowed(boolean randomChatTipShowed) {
		this.randomChatTipShowed = randomChatTipShowed;
	}

	public boolean isActivityMailShowed()
	{
		return activityMailShowed;
	}

	public void setActivityMailShowed(boolean activityMailShowed)
	{
		this.activityMailShowed = activityMailShowed;
	}
}
