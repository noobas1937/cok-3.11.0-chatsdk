package com.elex.im.core;

import java.util.ArrayList;

import com.elex.im.core.model.Channel;

public interface IAppConfig
{
	public String getAppId();
	
	public boolean isDefaultTranslateEnable();

	public String roomId2channelId(String roomId);
	
	public int group2channelType(String group);

	public String getRoomId(Channel channel);
	
	public String getGroupId(Channel channel);

	public String getRoomIdPrefix();

	public boolean isExtendedAudioMsg(Channel channel);

	public Channel getChannel(int channelType);
	
	public ArrayList<Channel> getPredefinedChannels();
	
	public boolean isMessageChannel(Channel channel);
	
	public boolean isInBasicChat(int channelType);

	public boolean canGetMultiUserInfo();
	
	public void excuteJNIVoidMethod(final String methodName, final Object[] params);
}
