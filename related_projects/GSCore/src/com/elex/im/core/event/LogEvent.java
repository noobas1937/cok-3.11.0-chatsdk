package com.elex.im.core.event;

public class LogEvent extends Event
{
	public static final String	LOG	= "gs_console_log";

	public String				log;

	public LogEvent(String log)
	{
		super(LOG);
		this.log = log;
	}
	
	@Override
	public String toString()
	{
		return "[LogEvent]";
	}
}