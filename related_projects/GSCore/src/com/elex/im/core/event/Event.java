package com.elex.im.core.event;

public abstract class Event
{
	public String type;

	public Event(String type)
	{
		this.type = type;
	}
}
