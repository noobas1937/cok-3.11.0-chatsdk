package com.elex.im.core.event;

public class WSStatusEvent extends Event
{
	public static final String	STATUS_CHANGE		= "ws_status";

	public static final int		CONNECTING			= 0;
	public static final int		CONNECTED			= 1;
	public static final int		RECONNECTING		= 2;
	/** 发生在尝试次数用尽，不再重连时 */
	public static final int		CONNECTION_FAILED	= 3;
	/** 发生在another login时，被服务器断开 */
	public static final int		DISCONNECTED		= 4;
	
	/** cokTest用的两个事件 */
	public static final int		TEST_COMPLETE		= 5;
	public static final int		CONNECT_ERROR		= 6;
	
	public static final int		AUTHORISING			= 7;

	/** 重连倒计时 */
	public int					reconnectCountDown	= -1;
	public int					subType				= -1;

	public WSStatusEvent(int sub_type)
	{
		super(STATUS_CHANGE);
		subType = sub_type;
	}

	public WSStatusEvent(int sub_type, int reconnectCountDown)
	{
		super(STATUS_CHANGE);
		subType = sub_type;
		this.reconnectCountDown = reconnectCountDown;
	}
	
	@Override
	public String toString()
	{
		return "[WSStatusEvent: " + subType + "]";
	}
}
