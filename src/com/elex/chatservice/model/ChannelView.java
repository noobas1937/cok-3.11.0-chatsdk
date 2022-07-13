package com.elex.chatservice.model;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.view.View;
import android.widget.ListView;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.view.MessagesAdapter;
import com.elex.chatservice.view.listview.PullDownToLoadMoreView;

public class ChannelView
{
	private boolean					isLoadingStart;
	public ListView					messagesListView;
	private MessagesAdapter			messagesAdapter;
	public PullDownToLoadMoreView	pullDownToLoadListView;
	public int						tab;
	public int						channelType;
	public ChatChannel				chatChannel;
	public Timer					mTimer;
	public TimerTask				mTimerTask;
	public boolean 					hasMoreData = true;

	public ChannelView()
	{
		init();
	}

	public boolean getLoadingStart()
	{
		return isLoadingStart;
	}

	public void setLoadingStart(boolean b)
	{
		isLoadingStart = b;
	}
	
	public void setVisibility(int visibility)
	{
		if(pullDownToLoadListView!=null)
			pullDownToLoadListView.setVisibility(visibility);
	}
	
	public int getVisibility()
	{
		if(pullDownToLoadListView!=null)
			return pullDownToLoadListView.getVisibility();
		return View.GONE;
	}
	
	public int getMessageCount()
	{
		if(messagesAdapter!=null)
			return messagesAdapter.getCount();
		else
			return 0;
	}

	public void init()
	{
		if (messagesAdapter != null)
			messagesAdapter.destroy();
		if (messagesListView != null)
		{
			messagesListView.setOnScrollListener(null);
			messagesListView.setAdapter(null);
		}
		if (pullDownToLoadListView != null)
		{
			pullDownToLoadListView.setListViewLoadListener(null);
			pullDownToLoadListView.removeAllViews();
		}
		isLoadingStart = false;
		messagesListView = null;
		messagesAdapter = null;
		pullDownToLoadListView = null;
		chatChannel = null;
		mTimer = null;
		mTimerTask = null;
	}
	
	public void stopTimerTask()
	{
		if(mTimerTask!=null)
		{
			mTimerTask.cancel();
			mTimerTask = null;
		}
		if(mTimer!=null)
		{
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}

	public void setMessagesAdapter(MessagesAdapter adapter)
	{
		messagesAdapter = adapter;
	}

	public MessagesAdapter getMessagesAdapter()
	{
		return messagesAdapter;
	}
}
