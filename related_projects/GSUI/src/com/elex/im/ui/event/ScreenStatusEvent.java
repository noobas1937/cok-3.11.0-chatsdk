package com.elex.im.ui.event;

import com.elex.im.core.event.Event;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.Msg;

public class ScreenStatusEvent extends Event
{
	/** 原生resume，需要2dx也手动处理一下resume */
	public static final String	GS_RESUME	= "gs_resume";
	/** 原生pause，需要2dx也手动处理一下pause */
	public static final String	GS_PAUSE	= "gs_pause";

	public ScreenStatusEvent(String type)
	{
		super(type);
	}

	@Override
	public String toString()
	{
		return "[ScreenStatusEvent: " + this.type + "]";
	}
}
