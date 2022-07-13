package com.elex.im.core.event;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

import com.elex.im.core.util.LogUtil;

public class EventCenter
{
	private CopyOnWriteArrayList<Bind> binds;
	private ConcurrentHashMap<String, Event> lastestEvent;

	private static EventCenter	instance;

	public static EventCenter getInstance()
	{
		if (instance == null)
		{
			instance = new EventCenter();
		}
		return instance;
	}

	private EventCenter()
	{
        binds = new CopyOnWriteArrayList<Bind>();
        lastestEvent = new ConcurrentHashMap<String, Event>();
	}

	public void addEventListener(String type, Object target, EventCallBack callback)
	{
		if(!hasEventListener(type, target))
			this.binds.add(new Bind(type, target, callback));
	}

	/**
	 * @param fireLastestEvent 为true时，会马上将最近一个该类型事件（如果存在）发送给监听者
	 */
	public void addEventListener(String type, Object target, boolean fireLastestEvent, EventCallBack callback)
	{
		addEventListener(type, target, callback);
		if(fireLastestEvent && lastestEvent.get(type) != null){
			callback.onCallback(lastestEvent.get(type));
			LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "addEventListener fire event", lastestEvent.get(type));
		}
	}

	public void removeEventListener(String type, Object target)
	{
		Iterator<Bind> iterator = this.binds.iterator();
		while (iterator.hasNext())
		{
			Bind bind = iterator.next();
			if (bind.type.equals(type) && bind.target == target)
			{
				binds.remove(bind);
			}
		}
	}

	public void removeAllEventListener(Object target)
	{
		Iterator<Bind> iterator = this.binds.iterator();
		while (iterator.hasNext())
		{
			Bind bind = iterator.next();
			if (bind.target == target)
			{
				binds.remove(bind);
			}
		}
	}

	public void removeAllEventListener(String type)
	{
		Iterator<Bind> iterator = this.binds.iterator();
		while (iterator.hasNext())
		{
			Bind bind = iterator.next();
			if (bind.type.equals(type))
			{
				binds.remove(bind);
			}
		}
	}

	public void removeAllEventListener()
	{
		this.binds.clear();
	}

	public boolean dispatchEvent(Event event)
	{
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "dispatch event", event);
		boolean hasListener = false;
		Iterator<Bind> iterator = this.binds.iterator();
		while (iterator.hasNext())
		{
			Bind bind = iterator.next();
			if(bind.type.equals(event.type)){
				bind.eventCallBack.onCallback(event);
			}
			hasListener = true;
		}

		lastestEvent.put(event.type, event);

		return hasListener;
	}

	public boolean hasEventListener(String type, Object target)
	{
		Iterator<Bind> iterator = this.binds.iterator();
		while (iterator.hasNext())
		{
			Bind bind = iterator.next();
			if(bind.type.equals(type) && bind.target == target){
				return true;
			}
		}
		return false;
	}
}
