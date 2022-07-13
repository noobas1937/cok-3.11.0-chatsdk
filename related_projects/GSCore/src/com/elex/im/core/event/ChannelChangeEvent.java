package com.elex.im.core.event;

import com.elex.im.core.model.Channel;
import com.elex.im.core.model.Msg;

public class ChannelChangeEvent extends Event
{
	/** 需要更新视图、跳到最后一行 */
	public static final String	AFTER_ADD_DUMMY_MSG	= "after_add_dummy_msg";
	/** 需要更新视图 */
	public static final String	DATASET_CHANGED		= "data_set_changed";
	/** 需要更新视图、视情况跳到最后一行 */
	public static final String	ON_RECIEVE_NEW_MSG	= "on_recieve_new_msg";
	public static final String	ON_RECIEVE_OLD_MSG	= "on_recieve_old_msg";

	public Channel				channel;
	public boolean				hasSelfMsg;
	public Msg[]				chatInfoArr;
	public Msg					oldFirstItem;
	public int					loadCount;

	public ChannelChangeEvent(String type, Channel channel)
	{
		super(type);
		this.channel = channel;
	}
	
	public ChannelChangeEvent(String type, Channel channel, Msg[] chatInfoArr, Msg oldFirstItem, int loadCount)
	{
		super(type);
		this.channel = channel;
		this.chatInfoArr = chatInfoArr;
		this.oldFirstItem = oldFirstItem;
		this.loadCount = loadCount;
	}
	
	public ChannelChangeEvent(String type, Channel channel, Msg[] chatInfoArr, boolean hasSelfMsg)
	{
		super(type);
		this.channel = channel;
		this.chatInfoArr = chatInfoArr;
		this.hasSelfMsg  = hasSelfMsg;
	}

	@Override
	public String toString()
	{
		return "[ChannelChangeEvent: " + this.type + "]";
	}
}
