package com.elex.im.core.event;

public class Bind
{
	public String			type;
	public EventCallBack	eventCallBack;
	public Object	target;

	public Bind(String type, Object target, EventCallBack eventCallBack)
	{
		this.type = type;
		this.eventCallBack = eventCallBack;
		this.target = target;
	}
}
