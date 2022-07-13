package com.elex.im.core.event;


public class DBStatusEvent extends Event
{
	/** 需要更新视图、跳到最后一行 */
	public static final String	INIT_COMPLETE	= "db_init_complete";

	public DBStatusEvent(String type)
	{
		super(type);
	}

	@Override
	public String toString()
	{
		return "[DBStatusEvent: " + this.type + "]";
	}
}
