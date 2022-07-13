package com.elex.chatservice.model;

public class LatestChannelChatInfo
{
	private LatestChatInfo latestCountryChatInfo = null;
	private LatestChatInfo latestAllianceChatInfo = null;
	private LatestChatInfo latestCustomChatInfo	= null;
	private LatestChatInfo latestBattleChatInfo	= null;
	
	public LatestChatInfo getLatestCountryChatInfo()
	{
		return latestCountryChatInfo;
	}
	public void setLatestCountryChatInfo(LatestChatInfo latestCountryChatInfo)
	{
		this.latestCountryChatInfo = latestCountryChatInfo;
	}
	public LatestChatInfo getLatestAllianceChatInfo()
	{
		return latestAllianceChatInfo;
	}
	public void setLatestAllianceChatInfo(LatestChatInfo latestAllianceChatInfo)
	{
		this.latestAllianceChatInfo = latestAllianceChatInfo;
	}
	public LatestChatInfo getLatestCustomChatInfo()
	{
		return latestCustomChatInfo;
	}
	public void setLatestCustomChatInfo(LatestChatInfo latestCustomChatInfo)
	{
		this.latestCustomChatInfo = latestCustomChatInfo;
	}
	public LatestChatInfo getLatestBattleChatInfo()
	{
		return latestBattleChatInfo;
	}
	public void setLatestBattleChatInfo(LatestChatInfo latestBattleChatInfo)
	{
		this.latestBattleChatInfo = latestBattleChatInfo;
	}
}
