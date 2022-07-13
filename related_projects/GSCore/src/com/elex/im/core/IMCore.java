package com.elex.im.core;

import android.app.Activity;

import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.event.EventCenter;
import com.elex.im.core.host.IHost;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.net.WebSocketManager;
import com.elex.im.core.util.TranslateManager;

public class IMCore
{
	private IAppConfig		appConfig;
	public IHost			host;
	public static Class<?>	hostClass;
	public static Activity	hostActivity;		// IF或wrapper的activity，原生未打开时依然存在
	private boolean			inited = false;

	private static IMCore	instance;

	public static IMCore getInstance()
	{
		if (instance == null)
		{
			instance = new IMCore();
		}
		return instance;
	}

	protected IMCore()
	{
	}

	public void init(IAppConfig config)
	{
		if(inited)
			return;
		
		appConfig = config;

		if(!WebSocketManager.getInstance().connectAsSupervisor)
		{
			ChannelManager.getInstance().loadInitMsgs();
		}

		WebSocketManager.getInstance().connect();
		
		inited = true;
	}

	public void reset()
	{
		UserManager.getInstance().reset();
		TranslateManager.getInstance().reset();
		ChannelManager.getInstance().reset();
	}
	
	public IAppConfig getAppConfig()
	{
		return appConfig;
	}

	public void destroy()
	{
	}

//	public ChannelManager getChannelManager()
//	{
//		return ChannelManager.getInstance();
//	}

	public void getUser(String uid)
	{
	}

	public void dispatch(Event event)
	{
	}

	public void addEventListener(String type, Object target, EventCallBack callback)
	{
		EventCenter.getInstance().addEventListener(type, target, callback);
	}

	/**
	 * @param fireLastestEvent
	 *            为true时，会马上将最近一个该类型事件（如果存在）发送给监听者
	 */
	public void addEventListener(String type, Object target, boolean fireLastestEvent, EventCallBack callback)
	{
		EventCenter.getInstance().addEventListener(type, target, fireLastestEvent, callback);
	}

	public void removeEventListener(String type, Object target)
	{
		EventCenter.getInstance().removeEventListener(type, target);
	}

	public void removeAllEventListener(Object target)
	{
		EventCenter.getInstance().removeAllEventListener(target);
	}

	public void removeAllEventListener(String type)
	{
		EventCenter.getInstance().removeAllEventListener(type);
	}

	public void removeAllEventListener()
	{
		EventCenter.getInstance().removeAllEventListener();
	}
}
